package com.example.expensify;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class AddExpenseActivity extends Activity {

    private EditText etAmount;
    private TextView tvSelectedMember;

    private String selectedMember = "Mayank";
    private String selectedGroup = "";

    // Array of team members for the selection dialog
    private final String[] teamMembers = {"Mayank", "Om", "Soham", "Aditya", "Aniket"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Retrieve the group passed from MainActivity
        if (getIntent() != null && getIntent().hasExtra("SELECTED_GROUP")) {
            selectedGroup = getIntent().getStringExtra("SELECTED_GROUP");
        } else {
            selectedGroup = "Unknown Group";
        }

        // Link the UI elements from XML
        etAmount = findViewById(R.id.et_amount);
        tvSelectedMember = findViewById(R.id.tv_selected_member);
        Button btnSync = findViewById(R.id.btn_sync);
        Button btnPlus = findViewById(R.id.btn_plus);
        Button btnMinus = findViewById(R.id.btn_minus);

        // 1. Setup Add (+) button logic (increments by 10, adjust as needed)
        btnPlus.setOnClickListener(v -> {
            String currentText = etAmount.getText().toString();
            int amount = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            etAmount.setText(String.valueOf(amount + 50));
        });

        // 2. Setup Subtract (-) button logic
        btnMinus.setOnClickListener(v -> {
            String currentText = etAmount.getText().toString();
            int amount = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            if (amount >= 10) {
                etAmount.setText(String.valueOf(amount - 50));
            } else {
                etAmount.setText("0"); // Prevents negative expenses
            }
        });

        // 3. Setup Member Selection Dialog
        tvSelectedMember.setOnClickListener(v -> showMemberSelectionDialog());

        // 4. Setup Sync button
        btnSync.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            if (!amount.isEmpty()) {
                sendDataToPhone(amount, selectedMember, selectedGroup);
            } else {
                Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMemberSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Member");
        builder.setItems(teamMembers, (dialog, which) -> {
            selectedMember = teamMembers[which];
            // Update the UI with the selected member
            tvSelectedMember.setText(selectedMember + " ▼");
        });
        builder.show();
    }

    private void sendDataToPhone(String amount, String member, String group) {
        String payloadString = amount + ":" + member + ":" + group;
        byte[] bytes = payloadString.getBytes(StandardCharsets.UTF_8);

        Task<List<Node>> nodesTask = Wearable.getNodeClient(this).getConnectedNodes();
        nodesTask.addOnSuccessListener(nodes -> {
            for (Node node : nodes) {
                Wearable.getMessageClient(this).sendMessage(node.getId(), "/add_group_expense", bytes);
            }
            Toast.makeText(this, "Syncing to " + group + "...", Toast.LENGTH_SHORT).show();

            // Optional: Close this activity and go back to MainActivity after sending
            // finish();
        });
    }
}