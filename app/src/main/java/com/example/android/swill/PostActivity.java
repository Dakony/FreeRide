package com.example.android.swill;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private EditText etPlate_number,etDestination,etDescription,etDateOfJourney;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Button PostRide;
    private ImageButton SelectPostImage;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Plate_number,Destination,Description,DateOfTravel, saveCurrentDate, saveCurrentTime,postRandomName, downloadUri, current_user_id;
    private StorageReference PostsImageReference;
    private DatabaseReference usersRef,PostsRef;
    private FirebaseAuth mAuth;

    private long countPost = 0 ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostsImageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post Ride");

        etPlate_number = (EditText) findViewById(R.id.plate_number);
        etDestination = (EditText) findViewById(R.id.destination);
        etDescription = (EditText) findViewById(R.id.description);
        PostRide = (Button) findViewById(R.id.submit);
        SelectPostImage = (ImageButton) findViewById(R.id.postImage);
        loadingBar = new ProgressDialog(this);


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                OpenGallary();
            }
        });

        PostRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                ValidatePostInfo();
            }
        });

        etDateOfJourney = (EditText) findViewById(R.id.dateOfJourney);
        //etDateOfJourney.setShowSoftInputOnFocus(false);
        etDateOfJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(PostActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                       mDateSetListener, year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
              final String date = day + "/" + month + "/" + year;
                etDateOfJourney.setText(date);

            }
        };


    }

    private void ValidatePostInfo()
    {
         Plate_number = etPlate_number.getText().toString();
         Destination = etDestination.getText().toString();
         Description = etDescription.getText().toString();
         DateOfTravel = etDateOfJourney.getText().toString();
        if(ImageUri == null)
        {
            Toast.makeText(this, "Please Upload Your Driver's License", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Plate_number))
        {
            Toast.makeText(this, "Please Enter Your Plate Number", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Destination))
        {
            Toast.makeText(this, "Please Indicate the destination of your Journey", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please Add Description of Your Journey", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(DateOfTravel))
        {
            Toast.makeText(this, "Please Indicate Date of Your Journey", Toast.LENGTH_SHORT).show();
        }
        else
            {
               loadingBar.setTitle("Post Ride");
                loadingBar.setMessage("Please Wait while we are uploading your Post");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                StoringImageToDB();
            }
    }

    private void StoringImageToDB()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH-mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostsImageReference.child("Drivers License Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    downloadUri = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    SavingInfoToDb();
                }else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    private void SavingInfoToDb()
    {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    countPost = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPost = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
             if(dataSnapshot.exists())
             {
                 final  String userFullname = dataSnapshot.child("fullname").getValue().toString();
                 final  String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                 HashMap postMap = new HashMap();
                 postMap.put("uid", current_user_id);
                 postMap.put("date", saveCurrentDate);
                 postMap.put("time", saveCurrentTime);
                 postMap.put("plateNumber", Plate_number);
                 postMap.put("destination", Destination);
                 postMap.put("description", Description);
                 postMap.put("dateOfTravel", DateOfTravel);
                 postMap.put("driversLicense", downloadUri);
                 postMap.put("profileImage", userProfileImage);
                 postMap.put("fullname", userFullname);
                 postMap.put("counter", countPost);

                 PostsRef.child( current_user_id + postRandomName).updateChildren(postMap)
                         .addOnCompleteListener(new OnCompleteListener() {
                             @Override
                             public void onComplete(@NonNull Task task)
                             {
                               if(task.isSuccessful())
                               {
                                   SendUserToMainActivity();
                                   Toast.makeText(PostActivity.this, "Your Post is Successfully", Toast.LENGTH_SHORT).show();
                                   //loadingBar.dismiss();
                               }else
                                   {
                                       String message = task.getException().getMessage();
                                       Toast.makeText(PostActivity.this, "Error has Occurred while posting" + message, Toast.LENGTH_SHORT).show();
                                       //loadingBar.dismiss();
                                   }
                             }
                         });
             }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallary()
    {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data!=null)
        {
          ImageUri = data.getData();
          SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, ProfileActivity.class);
        startActivity(mainIntent);
    }
}
