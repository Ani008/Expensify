package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GroupSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_success, container, false);

        // 1. Hide navigation
        hideNavigation();

        // Handle Copy Link
        view.findViewById(R.id.btnCopy).setOnClickListener(v ->
                Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        );

        // Handle Close
        view.findViewById(R.id.btnClose).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().popBackStack()
        );

        // Handle Go to Timeline
        view.findViewById(R.id.btnTimeline).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TimelineFragment())
                        .addToBackStack(null)
                        .commit();
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

            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = 0;
                fragmentContainer.requestLayout();
            }
        }
    }
}