package com.example.expensify;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class WalletFragment extends Fragment {

    public WalletFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        // --- EXISTING CODE: Cards and Schedules ---
        MaterialCardView cardFlatmates = view.findViewById(R.id.cardFlatmatesPay);
        MaterialCardView cardSummerTrip = view.findViewById(R.id.cardSummerTripPay);
        MaterialCardView cardOfficeLunches = view.findViewById(R.id.cardOfficeLunchesPay);

        TextView tvFlatmatesSchedule = view.findViewById(R.id.tvFlatmatesSchedule);
        TextView tvSummerTripSchedule = view.findViewById(R.id.tvSummerTripSchedule);
        TextView tvOfficeLunchesSchedule = view.findViewById(R.id.tvOfficeLunchesSchedule);

        if (cardFlatmates != null && tvFlatmatesSchedule != null) {
            cardFlatmates.setOnClickListener(v -> showDatePicker("Flatmates", tvFlatmatesSchedule));
        }
        if (cardSummerTrip != null && tvSummerTripSchedule != null) {
            cardSummerTrip.setOnClickListener(v -> showDatePicker("Summer Trip", tvSummerTripSchedule));
        }
        if (cardOfficeLunches != null && tvOfficeLunchesSchedule != null) {
            cardOfficeLunches.setOnClickListener(v -> showDatePicker("Office Lunches", tvOfficeLunchesSchedule));
        }

        // --- NEW CODE: Add Funds Button Logic ---
        MaterialButton btnAddFunds = view.findViewById(R.id.btnAddFunds);
        if (btnAddFunds != null) {
            btnAddFunds.setOnClickListener(v -> {
                // Launch UPI Payment Apps (Google Pay, PhonePe, etc.)
                openUpiPayment("500.00"); // Example amount
            });
        }

        return view;
    }

    /**
     * Method to open UPI apps like Google Pay
     */
    private void openUpiPayment(String amount) {
        // Replace with your actual merchant details
        String upiId = "adisuryawanshi13@oksbi";
        String name = "Expensify User";
        String note = "Adding funds to Wallet";

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)       // Payee VPA
                .appendQueryParameter("pn", name)        // Payee Name
                .appendQueryParameter("tn", note)        // Transaction Note
                .appendQueryParameter("am", amount)      // Amount
                .appendQueryParameter("cu", "INR")       // Currency
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // Show app chooser (Google Pay, PhonePe, PayTM, etc.)
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        if (null != chooser.resolveActivity(requireActivity().getPackageManager())) {
            startActivity(chooser);
        } else {
            Toast.makeText(getContext(), "No UPI app found, please install Google Pay", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * EXISTING: Helper method for Calendar
     */
    private void showDatePicker(String groupName, TextView tvScheduleToUpdate) {
        if (getContext() == null) return;
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String chosenDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tvScheduleToUpdate.setText("Auto-pays on: " + chosenDate);
                    tvScheduleToUpdate.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), groupName + " scheduled for " + chosenDate, Toast.LENGTH_SHORT).show();
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}