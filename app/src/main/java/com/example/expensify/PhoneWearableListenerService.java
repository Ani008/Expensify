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

        } else if (path.equals(PATH_REQUEST_MEMBERS)) {
            // THIS WAS MISSING! Watch asked for members of a specific group
            String groupName = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Log.d(TAG, "Fetching members for group: " + groupName);
            fetchGroupMembers(groupName, messageEvent.getSourceNodeId());

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

    // Add these constants at the top
    private static final String PATH_REQUEST_MEMBERS = "/request_group_members";
    private static final String PATH_MEMBERS_LIST = "/group_members_list";
    // --- NEW METHODS FOR FETCHING MEMBERS ---

    private void fetchGroupMembers(String groupName, String watchNodeId) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        // 1. Find the group by its name
        groupsRef.orderByChild("groupName").equalTo(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot groupSnapshot : snapshot.getChildren()) {

                                // IMPORTANT: Adjust "members" to match whatever you named the node inside the group
                                DataSnapshot membersNode = groupSnapshot.child("members");
                                List<String> phoneNumbers = new ArrayList<>();

                                for (DataSnapshot memberSnap : membersNode.getChildren()) {
                                    // If you store members like { "+919876543210": true }, use getKey()
                                    // If you store them in an array/list, use getValue(String.class)
                                    String phone = memberSnap.getValue(String.class);
                                    if (phone != null) {
                                        phoneNumbers.add(phone);
                                    }
                                }

                                // 2. Pass the phone numbers to the next function to get the names
                                fetchNamesFromUsersCollection(phoneNumbers, watchNodeId);
                                break;
                            }
                        } else {
                            sendPayloadToWatch(watchNodeId, PATH_MEMBERS_LIST, "EMPTY");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch group: " + error.getMessage());
                    }
                });
    }

    private void fetchNamesFromUsersCollection(List<String> phoneNumbers, String watchNodeId) {
        if (phoneNumbers.isEmpty()) {
            sendPayloadToWatch(watchNodeId, PATH_MEMBERS_LIST, "EMPTY");
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users"); // Adjust to your users collection name
        List<String> memberNames = new ArrayList<>();

        // We use an array to hold the counter because it must be effectively final inside the listener
        final int[] pendingQueries = {phoneNumbers.size()};

        for (String phone : phoneNumbers) {

            // Assuming the phone number is the KEY in the Users collection
            usersRef.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // IMPORTANT: Adjust "name" to match your user's name field in Firebase
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            memberNames.add(name);
                        }
                    } else {
                        memberNames.add("Unknown (" + phone + ")");
                    }

                    // Decrease our counter. If it hits 0, all names are fetched!
                    pendingQueries[0]--;
                    if (pendingQueries[0] == 0) {
                        String namesString = TextUtils.join(",", memberNames);
                        sendPayloadToWatch(watchNodeId, PATH_MEMBERS_LIST, namesString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    pendingQueries[0]--;
                }
            });
        }
    }

    // Helper method to send any path/payload combination to the watch
    private void sendPayloadToWatch(String watchNodeId, String path, String payloadString) {
        byte[] payload = payloadString.getBytes(StandardCharsets.UTF_8);
        Wearable.getMessageClient(this).sendMessage(watchNodeId, path, payload)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send data to watch on " + path, e));
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

            DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");

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