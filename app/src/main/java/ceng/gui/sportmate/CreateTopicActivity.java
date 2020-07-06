package ceng.gui.sportmate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import ceng.gui.sportmate.model.Topic;
import ceng.gui.sportmate.model.User;

public class CreateTopicActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    ImageButton btnShowCalendar, btnShowClock;
    Button btnCreate;
    TextView txtTitle, txtContent, txtDate, txtTime, txtPrice, txtAddress;
    Spinner spinnerBranch;

    ProgressDialog progressDialog;

    FirebaseDatabase mDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_topic);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference("Topics");

        progressDialog = new ProgressDialog(this, R.style.ProgressDialogCustom);
        progressDialog.setMessage("Topic is creating...");

        txtTitle = findViewById(R.id.txtCreateTitle);
        txtContent = findViewById(R.id.txtCreateContent);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        txtDate = findViewById(R.id.txtCreateDate);
        txtTime = findViewById(R.id.txtCreateTime);
        txtPrice = findViewById(R.id.txtCreatePrice);
        txtAddress = findViewById(R.id.txtCreateAddress);

        txtTitle.requestFocus();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.branches, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);
        spinnerBranch.setOnItemSelectedListener(this);

        btnShowCalendar = findViewById(R.id.btnCreateCalendar);
        btnShowCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnShowClock = findViewById(R.id.btnCreateTime);
        btnShowClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        btnCreate = findViewById(R.id.btnCreateTopic);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                createTopic();
                progressDialog.dismiss();
            }
        });

    }

    public void createTopic() {
        final String title = txtTitle.getText().toString();
        final String content = txtContent.getText().toString();
        final String branch = spinnerBranch.getSelectedItem().toString();
        final String date = txtDate.getText().toString();
        final String time = txtTime.getText().toString();
        final String price = txtPrice.getText().toString();
        final String address = txtAddress.getText().toString();

        String userId = user.getUid();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userDatabaseReference = firebaseDatabase.getReference("users");

        Query query = userDatabaseReference.child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String username = user.getUsername();
                String userImage = user.getImageURL();
                if (isFilled(new String[] {title, content, branch, date, time, price, address})) {
                    Topic topic = new Topic(username, userImage, title, content, branch, date, time, price, address);
                    databaseReference.child(title).setValue(topic);
                    Toast.makeText(CreateTopicActivity.this, "Topic is created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateTopicActivity.this, InitialActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(CreateTopicActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //To check if all fields are filled or not
    private boolean isFilled(String[] fields){
        for (String currentField : fields) {
            if (TextUtils.isEmpty(currentField)) {
                return false;
            }
        }
        return true;
    }

    //BRANCH SECTION
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }


    //DATE SECTION
    public void showDatePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DateAndTimePickerDialogCustom,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month++;
        String date = dayOfMonth + "/" + month + "/" + year;
        txtDate.setText(date);
    }

    //TIME SECTION
    public void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.DateAndTimePickerDialogCustom,
                this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String time = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
        txtTime.setText(time);
    }
}
