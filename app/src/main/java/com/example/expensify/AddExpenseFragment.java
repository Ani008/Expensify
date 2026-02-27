package com.example.expensify;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddExpenseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        // 1. Hide navigation and expand to full screen immediately
        hideNavigation();

        // Handle Cancel button
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Handle Add to Timeline button
        view.findViewById(R.id.btnAddExpense).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            // Removed the .post() call to prevent the millisecond delay/flicker.
            // Applying the margin change immediately during the creation phase.
            if (fragmentContainer != null) {
                if (fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                    params.bottomMargin = 0;
                    fragmentContainer.setLayoutParams(params);
                    // No need for requestLayout here as the fragment transition will trigger a layout pass
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 2. Restore the 70dp margin when leaving this fragment so the Home screen isn't cut off
        if (getActivity() != null) {
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();

                // Convert 70dp to pixels to match the original activity_main.xml layout
                int marginInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());

                params.bottomMargin = marginInPx;
                fragmentContainer.setLayoutParams(params);
                fragmentContainer.requestLayout();
            }
        }
    }
}