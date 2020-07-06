package ceng.gui.sportmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ceng.gui.sportmate.model.User;

public class SignupActivity extends AppCompatActivity {

    Button btnSignup;
    EditText txtFullName, txtUsername, txtEmail, txtPassword, txtConfirmPassword, txtPhoneNumber, txtCountry;
    CheckBox checkBoxPolicy;

    private FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    Bitmap profilePictureForNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this, R.style.ProgressDialogCustom);
        progressDialog.setMessage("Please wait...");

        final AlertDialog.Builder successfulDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        successfulDialog.setMessage("You've successfully sign up!")
                .setPositiveButton("Back to Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

        final AlertDialog.Builder failedDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        failedDialog.setMessage("While signing in, an unexpected error occurred. Please try after a while...")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

        txtFullName = findViewById(R.id.txtFullName);
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtSignupEmail);
        txtPassword = findViewById(R.id.txtSignupPassword);
        txtConfirmPassword = findViewById(R.id.txtSignupConfirmPassword);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
        txtCountry = findViewById(R.id.txtCountry);
        checkBoxPolicy = findViewById(R.id.checkBoxPolicy);

        txtFullName.requestFocus();

        btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = txtFullName.getText().toString().trim();
                final String username = txtUsername.getText().toString().trim();
                final String email = txtEmail.getText().toString().trim();
                final String password = txtPassword.getText().toString().trim();
                final String confirmPassword = txtConfirmPassword.getText().toString().trim();
                final String phoneNumber = txtPhoneNumber.getText().toString().trim();
                final String country = txtCountry.getText().toString().trim();

                if (fullName.isEmpty()) {
                    txtFullName.setError("Please type your name!");
                    txtFullName.requestFocus();
                } else if (username.isEmpty()) {
                    txtUsername.setError("Please type a username!");
                    txtUsername.requestFocus();
                } else if (email.isEmpty()) {
                    txtEmail.setError("Please enter an e-mail!");
                    txtEmail.requestFocus();
                } else if (password.isEmpty()) {
                    txtPassword.setError("Please enter a password!");
                    txtPassword.requestFocus();
                } else if (password.length() < 6){
                    txtPassword.setError("Your password should contain at least 6 characters!");
                    txtPassword.requestFocus();
                } else if (!password.equals(confirmPassword)) {
                    txtConfirmPassword.setError("Passwords are not same!");
                    txtConfirmPassword.requestFocus();
                } else if (country.isEmpty()){
                    txtCountry.setError("Please enter a country where you live");
                    txtCountry.requestFocus();
                } else if (!checkBoxPolicy.isChecked()){
                    checkBoxPolicy.setError("Please check the box to sign up to the application");
                } else {
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();
                                AlertDialog dialog = failedDialog.create();
                                dialog.show();
                            } else {
                                User user = new User(mAuth.getCurrentUser().getUid(), "default", fullName, username, email, phoneNumber, country, "offline");
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            AlertDialog dialog = successfulDialog.create();
                                            dialog.show();
                                        } else {
                                            AlertDialog dialog = failedDialog.create();
                                            dialog.show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }
}
