package com.example.expensify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
                selectedFragment = new Payment();
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

        // 4. Re-show the FAB when the user returns to Home
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof HomeFragment) {
                fabAdd.setVisibility(View.VISIBLE);
            } else {
                fabAdd.setVisibility(View.GONE);
            }
        });

        // 5. Check if the app was launched from a deep link
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String groupCode = null;

            // Check for the new Firebase Web App URL format: ?code=lYerYI
            if (data.getQueryParameter("code") != null) {
                groupCode = data.getQueryParameter("code");
            }
            // Fallback for your old path format: /join/X7B9Q2
            else {
                groupCode = data.getLastPathSegment();
            }

            if (groupCode != null && !groupCode.isEmpty()) {
                Toast.makeText(this, "Invitation Received: " + groupCode, Toast.LENGTH_SHORT).show();

                if (fabAdd != null) {
                    fabAdd.setVisibility(View.GONE);
                }

                // Navigate specifically to the InvitationFragment as requested
                invitation invitationFragment = new invitation();
                Bundle args = new Bundle();
                args.putString("GROUP_CODE", groupCode);
                invitationFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, invitationFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}