package com.unicef.dreamapp2.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.unicef.dreamapp2.MyPreferenceManager;
import com.unicef.dreamapp2.R;

public class PhoneActivity extends AppCompatActivity {

    public static final String TAG = "PhoneActivity";
    public static final String VERIFICATION_ID = "verification_id";
    public static final String TOKEN = "token";

    private EditText phoneEditText;
    private Button mLogin;
    private CountryCodePicker ccp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private SharedPreferences shared = null;
    private SharedPreferences.Editor editor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        initView();

        shared = MyPreferenceManager.getMySharedPreferences(this);
    }

    private void initView() {
        phoneEditText = findViewById(R.id.phoneEditText);
        ccp = findViewById(R.id.ccp);
    }

    public void onClickVerify(View view) {
        // Getting entered phone number with the country code
        String phone = "+"+ccp.getSelectedCountryCode() + phoneEditText.getText().toString().trim();

        // Checking phone number format/correctness
        if(phone.length()<13) {
            phoneEditText.setError("Введите верный номер");
            phoneEditText.requestFocus();
            return;
        }

        Intent intent = new Intent(this, CodeActivity.class);
        intent.putExtra("mobile", phone);
        startActivity(intent);
    }

  /*  @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }*/
}
