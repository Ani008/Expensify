package com.example.expensify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText regUsername, regPhoneNo, regUpiId;
    Button regBtn;
    FirebaseDatabase rootNode;
    DatabaseReference reference;

    // Use a consistent name for your SharedPreferences file
    public static final String SHARED_PREFS = "ExpensifyPrefs";
    // Key names for consistency across fragments
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_PHONE = "loggedInPhone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CHECK LOGIN STATUS FIRST
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_sign_up);

        // Initialize Views
        regUsername = findViewById(R.id.signup_username);
        regPhoneNo = findViewById(R.id.signup_phone);
        regUpiId = findViewById(R.id.signup_upi);
        regBtn = findViewById(R.id.signup_button);

        regBtn.setOnClickListener(view -> {
            String username = regUsername.getText().toString().trim();
            String phoneNo = regPhoneNo.getText().toString().trim();
            String upiId = regUpiId.getText().toString().trim();

            if (username.isEmpty() || phoneNo.isEmpty() || upiId.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "All fields are compulsory", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent double-click
            regBtn.setEnabled(false);

            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("Users");

            // Create User Object
            HashMap<String, String> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("phoneNo", phoneNo);
            userData.put("upiId", upiId);

            // Save to Firebase under the Phone Number key
            reference.child(phoneNo).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // 2. SAVE LOGIN STATE & PHONE NUMBER
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                        editor.putString(KEY_USER_PHONE, phoneNo); // Crucial for filtering groups later
                        editor.apply();

                        Toast.makeText(SignUpActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();

                        // 3. NAVIGATE TO MAIN
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        regBtn.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}