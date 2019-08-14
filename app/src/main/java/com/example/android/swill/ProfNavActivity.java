package com.example.android.swill;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfNavActivity extends AppCompatActivity {
    private CircleImageView navProfileImage;
    private TextView navFullName,navUsername,navEmail,navPhoneNumber;
    private Button updateProfile;

    private DatabaseReference profilePrefs;
    private String currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_nav);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profilePrefs = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        navProfileImage = (CircleImageView)findViewById(R.id.nav_profileImage);
        navFullName = (TextView)findViewById(R.id.nav_fullName);
        navUsername = (TextView)findViewById(R.id.nav_username);
        navEmail = (TextView)findViewById(R.id.nav_email);
        navPhoneNumber = (TextView)findViewById(R.id.nav_phoneNumber);
        updateProfile = (Button)findViewById(R.id.updateProfile);

        profilePrefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myEmail = mAuth.getCurrentUser().getEmail();
                    String myPhoneNumber = dataSnapshot.child("phone").getValue().toString();

                    Picasso.with(ProfNavActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(navProfileImage);
                    navFullName.setText(myProfileName);
                    navUsername.setText(myUsername);
                    navEmail.setText(myEmail);
                    navPhoneNumber.setText(myPhoneNumber);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                editProfileActivity();
            }
        });

    }

    private void editProfileActivity()
    {
        Intent editIntent = new Intent(ProfNavActivity.this, EditProfileActivity.class);
        startActivity(editIntent);
    }
}
