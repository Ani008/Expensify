package com.example.expensify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText logUsername, logPhoneNo, logUpiId;
    Button logBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logUsername = findViewById(R.id.login_username);
        logPhoneNo = findViewById(R.id.login_phone);
        logUpiId = findViewById(R.id.login_upi);
        logBtn = findViewById(R.id.login_button);

        logBtn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String userEnteredUsername = logUsername.getText().toString().trim();
        String userEnteredPhoneNo = logPhoneNo.getText().toString().trim();
        String userEnteredUpiId = logUpiId.getText().toString().trim();

        if (userEnteredUsername.isEmpty() || userEnteredPhoneNo.isEmpty() || userEnteredUpiId.isEmpty()) {
            Toast.makeText(this, "All fields are required to log in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUser = reference.orderByChild("phoneNo").equalTo(userEnteredPhoneNo);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Phone number found, verify other details
                    String dbUsername = dataSnapshot.child(userEnteredPhoneNo).child("username").getValue(String.class);
                    String dbUpiId = dataSnapshot.child(userEnteredPhoneNo).child("upiId").getValue(String.class);

                    if (dbUsername.equals(userEnteredUsername) && dbUpiId.equals(userEnteredUpiId)) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        // Intent to navigate to your main app Dashboard
                    } else {
                        Toast.makeText(LoginActivity.this, "Incorrect Username or UPI ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User does not exist. Please sign up.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}