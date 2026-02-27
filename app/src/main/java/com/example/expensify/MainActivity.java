package com.example.expensify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull; // Added for Wear OS
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// WEAR OS START: Import Wearable APIs
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import java.nio.charset.StandardCharsets;
// WEAR OS END

// WEAR OS START: Implement OnMessageReceivedListener
public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
// WEAR OS END

    private FloatingActionButton fabAdd;
    private static final String TAG = "MainActivity_Wear"; // For debugging

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

    // WEAR OS START: Register and unregister the listener based on Activity lifecycle
    @Override
    protected void onResume() {
        super.onResume();
        // Start listening for watch messages when the app is on screen
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening to save battery when the app is in the background
        Wearable.getMessageClient(this).removeListener(this);
    }

    // Catch the message sent directly from the watch
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/add_group_expense")) {
            String payload = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            String[] parts = payload.split(":");

            if (parts.length == 2) {
                try {
                    int amount = Integer.parseInt(parts[0]);
                    String member = parts[1];

                    Log.d(TAG, "Live expense received from watch: ₹" + amount + " for " + member);

                    // UI updates must happen on the main thread
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Watch Sync: Added ₹" + amount + " for " + member,
                                Toast.LENGTH_LONG).show();

                        // TODO: If you want to refresh your current fragment to show the new data,
                        // trigger that refresh here.
                    });

                } catch (NumberFormatException e) {
                    Log.e(TAG, "Failed to parse amount from watch", e);
                }
            }
        }
    }
    // WEAR OS END

    // --- DEEP LINKING METHODS ---

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
            String groupCode = data.getLastPathSegment();

            if (groupCode != null && !groupCode.isEmpty()) {
                Toast.makeText(this, "Joining group: " + groupCode, Toast.LENGTH_SHORT).show();

                if (fabAdd != null) {
                    fabAdd.setVisibility(View.GONE);
                }

                Fragment groupDetailsFragment = new TimelineFragment();
                Bundle args = new Bundle();
                args.putString("GROUP_CODE", groupCode);
                groupDetailsFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, groupDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}