package com.example.wear;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends Activity {

    private EditText etAmount;
    private TextView tvSelectedMember;
    private String selectedMember = "Mayank"; // Default member

    // Group members available for the split
    private final String[] groupMembers = {"Mayank", "Om", "Soham", "Aditya", "Aniket"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAmount = findViewById(R.id.et_amount);
        tvSelectedMember = findViewById(R.id.tv_selected_member);
        Button btnPlus = findViewById(R.id.btn_plus);
        Button btnMinus = findViewById(R.id.btn_minus);
        Button btnSync = findViewById(R.id.btn_sync);

        // Handle amount changes via buttons
        btnPlus.setOnClickListener(v -> {
            int currentAmount = getCurrentAmount();
            etAmount.setText(String.valueOf(currentAmount + 50));
        });

        btnMinus.setOnClickListener(v -> {
            int currentAmount = getCurrentAmount();
            if (currentAmount > 10) {
                etAmount.setText(String.valueOf(currentAmount - 50));
            } else {
                etAmount.setText("0");
            }
        });

        // Trigger our custom premium popup when clicked
        tvSelectedMember.setOnClickListener(v -> showPremiumMemberPopup());

        // Send data when sync is clicked
        btnSync.setOnClickListener(v -> {
            int finalAmount = getCurrentAmount();
            sendExpenseToPhone(finalAmount, selectedMember);
        });
    }

    // The method that builds and shows the blurred popup
    private void showPremiumMemberPopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Make the dialog background transparent so the blur shows through
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Window window = dialog.getWindow();
        if (window != null) {
            // Darken the background slightly
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.75f);

            // Apply the Blur effect (Only works on modern Wear OS versions)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                window.getAttributes().setBlurBehindRadius(40); // Higher number = more blur
            }
        }

        // Create the list of names using your custom spinner_item layout
        ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, groupMembers);
        listView.setAdapter(adapter);

        // When a name is tapped, update the UI and close the popup
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedMember = groupMembers[position];
            tvSelectedMember.setText(selectedMember + " ▼");
            dialog.dismiss();
        });

        // Push the list items toward the center of the watch screen
        listView.setPadding(30, 40, 30, 40);
        listView.setDivider(null); // Removes ugly default lines
        dialog.setContentView(listView);

        dialog.show();
    }

    private int getCurrentAmount() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) return 0;
        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void sendExpenseToPhone(int amountToSend, String member) {
        String payloadString = amountToSend + ":" + member;
        byte[] payload = payloadString.getBytes(StandardCharsets.UTF_8);

        Task<List<Node>> nodeListTask = Wearable.getNodeClient(this).getConnectedNodes();

        nodeListTask.addOnSuccessListener(nodes -> {
            if (nodes.isEmpty()) {
                Toast.makeText(this, "No phone connected", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Node node : nodes) {
                Task<Integer> sendMessageTask = Wearable.getMessageClient(this)
                        .sendMessage(node.getId(), "/add_group_expense", payload);

                sendMessageTask.addOnSuccessListener(id ->
                        Toast.makeText(this, "Sent to phone!", Toast.LENGTH_SHORT).show()
                ).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}