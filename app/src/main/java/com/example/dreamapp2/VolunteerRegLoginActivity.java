package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VolunteerRegLoginActivity extends AppCompatActivity {
    TextView StatusVolunteer, questionVolunteer;
    Button btnRegisterVolunteer, btnSingInVolunteer;
    EditText emailET, passwordET;
    FirebaseAuth mAuth;
    DatabaseReference VolunteerDatabaseRef;
    String OnlineVolunteerID;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_reg_login);

        StatusVolunteer = (TextView) findViewById(R.id.StatusVolunteer);
        questionVolunteer = (TextView) findViewById(R.id.questionVolunteer);
        btnSingInVolunteer = (Button) findViewById(R.id.btnSingInVolunteer);
        btnRegisterVolunteer = (Button) findViewById(R.id.btnRegisterVolunteer);
        emailET = (EditText) findViewById(R.id.volunteerEmail);
        passwordET = (EditText) findViewById(R.id.volunteerPassword);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        btnRegisterVolunteer.setVisibility(View.INVISIBLE);
        btnRegisterVolunteer.setEnabled(false);

        questionVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSingInVolunteer.setVisibility(View.INVISIBLE);
                questionVolunteer.setVisibility(View.INVISIBLE);
                btnRegisterVolunteer.setVisibility(View.VISIBLE);
                btnRegisterVolunteer.setEnabled(true);
                StatusVolunteer.setText("Регистрация для пользователей");
            }
        });
        btnRegisterVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                RegisterVolunteer(email, password);
            }
        });

        btnSingInVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                SignInVolunteer(email, password);
            }
        });
    }

    private void SignInVolunteer(String email, String password) {
        loadingBar.setTitle("Вход пользователя");
        loadingBar.setMessage("Пожалуйста, подождите");
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(VolunteerRegLoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Intent volunteerIntent = new Intent (VolunteerRegLoginActivity.this, VolunteerMapActivity.class);
                    startActivity(volunteerIntent);
                }
                else
                {
                    Toast.makeText(VolunteerRegLoginActivity.this, "Произошла ошибка, попробуйте снова", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }

    private void RegisterVolunteer(String email, String password) {
        loadingBar.setTitle("Регистрация пользователя");
        loadingBar.setMessage("Пожалуйста, подождите");
        loadingBar.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    OnlineVolunteerID = mAuth.getCurrentUser().getUid();
                    VolunteerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Volunteers").child(OnlineVolunteerID);
                   VolunteerDatabaseRef.setValue(true);

                    Intent volunteerIntent = new Intent (VolunteerRegLoginActivity.this, VolunteerMapActivity.class);
                    startActivity(volunteerIntent);

                    Toast.makeText(VolunteerRegLoginActivity.this, "Регистрация прошла успещно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                }
                else
                    {
                    Toast.makeText(VolunteerRegLoginActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }
}
