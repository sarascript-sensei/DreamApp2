package com.example.dreamapp2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OwnInfoPerson extends AppCompatActivity {

    private EditText NameField, PhoneField,ProblemField;

    private Button Confirm, Exit;
    private FirebaseAuth mAuth;
    private DatabaseReference mPersonDatabase;

    ImageView ProfilePhoto;
    private  String userID;
    private  String mName;
    private  String mPhone;
    private  String mProblem;
    private  String mProfileImage;
    private  Uri resultUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_info_person);

        NameField = (EditText) findViewById(R.id.name);
        PhoneField = (EditText) findViewById(R.id.phone);
        ProblemField = (EditText) findViewById(R.id.problem);

        Confirm = (Button) findViewById(R.id.confirm);
        Exit = (Button) findViewById(R.id.exit);
        ProfilePhoto = (ImageView) findViewById(R.id.profileImage);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mPersonDatabase = FirebaseDatabase.getInstance().getReference().child("Users"). child("Persons").child(userID);

        getUserInfo();
        ProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo();
            }
        });
        Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }



private  void getUserInfo() {
        mPersonDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&& snapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map <String, Object>)  snapshot.getValue();
                    if(map.get("Имя")!=null) {
                        mName = map.get("Имя").toString();
                        NameField.setText(mName);
                    }
                    if(map.get("Номер")!=null) {
                        mPhone = map.get("Номер").toString();
                        PhoneField.setText(mPhone);
                    }
                    if(map.get("Проблема")!=null) {
                        mProblem = map.get("Проблема").toString();
                        ProblemField.setText(mProblem);
                    }
                    if(map.get("Фотография")!=null) {
                        mProfileImage = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImage).into(ProfilePhoto);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}
    private void saveUserInfo() {
        mName = NameField.getText().toString();
        mPhone = PhoneField.getText().toString();
        mProblem = ProblemField.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("Имя", mName);
        userInfo.put("Номер", mPhone);
        userInfo.put("Проблема", mProblem);
        mPersonDatabase.updateChildren(userInfo);

        if (resultUri != null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profileImageUrl").child(userID);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    Map newImage = new HashMap();

                    newImage.put("progileImageUrl", downloadUrl.toString());
                    mPersonDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        } else {

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri =  data.getData();
            resultUri = imageUri;
            ProfilePhoto.setImageURI(resultUri);
        }
    }
}
