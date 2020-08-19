package com.unicef.dreamapp2.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.unicef.dreamapp2.MyPreferenceManager;
import com.unicef.dreamapp2.R;

import java.util.concurrent.TimeUnit;

/**
* @author Iman Augustine
 *
* */

public class CodeActivity extends AppCompatActivity {

    // Global variables
    private EditText codeEditText;
    private Button confirmBtn;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private String mVerificationId;
    private String mobile;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private SharedPreferences shared = null;
    private SharedPreferences.Editor editor = null;

    // PhoneAuthProvider.OnVerificationChangedCallbacks interface initialized
    // 1 - onVerificationCompleted - proceed to the main activity
    // 2 - onCodeSent - code is sent the phone number, should be entered manually
    // 3 - onVerificationFailed - on phone verification failed
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            String code = credential.getSmsCode();
            if(code!=null) {
                codeEditText.setText(code);
                // verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // Verification failed due to wrong configurations with Firebase
            // Log.d(TAG, "onVerificationFailed:" + e.getLocalizedMessage());
        }
    };

    // On creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        initView();

        mAuth = FirebaseAuth.getInstance();

        // Phone number from the previous step (Enter number)
        mobile = getIntent().getStringExtra("mobile");
        // Send verification code to the given number
        sendVerificationCode(mobile);

        // Shared preferences
        shared = MyPreferenceManager.getMySharedPreferences(this);
        editor = shared.edit();

    }

    // Initializes views
    private void initView() {
        codeEditText = findViewById(R.id.codeEditText);
        confirmBtn = findViewById(R.id.confirmButton);
    }

    // On confirm click
    public void onConfirmClick(View view) {
        String code = codeEditText.getText().toString().trim();

        // If code is empty or code's length is less than 6
        if (code.isEmpty() || code.length()<6) {
            // Show error
            codeEditText.setError("Введите верный код");
            codeEditText.requestFocus();
            return;
        }

        // Verify the code entered manually
        verifyVerificationCode(code);
    }

    // Verify verification code
    private void verifyVerificationCode(String otp) {
        // Creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        // Signing the user
        signInWithPhoneAuthCredential(credential);
    }

    // Sending verification code
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    // Sign in with the credential sent in onVerificationCompleted
    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(CodeActivity.this, AccountSetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("setup", true);
                            startActivity(intent);
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                codeEditText.setError("Введите правильный код");
                            }
                        }
                    }
                });
    }

    private void startUserMainActivity() {
      //  startActivity(new Intent(this, UserMainActivity.class));
        finish();
    }

    private void startVolunteerMainActivity() {
      //  startActivity(new Intent(this, VolunteerMainActivity.class));
        finish();
    }

   /* @Override
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



  /* mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(CodeActivity.this, PersonMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mRegistration = (Button) findViewById(R.id.registration);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CodeActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CodeActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                            current_user_db.setValue(email);
                        }
                    }
                });
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CodeActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CodeActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });*/