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

    // Start at 1 because the Creator/Admin is the first member
    private int memberCount = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        hideNavigation();

        EditText etGroupName = view.findViewById(R.id.etGroupName);
        EditText etGroupDescription = view.findViewById(R.id.etGroupDescription);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        TextView tvMemberCount = view.findViewById(R.id.tvMemberCount);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);
        Button btnCreateGroup = view.findViewById(R.id.btnCreateGroup);

        // Set initial UI state
        tvMemberCount.setText(String.valueOf(memberCount));

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Increase/Decrease Logic
        btnDecrease.setOnClickListener(v -> {
            // Cannot be less than 1 (The Admin)
            if (memberCount > 1) {
                memberCount--;
                tvMemberCount.setText(String.valueOf(memberCount));
            } else {
                Toast.makeText(getContext(), "You are the first member!", Toast.LENGTH_SHORT).show();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            memberCount++;
            tvMemberCount.setText(String.valueOf(memberCount));
        });

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupDesc = etGroupDescription.getText().toString().trim();

            if (groupName.isEmpty()) {
                etGroupName.setError("Please enter a group name");
                return;
            }

            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ExpensifyPrefs", Context.MODE_PRIVATE);
            String currentUserId = sharedPreferences.getString("loggedInPhone", "");
            String currentUserName = sharedPreferences.getString("loggedInName", "Admin"); // Get name for 'adminName' field

            if (currentUserId.isEmpty()) {
                Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Transition to Success Fragment
            GroupSuccessFragment successFrag = new GroupSuccessFragment();
            Bundle bundle = new Bundle();
            bundle.putString("groupName", groupName);
            bundle.putString("groupDesc", groupDesc);
            bundle.putInt("memberCount", memberCount);
            bundle.putString("creatorId", currentUserId);
            bundle.putString("adminName", currentUserName);
            successFrag.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_fade_in, R.anim.fade_out, R.anim.slide_up_fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, successFrag)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);
        }
    }
}