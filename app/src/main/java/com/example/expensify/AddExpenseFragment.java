package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private EditText etAmount, etWhatFor, etNotes;
    private String groupId, myPhone;

    // --- 1. THE DATA MODEL (Included right inside the Fragment) ---
    public static class Expense {
        public String expenseId, groupId, addedBy, amount, title, description, date;

        public Expense() { } // Required for Firebase

        public Expense(String expenseId, String groupId, String addedBy, String amount, String title, String description, String date) {
            this.expenseId = expenseId;
            this.groupId = groupId;
            this.addedBy = addedBy;
            this.amount = amount;
            this.title = title;
            this.description = description;
            this.date = date;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // Initialize UI Elements
        // Ensure these IDs match exactly with your fragment_add_expense.xml
        etAmount = view.findViewById(R.id.etAmount);
        etWhatFor = view.findViewById(R.id.etWhatFor);
        etNotes = view.findViewById(R.id.etNotes);

        // Get Context Data
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
        }

        SharedPreferences sp = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        myPhone = sp.getString("loggedInPhone", "");

        // Navigation: Cancel Button
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Logic: Add to Timeline Button
        view.findViewById(R.id.btnAddExpense).setOnClickListener(v -> saveExpenseToFirebase());

        return view;
    }

    private void saveExpenseToFirebase() {
        String amountStr = etAmount.getText().toString().trim();
        String titleStr = etWhatFor.getText().toString().trim();
        String descStr = etNotes.getText().toString().trim();

        if (amountStr.isEmpty() || titleStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter amount and title", Toast.LENGTH_SHORT).show();
            return;
        }

        // Path: expenses -> [groupId] -> [uniqueExpenseId]
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(groupId);
        String expenseId = ref.push().getKey();

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        // Create the object using our inner class
        Expense newExpense = new Expense(expenseId, groupId, myPhone, amountStr, titleStr, descStr, currentDate);

        if (expenseId != null) {
            ref.child(expenseId).setValue(newExpense).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Auto-return to Timeline
                } else {
                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}