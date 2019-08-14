package com.example.android.swill;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView ClickImage;
    private TextView ClickDescription,ClickDestination,ClickPlateNumber,ClickDateOfJourney,DriverLicense;
    private Button ClickEdit,ClickDelete;
    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID,databaseUserID,description,plateNumber,destination,dateOfTravel,image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        ClickImage = (ImageView)findViewById(R.id.click_image_post);
        ClickDescription = (TextView)findViewById(R.id.click_description);
        ClickDestination = (TextView)findViewById(R.id.click_destination);
        ClickPlateNumber = (TextView)findViewById(R.id.click_plate_number);
        ClickDateOfJourney = (TextView)findViewById(R.id.click_dateOfJourney);
        DriverLicense = (TextView)findViewById(R.id.driverLicense);
        ClickEdit = (Button)findViewById(R.id.edit_post);
        ClickDelete = (Button)findViewById(R.id.delete_post);

        ClickEdit.setVisibility(View.INVISIBLE);
        ClickDelete.setVisibility(View.INVISIBLE);
        ClickImage.setVisibility(View.INVISIBLE);
        DriverLicense.setVisibility(View.INVISIBLE);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               if(dataSnapshot.exists()){

                   description = dataSnapshot.child("description").getValue().toString();
                   plateNumber = dataSnapshot.child("plateNumber").getValue().toString();
                   destination = dataSnapshot.child("destination").getValue().toString();
                   dateOfTravel = dataSnapshot.child("dateOfTravel").getValue().toString();
                   image = dataSnapshot.child("driversLicense").getValue().toString();
                   databaseUserID = dataSnapshot.child("uid").getValue().toString();

                   ClickDescription.setText("Description: " + description);
                   ClickDestination.setText("Destination: " + destination);
                   ClickPlateNumber.setText("Plate Number: " + plateNumber);
                   ClickDateOfJourney.setText("Date Of Journey: "+dateOfTravel);
                   Picasso.with(ClickPostActivity.this).load(image).into(ClickImage);

                   if(currentUserID.equals(databaseUserID))
                   {
                       ClickEdit.setVisibility(View.VISIBLE);
                       ClickDelete.setVisibility(View.VISIBLE);
                       ClickImage.setVisibility(View.VISIBLE);
                       DriverLicense.setVisibility(View.VISIBLE);
                   }
                   ClickEdit.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) 
                       {
                       EditCurrentPost(description,plateNumber,destination,dateOfTravel);    
                       }
                   });
               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ClickDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                DeleteCurrentPost();
            }
        });

    }

    private void EditCurrentPost(String description, String plateNumber, String destination, String dateOfTravel)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        LinearLayout layout = new LinearLayout(ClickPostActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputDescription = new EditText(ClickPostActivity.this);
        inputDescription.setText("Description: "+description);
        layout.addView(inputDescription);

        final EditText inputDestination = new EditText(ClickPostActivity.this);
        inputDestination.setText("Destination: "+destination);
        layout.addView(inputDestination);

        final EditText inputPlateNumber = new EditText(ClickPostActivity.this);
        inputPlateNumber.setText("Plate Number: "+plateNumber);
        layout.addView(inputPlateNumber);

        final EditText inputDateOfTravel = new EditText(ClickPostActivity.this);
        inputDateOfTravel.setText("Date Of Travel: "+dateOfTravel);
        layout.addView(inputDateOfTravel);

        builder.setView(layout);



        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                ClickPostRef.child("description").setValue(inputDescription.getText().toString());
                ClickPostRef.child("destination").setValue(inputDestination.getText().toString());
                ClickPostRef.child("plateNumber").setValue(inputPlateNumber.getText().toString());
                ClickPostRef.child("dateOfTravel").setValue(inputDateOfTravel.getText().toString());

                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_light);
    }

    private void DeleteCurrentPost()
    {
        ClickPostRef.removeValue();
        Toast.makeText(this, "You have Successfully Delete Your Post", Toast.LENGTH_SHORT).show();
        SendUserToMainActivity();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this,ProfileActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
