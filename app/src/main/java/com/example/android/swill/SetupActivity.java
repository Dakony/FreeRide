package com.example.android.swill;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity implements View.OnClickListener,ValueEventListener {

    private EditText setup_username, setup_fullname,setup_phone;
    private Button setup_button_information;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    ProgressBar progressBar;
    String currentUserId;
    final static int Gallery_Pick = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        setup_username = (EditText) findViewById(R.id.setup_username);
        setup_fullname = (EditText) findViewById(R.id.setup_fullname);
        setup_phone = (EditText) findViewById(R.id.setup_phone);
        setup_button_information = (Button) findViewById(R.id.setup_button_information);
        setup_button_information.setOnClickListener(this);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);

        profileImage.setOnClickListener(this);
        UsersRef.addValueEventListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        loadingBar = new ProgressDialog(this);



    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.setup_button_information:
            SaveAccountInformation();
            break;
            case R.id.setup_profile_image:
                setupUpProfileImage();
                break;
        }
    }

    private void setupUpProfileImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please Wait while we are uploading your Image");
                   loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);


                Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                        Toast.makeText(SetupActivity.this,"Profile Image Save successfully to Firebase storage",Toast.LENGTH_SHORT).show();
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        UsersRef.child("profileimage").setValue(downloadUri)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SetupActivity.this,"Profile Image Save successfully to Firebase database",Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this,"Error Occurred" + message,Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                    }
                                });
                        }
                    }
                });
            }
            else
                {
                    Toast.makeText(this, "Error Occurred Image can not be Crop, Try again Later", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
            }
        }

    }

    private void SaveAccountInformation()
    {
    String username = setup_username.getText().toString();
    String fullname = setup_fullname.getText().toString();
    String phone = setup_phone.getText().toString();

    if(TextUtils.isEmpty(username)){
        setup_username.setError("Please Provide a username");
        setup_username.requestFocus();
        return;
    }
    if(TextUtils.isEmpty(fullname)){
        setup_fullname.setError("Please Type your Full name");
        setup_fullname.requestFocus();
        return;
    }
    if(TextUtils.isEmpty(phone)){
        setup_phone.setError("Please Type your Phone number");
        setup_phone.requestFocus();
        return;
    }else{

        progressBar.setVisibility(View.VISIBLE);
        HashMap userMap = new HashMap<>();
        userMap.put("username",username);
        userMap.put("fullname",fullname);
        userMap.put("phone",phone);
        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                progressBar.setVisibility(View.GONE);
              if(task.isSuccessful()){
                  sendUserToMainActivity();
                  Toast.makeText(SetupActivity.this, "Account Created Successfully", Toast.LENGTH_LONG).show();
              }else
              {
                  String message = task.getException().getMessage();
                  Toast.makeText(SetupActivity.this, "Error has Occurred" + message, Toast.LENGTH_LONG).show();
              }
            }
        });
    }


    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,ProfileActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        if(dataSnapshot.exists())
        {
            if(dataSnapshot.hasChild("profileimage"))
            {
                String image = dataSnapshot.child("profileimage").getValue().toString();
                Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileImage);
            }else{
                Toast.makeText(this, "Please Provide a Profile Image...", Toast.LENGTH_SHORT).show();
            }


        }

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
