package com.example.expensify;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.nio.charset.StandardCharsets;

public class PhoneWearableListenerService extends WearableListenerService {

    private static final String TAG = "ExpensifyPhoneService";
    private static final String PATH_REQUEST_GROUPS = "/request_groups";
    private static final String PATH_GROUPS_LIST = "/groups_list";
    private static final String PATH_ADD_EXPENSE = "/add_group_expense";

    // This method triggers automatically whenever the watch sends a message
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d(TAG, "Message received from watch on path: " + path);

        if (path.equals(PATH_REQUEST_GROUPS)) {
            // 1. Watch asked for the groups dropdown list
            // messageEvent.getSourceNodeId() gets the exact ID of the watch that asked
            sendGroupsToWatch(messageEvent.getSourceNodeId());

        } else if (path.equals(PATH_ADD_EXPENSE)) {
            // 2. Watch sent a new expense to save
            String payload = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            processNewExpense(payload);
        }
    }

    private void sendGroupsToWatch(String watchNodeId) {
        // For now, this is a hardcoded string.
        // Later, you can fetch this dynamically from your phone's database or Firebase.
        String groupsString = "Roommates,Goa Trip,Family,Office";
        byte[] payload = groupsString.getBytes(StandardCharsets.UTF_8);

        // Send the string back to the specific watch that requested it
        Wearable.getMessageClient(this).sendMessage(watchNodeId, PATH_GROUPS_LIST, payload)
                .addOnSuccessListener(taskId -> Log.d(TAG, "Successfully sent groups list to watch"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send groups list", e));
    }

    private void processNewExpense(String payload) {
        // Payload format from your watch code: amount:member:group
        String[] parts = payload.split(":");

        if (parts.length == 3) {
            String amount = parts[0];
            String member = parts[1]; // Will be "Mayank" based on your watch code
            String group = parts[2];

            // Log it to verify it arrived!
            // Here is where you will eventually add your logic to save this expense
            // to Firebase or your local database.
            Log.d(TAG, "SUCCESS! New Expense Received -> Amount: ₹" + amount +
                    ", Member: " + member +
                    ", Group: " + group);
        }
    }
}