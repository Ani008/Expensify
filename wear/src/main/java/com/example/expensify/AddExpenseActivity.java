package com.example.expensify;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 1. Add the OnMessageReceivedListener interface
public class AddExpenseActivity extends Activity implements MessageClient.OnMessageReceivedListener {

    private EditText etAmount;
    private TextView tvSelectedMember;

    private String selectedMember = "Loading...";
    private String selectedGroup = "";

    // 2. Change array to a dynamic List
    private List<String> teamMembers = new ArrayList<>();

    private static final String PATH_REQUEST_MEMBERS = "/request_group_members";
    private static final String PATH_MEMBERS_LIST = "/group_members_list";
    private static final String PATH_ADD_EXPENSE = "/add_group_expense";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        if (getIntent() != null && getIntent().hasExtra("SELECTED_GROUP")) {
            selectedGroup = getIntent().getStringExtra("SELECTED_GROUP");
        } else {
            selectedGroup = "Unknown Group";
        }

        etAmount = findViewById(R.id.et_amount);
        tvSelectedMember = findViewById(R.id.tv_selected_member);
        Button btnSync = findViewById(R.id.btn_sync);
        Button btnPlus = findViewById(R.id.btn_plus);
        Button btnMinus = findViewById(R.id.btn_minus);

        teamMembers.add("Loading...");
        tvSelectedMember.setText("Loading... ▼");

        btnPlus.setOnClickListener(v -> {
            String currentText = etAmount.getText().toString();
            int amount = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            etAmount.setText(String.valueOf(amount + 50));
        });

        btnMinus.setOnClickListener(v -> {
            String currentText = etAmount.getText().toString();
            int amount = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            if (amount >= 10) {
                etAmount.setText(String.valueOf(amount - 50));
            } else {
                etAmount.setText("0");
            }
        });

        tvSelectedMember.setOnClickListener(v -> {
            if (teamMembers.isEmpty() || teamMembers.get(0).equals("Loading...")) {
                Toast.makeText(this, "Fetching members...", Toast.LENGTH_SHORT).show();
                return;
            }
            showMemberSelectionDialog();
        });

        btnSync.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            if (!amount.isEmpty() && !selectedMember.equals("Loading...")) {
                sendDataToPhone(amount, selectedMember, selectedGroup);
            } else {
                Toast.makeText(this, "Enter amount and select member", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Ask the phone for the members of this specific group!
        requestMembersFromPhone(selectedGroup);
    }

    // Register and unregister the message listener
    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    private void requestMembersFromPhone(String group) {
        byte[] payload = group.getBytes(StandardCharsets.UTF_8);
        Task<List<Node>> nodesTask = Wearable.getNodeClient(this).getConnectedNodes();
        nodesTask.addOnSuccessListener(nodes -> {
            for (Node node : nodes) {
                Wearable.getMessageClient(this).sendMessage(node.getId(), PATH_REQUEST_MEMBERS, payload);
            }
        });
    }

    // 4. Receive the names from the phone
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(PATH_MEMBERS_LIST)) {
            String membersString = new String(messageEvent.getData(), StandardCharsets.UTF_8).trim();

            runOnUiThread(() -> {
                teamMembers.clear();
                if (membersString.isEmpty() || membersString.equals("EMPTY")) {
                    teamMembers.add("No members found");
                    tvSelectedMember.setText("No members found");
                } else {
                    teamMembers.addAll(Arrays.asList(membersString.split(",")));
                    selectedMember = teamMembers.get(0);
                    tvSelectedMember.setText(selectedMember + " ▼");
                }
            });
        }
    }

    private void showMemberSelectionDialog() {
        String[] membersArray = teamMembers.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Member");
        builder.setItems(membersArray, (dialog, which) -> {
            selectedMember = membersArray[which];
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
                Wearable.getMessageClient(this).sendMessage(node.getId(), PATH_ADD_EXPENSE, bytes);
            }
            Toast.makeText(this, "Syncing to " + group + "...", Toast.LENGTH_SHORT).show();
        });
    }
}