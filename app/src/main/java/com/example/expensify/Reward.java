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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the first screen (the one with the 25 points circle)
        return inflater.inflate(R.layout.fragment_reward, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnGoToStore = view.findViewById(R.id.btnGoToStore);

        if (btnGoToStore != null) {
            btnGoToStore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to the store fragment
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