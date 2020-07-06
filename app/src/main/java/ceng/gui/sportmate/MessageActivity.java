package ceng.gui.sportmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ceng.gui.sportmate.adapters.MessageAdapter;
import ceng.gui.sportmate.model.Chat;
import ceng.gui.sportmate.model.User;
import ceng.gui.sportmate.notification.Client;
import ceng.gui.sportmate.notification.Data;
import ceng.gui.sportmate.notification.MyResponse;
import ceng.gui.sportmate.notification.Sender;
import ceng.gui.sportmate.notification.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    ImageView chatProfileImage;
    TextView username, txtMessage;
    ImageButton btnSend;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    static String receiverUserId;

    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;

    APIService apiService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        String receiverUsername = getIntent().getStringExtra("username");

        username = findViewById(R.id.txtMessageUsername);
        //username.setText(receiverUsername);

        chatProfileImage = findViewById(R.id.chatProfileImage);

        FirebaseDatabase firebaseDatabaseForUser = FirebaseDatabase.getInstance();
        DatabaseReference userDatabaseReference = firebaseDatabaseForUser.getReference("users");

        Query queryforUser = userDatabaseReference.orderByChild("username").equalTo(receiverUsername);
        queryforUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    username.setText(snapshot.child("fullName").getValue().toString());
                    receiverUserId = snapshot.child("id").getValue().toString();
                    if (snapshot.child("imageURL").getValue().toString().equals("default")){
                        chatProfileImage.setImageResource(R.drawable.ic_default_profile_image);
                    } else {
                        Glide.with(MessageActivity.this).load(snapshot.child("imageURL").getValue().toString()).into(chatProfileImage);
                    }

                    readMesagges(user.getUid(), receiverUserId, snapshot.child("imageURL").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        txtMessage = findViewById(R.id.txtMessage);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = txtMessage.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    sendMessage(user.getUid(), receiverUserId, message);
                    Log.d("test", receiverUserId);
                } else {
                    Toast.makeText(MessageActivity.this, "You cannot send an empty message", Toast.LENGTH_SHORT).show();
                }
                txtMessage.setText("");
            }
        });

    }

    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> messagePackage = new HashMap<>();
        messagePackage.put("sender", sender);
        messagePackage.put("receiver", receiver);
        messagePackage.put("message", message);

        databaseReference.child("Chats").push().setValue(messagePackage);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(user.getUid())
                .child(receiverUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(receiverUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(receiverUserId)
                .child(user.getUid());
        chatRefReceiver.child("id").setValue(user.getUid());

        final String msg = message;

        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMesagges(final String myid, final String userid, final String imageURL) {
        mchat = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Token token = dataSnapshot1.getValue(Token.class);
                    Data data = new Data(user.getUid(),
                            R.mipmap.ic_launcher,
                            username+": " + message,
                            "New Message", receiver);
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this,
                                                    "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users").child(user.getUid());

        HashMap<String, Object> statusValue = new HashMap<>();
        statusValue.put("status", status);

        reference.updateChildren(statusValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(receiverUserId);
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUser("none");
        status("offline");
    }
}
