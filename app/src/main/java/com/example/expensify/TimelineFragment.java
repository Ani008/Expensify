package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Added for testing the group code

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TimelineFragment extends Fragment {

    private String groupId;
    private TextView tvGroupTitle, membersCount, expensesCount, tvEmptyState;
    private LinearLayout timelineContainer; // To add items dynamically
    private View emptyStateView; // Add a TextView or View in XML for "No Expenses"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // Initialize Views
        tvGroupTitle = view.findViewById(R.id.tvGroupTitle);
        membersCount = view.findViewById(R.id.membersCount);
        expensesCount = view.findViewById(R.id.expensesCount);
        timelineContainer = view.findViewById(R.id.timelineContainer);
        tvEmptyState = view.findViewById(R.id.tvEmptyState); // Add this ID to your XML LinearLayout

        // Inside onCreateView...
        view.findViewById(R.id.btnAddExpense).setOnClickListener(v -> {
            // 1. Create the fragment instance
            AddExpenseFragment addExpenseFragment = new AddExpenseFragment();

            // 2. Pass the groupId so the new expense knows which group it belongs to
            Bundle bundle = new Bundle();
            bundle.putString("groupId", groupId);
            addExpenseFragment.setArguments(bundle);

            // 3. Open the fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addExpenseFragment)
                        .addToBackStack(null) // Allows user to go back to Timeline
                        .commit();
            }
        });

        // Get ID from HomeFragment
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            String groupName = getArguments().getString("groupName");
            tvGroupTitle.setText(groupName);
        }

        fetchGroupDetails();
        fetchExpenses();

        return view;
    }

    private void fetchGroupDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long count = snapshot.child("members").getChildrenCount();
                    membersCount.setText(count + " Members");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchExpenses() {
        DatabaseReference expRef = FirebaseDatabase.getInstance().getReference("expenses").child(groupId);
        expRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timelineContainer.removeAllViews(); // Clear previous items to avoid duplicates

                double totalBalance = 0.0; // Variable to calculate the sum

                if (!snapshot.exists()) {
                    expensesCount.setText("0 Expenses");
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    expensesCount.setText(snapshot.getChildrenCount() + " Expenses");

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // 1. Get the data
                        AddExpenseFragment.Expense expense = ds.getValue(AddExpenseFragment.Expense.class);

                        if (expense != null) {
                            // 2. Inflate the card layout directly here
                            View card = getLayoutInflater().inflate(R.layout.item_timeline_expense, timelineContainer, false);

                            // 3. Find views within the inflated card
                            TextView tvDate = card.findViewById(R.id.tvDate);
                            TextView tvTitle = card.findViewById(R.id.tvTitle);
                            TextView tvAmount = card.findViewById(R.id.tvAmount);
                            TextView tvPaidBy = card.findViewById(R.id.tvSubtitle);

                            // 4. Set the text from the expense object
                            tvDate.setText(expense.date != null ? expense.date.toUpperCase() : "TODAY");
                            tvTitle.setText(expense.title);
                            tvAmount.setText("₹" + expense.amount);
                            tvPaidBy.setText("Paid by " + (expense.addedBy != null ? expense.addedBy : "Member"));

                            // 5. Accumulate the total (optional but helpful)
                            try {
                                totalBalance += Double.parseDouble(expense.amount);
                            } catch (Exception e) { /* handle non-numeric amounts */ }

                            // 6. Add the card to the container
                            timelineContainer.addView(card);
                        }
                    }
                }

                // Update the "TOTAL BALANCE" header at the top
                TextView tvTotalBalanceStatus = getView().findViewById(R.id.tvTotalBalanceStatus);
                if(tvTotalBalanceStatus != null) {
                    tvTotalBalanceStatus.setText("₹" + String.format("%.2f", totalBalance));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}