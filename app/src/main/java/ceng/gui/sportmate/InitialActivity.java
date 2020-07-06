package ceng.gui.sportmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import ceng.gui.sportmate.fragments.ProfileFragment;
import ceng.gui.sportmate.fragments.TopicsFragment;

public class InitialActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;

    BottomNavigationView navigationView;

    private int backButtonCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        navigationView = findViewById(R.id.bottomNavigationView);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fts = fm.beginTransaction();

        Fragment fragment = TopicsFragment.newInstance();

        fts.add(R.id.fragmentContainer, fragment);
        fts.addToBackStack("");
        fts.commit();

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.mainPage:
                        mainPage(menuItem);
                        return true;
                    case R.id.profilePage:
                        profilePage(menuItem);
                        return true;
                    case R.id.newTopic:
                        newTopic(menuItem);
                        return true;
                }
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void newTopic(MenuItem item) {
        Intent intent = new Intent(this, CreateTopicActivity.class);
        startActivity(intent);
    }

    public void mainPage(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fts = fm.beginTransaction();

        Fragment fragment = TopicsFragment.newInstance();

        fts.add(R.id.fragmentContainer, fragment);
        fts.commit();
    }

    public void profilePage(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fts = fm.beginTransaction();

        Fragment fragment = ProfileFragment.newInstance();

        fts.add(R.id.fragmentContainer, fragment, "profileFragment");
        fts.addToBackStack("profileFragment");
        fts.commit();
    }

    private void status(String status) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users").child(user.getUid());

        HashMap<String, Object> statusValue = new HashMap<>();
        statusValue.put("status", status);

        reference.updateChildren(statusValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
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
