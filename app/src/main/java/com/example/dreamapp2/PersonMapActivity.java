package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PersonMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{
    private GoogleMap mMap;
    Button MarkerChoiser, DescripMarker;
    GoogleApiClient googleApiClient;
    private String personId;
    Location lastLocation;
    LocationRequest locationRequest;
    private LatLng PersonPostion;
    Marker VolunteerMarker;
    final Context context = this;
    private EditText result;

    BitmapDescriptorFactory icon;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference PersonDatabaseRef;

    private DatabaseReference VolunteersRef;

    private ImageButton LogOutPerson;
    private DatabaseReference VolunteersAvailableRef;
    private DatabaseReference VolunteersLocationRef;
    private int radius = 1;
    private Boolean volunteerFound = false;
    private  String volunteerFoundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_map);


        LogOutPerson = (FloatingActionButton) findViewById(R.id.LogOut);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        personId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PersonDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Person's Request");
        VolunteersAvailableRef = FirebaseDatabase.getInstance().getReference().child("Volunteer Available");
        VolunteersLocationRef = FirebaseDatabase.getInstance().getReference().child("Volunteer Helping");

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

        TextView textView = (TextView)findViewById(R.id.alertDialogTextView);

        this.Marker(textView);


        };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
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
    private void Marker(TextView textView) {
        final TextView textViewTmp = textView;

        Button choise = (Button) findViewById(R.id.choise);

        choise.setOnClickListener(new View.OnClickListener() {

            // Each image in array will be displayed at each item beginning.
            private int[] imageIdArr = {R.drawable.oxygen, R.drawable.medicine, R.drawable.diet};
            // Each item text.
            private String[] listItemArr = {"СИЗ", "Лекарства", "Еда"};

            // Image and text item data's key.
            private final String CUSTOM_ADAPTER_IMAGE = "image";
            private final String CUSTOM_ADAPTER_TEXT = "text";

            @Override
            public void onClick(View view) {
                // Create a alert dialog builder.
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonMapActivity.this);
                // Set icon value.
                builder.setIcon(R.mipmap.ic_launcher);
                // Set title value.
                builder.setTitle("Simple Adapter Alert Dialog");

                // Create SimpleAdapter list data.
                List<Map<String, Object>> dialogItemList = new ArrayList<Map<String, Object>>();
                int listItemLen = listItemArr.length;
                for (int i = 0; i < listItemLen; i++) {
                    Map<String, Object> itemMap = new HashMap<String, Object>();
                    itemMap.put(CUSTOM_ADAPTER_IMAGE, imageIdArr[i]);
                    itemMap.put(CUSTOM_ADAPTER_TEXT, listItemArr[i]);

                    dialogItemList.add(itemMap);
                }

                // Create SimpleAdapter object.
                SimpleAdapter simpleAdapter = new SimpleAdapter(PersonMapActivity.this, dialogItemList,
                        R.layout.android_user_input_dialog,
                        new String[]{CUSTOM_ADAPTER_IMAGE, CUSTOM_ADAPTER_TEXT},
                        new int[]{R.id.alertDialogItemImageView, R.id.alertDialogItemTextView});

                // Set the data adapter.


                builder.setCancelable(false);
                builder.create();
                builder.show();
            }
        });
    }
}