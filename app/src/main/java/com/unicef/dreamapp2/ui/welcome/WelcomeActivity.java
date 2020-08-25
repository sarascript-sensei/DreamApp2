package com.unicef.dreamapp2.ui.welcome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.unicef.dreamapp2.application.MyPreferenceManager;
import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.ui.login.PhoneActivity;

/**
*
* @author Tan Ton
 *
 * This is a welcome activity where a user logs in as a volunteer or as person needing help.
*
* */

public class WelcomeActivity extends AppCompatActivity {
    // Global variables
    private Button volunteerBtn; // Volunteer
    private Button userBtn; // A person

    // Shared preferences and editor
    private SharedPreferences shared = null;
    private SharedPreferences.Editor editor = null;

    // On creation of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initializes shared preferences
        shared = MyPreferenceManager.getMySharedPreferences(this);
        editor = shared.edit();

        // Checks location permission
        checkLocationPermission();
    }

    // Starts regular person log in activity
    public void openRegularLogin(View view) {
        editor.putString(MyPreferenceManager.USER_TYPE, MyPreferenceManager.REGULAR_USER).commit();
        startActivity( new Intent(this, PhoneActivity.class));
    }

    // Starts a volunteer log in activity
    public void openVolunteerLogin(View view) {
        editor.putString(MyPreferenceManager.USER_TYPE, MyPreferenceManager.VOLUNTEER).commit();
        startActivity(new Intent(this, PhoneActivity.class));
    }

    private void checkLocationPermission() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
    }
}