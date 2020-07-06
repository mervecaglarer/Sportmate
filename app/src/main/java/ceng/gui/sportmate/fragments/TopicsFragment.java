package ceng.gui.sportmate.fragments;


import java.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ceng.gui.sportmate.R;
import ceng.gui.sportmate.model.Topic;
import ceng.gui.sportmate.adapters.TopicAdapter;
import ceng.gui.sportmate.model.TopicItem;


public class TopicsFragment extends Fragment {

    RecyclerView topicRecyclerView;
    EditText searchInput;
    TopicAdapter topicAdapter;
    List<TopicItem> mData;
    CharSequence search="";

    ProgressBar progressBar;

    FirebaseDatabase firebaseDatabase;

    public TopicsFragment() {
        // Required empty public constructor
    }

    public static TopicsFragment newInstance() {
        TopicsFragment fragment = new TopicsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_topics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBarTopics);
        progressBar.setVisibility(View.VISIBLE);

        topicRecyclerView = view.findViewById(R.id.topic_rv);
        topicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchInput = view.findViewById(R.id.search_input);

        mData = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference("Topics");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mData.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Topic topic = snapshot.getValue(Topic.class);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date strDate = null;
                    try {
                        strDate = sdf.parse(topic.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (!new Date().after(strDate)) {
                        DatabaseReference usersDatabaseReference = firebaseDatabase.getReference("users");
                        Query queryforUser = usersDatabaseReference.orderByChild("username").equalTo(topic.getUsername());
                        queryforUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String userProfilePictureURL = snapshot.child("imageURL").getValue().toString();
                                    mData.add(new TopicItem(topic.getTitle(), topic.getTopicContent(), topic.getDate(), userProfilePictureURL));
                                }
                                topicAdapter = new TopicAdapter(getActivity(), mData);
                                topicRecyclerView.setAdapter(topicAdapter);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }

//                topicAdapter = new TopicAdapter(getActivity(), mData);
//                topicRecyclerView.setAdapter(topicAdapter);
//                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                topicAdapter.getFilter().filter(s);
                search = s;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
