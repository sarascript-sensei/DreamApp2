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
import android.widget.ImageButton;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class VolunteerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private ImageButton Logoutvolunteer;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogoutVolunteer;
    private DatabaseReference assignedPersonRef, AssignedPersonPositionRef;
    private String volunteerID, personID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        volunteerID = mAuth.getCurrentUser().getUid();

        Logoutvolunteer = (ImageButton)findViewById(R.id.LogOutVolunteer);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Logoutvolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLogoutVolunteer = true;
                mAuth.signOut();

                Logoutvolunteer();
                DisconnectVolunteer();
            }
        });

        getAssignedPersonRequest();
    }

    private void getAssignedPersonRequest() {
        assignedPersonRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(volunteerID).child("PersonHelpID");

        assignedPersonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    personID = snapshot.getValue().toString();

                    getAssignedPersonPosition();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedPersonPosition() {
        AssignedPersonPositionRef = FirebaseDatabase.getInstance().getReference().child("Person Request")
                .child(personID).child("l");

        AssignedPersonPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    List<Object> personPosition = (List<Object>) snapshot.getValue();
                    double LocationLatitude = 0;
                    double LocationLongitude = 0;
                    //Сюда добавить код с запросом на принятие помощи

                    if (personPosition.get(0) != null)
                    {
                        LocationLatitude = Double.parseDouble(personPosition.get(0).toString());
                    }
                    if (personPosition.get(1) != null)
                    {
                        LocationLongitude = Double.parseDouble(personPosition.get(1).toString());
                    }
                    LatLng VolunteerLatLong = new LatLng(LocationLatitude, LocationLongitude);
                    mMap.addMarker(new MarkerOptions().position(VolunteerLatLong));
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

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

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
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference VolunteerAvalabilityRef = FirebaseDatabase.getInstance().getReference().child("Volunteer Avaible");
            GeoFire geoFireAvailable = new GeoFire(VolunteerAvalabilityRef);


            DatabaseReference VolunteerHelping = FirebaseDatabase.getInstance().getReference().child("Volunteer Helping");
            GeoFire geoFireHelping = new GeoFire(VolunteerHelping);


            switch ( personID )
            {
                case "":
                    geoFireHelping.removeLocation(userID);
                    geoFireAvailable.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userID);
                    geoFireHelping.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
            }
        }

    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!currentLogoutVolunteer) {

            DisconnectVolunteer();
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference VolunteerAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Volunteer Available");


            GeoFire geoFire = new GeoFire(VolunteerAvailabilityRef);
            geoFire.removeLocation(userID);
        }

    }

    private void DisconnectVolunteer() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference VolunteerAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Volunteer Available");


        GeoFire geoFire = new GeoFire(VolunteerAvailabilityRef);
        geoFire.removeLocation(userID);
    }

    private void Logoutvolunteer()
    {
        Intent welcomeIntent = new Intent(VolunteerMapActivity.this, WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }
    }