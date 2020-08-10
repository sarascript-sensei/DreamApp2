package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class VolunteerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    private SupportMapFragment mapFragment;
    private LinearLayout mPersonInfo;

    private ImageView mPersonImage;

    private TextView mPersonName, mPersonPhone, mPersonProblem;

    Location lastLocation;
    LocationRequest locationRequest;

    FloatingActionButton Community;
    private String personId = "";


    private ImageButton LogOutVolunteer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_map);

        LogOutVolunteer = (FloatingActionButton) findViewById(R.id.LogOutVolunteer);
        Community = (FloatingActionButton) findViewById(R.id.comminty);

        mPersonInfo = (LinearLayout) findViewById(R.id.personInfo);

        mPersonImage = (ImageView) findViewById(R.id.profileImage);

        mPersonName = (TextView) findViewById(R.id.personName);
        mPersonPhone = (TextView) findViewById(R.id.personPhone);
        mPersonProblem = (TextView) findViewById(R.id.personProblem);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VolunteerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        LogOutVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent welcomeIntent = new Intent(VolunteerMapActivity.this, WelcomeActivity.class);
                startActivity(welcomeIntent);
                finish();
                return;
            }
        });
        Community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent communityIntent = new Intent(VolunteerMapActivity.this, Community.class);
                startActivity(communityIntent);
            }
        });

        getAssignedPerson();
    }

    private void getAssignedPerson(){
        String volunteerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedPersonRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(volunteerId).child("personHelpId");
        assignedPersonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                        personId = snapshot.getValue().toString();
                        getAssignedPersonPickUpLocation();
                        getAssignedPersonInfo();
                }else {
                    personId = "";
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        }
    private  void getAssignedPersonInfo() {
        mPersonInfo.setVisibility(View.VISIBLE);
        DatabaseReference mPersonDatabase = FirebaseDatabase.getInstance().getReference().child("Users"). child("Persons").child(personId);
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

private void getAssignedPersonPickUpLocation() {
    DatabaseReference assignedPersonPickupLocation = FirebaseDatabase.getInstance().getReference().child("personRequest").child(personId).child("l");
    assignedPersonPickupLocation.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()) {
                List<Object> map = (List<Object>) snapshot.getValue();
                double locationLat = 0;
                double locationLng = 0;
                if(map.get(0) != null){
                    locationLat = Double.parseDouble(map.get(1).toString());
                }
                if (map.get(1) != null) {
                    locationLng = Double.parseDouble(map.get(1).toString());
                }
                LatLng volunteerLatLng = new LatLng(locationLat, locationLng);
                mMap.addMarker(new MarkerOptions().position(volunteerLatLng).title("Местоположение пользователя"));
            }
            }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VolunteerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null) {
            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("VolunteersAvailable");
            DatabaseReference refHelping = FirebaseDatabase.getInstance().getReference("VolunteersHelping");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireHelping = new GeoFire(refHelping);

            switch (personId) {
                case "":
                    geoFireHelping.removeLocation(userID);
                    geoFireAvailable.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userID);
                    geoFireHelping.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }


        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStop() {
        super.onStop();

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VolunteersAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userID);
    }

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Разрешите приложению найти вас", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
            boolean getDriversAroundStarted = false;

            List<Marker> markers = new ArrayList<Marker>();
            private void getDriversAround(){
                getDriversAroundStarted = true;
                DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("personNeededHelp");

                GeoFire geoFire = new GeoFire(driverLocation);
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lastLocation.getLongitude(), lastLocation.getLatitude()), 999999999);

                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {

                        for(Marker markerIt : markers){
                            if(markerIt.getTag().equals(key))
                                return;
                        }

                        LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                        Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.oxygen)));
                        mDriverMarker.setTag(key);

                        markers.add(mDriverMarker);


                    }

                    @Override
                    public void onKeyExited(String key) {
                        for(Marker markerIt : markers){
                            if(markerIt.getTag().equals(key)){
                                markerIt.remove();
                            }
                        }
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        for(Marker markerIt : markers){
                            if(markerIt.getTag().equals(key)){
                                markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                            }
                        }
                    }

                    @Override
                    public void onGeoQueryReady() {
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
            }
        }


