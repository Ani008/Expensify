package com.example.expensify;

import android.content.Intent; // Added for deep links
import android.net.Uri; // Added for deep links
import android.os.Bundle;
import android.view.View;
import android.widget.Toast; // Added for deep links

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

            View itemView = findViewById(item.getItemId());
            if (itemView != null) {
                // Subtle Lift Animation: Slide up by 8 pixels and back down for others
                itemView.animate().translationY(-16f).setDuration(200).start();

                // Reset other icons (optional, but makes the 'active' one stand out)
                for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                    int id = bottomNav.getMenu().getItem(i).getItemId();
                    if (id != item.getItemId()) {
                        View otherView = findViewById(id);
                        if (otherView != null) otherView.animate().translationY(0f).setDuration(200).start();
                    }
                }
            }
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                fabAdd.setVisibility(View.VISIBLE);
            }
            else if (id == R.id.nav_history) {
                selectedFragment = new SpendingSummaryFragment();
                fabAdd.setVisibility(View.GONE);
            }
            else if (id == R.id.nav_settings) {
                selectedFragment = new Payment();
                fabAdd.setVisibility(View.GONE);
            }
            else {
                // Default fallback
                fabAdd.setVisibility(View.GONE);
            }

            // Execute the fragment swap
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Optional: Adds a smooth transition between tabs
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

        // 5. NEW: Check if the app was launched from a deep link
        handleDeepLink(getIntent());
    }

    // --- NEW METHODS FOR DEEP LINKING ADDED BELOW ---

    // Catches the link if the app is already open in the background
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    // Extracts the 6-character code from the URL
    // Extracts the 6-character code from the URL and navigates
    private void handleDeepLink(Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();

        // Check if the intent is a VIEW action and contains a URL
        if (Intent.ACTION_VIEW.equals(action) && data != null) {

            // Example data: https://expenseshare.app/join/X7B9Q2
            String groupCode = data.getLastPathSegment();

            if (groupCode != null && !groupCode.isEmpty()) {
                Toast.makeText(this, "Joining group: " + groupCode, Toast.LENGTH_SHORT).show();

                // 1. Hide the FAB since we are leaving the Home screen
                if (fabAdd != null) {
                    fabAdd.setVisibility(View.GONE);
                }

                // 2. Create the fragment and pack the groupCode into a Bundle
                Fragment groupDetailsFragment = new TimelineFragment(); // Replace with your actual fragment class
                Bundle args = new Bundle();
                args.putString("GROUP_CODE", groupCode);
                groupDetailsFragment.setArguments(args);

                // 3. Perform the Fragment Transaction
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, groupDetailsFragment)
                        .addToBackStack(null) // Allows the user to press 'Back' to return to Home
                        .commit();
            }
        }
    }
}