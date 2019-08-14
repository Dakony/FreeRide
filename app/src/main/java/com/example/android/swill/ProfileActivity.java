package com.example.android.swill;

import android.content.Context;
import android.content.Intent;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView navigationView;
    private NavigationMenuView navMenuview;
    private RecyclerView postLists;

    private CircleImageView navProfileImage;
    private TextView navProfileUserName;
    private ImageButton SearchButton;
    private FloatingActionButton AddPost;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRefs, PostsRef, CommentRef;

    String currentUserID;
    final String PostKey="";

    Context context = this;

    public ProfileActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRefs = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");



        AddPost = (FloatingActionButton)findViewById(R.id.add_post);
        AddPost.setOnClickListener(this);

        SearchButton = (ImageButton)findViewById(R.id.search);
        SearchButton.setOnClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        mDrawerLayout =(DrawerLayout) findViewById(R.id.drawer_Layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postLists = (RecyclerView)findViewById(R.id.all_users_post_list);
        postLists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postLists.setLayoutManager(linearLayoutManager);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);


        navMenuview = (NavigationMenuView) navigationView.getChildAt(0);
        navMenuview.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));

        navProfileImage = (CircleImageView)navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView)navView.findViewById(R.id.username);


       UsersRefs.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("username")) {
                        String fullname = dataSnapshot.child("username").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }

                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this, "Sorry Username doesn't exists...", Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       DisplayAllUsersPosts();

    }

    private void DisplayAllUsersPosts()
    {
        Query sortPostInDecendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerAdapter< Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                        Posts.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        sortPostInDecendingOrder
                )

                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                        final String PostKey = getRef(position).getKey();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setDestination(model.getDestination());
                        viewHolder.setDateOfTravel(model.getDateOfTravel());
                        viewHolder.setProfileImage( getApplicationContext(),model.getProfileImage());
                        viewHolder.setCommentStatus();
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                Intent clickPostIntent = new Intent(ProfileActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                Intent commentIntent = new Intent(ProfileActivity.this, CommentsActivity.class);
                                commentIntent.putExtra("PostKey", PostKey);
                                startActivity(commentIntent);
                            }
                        });
                    }

                };
        postLists.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton commentPostButton;
        TextView DisplayNoOfInterest;
        int countComments;
        String currentUserId,PostKey;
        DatabaseReference CommentsRef;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;

            commentPostButton = (ImageButton) mView.findViewById(R.id.commentPost);
            DisplayNoOfInterest = (TextView) mView.findViewById(R.id.interest);

            CommentsRef = FirebaseDatabase.getInstance().getReference();
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();



        }

        public void setCommentStatus()
        {
            CommentsRef.child("Posts").child("Comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        countComments = (int) dataSnapshot.getChildrenCount();
                        DisplayNoOfInterest.setText(Integer.toString(countComments));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView)mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileImage(Context ctx, String profileImage)
        {
             CircleImageView image = (CircleImageView)mView.findViewById(R.id.post_profile_image);
             Picasso.with(ctx).load(profileImage).into(image);
        }
        public void setTime(String time)
        {
            TextView postTime = (TextView)mView.findViewById(R.id.post_time);
            postTime.setText("  " +time);
        }
        public void setDate(String date)
        {
            TextView postDate = (TextView)mView.findViewById(R.id.post_date);
            postDate.setText(date);
        }
        public void setDescription(String description)
        {
            TextView postDescription = (TextView)mView.findViewById(R.id.travel_description);
            postDescription.setText("Description of Journey: "+ description);
        }
        public void setDestination(String destination)
        {
            TextView postDestination = (TextView)mView.findViewById(R.id.travel_destination);
            postDestination.setText("Destination: "+ destination);
        }
        public void setDateOfTravel(String dateOfTravel)
        {
            TextView postDateoftravel = (TextView)mView.findViewById(R.id.travel_date);
            postDateoftravel.setText("Date of Journey: " + dateOfTravel);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendUserToLogin();
        }else{
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    sendUserToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetUpActivity() {
        Intent SetUpIntent = new Intent(ProfileActivity.this,SetupActivity.class);
        SetUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetUpIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_menu:
                break;
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                SendUserToNavProfile();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                sendUserToLogin();
                break;
        }
        return false;
    }



    private void SendUserToNavProfile()
    {
        Intent navIntent = new Intent(ProfileActivity.this, ProfNavActivity.class);
        startActivity(navIntent);
    }

    private void sendUserToLogin() {
        Intent loginIntent = new Intent(ProfileActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_post:
                SendUserToPostActivity();
                break;
            case R.id.search:
                SendUserToSearchActivity();
                break;
        }
    }



    private void SendUserToSearchActivity()
    {
        Intent searchIntent = new Intent(ProfileActivity.this, SearchActivity.class);
        startActivity(searchIntent);
    }

    private void SendUserToPostActivity() {
        Intent addPostIntent = new Intent(ProfileActivity.this, PostActivity.class);
        startActivity(addPostIntent);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_Layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }
}
