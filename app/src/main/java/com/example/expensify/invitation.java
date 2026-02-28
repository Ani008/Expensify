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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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

        tvInviteGroupName = view.findViewById(R.id.tvInviteGroupName);
        tvInviterName = view.findViewById(R.id.tvInviterName);
        tvInviteSize = view.findViewById(R.id.tvInviteSize);

        if (getArguments() != null) {
            groupId = getArguments().getString("GROUP_CODE");
        }

        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        if (groupId != null) {
            fetchGroupDetails();
        } else {
            Toast.makeText(getContext(), "Invalid Invitation Link", Toast.LENGTH_SHORT).show();
        }

        view.findViewById(R.id.btnJoinInvite).setOnClickListener(v -> joinGroupLogic());

        view.findViewById(R.id.btnRejectInvite).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void fetchGroupDetails() {
        DatabaseReference specificGroupRef = groupsRef.child(groupId);
        specificGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    String name = snapshot.child("groupName").getValue(String.class);
                    String creator = snapshot.child("adminName").getValue(String.class);

                    // Get count from the actual 'memberCount' field we created earlier
                    Long count = snapshot.child("memberCount").getValue(Long.class);

                    tvInviteGroupName.setText(name);
                    tvInviterName.setText(creator != null ? creator : "Group Admin");
                    tvInviteSize.setText((count != null ? count : 0) + " members");
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
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        String userPhone = sharedPreferences.getString("loggedInPhone", "");

        if (userPhone.isEmpty()) {
            Toast.makeText(getContext(), "Error: Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already a member first to avoid double counting
        groupsRef.child(groupId).child("members").child(userPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(getContext(), "You are already a member!", Toast.LENGTH_SHORT).show();
                        } else {
                            performTransactionJoin(userPhone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * Uses a Firebase Transaction to safely increment the memberCount
     * while adding the user to the members list.
     */
    private void performTransactionJoin(String userPhone) {
        DatabaseReference groupRootRef = groupsRef.child(groupId);

        groupRootRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    return Transaction.success(currentData);
                }

                // 1. Increment the memberCount integer
                Integer currentCount = currentData.child("memberCount").getValue(Integer.class);
                if (currentCount == null) currentCount = 0;
                currentData.child("memberCount").setValue(currentCount + 1);

                // 2. Add user to the 'members' map
                // We store 'true' to keep the structure light, matching the screenshot
                currentData.child("members").child(userPhone).setValue(true);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentSnapshot) {
                if (committed) {
                    Toast.makeText(getContext(), "Joined successfully!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "Join failed: " + (error != null ? error.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}