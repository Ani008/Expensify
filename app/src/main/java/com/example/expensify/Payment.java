package com.example.expensify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class Payment extends Fragment
        implements upiAdapter.PayClickListener {

    RecyclerView recyclerView;

    ArrayList<String> usernames = new ArrayList<>();
    ArrayList<String> upiIds = new ArrayList<>();

    upiAdapter adapter;
    DatabaseReference usersRef;

    private final int UPI_PAYMENT = 101;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_payment,
                container,
                false);

        recyclerView = view.findViewById(R.id.upiRecyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        // ✅ PASS LISTENER (VERY IMPORTANT)
        adapter = new upiAdapter(
                getContext(),
                usernames,
                upiIds,
                this
        );

        recyclerView.setAdapter(adapter);

        fetchUsers();

        return view;
    }

    // ✅ FETCH USERS FROM FIREBASE
    private void fetchUsers() {

        usersRef = FirebaseDatabase
                .getInstance()
                .getReference("Users");

        usersRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        usernames.clear();
                        upiIds.clear();

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            String username =
                                    ds.child("username")
                                            .getValue(String.class);

                            String upi =
                                    ds.child("upiId")
                                            .getValue(String.class);

                            if (upi != null && !upi.isEmpty()) {
                                usernames.add(username);
                                upiIds.add(upi);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {}
                });
    }

    // ✅ CALLED FROM ADAPTER BUTTON CLICK
    @Override
    public void onPayClick(String upiId, String username) {
        startUpiPayment(upiId, username);
    }

    // ✅ OPEN GOOGLE PAY
    private void startUpiPayment(String upiId, String name) {
        // Generate a unique transaction ID every single time to avoid "Duplicate" errors
        String uniqueTransactionId = "EXP" + System.currentTimeMillis();

        Uri uri = new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)      // Payee VPA
                .appendQueryParameter("pn", name)       // Payee Name
                .appendQueryParameter("tr", uniqueTransactionId) // MUST be unique
                .appendQueryParameter("tn", "Expensify Payment") // Use a clean note
                .appendQueryParameter("am", "1.00")     // Keep it low for testing
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        // DONT use a chooser; let the OS show the "Pay With" dialog
        // It's more stable for real money transfers.
        try {
            startActivityForResult(intent, UPI_PAYMENT);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No UPI app found", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ RETURN FROM GPAY
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {

            if (data != null) {

                String response =
                        data.getStringExtra("response");

                if (response != null) {

                    response = response.toLowerCase();

                    if (response.contains("success")) {

                        Toast.makeText(
                                getContext(),
                                "Payment Successful ✅",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                getContext(),
                                "Payment Failed ❌",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            } else {
                Toast.makeText(
                        getContext(),
                        "Payment Cancelled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}