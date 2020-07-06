package ceng.gui.sportmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ceng.gui.sportmate.model.Topic;
import ceng.gui.sportmate.model.User;

public class TopicDetailsActivity extends AppCompatActivity {

    TextView txtTitle, txtContent, txtUsername, txtBranch, txtDate, txtTime, txtPrice, txtAddress;
    Button btnContact;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseReference, topicsDatabaseReference;

    String username;
    String topicOwnerUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_details);

        txtTitle = findViewById(R.id.txtDetailTitle);
        txtContent = findViewById(R.id.txtDetailContent);
        txtBranch = findViewById(R.id.txtDetailBranch);
        txtDate = findViewById(R.id.txtDetailDate);
        txtTime = findViewById(R.id.txtDetailTime);
        txtPrice = findViewById(R.id.txtDetailPrice);
        txtAddress = findViewById(R.id.txtDetailAddress);
        txtUsername = findViewById(R.id.txtDetailUsername);
        btnContact = findViewById(R.id.btnContact);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();


        usersDatabaseReference = firebaseDatabase.getReference("users");

        Query queryforUser = usersDatabaseReference.child(currentUser.getUid());
        queryforUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username = user.getUsername();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        topicsDatabaseReference = firebaseDatabase.getReference("Topics");

        final String title = getIntent().getStringExtra("title");

        Query query = topicsDatabaseReference.child(title);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Topic topic = dataSnapshot.getValue(Topic.class);
                topicOwnerUsername = topic.getUsername();
                if (topicOwnerUsername.equals(username)) {
                    btnContact.setVisibility(View.GONE);
                }
                txtUsername.setText(topicOwnerUsername);
                txtTitle.setText(topic.getTitle());
                txtContent.setText(topic.getTopicContent());
                txtBranch.setText(topic.getBranch());
                txtDate.setText(topic.getDate());
                txtTime.setText(topic.getTime());
                txtPrice.setText(topic.getPrice());
                txtAddress.setText(topic.getAddress());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TopicDetailsActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        txtUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicDetailsActivity.this, UsersProfileActivity.class);
                intent.putExtra("username", txtUsername.getText().toString());
                startActivity(intent);
            }
        });

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicDetailsActivity.this, MessageActivity.class);
                intent.putExtra("username", txtUsername.getText().toString());
                startActivity(intent);
            }
        });

    }

}
