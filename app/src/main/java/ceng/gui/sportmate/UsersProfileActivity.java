package ceng.gui.sportmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UsersProfileActivity extends AppCompatActivity {

    TextView txtUsername, txtName, txtEmail, txtPhoneNumber, txtCountry;
    String imageURL;
    ImageView profilePicture;
    Button btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        txtUsername = findViewById(R.id.txtUsersProfileUsername);
        txtName = findViewById(R.id.txtUsersProfileName);
        txtEmail = findViewById(R.id.txtUsersProfileEmail);
        txtPhoneNumber = findViewById(R.id.txtUsersProfilePhone);
        txtCountry = findViewById(R.id.txtUsersProfileCountry);
        profilePicture = findViewById(R.id.usersProfileImage);

        String username = getIntent().getStringExtra("username");

        FirebaseDatabase firebaseDatabaseForUser = FirebaseDatabase.getInstance();
        DatabaseReference userDatabaseReference = firebaseDatabaseForUser.getReference("users");

        Query queryforUser = userDatabaseReference.orderByChild("username").equalTo(username);
        queryforUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    txtUsername.setText(snapshot.child("username").getValue().toString());
                    txtName.setText(snapshot.child("fullName").getValue().toString());
                    txtEmail.setText(snapshot.child("email").getValue().toString());
                    txtPhoneNumber.setText(snapshot.child("phoneNumber").getValue().toString());
                    txtCountry.setText(snapshot.child("country").getValue().toString());
                    imageURL = snapshot.child("imageURL").getValue().toString();
                    if (imageURL.equals("default")){
                        profilePicture.setImageResource(R.drawable.ic_default_profile_image);
                    } else {
                        Glide.with(UsersProfileActivity.this).load(imageURL).into(profilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersProfileActivity.this, MessageActivity.class);
                intent.putExtra("username", txtUsername.getText().toString());
                startActivity(intent);
            }
        });

    }
}
