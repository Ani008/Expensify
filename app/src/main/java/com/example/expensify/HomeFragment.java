package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
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

        // 1. Initialize RecyclerView and List
        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupList = new ArrayList<>();

        // 2. Initialize Adapter with Click Listener
        adapter = new GroupAdapter(groupList, group -> {
            // Navigate to TimelineFragment
            TimelineFragment timelineFragment = new TimelineFragment();

            Bundle bundle = new Bundle();
            bundle.putString("groupId", group.groupId);
            bundle.putString("groupName", group.groupName);
            timelineFragment.setArguments(bundle);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, timelineFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // CRITICAL: Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);

        // 3. Fetch Data from Firebase
        fetchGroupsFromFirebase();

        return view;
    }

    private void fetchGroupsFromFirebase() {
        // Use getContext() or requireContext() safely
        Context context = getContext();
        if (context == null) return;

        SharedPreferences sharedPreferences = context.getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        String myPhone = sharedPreferences.getString("loggedInPhone", "");

        if (myPhone.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");
        // Ensure your Firebase Indexing is set up for "creatorId" in the Firebase Console
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}