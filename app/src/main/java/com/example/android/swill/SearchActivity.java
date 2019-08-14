package com.example.android.swill;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private RecyclerView searchResultlist;
    private EditText searchInputText;
    private ImageButton searchButton;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar)findViewById(R.id.search_post);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search");


        searchResultlist = (RecyclerView)findViewById(R.id.search_result_list);
        searchResultlist.setHasFixedSize(true);
        searchResultlist.setLayoutManager(new LinearLayoutManager(this));

        //searchInputText =(EditText)findViewById(R.id.etSearch);
        //searchButton = (ImageButton)findViewById(R.id.search_btn);

        /*searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String searchBoxInput = searchInputText.getText().toString();
                searchPost(searchBoxInput);
            }
        });*/


    }


    private void search(String searchText)
    {

        Query SearchPost = allUsersDatabaseRef.orderByChild("destination").startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerAdapter<FindPost, FindPostViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindPost, FindPostViewHolder>
                (
                        FindPost.class,
                        R.layout.all_users_display_layout,
                        FindPostViewHolder.class,
                        SearchPost

                )
        {
            @Override
            protected void populateViewHolder(FindPostViewHolder viewHolder, FindPost model, int position)
            {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setDestination(model.getDestination());
                viewHolder.setDateOfTravel(model.getDateOfTravel());
                viewHolder.setProfileImage(getApplication(),model.getProfileImage());
            }
        };
        searchResultlist.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
    /*private void searchPost(String searchBoxInput)
    {
        Toast.makeText(this, "Searching ....", Toast.LENGTH_LONG).show();
        Query SearchPost = allUsersDatabaseRef.orderByChild("destination").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindPost, FindPostViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindPost, FindPostViewHolder>
                (
                        FindPost.class,
                        R.layout.all_users_display_layout,
                        FindPostViewHolder.class,
                        SearchPost

                )
        {
            @Override
            protected void populateViewHolder(FindPostViewHolder viewHolder, FindPost model, int position)
            {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setDestination(model.getDestination());
                viewHolder.setDateOfTravel(model.getDateOfTravel());
                viewHolder.setProfileImage(getApplication(),model.getProfileImage());
            }
        };
        searchResultlist.setAdapter(firebaseRecyclerAdapter);
    }*/

    public static class FindPostViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindPostViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setProfileImage(Context ctx, String profileImage)
        {
            CircleImageView myImage = (CircleImageView)mView.findViewById(R.id.search_post_profile_image);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname)
        {
            TextView myName = (TextView)mView.findViewById(R.id.search_post_username);
            myName.setText(fullname);
        }

        public void setDate(String date)
        {
            TextView myDate = (TextView)mView.findViewById(R.id.search_post_date);
            myDate.setText(date);
        }

        public void setTime(String time)
        {
            TextView myTime = (TextView)mView.findViewById(R.id.search_post_time);
            myTime.setText(time);
        }
        public void setDescription(String description)
        {
            TextView DescriptionOfPost = (TextView)mView.findViewById(R.id.search_travel_description);
            DescriptionOfPost.setText(description);
        }

        public void setDestination(String destination)
        {
            TextView DestinationOfPost = (TextView)mView.findViewById(R.id.search_travel_destination);
            DestinationOfPost.setText(destination);
        }
        public void setDateOfTravel(String dateOfTravel)
        {
            TextView DateofTraveOfPost = (TextView)mView.findViewById(R.id.search_travel_date);
            DateofTraveOfPost.setText(dateOfTravel);
        }
    }
}
