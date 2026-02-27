package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    // Missing declarations added here
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupList = new ArrayList<>();
        // Initialize adapter early to avoid null pointer issues
        adapter = new GroupAdapter(groupList);
        recyclerView.setAdapter(adapter);

        // --- Setup "Browse Templates >" Click Listener ---
        TextView tvOpenTemplates = view.findViewById(R.id.tvOpenTemplates);
        if (tvOpenTemplates != null) {
            tvOpenTemplates.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new template())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }



        // 2. Fetch Data from Firebase
        fetchGroupsFromFirebase();

        return view;
    }

    private void fetchGroupsFromFirebase() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        // Ensure this matches the key used in your SignUpActivity
        String myPhone = sharedPreferences.getString("loggedInPhone", "");

        if (myPhone.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Querying the "groups" node where "creatorId" matches my phone number
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");
        Query query = ref.orderByChild("creatorId").equalTo(myPhone);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Group group = ds.getValue(Group.class);
                        if (group != null) {
                            groupList.add(group);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) { // Check if fragment is still attached
                    Toast.makeText(getContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}