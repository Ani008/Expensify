package com.example.expensify;

import android.app.Activity;
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
    private String selectedMember = "Mayank";
    private String selectedGroup = ""; // We will update this dynamically now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Retrieve the group passed from MainActivity
        if (getIntent() != null && getIntent().hasExtra("SELECTED_GROUP")) {
            selectedGroup = getIntent().getStringExtra("SELECTED_GROUP");
        } else {
            selectedGroup = "Unknown Group"; // Fallback safety
        }

        // Optional: Display the selected group on screen so the user knows where they are adding it
        // TextView tvGroupName = findViewById(R.id.tv_group_name);
        // tvGroupName.setText("Adding to: " + selectedGroup);

        etAmount = findViewById(R.id.et_amount);
        Button btnSync = findViewById(R.id.btn_sync);

        btnSync.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            if (!amount.isEmpty()) {
                sendDataToPhone(amount, selectedMember, selectedGroup);
            } else {
                Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show();
            }
        });
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