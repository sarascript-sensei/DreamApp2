package com.example.dreamapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button volunteerBtn, personBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        volunteerBtn = (Button)findViewById(R.id.volunteerBtn);
        personBtn = (Button)findViewById(R.id.personBtn);

        volunteerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent volunteerIntent = new Intent(WelcomeActivity.this, VolunteerRegLoginActivity.class);
                startActivity(volunteerIntent);
            }
        });

        personBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent personIntent = new Intent(WelcomeActivity.this, PersonRegLoginActivity.class);
                startActivity(personIntent);
            }
        });
    }
}