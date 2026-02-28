package com.example.expensify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    private String groupId, myPhone, myRealName;

    private String selectedPayerPhone, selectedPayerName;
    private HashMap<String, String> finalSplitMap = new HashMap<>();

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
        myRealName = sp.getString("loggedInName", "Admin");

        selectedPayerPhone = myPhone;
        selectedPayerName = myRealName;
        tvPaidByLabel.setText("You");

        // --- THE "WTF" FIX: ADDING THE CLICK LISTENERS TO THE ROWS ---
        View paidByRow = view.findViewById(R.id.paidByRow);
        View splitRow = view.findViewById(R.id.splitRow);

        if (paidByRow != null) paidByRow.setOnClickListener(v -> showPayerDialog());
        if (splitRow != null) splitRow.setOnClickListener(v -> showGPaySplitDialog());

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnAddExpense).setOnClickListener(v -> saveExpenseToFirebase());

        return view;
    }

    private void showPayerDialog() {
        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot membersSnapshot) {
                if (!membersSnapshot.exists()) return;
                List<String> displayNames = new ArrayList<>();
                List<String> realNames = new ArrayList<>();
                List<String> phones = new ArrayList<>();
                long totalMembers = membersSnapshot.getChildrenCount();
                final int[] fetchedCount = {0};

                for (DataSnapshot memberDs : membersSnapshot.getChildren()) {
                    String phoneKey = memberDs.getKey();
                    usersRef.child(phoneKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String userName = userSnapshot.child("username").getValue(String.class);
                            if (userName == null) userName = phoneKey;
                            phones.add(phoneKey);
                            realNames.add(userName);
                            displayNames.add(phoneKey.equals(myPhone) ? "You (" + userName + ")" : userName);
                            fetchedCount[0]++;
                            if (fetchedCount[0] == totalMembers) launchPayerUI(displayNames, realNames, phones);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { fetchedCount[0]++; }
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void launchPayerUI(List<String> display, List<String> real, List<String> phones) {
        new AlertDialog.Builder(requireContext()).setTitle("Who paid?").setItems(display.toArray(new String[0]), (dialog, which) -> {
            selectedPayerPhone = phones.get(which);
            selectedPayerName = real.get(which);
            tvPaidByLabel.setText(display.get(which));
        }).show();
    }

    private void showGPaySplitDialog() {
        String totalStr = etAmount.getText().toString().trim();
        if (totalStr.isEmpty()) { Toast.makeText(getContext(), "Enter amount first", Toast.LENGTH_SHORT).show(); return; }
        double totalAmount = Double.parseDouble(totalStr);

        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MemberSplit> list = new ArrayList<>();
                long total = snapshot.getChildrenCount();
                final int[] processed = {0};

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String phoneKey = ds.getKey();
                    if (phoneKey != null && phoneKey.equals(myPhone)) { // EXCLUDE YOU
                        processed[0]++;
                        if (processed[0] == total) showActualSplitBottomSheet(list, totalAmount);
                        continue;
                    }
                    usersRef.child(phoneKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String userName = userSnapshot.child("username").getValue(String.class);
                            list.add(new MemberSplit(userName != null ? userName : phoneKey, phoneKey, 0.0));
                            processed[0]++;
                            if (processed[0] == total) showActualSplitBottomSheet(list, totalAmount);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { processed[0]++; }
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showActualSplitBottomSheet(List<MemberSplit> memberList, double totalAmount) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_split_selector, null);
        bottomSheet.setContentView(sheetView);

        RecyclerView rvMembers = sheetView.findViewById(R.id.rvSplitMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tabEqual = sheetView.findViewById(R.id.tabEqual);
        TextView tabCustom = sheetView.findViewById(R.id.tabCustom);

        // DEFAULT: Equal Split
        double share = totalAmount / memberList.size();
        for (MemberSplit m : memberList) m.amount = share;
        SplitAdapter adapter = new SplitAdapter(memberList);
        rvMembers.setAdapter(adapter);

        // --- TAB SWITCHING LOGIC ---
        tabEqual.setOnClickListener(v -> {
            tabEqual.setBackgroundResource(R.drawable.tab_selected); // Make sure this drawable exists!
            tabEqual.setTextColor(Color.WHITE);
            tabCustom.setBackgroundResource(0);
            tabCustom.setTextColor(Color.GRAY);

            double s = totalAmount / memberList.size();
            for (MemberSplit m : memberList) m.amount = s;
            adapter.notifyDataSetChanged();
        });

        tabCustom.setOnClickListener(v -> {
            tabCustom.setBackgroundResource(R.drawable.tab_selected);
            tabCustom.setTextColor(Color.WHITE);
            tabEqual.setBackgroundResource(0);
            tabEqual.setTextColor(Color.GRAY);
            Toast.makeText(getContext(), "Manual mode active", Toast.LENGTH_SHORT).show();
        });

        sheetView.findViewById(R.id.btnDoneSplit).setOnClickListener(v -> {
            finalSplitMap.clear();
            for (MemberSplit m : memberList) finalSplitMap.put(m.phone, String.format("%.2f", m.amount));
            tvSplitLabel.setText("Split between " + memberList.size() + " others");
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    private void saveExpenseToFirebase() {
        String amountStr = etAmount.getText().toString().trim();
        String titleStr = etWhatFor.getText().toString().trim();
        if (amountStr.isEmpty() || titleStr.isEmpty()) { Toast.makeText(getContext(), "Required fields empty", Toast.LENGTH_SHORT).show(); return; }

        if (finalSplitMap.isEmpty()) {
            // Auto-split with others if user didn't open dialog
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("members");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    long divisor = (count > 1) ? count - 1 : 1;
                    double s = Double.parseDouble(amountStr) / divisor;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (count > 1 && ds.getKey().equals(myPhone)) continue;
                        finalSplitMap.put(ds.getKey(), String.format("%.2f", s));
                    }
                    performFirebaseSave(amountStr, titleStr);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else { performFirebaseSave(amountStr, titleStr); }
    }

    private void performFirebaseSave(String amountStr, String titleStr) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(groupId);
        String expenseId = ref.push().getKey();
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        Expense expense = new Expense(expenseId, groupId, myPhone, amountStr, titleStr, etNotes.getText().toString(), date, selectedPayerPhone, selectedPayerName, finalSplitMap);
        if (expenseId != null) ref.child(expenseId).setValue(expense).addOnSuccessListener(aVoid -> getParentFragmentManager().popBackStack());
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