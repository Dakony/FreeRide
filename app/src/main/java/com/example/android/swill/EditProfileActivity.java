package com.example.android.swill;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class EditProfileActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText fullName, phoneNumber;
    private CircleImageView profileImage;
    private TextView btn_save;
    private ProgressDialog loadingBar;

    private DatabaseReference editProfilePrefs;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;
    private ProgressBar progressbar;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        editProfilePrefs = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        mToolbar = (Toolbar)findViewById(R.id.edit_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        fullName = (EditText)findViewById(R.id.editFullName);
        phoneNumber = (EditText)findViewById(R.id.editPhoneNumber);
        profileImage = (CircleImageView)findViewById(R.id.profilePic);
        btn_save = (TextView)findViewById(R.id.save);
        progressbar = (ProgressBar)findViewById(R.id.progressBarImage);

        loadingBar = new ProgressDialog(this);

        editProfilePrefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String Image = dataSnapshot.child("profileimage").getValue().toString();
                    String profileName = dataSnapshot.child("fullname").getValue().toString();
                    String profileNumber = dataSnapshot.child("phone").getValue().toString();

                    Picasso.with(EditProfileActivity.this).load(Image).placeholder(R.drawable.profile).into(profileImage);
                    fullName.setText(profileName);
                    phoneNumber.setText(profileNumber);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getdetails();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });
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
               /* loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please Wait while we are uploading your Image");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();*/
               progressbar.setVisibility(View.VISIBLE);



                Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        progressbar.setVisibility(View.GONE);
                        if(task.isSuccessful())
                        {
                            Toast.makeText(EditProfileActivity.this,"Profile Image Save successfully to Firebase storage",Toast.LENGTH_SHORT).show();
                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                            editProfilePrefs.child("profileimage").setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(EditProfileActivity.this, EditProfileActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(EditProfileActivity.this,"Profile Image Save successfully to Firebase database",Toast.LENGTH_SHORT).show();
                                                //loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(EditProfileActivity.this,"Error Occurred" + message,Toast.LENGTH_SHORT).show();
                                                //loadingBar.dismiss();
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

    private void getdetails()
    {
        String getFullName = fullName.getText().toString();
        String getPhoneNumber  = phoneNumber.getText().toString();
        if(TextUtils.isEmpty(getFullName))
        {
            Toast.makeText(this, "Sorry Please Provide a name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(getPhoneNumber))
        {
            Toast.makeText(this, "Provide Phone Number", Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingBar.setTitle("Updating Profile");
                loadingBar.setMessage("Please Wait while we are updating your profile");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                UpdateAccountinfo(getFullName,getPhoneNumber );

            }

    }

    private void UpdateAccountinfo(String getFullName, String getPhoneNumber)
    {
        HashMap userMap = new HashMap();
                userMap.put("fullname", getFullName);
                userMap.put("phone", getPhoneNumber);
                editProfilePrefs.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            SendUserToMainActivity();
                            Toast.makeText(EditProfileActivity.this, "Account Profile has been Updated Successfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        else
                            {
                                Toast.makeText(EditProfileActivity.this, "Error  has Occurred, please try and again", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(EditProfileActivity.this,ProfileActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
