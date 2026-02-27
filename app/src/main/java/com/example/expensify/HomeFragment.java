package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<GroupSuccessFragment.Group> groupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Show Navigation (In case it was hidden by CreateGroupFragment)
        showNavigation();

        // 2. Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupList = new ArrayList<>();

        // 3. Setup Adapter with Click Listener
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

        // 4. "Browse Templates" Click Listener
        TextView tvOpenTemplates = view.findViewById(R.id.tvOpenTemplates);
        if (tvOpenTemplates != null) {
            tvOpenTemplates.setOnClickListener(v -> navigateTo(new template()));
        }

        // 6. Start Data Fetch
        fetchGroupsFromFirebase();

        return view;
    }

    private void fetchGroupsFromFirebase() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        String myPhone = prefs.getString("loggedInPhone", "");

        if (myPhone.isEmpty()) {
            Toast.makeText(context, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups");

        // Real-time listener: Updates list automatically when someone joins or a group is created
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                groupList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Logic: Show if User is Creator OR User is in the Members list
                    String creatorId = ds.child("creatorId").getValue(String.class);
                    boolean isMember = ds.child("members").hasChild(myPhone);

                    if (myPhone.equals(creatorId) || isMember) {
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
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void showNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            if (navBar != null) navBar.setVisibility(View.VISIBLE);
            if (fab != null) fab.setVisibility(View.VISIBLE);

            // Reset fragment container margin to accommodate bottom nav
            View container = getActivity().findViewById(R.id.fragment_container);
            if (container != null && container.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                // Usually 56dp or 60dp for bottom nav
                params.bottomMargin = (int) (60 * getResources().getDisplayMetrics().density);
                container.setLayoutParams(params);
            }
        }
    }
}