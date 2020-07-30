package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class PersonRegLoginActivity extends AppCompatActivity {
    TextView StatusPerson, questionPerson;
    Button btnRegisterPerson, btnSingIn;
    EditText emailET, passwordET;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_reg_login);

        StatusPerson = (TextView) findViewById(R.id.StatusPerson);
        questionPerson = (TextView) findViewById(R.id.questionPerson);
        btnSingIn = (Button) findViewById(R.id.btnSingIn);
        btnRegisterPerson = (Button) findViewById(R.id.btnRegisterPerson);
        emailET = (EditText) findViewById(R.id.personEmail);
        passwordET = (EditText) findViewById(R.id.PersonPassword);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        btnRegisterPerson.setVisibility(View.INVISIBLE);
        btnRegisterPerson.setEnabled(false);

        questionPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSingIn.setVisibility(View.INVISIBLE);
                questionPerson.setVisibility(View.INVISIBLE);
                btnRegisterPerson.setVisibility(View.VISIBLE);
                btnRegisterPerson.setEnabled(true);
                StatusPerson.setText("Регистрация для пользователей");
            }
        });
        btnRegisterPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                RegisterPerson(email, password);
            }
        });

        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                SignInPerson(email, password);
            }
        });
    }

    private void SignInPerson(String email, String password) {
        loadingBar.setTitle("Вход пользователя");
        loadingBar.setMessage("Пожалуйста, подождите");
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PersonRegLoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Intent personIntent = new Intent (PersonRegLoginActivity.this, PersonMapActivity.class);
                    startActivity(personIntent);
                }
                else {
                    Toast.makeText(PersonRegLoginActivity.this, "Произошла ошибка, попробуйте снова", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }

    private void RegisterPerson(String email, String password) {
        loadingBar.setTitle("Регистрация пользователя");
        loadingBar.setMessage("Пожалуйста, подождите");
        loadingBar.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PersonRegLoginActivity.this, "Регистрация прошла успещно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Intent personIntent = new Intent (PersonRegLoginActivity.this, PersonMapActivity.class);
                    startActivity(personIntent);
                }
                else {
                    Toast.makeText(PersonRegLoginActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }
}
