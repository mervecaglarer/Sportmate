package ceng.gui.sportmate.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import ceng.gui.sportmate.EditProfileActivity;
import ceng.gui.sportmate.MainActivity;
import ceng.gui.sportmate.MessageBoxActivity;
import ceng.gui.sportmate.R;
import ceng.gui.sportmate.model.User;


public class ProfileFragment extends Fragment {

    private TextView txtUsername, txtName, txtEmail, txtPhoneNumber, txtCountry;
    private ImageView profilePicture, messageBox;
    private ImageButton btnLogout;

    private FloatingActionButton floatingActionButton;
    private ProgressDialog progressDialog;
    private MessageCounter messageCounter;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersDatabaseReference;

    private ProgressBar progressBar;
    private View messageBoxContainer;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtUsername = view.findViewById(R.id.txtProfileUsername);
        txtName = view.findViewById(R.id.txtProfileName);
        txtEmail = view.findViewById(R.id.txtProfileEmail);
        txtPhoneNumber = view.findViewById(R.id.txtProfilePhone);
        txtCountry = view.findViewById(R.id.txtProfileCountry);
        profilePicture = view.findViewById(R.id.imgViewProfileImage);
        btnLogout = view.findViewById(R.id.btnLogout);
        floatingActionButton = view.findViewById(R.id.btnEdit);

        messageBoxContainer = view.findViewById(R.id.messageBox);
        messageBox = messageBoxContainer.findViewById(R.id.chatIcon);

        progressBar = view.findViewById(R.id.progressBarProfile);
        progressBar.setVisibility(View.VISIBLE);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDatabaseReference = firebaseDatabase.getReference("users");

        if (currentUser != null) {
            usersDatabaseReference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        txtUsername.setText(user.getUsername());
                        txtName.setText(user.getFullName());
                        txtEmail.setText(user.getEmail());
                        txtPhoneNumber.setText(user.getPhoneNumber());
                        txtCountry.setText(user.getCountry());
                        if (user.getImageURL().equals("default")){
                            profilePicture.setImageResource(R.drawable.ic_default_profile_image);
                        } else {
                            Glide.with(Objects.requireNonNull(getActivity())).load(user.getImageURL()).into(profilePicture);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

        } else {
            Toast.makeText(getActivity(), "While retrieving your data, an error occurred!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }

        // To go to edit page
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        // Inbox page intent
        messageBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MessageBoxActivity.class);
                startActivity(intent);
            }
        });


        //LOGOUT SECTION
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder logoutDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                logoutDialog.setTitle("Logout")
                        .setMessage("Are you sure to logout?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mAuth.signOut();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        });
                logoutDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog dialog = logoutDialog.create();
                dialog.show();
            }
        });
    }

    class MessageCounter {

        private TextView messageNumber;
        private final int MAX_NUMBER = 99;
        private int messageCounter = 0;

        public MessageCounter(View view) {
            messageNumber = view.findViewById(R.id.messageNumber);
        }

        public void increase() {
            messageCounter++;

            if (messageCounter <= MAX_NUMBER)
                messageNumber.setText(String.valueOf(messageCounter));
            else
                messageNumber.setText("99+");
        }
    }
}
