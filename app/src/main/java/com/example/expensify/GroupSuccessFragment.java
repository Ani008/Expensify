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

        // 1. Handle Copy Link
        view.findViewById(R.id.btnCopy).setOnClickListener(v ->
                Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        );

        // 2. Handle Close Button (X) - Goes back to the previous screen (Home)
        view.findViewById(R.id.btnClose).setOnClickListener(v ->
                getActivity().getSupportFragmentManager().popBackStack()
        );

        // 3. Handle "Go to Timeline" Button - Opens the TimelineFragment
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
}