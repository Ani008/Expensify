package com.example.expensify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CreateGroupFragment extends Fragment {

    private int memberCount = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        // 1. UI Preparation
        hideNavigation();

        // 2. Initialize Views
        EditText etGroupName = view.findViewById(R.id.etGroupName);
        EditText etGroupDescription = view.findViewById(R.id.etGroupDescription);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        TextView tvMemberCount = view.findViewById(R.id.tvMemberCount);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);
        Button btnCreateGroup = view.findViewById(R.id.btnCreateGroup);

        // 3. Handle Back Navigation
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // 4. Handle Member Counter Logic
        btnDecrease.setOnClickListener(v -> {
            if (memberCount > 1) {
                memberCount--;
                tvMemberCount.setText(String.valueOf(memberCount));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            memberCount++;
            tvMemberCount.setText(String.valueOf(memberCount));
        });

        // 5. Handle "Create Group" Button Click
        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupDesc = etGroupDescription.getText().toString().trim();

            // Validation: Group name is mandatory
            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter a group name");
                return;
            }

            // Retrieve the logged-in user's phone number from SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
            String currentUserId = sharedPreferences.getString("loggedInPhone", "");

            // Safety check: if session is lost, prompt login
            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 6. Transition to Success Screen and pass all data via Bundle
            GroupSuccessFragment successFrag = new GroupSuccessFragment();
            Bundle bundle = new Bundle();
            bundle.putString("groupName", groupName);      // e.g., "Manali"
            bundle.putString("groupDesc", groupDesc);      // e.g., "Trip with friends"
            bundle.putInt("memberCount", memberCount);     // e.g., 2
            bundle.putString("creatorId", currentUserId);  // e.g., "698521478" (used for username lookup)
            successFrag.setArguments(bundle);

            // Navigate with slide-up animations
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_fade_in, R.anim.fade_out, R.anim.slide_up_fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, successFrag)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    /**
     * Hides the bottom navigation and FAB to provide a clean, focused creation experience.
     */
    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            // Remove bottom margin of the container to use full screen space
            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = 0;
                fragmentContainer.setLayoutParams(params);
            }
        }
    }
}