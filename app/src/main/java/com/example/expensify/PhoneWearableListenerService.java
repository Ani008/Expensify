package com.example.expensify;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneWearableListenerService extends WearableListenerService {

    private static final String TAG = "ExpensifyPhoneService";
    private static final String PATH_REQUEST_GROUPS = "/request_groups";
    private static final String PATH_GROUPS_LIST = "/groups_list";
    private static final String PATH_ADD_EXPENSE = "/add_group_expense";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d(TAG, "Message received from watch on path: " + path);

        if (path.equals(PATH_REQUEST_GROUPS)) {
            // Watch asked for the groups dropdown list
            sendGroupsToWatch(messageEvent.getSourceNodeId());

        } else if (path.equals(PATH_ADD_EXPENSE)) {
            // Watch sent a new expense to save
            String payload = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            processNewExpense(payload);
        }
    }

    private void sendGroupsToWatch(String watchNodeId) {
        // Pointing to your "Groups" collection
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        groupsRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    List<String> groupNames = new ArrayList<>();

                    // Loop through all the random ID children in the "Groups" node
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {

                        // Look inside the random ID node for the actual name field.
                        // IMPORTANT: Change "groupName" if your field is actually called "name" or "title"
                        String name = snapshot.child("groupName").getValue(String.class);

                        // Only add it if it's not null or empty
                        if (name != null && !name.trim().isEmpty()) {
                            groupNames.add(name);
                        }
                    }

                    // Check if we found any valid groups
                    if (groupNames.isEmpty()) {
                        sendPayloadToWatch(watchNodeId, "EMPTY");
                    } else {
                        // Join the list into a single comma-separated string
                        String groupsString = TextUtils.join(",", groupNames);
                        sendPayloadToWatch(watchNodeId, groupsString);
                    }
                } else {
                    Log.w(TAG, "Firebase groups fetch failed or node does not exist.");
                    sendPayloadToWatch(watchNodeId, "EMPTY");
                }
            }
        });
    }

    // Helper method to keep the sending logic clean
    private void sendPayloadToWatch(String watchNodeId, String payloadString) {
        byte[] payload = payloadString.getBytes(StandardCharsets.UTF_8);

        Wearable.getMessageClient(this).sendMessage(watchNodeId, PATH_GROUPS_LIST, payload)
                .addOnSuccessListener(taskId -> Log.d(TAG, "Successfully sent to watch: " + payloadString))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send data to watch", e));
    }

    private void processNewExpense(String payload) {
        String[] parts = payload.split(":");

        if (parts.length == 3) {
            String amountString = parts[0];
            String member = parts[1];
            String groupName = parts[2];

            // Safely convert the amount string to a number
            double amount = 0;
            try {
                amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid amount format received from watch: " + amountString);
                return;
            }

            Log.d(TAG, "Attempting to save to Firebase -> Amount: ₹" + amount +
                    ", Member: " + member +
                    ", Group: " + groupName);

            DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Groups");

            // 1. Search Firebase for the group that matches the name sent by the watch
            // IMPORTANT: "groupName" must exactly match the key in your Firebase structure

            final double finalAmount = amount;
            groupsRef.orderByChild("groupName").equalTo(groupName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // 2. We found a match! Loop through results (usually just one)
                                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                                    String groupId = groupSnapshot.getKey(); // Gets the -Nxyz... ID

                                    // 3. Prepare the expense data
                                    Map<String, Object> expenseData = new HashMap<>();
                                    expenseData.put("amount", finalAmount);
                                    expenseData.put("paidBy", member);
                                    expenseData.put("timestamp", ServerValue.TIMESTAMP); // Saves exact server time

                                    // 4. Push this new expense into an "expenses" sub-collection inside this group
                                    groupsRef.child(groupId).child("expenses").push().setValue(expenseData)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Expense successfully saved to Firebase!"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Failed to save expense to Firebase", e));

                                    break; // Stop after the first matching group
                                }
                            } else {
                                Log.e(TAG, "Could not find a group named: " + groupName + " in Firebase.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database query cancelled or failed: " + error.getMessage());
                        }
                    });
        }
    }
}