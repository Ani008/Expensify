package com.example.expensify;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        view.setBackgroundColor(Color.WHITE);

        // --- NEW: Setup "Browse Templates >" Click Listener ---
        TextView tvOpenTemplates = view.findViewById(R.id.tvOpenTemplates);
        if (tvOpenTemplates != null) {
            tvOpenTemplates.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Navigate to the fragment_templates page
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new fragment_templates())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // --- SETUP CARD 1 (Europe Trip - RED) ---
        View cardEurope = view.findViewById(R.id.cardEurope);
        ((TextView) cardEurope.findViewById(R.id.tvTitle)).setText("Europe Trip 2024");
        ((TextView) cardEurope.findViewById(R.id.tvStatus)).setText("YOU OWE ₹450");
        // Background is red by default from our previous item layout

        // --- SETUP CARD 2 (Flatmates - GREEN) ---
        View cardFlatmates = view.findViewById(R.id.cardFlatmates);
        TextView tvStatusFlat = cardFlatmates.findViewById(R.id.tvStatus);
        ((TextView) cardFlatmates.findViewById(R.id.tvTitle)).setText("Flatmates");
        ((TextView) cardFlatmates.findViewById(R.id.tvMembers)).setText("3 members");
        ((TextView) cardFlatmates.findViewById(R.id.tvLabel)).setText("SETTLEMENT");
        ((TextView) cardFlatmates.findViewById(R.id.tvPercent)).setText("100%");
        ((ProgressBar) cardFlatmates.findViewById(R.id.progressBar)).setProgress(100);

        tvStatusFlat.setText("ALL SETTLED");
        tvStatusFlat.setTextColor(Color.parseColor("#2E7D32")); // Dark Green text
        tvStatusFlat.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_tag_bg_green));

        // --- SETUP CARD 3 (Office Lunch - BLUE) ---
        View cardOffice = view.findViewById(R.id.cardOffice);
        TextView tvStatusOffice = cardOffice.findViewById(R.id.tvStatus);
        ((TextView) cardOffice.findViewById(R.id.tvTitle)).setText("Office Lunch");
        ((TextView) cardOffice.findViewById(R.id.tvMembers)).setText("12 members");
        ((TextView) cardOffice.findViewById(R.id.tvLabel)).setText("COLLECTION");
        ((TextView) cardOffice.findViewById(R.id.tvPercent)).setText("75%");
        ((ProgressBar) cardOffice.findViewById(R.id.progressBar)).setProgress(75);

        tvStatusOffice.setText("YOU ARE OWED ₹1,200");
        tvStatusOffice.setTextColor(Color.parseColor("#1565C0")); // Dark Blue text
        tvStatusOffice.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_tag_bg_blue));

        return view;
    }
}