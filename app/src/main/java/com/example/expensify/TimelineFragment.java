package com.example.expensify;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView tvGroupTitle, membersCount, expensesCount, tvEmptyState, tvTotalBalanceStatus;
    private LinearLayout timelineContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // 1. Initialize Views
        tvGroupTitle = view.findViewById(R.id.tvGroupTitle);
        membersCount = view.findViewById(R.id.membersCount);
        expensesCount = view.findViewById(R.id.expensesCount);
        timelineContainer = view.findViewById(R.id.timelineContainer);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvTotalBalanceStatus = view.findViewById(R.id.tvTotalBalanceStatus);

        // 2. Hide Main Activity Navigation (Full Screen Mode)
        hideNavigation();

        // 3. Get Data from Arguments (passed from HomeFragment)
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            String groupName = getArguments().getString("groupName");
            tvGroupTitle.setText(groupName);
        }

        // 4. Add Expense Button Logic
        View btnAddExpense = view.findViewById(R.id.btnAddExpense);
        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                AddExpenseFragment addExpenseFrag = new AddExpenseFragment();
                Bundle bundle = new Bundle();
                bundle.putString("groupId", groupId); // Pass the ID forward
                addExpenseFrag.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, addExpenseFrag)
                        .addToBackStack(null)
                        .commit();
            });
        }

        // 5. Settle Up Button Logic
        View btnSettleUp = view.findViewById(R.id.btnSettleUp);
        if (btnSettleUp != null) {
            btnSettleUp.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new SettlementSummaryFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // 6. Fetch Data
        if (groupId != null) {
            fetchGroupDetails();
            fetchExpenses();
        } else {
            Toast.makeText(getContext(), "Error: Group ID missing", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchGroupDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
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
                if (!isAdded()) return;

                timelineContainer.removeAllViews();
                double totalBalance = 0.0;

                if (!snapshot.exists()) {
                    expensesCount.setText("0 Expenses");
                    tvEmptyState.setVisibility(View.VISIBLE);
                    if (tvTotalBalanceStatus != null) tvTotalBalanceStatus.setText("₹0.00");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    expensesCount.setText(snapshot.getChildrenCount() + " Expenses");

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        AddExpenseFragment.Expense expense = ds.getValue(AddExpenseFragment.Expense.class);

                        if (expense != null) {
                            // 1. Inflate the card
                            View card = getLayoutInflater().inflate(R.layout.item_timeline_expense, timelineContainer, false);

                            // 2. FIND THE VIEWS (This was missing in your code)
                            TextView tvDate = card.findViewById(R.id.tvDate);
                            TextView tvTitle = card.findViewById(R.id.tvTitle);
                            TextView tvAmount = card.findViewById(R.id.tvAmount);
                            TextView tvSubtitle = card.findViewById(R.id.tvSubtitle);

                            // 3. BIND THE DATA
                            if (tvDate != null) tvDate.setText(expense.date != null ? expense.date.toUpperCase() : "TODAY");
                            if (tvTitle != null) tvTitle.setText(expense.title);
                            if (tvAmount != null) tvAmount.setText("₹" + expense.amount);
                            if (tvSubtitle != null) tvSubtitle.setText("Paid by " + (expense.addedBy != null ? expense.addedBy : "Member"));

                            // 4. THE CLICK LOGIC
                            card.setOnClickListener(v -> {
                                SettlementSummaryFragment summaryFrag = new SettlementSummaryFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("expenseId", expense.expenseId);
                                bundle.putString("expenseTitle", expense.title);
                                bundle.putString("expenseAmount", expense.amount);
                                bundle.putString("groupId", groupId);
                                summaryFrag.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                        .replace(R.id.fragment_container, summaryFrag)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            // 5. Update Total
                            try {
                                totalBalance += Double.parseDouble(expense.amount);
                            } catch (Exception e) { /* safe catch */ }

                            // 6. Add to screen
                            timelineContainer.addView(card);
                        }
                    }

                    // Update header total balance
                    if (tvTotalBalanceStatus != null) {
                        tvTotalBalanceStatus.setText("₹" + String.format("%.2f", totalBalance));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = 0;
                fragmentContainer.setLayoutParams(params);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restore Bottom Nav when leaving the fragment
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            if (navBar != null) navBar.setVisibility(View.VISIBLE);
            if (fab != null) fab.setVisibility(View.VISIBLE);

            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                int marginInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
                params.bottomMargin = marginInPx;
                fragmentContainer.setLayoutParams(params);
            }
        }
    }
}