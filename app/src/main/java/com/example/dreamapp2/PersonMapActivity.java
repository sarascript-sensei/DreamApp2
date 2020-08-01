package com.example.dreamapp2;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class PersonMapActivity extends FragmentActivity implements OnMapReadyCallback {
    BitmapDescriptor icon;
    GoogleMap map;
    FloatingActionButton fab, fab1, fab2;
    FloatingActionMenu materialDesignFAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        fab = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        fab1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        fab2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(PersonMapActivity.this, "Маркер выбран", Toast.LENGTH_SHORT).show();
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (fab.isSelected()) {
            icon = BitmapDescriptorFactory.fromResource(R.id.material_design_floating_action_menu_item1);
            LatLng bangalore = new LatLng(12.9716, 77.5946);

            MarkerOptions markerOptions = new MarkerOptions().position(bangalore)
                    .title("Current Location")
                    .snippet("hello").icon(icon);


            map.addMarker(markerOptions);
        }
    }
}