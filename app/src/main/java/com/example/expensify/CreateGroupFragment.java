package com.example.expensify;

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

// Import Firebase classes
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateGroupFragment extends Fragment {

    private int memberCount = 1; // Default to 1 member
    private DatabaseReference databaseReference; // Firebase reference

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        // Initialize Firebase Database reference pointing to a "groups" node
        databaseReference = FirebaseDatabase.getInstance().getReference("groups");

        // 1. Initialize the Views
        EditText etGroupName = view.findViewById(R.id.etGroupName);
        EditText etGroupDescription = view.findViewById(R.id.etGroupDescription); // Added description field
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

        // 3. Handle Counter Decrease
        btnDecrease.setOnClickListener(v -> {
            if (memberCount > 1) { // Prevent going below 1
                memberCount--;
                tvMemberCount.setText(String.valueOf(memberCount));
            } else {
                Toast.makeText(requireContext(), "A group must have at least 1 member", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Handle Counter Increase
        btnIncrease.setOnClickListener(v -> {
            memberCount++;
            tvMemberCount.setText(String.valueOf(memberCount));
        });

        // 5. Handle Create Group Button click
        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupDesc = etGroupDescription.getText().toString().trim();

            // Validate that the user actually entered a name
            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter a group name");
                return; // Stop execution here if it's empty
            }

            // Disable the button to prevent multiple clicks while saving
            btnCreateGroup.setEnabled(false);
            btnCreateGroup.setText("Creating...");

            // Generate a unique ID for the new group in Firebase
            String groupId = databaseReference.push().getKey();

            // Create a new Group object (Make sure you created the Group.java model class!)
            Group newGroup = new Group(groupId, groupName, groupDesc, memberCount);

            // Save data to Firebase
            if (groupId != null) {
                databaseReference.child(groupId).setValue(newGroup)
                        .addOnSuccessListener(aVoid -> {
                            // If successful, proceed to the success screen
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new GroupSuccessFragment())
                                        .addToBackStack(null)
                                        .commit();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // If it fails, show an error and re-enable the button
                            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            btnCreateGroup.setEnabled(true);
                            btnCreateGroup.setText("Create Group");
                        });
            }
        });

        return view;
    }
}