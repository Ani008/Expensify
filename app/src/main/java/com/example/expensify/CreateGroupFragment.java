package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateGroupFragment extends Fragment {

    private int memberCount = 1;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        // 1. Hide the Bottom Navigation Bar and FAB and remove container margin
        hideNavigation();

        // Handle Back Button
        // 1. Initialize Firebase & Views
        databaseReference = FirebaseDatabase.getInstance().getReference("groups");

        EditText etGroupName = view.findViewById(R.id.etGroupName);
        EditText etGroupDescription = view.findViewById(R.id.etGroupDescription);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        TextView tvMemberCount = view.findViewById(R.id.tvMemberCount);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);
        Button btnCreateGroup = view.findViewById(R.id.btnCreateGroup);

        // 2. Handle Back Button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // 3. Handle Counter
        btnDecrease.setOnClickListener(v -> {
            if (memberCount > 1) {
                memberCount--;
                tvMemberCount.setText(String.valueOf(memberCount));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            memberCount++;
            tvMemberCount.setText(String.valueOf(memberCount));
        });

        // 4. Handle Create Group
        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupDesc = etGroupDescription.getText().toString().trim();

            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter a group name");
                return;
            }

            // --- HACKATHON FIX: Use SharedPreferences instead of FirebaseAuth ---
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
            String currentUserId = sharedPreferences.getString("loggedInPhone", "");

            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Error: Session expired. Please Login again.", Toast.LENGTH_SHORT).show();
                return;
            }
            // ---------------------------------------------------------------------

            btnCreateGroup.setEnabled(false);
            btnCreateGroup.setText("Creating...");

            // Generate unique ID
            String groupId = databaseReference.push().getKey();

            // Create object with phone number as the creatorId
            Group newGroup = new Group(groupId, groupName, groupDesc, memberCount, currentUserId);

            if (groupId != null) {
                databaseReference.child(groupId).setValue(newGroup)
                        .addOnSuccessListener(aVoid -> {
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.slide_up_fade_in, R.anim.fade_out, R.anim.slide_up_fade_in, R.anim.fade_out)
                                        .replace(R.id.fragment_container, new GroupSuccessFragment())
                                        .addToBackStack(null)
                                        .commit();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            btnCreateGroup.setEnabled(true);
                            btnCreateGroup.setText("Create Group");
                        });
            }
        });

        return view;
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = 0;
                fragmentContainer.requestLayout();
            }
        }
    }
}