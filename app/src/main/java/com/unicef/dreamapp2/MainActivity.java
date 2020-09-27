package com.unicef.dreamapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.unicef.dreamapp2.application.MyPreferenceManager;
import com.unicef.dreamapp2.ui.appintro.CustomAppIntro;
import com.unicef.dreamapp2.ui.main.main.CustomerMainActivity;
import com.unicef.dreamapp2.ui.main.main.VolunteerMainActivity;



public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences shared = null;
    private String userType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shared = MyPreferenceManager.getMySharedPreferences(this);
        userType = shared.getString(MyPreferenceManager.USER_TYPE, null);

        mAuth = FirebaseAuth.getInstance();
        mAuth.useAppLanguage();

        new Handler().postDelayed(() -> {
            if(mAuth.getCurrentUser()!=null) {
                if (userType.equals(MyPreferenceManager.REGULAR_USER)) {
                    startActivity(new Intent(MainActivity.this, CustomerMainActivity.class)); // Enter customer main page
                } else {
                    startActivity(new Intent(MainActivity.this, VolunteerMainActivity.class)); // Enter volunteer page
                }
            } else {
                startActivity(new Intent(MainActivity.this, CustomAppIntro.class));
               // startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}