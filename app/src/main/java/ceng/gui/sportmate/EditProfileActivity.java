package ceng.gui.sportmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import ceng.gui.sportmate.model.User;

public class EditProfileActivity extends AppCompatActivity {

    TextInputEditText txtName, txtPhoneNumber, txtCountry;
    ImageView profilePicture;
    Button btnSubmit;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private Uri imageUri;
    private StorageTask uploadTask;
    ProgressDialog progressDialog;
    ProgressDialog progressDialogForUpdate;

    User userObj;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CAMERA = 2;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static String[] PERMISSION_CAMERA = {
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        txtName = findViewById(R.id.txtChangeName);
        txtPhoneNumber = findViewById(R.id.txtChangePhoneNumber);
        txtCountry = findViewById(R.id.txtChangeCountry);
        profilePicture = findViewById(R.id.imgViewChangeProfilePicture);
        btnSubmit = findViewById(R.id.btnChangeSubmit);

        txtName.requestFocus();

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        if (firebaseUser != null) {
            databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userObj = dataSnapshot.getValue(User.class);
                    txtName.setText(userObj.getFullName());
                    txtPhoneNumber.setText(userObj.getPhoneNumber());
                    txtCountry.setText(userObj.getCountry());
                    if (userObj.getImageURL().equals("default")){
                        profilePicture.setImageResource(R.drawable.ic_default_profile_image);
                    } else {
                        Glide.with(getApplicationContext()).load(userObj.getImageURL()).into(profilePicture);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Toast.makeText(EditProfileActivity.this, "While retrieving your data, an error occurred!", Toast.LENGTH_SHORT).show();
        }

        //TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.mItemChangePassword) {
                    changePassword();
                } else if (item.getItemId() == R.id.mItemDeleteAccount) {
                    deleteAccount();
                }
                return false;
            }
        });

        //CHANGE PROFILE PICTURE SECTION
        TextView txtChangeProfilePicture = findViewById(R.id.txtChangeProfilePicture);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        txtChangeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopupForChangeProfilePicture();
            }
        });

        progressDialogForUpdate = new ProgressDialog(this, R.style.ProgressDialogCustom);
        progressDialogForUpdate.setMessage("Please wait...");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialogForUpdate.show();
                update(userObj);
            }
        });

    }

    private void openPopupForChangeProfilePicture() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this, R.style.AlertDialogCustom);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        arrayAdapter.add("Take a photo");
        arrayAdapter.add("Choose from gallery");
        arrayAdapter.add("Enter a URL");

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    int permission = ActivityCompat.checkSelfPermission(
                            EditProfileActivity.this, Manifest.permission.CAMERA);

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                EditProfileActivity.this, PERMISSION_CAMERA, REQUEST_CAMERA);
                    } else {
                        openCamera();
                    }
                }

                if (which == 1) {
                    int permission = ActivityCompat.checkSelfPermission(
                            EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                EditProfileActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                    } else {
                        openGallery();
                    }
                }

                if (which == 2) {
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(EditProfileActivity.this, R.style.AlertDialogCustom);
                    builderInner.setTitle(" ");
                    final EditText input = new EditText(EditProfileActivity.this);
                    ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.txtColor));
                    ViewCompat.setBackgroundTintList(input, colorStateList);
                    builderInner.setView(input);

                    builderInner.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ImageDownloader imageDownloader = new ImageDownloader();
                            imageDownloader.execute(input.getText().toString());
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("imageURL", input.getText().toString());
                            reference.updateChildren(map);
                        }
                    });

                    builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    builderInner.show();

                }

            }
        });
        builderSingle.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }

                return;
            }

            case REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }

                return;
            }
        }
    }

    private void update(User user) {
        user.setFullName(txtName.getText().toString());
        user.setPhoneNumber(txtPhoneNumber.getText().toString());
        user.setCountry(txtCountry.getText().toString());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        databaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialogForUpdate.dismiss();
                if (task.isSuccessful()) {
                    getSupportFragmentManager().popBackStack();
                    EditProfileActivity.this.finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "An error occurred while updating profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            Bitmap image = (Bitmap) bundle.get("data");
            profilePicture.setImageBitmap(image);

            //Bitmap to Uri
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), image, "Title", null);

            //Upload the image that came from camera activity
            imageUri = Uri.parse(path);
            uploadImage();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", "" + mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    class ImageDownloader extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            String fileName = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                    .toString() + "/" + fileName;
            Bitmap image = download(strings[0], imagePath);
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            float w = bitmap.getWidth();
            float h = bitmap.getHeight();
            int W = 400;
            int H = (int) ((h * W) / w);
            progressDialog.dismiss();
            profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, W, H, false));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditProfileActivity.this);
            progressDialog.setMax(100);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("Downloading");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        private Bitmap download(String strurl, String imagePath) {
            try {
                URL url = new URL(strurl);
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileSize = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(imagePath);

                int total = 0;

                byte[] data = new byte[1024];
                int size;

                while ((size = input.read(data)) != -1) {
                    output.write(data, 0, size);
                    total += size;

                    int percentage = (int) (((double) total / fileSize) * 100);
                    publishProgress(percentage);
                }

                output.close();
                input.close();

            } catch (IOException e) {
                Log.e("Download Error", strurl, e);
            }

            return BitmapFactory.decodeFile(imagePath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mItemChangePassword:
                changePassword();
                return true;
            case R.id.mItemDeleteAccount:
                deleteAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void changePassword() {
        final AlertDialog.Builder successfulDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        successfulDialog.setMessage("A link will be sent to your e-mail address!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog.Builder failedDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        failedDialog.setMessage("While sending reset e-mail, an unexpected error occurred. Please try again...")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail;

        if (user != null) {
            userEmail = user.getEmail();
            mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
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
            AlertDialog dialog = failedDialog.create();
            dialog.show();
        }
    }

    public void deleteAccount() {
        final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this, R.style.ProgressDialogCustom);
        progressDialog.setMessage("Please wait... Your account is deleting.");

        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        confirmDialog.setTitle("Are you sure?");
        confirmDialog.setMessage("After deleting this account, you will have to register again in order to use the app.");
        confirmDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        confirmDialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = confirmDialog.create();
        dialog.show();

    }
}
