package com.example.expensify;

import android.os.Bundle;
import android.util.TypedValue;
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

        // 1. Hide the Bottom Navigation Bar and FAB and remove container margin
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            // Force the container to take the full screen by removing the bottom margin
            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = 0;
                fragmentContainer.requestLayout();
            }
        }

        // 2. Find the "Add Expense" button in the inflated view
        View btnAddExpense = view.findViewById(R.id.btnAddExpense);

        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                // 3. Navigate to AddExpenseFragment
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new AddExpenseFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 4. Restore the margin when leaving the fragment so the Home screen isn't cut off
        if (getActivity() != null) {
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();

                // Convert 70dp back to pixels to match activity_main.xml
                int marginInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());

                params.bottomMargin = marginInPx;
                fragmentContainer.requestLayout();
            }
        }
    }
}