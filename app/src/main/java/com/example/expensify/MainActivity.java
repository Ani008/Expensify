package com.example.expensify;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        fabAdd = findViewById(R.id.fab_add);

        // 1. Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            fabAdd.setVisibility(View.VISIBLE); // Show on Home
        }

        // 2. Handle the "+" button click
        fabAdd.setOnClickListener(v -> {
            // When we go to Create Group, hide the FAB
            fabAdd.setVisibility(View.GONE);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateGroupFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Handle Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                fabAdd.setVisibility(View.VISIBLE);

            } else if (id == R.id.nav_settings) {
                selectedFragment = new Payment();  // 👈 OPEN PAYMENT FRAGMENT
                fabAdd.setVisibility(View.GONE);

            } else {
                fabAdd.setVisibility(View.GONE);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // 4. IMPORTANT: Re-show the FAB when the user presses the Back button to return to Home
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                fabAdd.setVisibility(View.VISIBLE);
            } else {
                fabAdd.setVisibility(View.GONE);
            }
        });
    }
}