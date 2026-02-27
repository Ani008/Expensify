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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupSuccessFragment.Group> groupList; // Using the Group model from Success fragment

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        showNavigation(); // Ensure nav is visible when returning home

        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupList = new ArrayList<>();

        adapter = new GroupAdapter(groupList, group -> {
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

        recyclerView.setAdapter(adapter);
        fetchMyGroups();

        return view;
    }

    private void fetchMyGroups() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        String myPhone = prefs.getString("loggedInPhone", "");

        if (myPhone.isEmpty()) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");

        // We listen to ALL groups and filter locally to see if the user is a member
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Check 1: Did I create it? OR Check 2: Am I in the members list?
                    boolean isCreator = myPhone.equals(ds.child("creatorId").getValue(String.class));
                    boolean isMember = ds.child("members").hasChild(myPhone);

                    if (isCreator || isMember) {
                        GroupSuccessFragment.Group group = ds.getValue(GroupSuccessFragment.Group.class);
                        if (group != null) {
                            groupList.add(group);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            if (navBar != null) navBar.setVisibility(View.VISIBLE);
            if (fab != null) fab.setVisibility(View.VISIBLE);
        }
    }
}