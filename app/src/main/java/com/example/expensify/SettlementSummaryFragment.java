package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettlementSummaryFragment extends Fragment {

    private String expenseId, expenseTitle, expenseAmount, groupId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settlement_summary, container, false);

        // 1. HIDDING NAVIGATION (Crucial Step)
        hideNavigation();

        // 2. Initializing Views and Catching Data
        if (getArguments() != null) {
            expenseId = getArguments().getString("expenseId");
            expenseTitle = getArguments().getString("expenseTitle");
            expenseAmount = getArguments().getString("expenseAmount");
            groupId = getArguments().getString("groupId");

            TextView tvTitle = view.findViewById(R.id.tvTitle);
            if (tvTitle != null) tvTitle.setText(expenseTitle);
        }

        // 3. Back Button Logic (So you can return to Timeline)
        View btnBack = view.findViewById(R.id.btnCancel); // Using ID from your UI style
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        return view;
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            // We use getActivity().findViewById to find views that live in activity_main.xml
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            // Removing the bottom margin of the fragment container so it fills the screen
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
        // NOTE: We don't restore navigation here because the TimelineFragment
        // also wants it hidden. If we restored it here, you'd see the bar
        // pop up for a second while switching back.
    }
}