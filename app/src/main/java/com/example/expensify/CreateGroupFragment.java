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

        // 1. Initialize Firebase & Views
        databaseReference = FirebaseDatabase.getInstance().getReference("groups");
        EditText etGroupName = view.findViewById(R.id.etGroupName);
        EditText etGroupDescription = view.findViewById(R.id.etGroupDescription);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        TextView tvMemberCount = view.findViewById(R.id.tvMemberCount);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);
        Button btnCreateGroup = view.findViewById(R.id.btnCreateGroup);

        // 2. Handle Back Button (Independent)
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // 3. Handle Counter (Independent)
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

        // 4. Handle Create Group & Firebase (Independent)
        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupDesc = etGroupDescription.getText().toString().trim();

            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter a group name");
                return;
            }

            btnCreateGroup.setEnabled(false);
            btnCreateGroup.setText("Creating...");

            String groupId = databaseReference.push().getKey();
            Group newGroup = new Group(groupId, groupName, groupDesc, memberCount);

            if (groupId != null) {
                databaseReference.child(groupId).setValue(newGroup)
                        .addOnSuccessListener(aVoid -> {
                            // Navigate to Success Fragment with Animations
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

        return view; // Return the view at the very end of the method
    }
}