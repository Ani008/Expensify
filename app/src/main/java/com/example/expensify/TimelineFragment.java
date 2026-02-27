package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimelineFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // This looks for the ID we just added in the XML
        View btnAddExpense = view.findViewById(R.id.btnAddExpense);

        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // This opens the Add Expense screen
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new AddExpenseFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        return view;
    }
}