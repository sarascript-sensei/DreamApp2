package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.List;


public class PersonMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;

    Location lastLocation;
    FloatingActionButton OwnInfoUser;
    LocationRequest locationRequest;
    private Button MarkerChoise, Descript;


    private ImageButton LogOut;
    private LatLng pickupLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_map);

        LogOut = (FloatingActionButton) findViewById(R.id.LogOut);
        MarkerChoise = (Button) findViewById(R.id.choise);
        Descript = (Button) findViewById(R.id.descript);
        OwnInfoUser = (FloatingActionButton) findViewById(R.id.OwnInfo);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }
        OwnInfoUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent communityIntent = new Intent(PersonMapActivity.this, OwnInfoPerson.class);
                startActivity(communityIntent);
                return;
            }
        });
        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent welcomeIntent = new Intent(PersonMapActivity.this, WelcomeActivity.class);
                startActivity(welcomeIntent);
                finish();
                return;
            }
        });

        MarkerChoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("personRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                pickupLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonMapActivity.this);
                builder.setTitle("Я нуждаюсь в ");

// add a list
                String[] animals = {"Лекарства", "Продукты", "СИЗ", "Попутка", "Помощь(SOS)"};
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(pickupLocation);
                builder.setItems(animals, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mMap.clear();
                                markerOptions.title("Лекарства").icon(BitmapDescriptorFactory.fromResource(R.mipmap.medicine));
                                mMap.addMarker(markerOptions);
                                break;
                            case 1:
                                mMap.clear();
                                markerOptions.title("Продукты").icon(BitmapDescriptorFactory.fromResource(R.mipmap.diet));
                                mMap.addMarker(markerOptions);
                                break;
                            case 2:
                                mMap.clear();
                                markerOptions.title("СИЗ").icon(BitmapDescriptorFactory.fromResource(R.mipmap.oxygen));
                                mMap.addMarker(markerOptions);
                                break;
                            case 3:
                                mMap.clear();
                                markerOptions.title("Попутка").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car));
                                mMap.addMarker(markerOptions);
                                break;
                            case 4:
                                mMap.clear();
                                markerOptions.title("Помощь(SOS)").icon(BitmapDescriptorFactory.fromResource(R.drawable.help));
                                mMap.addMarker(markerOptions);
                                break;
                        }
                    }
                });

// create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                //Код, который ищет волонтёра рядом (поддаётся изменениям)
                getClosestVolunteer();
            }
        });
    }

    private  int radius = 1;
    private  Boolean volunteerFound = false;
    private String volunteerFoundID;
    private void getClosestVolunteer (){
        DatabaseReference volunteersLocation = FirebaseDatabase.getInstance().getReference().child("VolunteersAvailable");

        GeoFire geofire = new GeoFire(volunteersLocation);

        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        //Какой из волонтёров будет выбран
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!volunteerFound) {
                    volunteerFound = true;
                    volunteerFoundID = key;

                    DatabaseReference volunteerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(volunteerFoundID);
                    String personId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("personHelpId", personId);
                    volunteerRef.updateChildren(map);

                    getVolunteerLocation();
                    MarkerChoise.setText("Поиск волонтёра...");

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
                if (!volunteerFound) {
                    radius++;
                    getClosestVolunteer();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    //Где волонтёр
    private  Marker VolunteerMarker;
    private void getVolunteerLocation () {
        DatabaseReference volunteerLocationRef = FirebaseDatabase.getInstance().getReference().child("volunteersHelping").child(volunteerFoundID).child("l");
        volunteerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List <Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    MarkerChoise.setText("Волонтёр найден");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(1).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng volunteerLatLng = new LatLng(locationLat, locationLng);
                    if(VolunteerMarker != null) {
                        VolunteerMarker.remove();
                    }
                    Location loc1 = new Location(" ");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location(" ");
                    loc2.setLatitude(volunteerLatLng.latitude);
                    loc2.setLongitude(volunteerLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    VolunteerMarker = mMap.addMarker(new MarkerOptions().position(volunteerLatLng).title("Волонтёр"));
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
            return;
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
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VolunteersAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userID, new GeoLocation(location.getLatitude(),location.getLongitude()));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonMapActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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
    }
    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case  LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Разрешите приложению найти вас", Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }
}