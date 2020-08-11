package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VolunteerMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LinearLayout mPersonInfo;
    private String requestService;

    private LatLng pickupLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private ImageView mPersonImage;
    private Boolean requestBol = false;
    private TextView mPersonName, mPersonPhone, mPersonProblem;

    Location lastLocation;
    LocationRequest locationRequest;

    FloatingActionButton Community;
    private String personId = "";
    private Marker pickupMarker;

    private Boolean isLoggingOut = false;

    private ImageButton LogOutVolunteer;
    private Switch mWorkingSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_map);

        LogOutVolunteer = (FloatingActionButton) findViewById(R.id.LogOutVolunteer);
        Community = (FloatingActionButton) findViewById(R.id.comminty);

        mPersonInfo = (LinearLayout) findViewById(R.id.personInfo);

        mPersonImage = (ImageView) findViewById(R.id.profileImage);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mPersonName = (TextView) findViewById(R.id.personName);
        mPersonPhone = (TextView) findViewById(R.id.personPhone);
        mPersonProblem = (TextView) findViewById(R.id.personProblem);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        LogOutVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;

                disconnectVolunteer();

                FirebaseAuth.getInstance().signOut();
                Intent welcomeIntent = new Intent(VolunteerMapActivity.this, WelcomeActivity.class);
                startActivity(welcomeIntent);
                finish();
                return;
            }
        });
        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    connectVolunteer();
                    getClosestPerson();

                } else {
                    disconnectVolunteer();
                }
            }
        });

        Community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent communityIntent = new Intent(VolunteerMapActivity.this, Community.class);
                startActivity(communityIntent);
            }
        });
    }

    private int radius = 1;
    private Boolean personFound = false;
    private String personFoundID;

    GeoQuery geoQuery;

    private void getClosestPerson() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("personRequest");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!personFound && requestBol) {
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Persons").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String, Object> personMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (personFound) {
                                    return;
                                }
                                if (personMap.get("service").equals(requestService)) {
                                    personFound = true;
                                    personFoundID = dataSnapshot.getKey();

                                    DatabaseReference personRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Persons").child(personFoundID).child("customerRequest");
                                    String personId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("personHelpId", personId);
                                    personRef.updateChildren(map);

                                    getPersonLocation();
                                    getAssignedPersonInfo();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!personFound) {
                    radius++;
                    getClosestPerson();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }

        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (getApplicationContext() != null) {
                        lastLocation = location;

                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("VolunteersAvailable");
                        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("VolunteersWorking");
                        GeoFire geoFireAvailable = new GeoFire(refAvailable);
                        GeoFire geoFireWorking = new GeoFire(refWorking);

                        switch (personId){
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
            }
        };


    private Marker PersonMarker;
    private DatabaseReference personLocationRef;
    private ValueEventListener personLocationRefListener;
    private void getPersonLocation(){
        personLocationRef = FirebaseDatabase.getInstance().getReference().child("personRequest").child(personFoundID).child("l");
        personLocationRefListener = personLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng personLatLng = new LatLng(locationLat, locationLng);
                    if (PersonMarker != null) {
                        PersonMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(personLatLng.latitude);
                    loc2.setLongitude(personLatLng.longitude);

                    PersonMarker = mMap.addMarker(new MarkerOptions().position(personLatLng).title("здесь нужна помощь"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    private void getAssignedPersonInfo() {
        mPersonInfo.setVisibility(View.VISIBLE);
        DatabaseReference mPersonDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Persons").child(personId);
        mPersonDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("Имя") != null) {
                        mPersonName.setText(map.get("Имя").toString());
                    }
                    if (map.get("Номер") != null) {
                        mPersonPhone.setText(map.get("Номер").toString());
                    }
                    if (map.get("Проблема") != null) {
                        mPersonProblem.setText(map.get("Проблема").toString());
                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mPersonImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(VolunteerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(VolunteerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    private void connectVolunteer(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectVolunteer(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VolunteersAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

}
