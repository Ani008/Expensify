package com.example.expensify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import java.util.ArrayList;
import java.util.List;

public class SettlementSummaryFragment extends Fragment {

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;
    private SettlementAdapter adapter;
    private List<Settlement> settlementList = new ArrayList<>();
    private String myPhone, groupId, expenseId;
    private TextView tvTitle, tvExpenseAmount, tvSubtitle;

    // --- DATA MODEL (Internal) ---
    public static class Settlement {
        public String fromPhone, fromName, toName, toPhone, amount;
        public Settlement(String fromPhone, String fromName, String toName, String toPhone, String amount) {
            this.fromPhone = fromPhone; this.fromName = fromName;
            this.toName = toName; this.toPhone = toPhone; this.amount = amount;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settlement_summary, container, false);

        if (getActivity() != null) {
            bottomNav = getActivity().findViewById(R.id.bottom_navigation); // Ensure this ID matches your Activity XML
        }

        tvTitle = view.findViewById(R.id.tvTitle);
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        recyclerView = view.findViewById(R.id.rvSettlements);

        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            expenseId = getArguments().getString("expenseId");
        }

        SharedPreferences sp = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        myPhone = sp.getString("loggedInPhone", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SettlementAdapter(settlementList);
        recyclerView.setAdapter(adapter);

        fetchExpenseAndNames();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Try to find the nav bar again just in case onCreateView missed it
        if (getActivity() != null) {
            bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            // If your ID in activity_main.xml is different, change it above!

            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
                bottomNav.animate().alpha(0f).setDuration(200); // Fades it out for a smoother look
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.animate().alpha(1f).setDuration(200); // Fades it back in
        }
    }

    private void fetchExpenseAndNames() {
        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("expenses").child(groupId).child(expenseId);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        expenseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String title = snapshot.child("title").getValue(String.class);
                String totalAmt = snapshot.child("amount").getValue(String.class);
                String paidByPhone = snapshot.child("paidByPhone").getValue(String.class);
                String paidByName = snapshot.child("paidByName").getValue(String.class);

                tvTitle.setText(title);
                tvExpenseAmount.setText("₹" + totalAmt);

                settlementList.clear();
                DataSnapshot splits = snapshot.child("splitDetails");

                for (DataSnapshot ds : splits.getChildren()) {
                    String debtorPhone = ds.getKey();
                    String amountOwed = ds.getValue(String.class);

                    if (debtorPhone != null && !debtorPhone.equals(paidByPhone)) {
                        // Create settlement with phone as placeholder name
                        Settlement s = new Settlement(debtorPhone, debtorPhone, paidByName, paidByPhone, amountOwed);
                        settlementList.add(s);

                        // Fetch real username from users collection
                        usersRef.child(debtorPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String uName = userSnapshot.child("username").getValue(String.class);
                                if (uName != null) {
                                    s.fromName = debtorPhone.equals(myPhone) ? "You" : uName;
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
                tvSubtitle.setText(settlementList.size() + " payments found.");
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- ADAPTER (Internal) ---
    private class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.ViewHolder> {
        List<Settlement> list;
        SettlementAdapter(List<Settlement> list) { this.list = list; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_settlement_row, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Settlement s = list.get(pos);
            h.tvFrom.setText(s.fromName);
            h.tvTo.setText(s.toName);
            h.tvAmount.setText("₹" + s.amount);

            h.btnPay.setText("Pay Now");
            h.btnPay.setOnClickListener(v -> {
                String upiId = s.toPhone + "@paytm"; // Standard placeholder
                openUPI(upiId, s.toName, "Expensify: " + tvTitle.getText().toString(), s.amount);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFrom, tvTo, tvAmount; Button btnPay;
            ViewHolder(View v) {
                super(v);
                tvFrom = v.findViewById(R.id.tvFromName);
                tvTo = v.findViewById(R.id.tvToName);
                tvAmount = v.findViewById(R.id.tvAmount);
                btnPay = v.findViewById(R.id.btnPayNowRow);
            }
        }
    }

    private void openUPI(String upiId, String name, String note, String amount) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Intent chooser = Intent.createChooser(intent, "Pay with...");

        // Use 123 as a request code so we can identify this specific return
        startActivityForResult(chooser, 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are returning from the UPI intent (code 123)
        if (requestCode == 123) {
            // Even if they cancelled or it failed, we show the Reward Fragment as requested
            navigateToRewards();
        }
    }

    private void navigateToRewards() {
        // Replace current fragment with Reward Fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Reward()) // Ensure class name is correct
                .addToBackStack(null)
                .commit();

        Toast.makeText(getContext(), "Transaction processed!", Toast.LENGTH_SHORT).show();
    }
}