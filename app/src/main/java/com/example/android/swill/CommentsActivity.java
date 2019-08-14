package com.example.android.swill;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView CommentList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private DatabaseReference UsersRef,PostsRef, CommentRef;
    private FirebaseAuth mAuth;

    String Post_key,current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);



        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        Post_key = getIntent().getExtras().get("PostKey").toString();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_key).child("Comments");

        //CommentRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(Post_key);

        CommentList = (RecyclerView)findViewById(R.id.comment_list);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText)findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton)findViewById(R.id.post_comment_btn);


        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(userName);

                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        PostsRef

                )
        {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position)
            {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());

            }
        };

        CommentList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public CommentsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username)
        {
            TextView myUsername  = (TextView) mView.findViewById(R.id.comment_username);
            myUsername.setText("@"+username +"  ");
        }
        public void setComment(String comment)
        {
            TextView myComment  = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
        public void setDate(String date)
        {
            TextView mydate  = (TextView) mView.findViewById(R.id.comment_date);
            mydate.setText(" Date: "+date);
        }
        public void setTime(String time)
        {
            TextView myTime  = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText("  Time: "+time);
        }
    }

    private void ValidateComment(String userName)
    {
        String commetText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commetText))
        {
            Toast.makeText(this, "Please Indicate your Interest", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());


            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
                commentsMap.put("uid", current_user_id);
                commentsMap.put("comment", commetText);
                commentsMap.put("date", saveCurrentDate);
                commentsMap.put("time", saveCurrentTime);
                commentsMap.put("username", userName);
            PostsRef.child(RandomKey).updateChildren(commentsMap)
                     .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(CommentsActivity.this, "Your comment has been added successfully... ", Toast.LENGTH_SHORT).show();
                    }
                    else
                        {
                            Toast.makeText(CommentsActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                }
            });
        }
    }
}
