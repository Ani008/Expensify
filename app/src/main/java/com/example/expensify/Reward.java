package com.example.expensify;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;

public class Reward extends Fragment {

    // IMPORTANT: You must inflate the layout here!
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Replace 'fragment_reward' with the actual name of your XML file
        return inflater.inflate(R.layout.fragment_reward, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize the button
        MaterialButton btnGoToStore = view.findViewById(R.id.btnGoToStore);

        // 2. Set the click listener
        if (btnGoToStore != null) {
            btnGoToStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Make sure your RewardStore fragment class name matches exactly (rewardStore)
                    rewardStore storeFragment = new rewardStore();

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, storeFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }
}