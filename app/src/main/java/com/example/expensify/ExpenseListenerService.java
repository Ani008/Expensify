package com.example.expensify;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

public class ExpenseListenerService extends WearableListenerService {

    private static final String TAG = "WearOS_Sync";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        // Check if the message is from our watch app path
        if (messageEvent.getPath().equals("/add_group_expense")) {
            String payload = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            String[] parts = payload.split(":");

            if (parts.length == 2) {
                try {
                    int amount = Integer.parseInt(parts[0]);
                    String member = parts[1];

                    Log.d(TAG, "Received expense: ₹" + amount + " for " + member);

                    // TODO: Insert this into your local phone Database (Room/SQLite)
                    saveExpenseToDatabase(amount, member);

                } catch (NumberFormatException e) {
                    Log.e(TAG, "Failed to parse amount from watch", e);
                }
            }
        }
    }

    private void saveExpenseToDatabase(int amount, String member) {
        // Your existing database insertion logic goes here
    }
}