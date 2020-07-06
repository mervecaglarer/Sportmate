package ceng.gui.sportmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;
    EditText txtEmail, txtPassword;
    TextView txtSignUp, txtForgotPassword;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    ProgressDialog progressDialog;

    private int backButtonCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        String label = getString(R.string.app_name);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_logo);
        int color = ResourcesCompat.getColor(getResources(), R.color.txtColor, null);

        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(label, icon, color);
        setTaskDescription(taskDesc);*/

        txtEmail = findViewById(R.id.txtLoginEmail);
        txtPassword = findViewById(R.id.txtLoginPassword);

        txtEmail.requestFocus();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Intent intent = new Intent(MainActivity.this, InitialActivity.class);
                    startActivity(intent);
                }
            }
        };

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            Intent intent = new Intent(this, InitialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        progressDialog = new ProgressDialog(this, R.style.ProgressDialogCustom);
        progressDialog.setMessage("You're logging in...");

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();
                if (email.isEmpty()) {
                    txtEmail.setError("Please enter an e-mail address!");
                    txtEmail.requestFocus();
                } else if (password.isEmpty()) {
                    txtPassword.setError("Please enter a password!");
                    txtPassword.requestFocus();
                } else {
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Intent intent = new Intent(MainActivity.this, InitialActivity.class);
                                startActivity(intent);
                                mAuth.addAuthStateListener(mAuthStateListener);
                            }

                        }
                    });
                }

            }
        });

        txtSignUp = findViewById(R.id.txtSignUp);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (backButtonCount >= 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
}
