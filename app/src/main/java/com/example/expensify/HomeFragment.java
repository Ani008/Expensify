package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        view.setBackgroundColor(Color.WHITE);

        // 1. Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupList = new ArrayList<>();

        // 2. Fetch Data from Firebase
        fetchGroupsFromFirebase();

        return view;
    }

    private void fetchGroupsFromFirebase() {
        // Get the phone number we saved in SignUpActivity
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        String myPhone = sharedPreferences.getString("loggedInPhone", "");

        if (myPhone.isEmpty()) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");

        // Filter: Only fetch groups where creatorId == my logged-in phone number
        Query query = ref.orderByChild("creatorId").equalTo(myPhone);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Group group = ds.getValue(Group.class);
                    if (group != null) {
                        groupList.add(group);
                    }
                }
                // 3. Set/Update Adapter
                adapter = new GroupAdapter(groupList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}