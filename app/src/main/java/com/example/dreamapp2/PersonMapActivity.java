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
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class PersonMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{
    private GoogleMap mMap;
    FloatingActionButton fab, fab1, fab2;
    FloatingActionMenu materialDesignFAM;
    GoogleApiClient googleApiClient;
    private String personId;
    Location lastLocation;
    LocationRequest locationRequest;
    private LatLng PersonPostion;

    BitmapDescriptorFactory icon;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference PersonDatabaseRef;

    private ImageButton LogOutPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_map);


        LogOutPerson = (ImageButton)findViewById(R.id.LogOutPerson);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        personId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PersonDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Person's Request");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LogOutPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LogOutPerson();
            }
        });

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        fab = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        fab1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        fab2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(PersonMapActivity.this, "Маркер выбран", Toast.LENGTH_SHORT).show();
                GeoFire geofire = new GeoFire(PersonDatabaseRef);
                geofire.setLocation(personId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                PersonPostion = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.oxygen);

                MarkerOptions markerOptions = new MarkerOptions().position(PersonPostion)
                        .title("Current Location")
                        .snippet("hello").icon(icon);


                mMap.addMarker(markerOptions);
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(PersonMapActivity.this, "Маркер выбран", Toast.LENGTH_SHORT).show();

            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(PersonMapActivity.this, "Маркер выбран", Toast.LENGTH_SHORT).show();
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
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

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
    }
    private void LogOutPerson() {
        Intent welcomeIntent = new Intent(PersonMapActivity.this, WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }
}