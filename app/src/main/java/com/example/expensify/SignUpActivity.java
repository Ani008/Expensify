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

    // Define a name for your SharedPreferences file
    public static final String SHARED_PREFS = "ExpensifyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CHECK LOGIN STATUS FIRST
        // Before even loading the UI, check if the user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is already logged in, navigate directly to MainActivity
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SignUpActivity so the user can't press the back button to return here
            return;   // Stop executing the rest of onCreate
        }

        // If not logged in, load the Sign-Up UI
        setContentView(R.layout.activity_sign_up);

        regUsername = findViewById(R.id.signup_username);
        regPhoneNo = findViewById(R.id.signup_phone);
        regUpiId = findViewById(R.id.signup_upi);
        regBtn = findViewById(R.id.signup_button);

        regBtn.setOnClickListener(view -> {
            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("Users");

            String username = regUsername.getText().toString().trim();
            String phoneNo = regPhoneNo.getText().toString().trim();
            String upiId = regUpiId.getText().toString().trim();

            if (username.isEmpty() || phoneNo.isEmpty() || upiId.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "All fields are compulsory", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("phoneNo", phoneNo);
            userData.put("upiId", upiId);

            reference.child(phoneNo).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SignUpActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();

                        // 2. SAVE LOGIN STATE
                        // Save to SharedPreferences that the user is now logged in
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("phoneNo", phoneNo); // Save phone number to fetch their specific data later
                        editor.apply();

                        // 3. NAVIGATE TO MAIN ACTIVITY
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Destroy the Sign-Up activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}