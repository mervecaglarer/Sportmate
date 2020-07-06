package ceng.gui.sportmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    Button btnSubmit;
    EditText txtEmail;

    ProgressDialog progressDialog;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmail = findViewById(R.id.txtForgotPasswordField);
        btnSubmit = findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this, R.style.ProgressDialogCustom);
        progressDialog.setMessage("Please wait...");

        final AlertDialog.Builder successfulDialog = new AlertDialog.Builder(ForgotPasswordActivity.this, R.style.AlertDialogCustom);
        successfulDialog.setTitle("Forgot Password")
                .setMessage("A link is sent to your e-mail address.")
                .setPositiveButton("Back to Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

        final AlertDialog.Builder failedDialog = new AlertDialog.Builder(ForgotPasswordActivity.this, R.style.AlertDialogCustom);
        failedDialog.setTitle("Forgot Password")
                .setMessage("While sending reset e-mail, an unexpected error occurred. Please try again...")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidEmail(txtEmail.getText().toString())) {
                    progressDialog.show();
                    mAuth.sendPasswordResetEmail(txtEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                } else {
                    txtEmail.setError("Please enter a valid e-mail address!");
                    txtEmail.requestFocus();
                }
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
