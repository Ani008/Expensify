package com.example.expensify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements
        CapabilityClient.OnCapabilityChangedListener,
        MessageClient.OnMessageReceivedListener {

    private TextView tvConnectionStatus;
    private TextView tvSelectGroup;
    private List<String> groupList;

    private static final String CAPABILITY_NAME = "verify_expensify_phone";
    private static final String PATH_REQUEST_GROUPS = "/request_groups";
    private static final String PATH_GROUPS_LIST = "/groups_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        tvSelectGroup = findViewById(R.id.tv_select_group);

        groupList = new ArrayList<>();
        groupList.add("Loading...");
        tvSelectGroup.setText("Loading... ▼");

        // When the user taps the TextView, show the dialog of groups
        tvSelectGroup.setOnClickListener(v -> {
            if (groupList.isEmpty() || groupList.get(0).equals("Loading...")) {
                Toast.makeText(this, "Waiting for groups from phone...", Toast.LENGTH_SHORT).show();
                return;
            }
            showGroupSelectionDialog();
        });
    }

    private void showGroupSelectionDialog() {
        // Convert our list to an array for the AlertDialog
        String[] groupsArray = groupList.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Group");
        builder.setItems(groupsArray, (dialog, which) -> {
            String selectedGroup = groupsArray[which];

            // Update the TextView to show the selection
            tvSelectGroup.setText(selectedGroup + " ▼");

            // Redirect to the next screen and pass the selected group
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            intent.putExtra("SELECTED_GROUP", selectedGroup);
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getCapabilityClient(this).addListener(this, CAPABILITY_NAME);
        Wearable.getMessageClient(this).addListener(this);
        checkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getCapabilityClient(this).removeListener(this, CAPABILITY_NAME);
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        boolean isConnected = !capabilityInfo.getNodes().isEmpty();
        updateUI(isConnected);
        if (isConnected) {
            requestGroupsFromPhone();
        }
    }

    private void checkConnection() {
        Wearable.getCapabilityClient(this)
                .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener(capabilityInfo -> {
                    boolean isConnected = !capabilityInfo.getNodes().isEmpty();
                    updateUI(isConnected);
                    if (isConnected) {
                        requestGroupsFromPhone();
                    }
                });
    }

    private void requestGroupsFromPhone() {
        Task<List<Node>> nodesTask = Wearable.getNodeClient(this).getConnectedNodes();
        nodesTask.addOnSuccessListener(nodes -> {
            for (Node node : nodes) {
                Wearable.getMessageClient(this).sendMessage(node.getId(), PATH_REQUEST_GROUPS, new byte[0]);
            }
        });
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(PATH_GROUPS_LIST)) {
            String groupsString = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            List<String> receivedGroups = Arrays.asList(groupsString.split(","));

            runOnUiThread(() -> {
                groupList.clear();
                groupList.addAll(receivedGroups);

                // Set the TextView to the first group in the list by default
                if (!groupList.isEmpty()) {
                    tvSelectGroup.setText(groupList.get(0) + " ▼");
                }
            });
        }
    }

    private void updateUI(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                tvConnectionStatus.setText("Connected to Phone");
                tvConnectionStatus.setTextColor(Color.GREEN);
            } else {
                tvConnectionStatus.setText("Phone Disconnected");
                tvConnectionStatus.setTextColor(Color.RED);
            }
        });
    }
}