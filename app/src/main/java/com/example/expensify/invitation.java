package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class invitation extends Fragment {

    private String groupId;
    private TextView tvInviteGroupName, tvInviterName, tvInviteSize;
    private DatabaseReference groupsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitation, container, false);

        // 1. Initialize Views
        tvInviteGroupName = view.findViewById(R.id.tvInviteGroupName);
        tvInviterName = view.findViewById(R.id.tvInviterName);
        tvInviteSize = view.findViewById(R.id.tvInviteSize);

        // 2. Get Code from Deep Link
        if (getArguments() != null) {
            groupId = getArguments().getString("GROUP_CODE");
        }

        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        if (groupId != null) {
            fetchGroupDetails();
        } else {
            Toast.makeText(getContext(), "Invalid Invitation Link", Toast.LENGTH_SHORT).show();
        }

        // 3. Button Logic
        view.findViewById(R.id.btnJoinInvite).setOnClickListener(v -> joinGroupLogic());

        view.findViewById(R.id.btnRejectInvite).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void fetchGroupDetails() {
        Log.d("INVITE_DEBUG", "Fetching details for code: " + groupId);

        DatabaseReference specificGroupRef = groupsRef.child(groupId);
        specificGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    String name = snapshot.child("groupName").getValue(String.class);
                    String creator = snapshot.child("adminName").getValue(String.class);
                    if (creator == null) creator = "Group Admin";

                    long membersCount = snapshot.child("members").getChildrenCount();

                    tvInviteGroupName.setText(name);
                    tvInviterName.setText(creator);
                    tvInviteSize.setText(membersCount + " members");
                } else {
                    Toast.makeText(getContext(), "Group not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("INVITE_DEBUG", error.getMessage());
            }
        });
    }

    private void joinGroupLogic() {
        // 1. Get logged-in user's phone from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        String userPhone = sharedPreferences.getString("loggedInPhone", "");

        if (userPhone.isEmpty()) {
            Toast.makeText(getContext(), "Error: Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Fetch joining user's username from the "users" node
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userPhone);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String joiningUserName = "New Member";
                if (snapshot.exists()) {
                    joiningUserName = snapshot.child("username").getValue(String.class);
                }

                // 3. Add to Firebase members list
                addUserToGroupMembers(userPhone, joiningUserName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserToGroupMembers(String phone, String name) {
        // Path: groups -> [groupId] -> members -> [phone]
        DatabaseReference memberRef = groupsRef.child(groupId).child("members").child(phone);

        HashMap<String, Object> memberData = new HashMap<>();
        memberData.put("name", name);
        memberData.put("phone", phone);
        memberData.put("joinedAt", System.currentTimeMillis());

        memberRef.setValue(memberData).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Welcome to " + tvInviteGroupName.getText() + "!", Toast.LENGTH_SHORT).show();

            // Go back to Home/MainActivity
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Join failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}