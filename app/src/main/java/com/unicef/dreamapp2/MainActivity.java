package com.unicef.dreamapp2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.unicef.dreamapp2.ui.main.main.CustomerMainActivity;
import com.unicef.dreamapp2.ui.main.main.VolunteerMainActivity;
import com.unicef.dreamapp2.ui.welcome.WelcomeActivity;

/**
 * @author Iman Augustine
 *
 * */

public class MainActivity extends AppCompatActivity {

    private SharedPreferences shared = null;
    private String userType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shared = MyPreferenceManager.getMySharedPreferences(this);
        userType = shared.getString(MyPreferenceManager.USER_TYPE, null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                    if (userType.equals(MyPreferenceManager.REGULAR_USER)) {
                        startActivity(new Intent(MainActivity.this, CustomerMainActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, VolunteerMainActivity.class));
                    }
                } else {
                    startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                }
            }
        }, 800);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}