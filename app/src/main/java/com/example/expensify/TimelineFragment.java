package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Added for testing the group code

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimelineFragment extends Fragment {

    // Variable to hold the code for the group we are currently viewing
    private String currentGroupCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // --- 1. NEW: Retrieve the Group Code passed from MainActivity ---
        if (getArguments() != null) {
            currentGroupCode = getArguments().getString("GROUP_CODE");

            if (currentGroupCode != null) {
                // Testing: Show a toast to prove the code successfully made it here!
                Toast.makeText(requireContext(), "Viewing Timeline for: " + currentGroupCode, Toast.LENGTH_SHORT).show();

                // TODO: Later, you will use this 'currentGroupCode' to fetch expenses from your database
            }
        }

        // --- 2. Existing Add Expense Button Logic ---
        View btnAddExpense = view.findViewById(R.id.btnAddExpense);

        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                if (getActivity() != null) {

                    // We also need to pass this group code to the AddExpenseFragment!
                    Fragment addExpenseFrag = new AddExpenseFragment();

                    // Pack the code into a bundle so the next screen knows which group to add the expense to
                    if (currentGroupCode != null) {
                        Bundle args = new Bundle();
                        args.putString("GROUP_CODE", currentGroupCode);
                        addExpenseFrag.setArguments(args);
                    }

                    // Open the Add Expense screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, addExpenseFrag)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        return view;
    }
}