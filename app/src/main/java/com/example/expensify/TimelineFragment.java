package com.example.expensify;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        // --- 1. Navigation Management (Full Screen) ---
        hideNavigation();

        // --- 2. Retrieve the Group Code passed from MainActivity ---
        if (getArguments() != null) {
            currentGroupCode = getArguments().getString("GROUP_CODE");

            if (currentGroupCode != null) {
                // Testing: Show a toast to prove the code successfully made it here!
                Toast.makeText(requireContext(), "Viewing: " + currentGroupCode, Toast.LENGTH_SHORT).show();
            }
        }

        // --- 3. Settle Up Button Logic ---
        View btnSettleUp = view.findViewById(R.id.btnSettleUp);
        if (btnSettleUp != null) {
            btnSettleUp.setOnClickListener(v -> {
                // Open the Settlement Summary screen
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new SettlementSummaryFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // --- 4. Add Expense Button Logic ---
        View btnAddExpense = view.findViewById(R.id.btnAddExpense);
        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                Fragment addExpenseFrag = new AddExpenseFragment();

                // Pass the group code forward to the next screen
                if (currentGroupCode != null) {
                    Bundle args = new Bundle();
                    args.putString("GROUP_CODE", currentGroupCode);
                    addExpenseFrag.setArguments(args);
                }

                // Open the Add Expense screen
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, addExpenseFrag)
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

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
        // Restore bottom margin for Home screen when leaving
        if (getActivity() != null) {
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