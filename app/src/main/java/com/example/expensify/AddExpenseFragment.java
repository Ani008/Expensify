package com.example.expensify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private EditText etAmount, etWhatFor, etNotes;
    private TextView tvPaidByLabel, tvSplitLabel;
    private String groupId, myPhone, myName;

    private String selectedPayerPhone, selectedPayerName;
    private HashMap<String, String> finalSplitMap = new HashMap<>();

    // --- INNER DATA MODEL ---
    public static class Expense {
        public String expenseId, groupId, addedBy, amount, title, description, date;
        public String paidByPhone, paidByName;
        public HashMap<String, String> splitDetails;

        public Expense() { }

        public Expense(String id, String gId, String addBy, String amt, String t, String d, String dt, String pPh, String pNm, HashMap<String, String> split) {
            this.expenseId = id; this.groupId = gId; this.addedBy = addBy; this.amount = amt;
            this.title = t; this.description = d; this.date = dt;
            this.paidByPhone = pPh; this.paidByName = pNm; this.splitDetails = split;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        etAmount = view.findViewById(R.id.etAmount);
        etWhatFor = view.findViewById(R.id.etWhatFor);
        etNotes = view.findViewById(R.id.etNotes);
        tvPaidByLabel = view.findViewById(R.id.tvPaidByLabel);
        tvSplitLabel = view.findViewById(R.id.tvSplitLabel);

        if (getArguments() != null) groupId = getArguments().getString("groupId");
        SharedPreferences sp = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
        myPhone = sp.getString("loggedInPhone", "");
        myName = sp.getString("username", "You");

        selectedPayerPhone = myPhone;
        selectedPayerName = myName;

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnAddExpense).setOnClickListener(v -> saveExpenseToFirebase());

        // Make sure these IDs match your fragment_add_expense.xml exactly!
        view.findViewById(R.id.selectionContainer).findViewById(R.id.paidByRow).setOnClickListener(v -> showPayerDialog());
        view.findViewById(R.id.selectionContainer).findViewById(R.id.splitRow).setOnClickListener(v -> showGPaySplitDialog());

        return view;
    }

    private void showPayerDialog() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> names = new ArrayList<>();
                ArrayList<String> phones = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    names.add(ds.child("name").getValue(String.class));
                    phones.add(ds.getKey());
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("Who paid?")
                        .setItems(names.toArray(new String[0]), (dialog, which) -> {
                            selectedPayerName = names.get(which);
                            selectedPayerPhone = phones.get(which);
                            tvPaidByLabel.setText(selectedPayerName);
                        }).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showGPaySplitDialog() {
        String totalStr = etAmount.getText().toString().trim();
        if (totalStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter amount first", Toast.LENGTH_SHORT).show();
            return;
        }
        double totalAmount = Double.parseDouble(totalStr);

        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_split_selector, null);
        bottomSheet.setContentView(sheetView);

        RecyclerView rvMembers = sheetView.findViewById(R.id.rvSplitMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tabEqual = sheetView.findViewById(R.id.tabEqual);
        TextView tabCustom = sheetView.findViewById(R.id.tabCustom);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MemberSplit> memberList = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    memberList.add(new MemberSplit(ds.child("name").getValue(String.class), ds.getKey(), 0.0));
                }

                // ... inside onDataChange, after rvMembers.setLayoutManager ...

// Find the tab views from the sheetView
                TextView tabEqual = sheetView.findViewById(R.id.tabEqual);
                TextView tabCustom = sheetView.findViewById(R.id.tabCustom);

// Initial Equal Split Calculation
                double equalShare = totalAmount / memberList.size();
                for (MemberSplit m : memberList) m.amount = equalShare;

                SplitAdapter adapter = new SplitAdapter(memberList);
                rvMembers.setAdapter(adapter);

// --- TAB SWITCHING LOGIC ---

                tabEqual.setOnClickListener(v -> {
                    // 1. Change Visuals: Equal tab becomes Black/White, Custom tab becomes Clear/Gray
                    tabEqual.setBackgroundResource(R.drawable.tab_selected);
                    tabEqual.setTextColor(android.graphics.Color.WHITE);
                    tabCustom.setBackgroundResource(0);
                    tabCustom.setTextColor(android.graphics.Color.GRAY);

                    // 2. Force Math: Reset everyone to equal share
                    double share = totalAmount / memberList.size();
                    for (MemberSplit m : memberList) {
                        m.amount = share;
                    }

                    // 3. Refresh List: Update the EditTexts on screen
                    adapter.notifyDataSetChanged();
                });

                tabCustom.setOnClickListener(v -> {
                    // 1. Change Visuals: Custom tab becomes Black/White, Equal tab becomes Clear/Gray
                    tabCustom.setBackgroundResource(R.drawable.tab_selected);
                    tabCustom.setTextColor(android.graphics.Color.WHITE);
                    tabEqual.setBackgroundResource(0);
                    tabEqual.setTextColor(android.graphics.Color.GRAY);

                    // 2. Just a Toast hint: Let the user know they can now type
                    Toast.makeText(getContext(), "Manual mode: Tap values to edit", Toast.LENGTH_SHORT).show();
                });

// --- DONE BUTTON LOGIC ---
                sheetView.findViewById(R.id.btnDoneSplit).setOnClickListener(v -> {
                    finalSplitMap.clear();
                    for (MemberSplit m : memberList) {
                        finalSplitMap.put(m.phone, String.format("%.2f", m.amount));
                    }
                    tvSplitLabel.setText("Split between " + memberList.size() + " people");
                    bottomSheet.dismiss();
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        bottomSheet.show();
    }

    private void saveExpenseToFirebase() {
        String amountStr = etAmount.getText().toString().trim();
        String titleStr = etWhatFor.getText().toString().trim();

        if (amountStr.isEmpty() || titleStr.isEmpty()) {
            Toast.makeText(getContext(), "Amount and Title required", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(groupId);
        String expenseId = ref.push().getKey();
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        Expense expense = new Expense(expenseId, groupId, myPhone, amountStr, titleStr, etNotes.getText().toString(), date, selectedPayerPhone, selectedPayerName, finalSplitMap);

        if (expenseId != null) {
            ref.child(expenseId).setValue(expense).addOnSuccessListener(aVoid -> getParentFragmentManager().popBackStack());
        }
    }

    private static class MemberSplit {
        String name, phone; double amount;
        MemberSplit(String n, String p, double a) { this.name = n; this.phone = p; this.amount = a; }
    }

    private class SplitAdapter extends RecyclerView.Adapter<SplitAdapter.ViewHolder> {
        List<MemberSplit> list;
        SplitAdapter(List<MemberSplit> list) { this.list = list; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_split_member, p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            MemberSplit m = list.get(pos);
            h.name.setText(m.name);
            h.amt.setText(String.format("%.2f", m.amount));
            h.amt.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int b, int c, int a) {}
                public void onTextChanged(CharSequence s, int b, int be, int c) {}
                public void afterTextChanged(Editable s) {
                    try { m.amount = Double.parseDouble(s.toString()); } catch (Exception e) { m.amount = 0; }
                }
            });
        }
        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name; EditText amt;
            ViewHolder(View v) { super(v); name = v.findViewById(R.id.tvMemberName); amt = v.findViewById(R.id.etMemberAmount); }
        }
    }
}