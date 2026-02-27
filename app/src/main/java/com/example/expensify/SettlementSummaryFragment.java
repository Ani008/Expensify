package com.example.expensify;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettlementSummaryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settlement_summary, container, false);

        // Hide navigation
        hideNavigation();

        // Handle Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

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
        // Restore bottom margin for Home screen
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