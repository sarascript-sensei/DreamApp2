package com.unicef.dreamapp2.ui.main.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unicef.dreamapp2.MyPreferenceManager;
import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.singleclicklistener.OnSingleClickNavigationViewListener;
import com.unicef.dreamapp2.ui.welcome.WelcomeActivity;

import java.util.List;
import java.util.Map;

/**
 * @author Tan Ton
 *
 *
 * */

public class VolunteerMainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button mLogout, mSettings, mRideStatus, mHistory;

    private Switch switchHeroMode;

    private int status = 0;

    private String customerId = "";
    private LatLng pickupLatLng;

    private Boolean isLoggingOut = false;

    private SupportMapFragment mapFragment;

    private LinearLayout mCustomerInfo;

    private ImageView mCustomerProfileImage;
    private TextView mCustomerName;
    private TextView mCustomerPhone;
    private TextView mCustomerProblem;
    private TextView userName;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Shared preferences
    private SharedPreferences shared = null;
    private String mUserType = null;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID;

    // On navigation item selected listener in the drawer layout
    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new OnSingleClickNavigationViewListener() {
                @Override
                public boolean onSingleClick(MenuItem item) {
                    switch (item.getItemId()) {
                        // History
                        case R.id.history:
                            drawerLayout.closeDrawers();
                            Toast.makeText(VolunteerMainActivity.this, "History!", Toast.LENGTH_SHORT).show();
                            break;
                            // Chat
                        case R.id.chat:
                            Toast.makeText(VolunteerMainActivity.this, "Chat!", Toast.LENGTH_SHORT).show();
                            drawerLayout.closeDrawers();
                            break;
                            // Edit profile
                        case R.id.change_profile:
                            Toast.makeText(VolunteerMainActivity.this, "Change profile!", Toast.LENGTH_SHORT).show();
                            drawerLayout.closeDrawers();
                            break;
                            // Logs out
                        case R.id.logout:
                            drawerLayout.closeDrawers();
                            logout();
                            break;
                    }
                    return true;
                }
            };
    //--------------------------------------------------ON-CREATE-ACTIVITY----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_main);
        // Initializes views
        initView();
        // Shared preferences
        shared = MyPreferenceManager.getMySharedPreferences(this);
        mUserType = shared.getString(MyPreferenceManager.USER_TYPE, null);
        // Firebase realtime database
        mAuth = FirebaseAuth.getInstance();
        // User ID
        userID = mAuth.getCurrentUser().getUid();
        // Firebase database
        mCustomerDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(shared.getString(MyPreferenceManager.USER_TYPE, null))
                .child(userID);
        // Loads volunteer's information
        loadUserInfo();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // getAssignedCustomer();
    }
    //-----------------------------------------------------------------------------------------------------------------------
    private void initView() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Setting on map ready callback interface
        mapFragment.getMapAsync(this);
        // Customer layout information
        mCustomerInfo = findViewById(R.id.customerInfo);
        // Customer profile image
        mCustomerProfileImage = findViewById(R.id.customerProfileImage);
        // Customer name
        mCustomerName = findViewById(R.id.customerName);
        // Customer problem
        mCustomerProblem = findViewById(R.id.customerProblem);
        // Switch widget
        switchHeroMode =  findViewById(R.id.workingSwitch);
        switchHeroMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    connectDriver();
                } else {
                    disconnectDriver();
                }
            }
        });
        // DrawerLayout
        drawerLayout = findViewById(R.id.drawer);
        // Navigation view
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
        // TextView
        userName = navigationView.getHeaderView(0).findViewById(R.id.userName);
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // Log out
    private void logout() {
        isLoggingOut = true; // Logging out
        disconnectDriver(); // Disconnects drivers
        FirebaseAuth.getInstance().signOut(); // Signs out from Firebase
        startActivity(new Intent(VolunteerMainActivity.this, WelcomeActivity.class)); // Starts WelcomeActivity
        finish(); // Finishes the current activity
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // Loads user profile information from Firebase database
    // 1 - User name
    // 2 - User phone number
    // 3 - User problem ( or a regular user, not volunteer)
    private void loadUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        // Map data structure
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        // User name
                        if (map.get("name") != null) {
                             userName.setText(map.get("name").toString());
                        }
                        // User profile image URI
                        if (map.get("profileImageUrl") != null) {
                           Glide.with(getApplication())
                                   .load(Uri.parse(map.get("profileImageUrl").toString())).into(mProfileImage);
                        }
                    }
                } catch(NullPointerException error) {
                    Log.d("AccountSetupActivity", "onDataChange: error: "+error.getLocalizedMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Cancelled
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // Get the assigned customer
    private void getAssignedCustomer() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child("Drivers")
                .child(driverId)
                .child("customerRequest")
                .child("customerRideId");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerInfo();
                } else {
                    customerId = " ";
                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }
                    if (assignedCustomerPickupLocationRefListener != null) {
                        assignedCustomerRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                    }
                    mCustomerInfo.setVisibility(View.GONE);
                    mCustomerName.setText(" ");
                    mCustomerPhone.setText(" ");
                    mCustomerProblem.setText(" ");
                    mCustomerProfileImage.setImageResource(R.mipmap.ic_default_user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //-----------------------------------------------------------------------------------------------------------------------
    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat, locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Help location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Gets assigned customer info
    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mCustomerName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mCustomerPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mCustomerProfileImage);
                    }
                    if (map.get("problem") != null) {
                        mCustomerProblem.setText(map.get("problem").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // Map ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef
                .child("Users").child("Customers");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    //Toast.makeText(L_Location_Activity.this,"for",Toast.LENGTH_LONG).show();

                    String latitude_Display = ds
                            .child("latitude")
                            .getValue().toString();

                    String longitude_Display = ds
                            .child("longitude")
                            .getValue().toString();


                    String latLng = latitude_Display;
                    String latLng1 = longitude_Display;


                    double latitude = Double.parseDouble(latLng);
                    double longitude = Double.parseDouble(latLng1);

                    // map.clear();
                    LatLng currentLocation = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentLocation);
                    //markerOptions.title("i'm here");
                    //map.addMarker( markerOptions );
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title("Hello world"))
                            .setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.superhero));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        usersRef.addListenerForSingleValueEvent(eventListener);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // Location call backs interface listener
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLastLocation = location;
                }


                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking = new GeoFire(refWorking);

                switch (customerId) {
                    case "":
                        geoFireWorking.removeLocation(userId);
                        geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        break;

                    default:
                        geoFireAvailable.removeLocation(userId);
                        geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        break;
                }
            }
        }
    };
    //-----------------------------------------------------------------------------------------------------------------------
    // Check geo location permission
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(VolunteerMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(VolunteerMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // Connects driver
    private void connectDriver() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    // Disconnects driver
    private void disconnectDriver() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }


}

//-----------------------------------------------------------------------------------------------------------------------
