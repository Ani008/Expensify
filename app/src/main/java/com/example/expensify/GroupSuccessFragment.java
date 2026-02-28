package com.example.expensify;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GroupSuccessFragment extends Fragment {

    private String generatedInviteLink;
    private String currentGroupCode;

    // This Model matches your Firebase Screenshot perfectly
    public static class Group {
        public String groupId, groupName, description, creatorId, adminName;
        public int memberCount;
        public long createdAt;
        public Map<String, Boolean> members; // The sub-node for members

        public Group() {}

        public Group(String groupId, String groupName, String description, int memberCount, String creatorId, String adminName) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.description = description;
            this.memberCount = memberCount;
            this.creatorId = creatorId;
            this.adminName = adminName;
            this.createdAt = System.currentTimeMillis();

            // Initialize members list and add the creator immediately
            this.members = new HashMap<>();
            this.members.put(creatorId, true);
        }
    }

    public GroupSuccessFragment() {
        super(R.layout.fragment_group_success);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideNavigation();

        // 1. Bind Views
        ImageView btnClose = view.findViewById(R.id.btnClose);
        TextView btnCopy = view.findViewById(R.id.btnCopy);
        TextView tvInviteLink = view.findViewById(R.id.tvInviteLink);
        LinearLayout btnShareWhatsapp = view.findViewById(R.id.btnShareWhatsapp);
        AppCompatButton btnTimeline = view.findViewById(R.id.btnTimeline);

        // 2. Generate unique Group ID/Code
        currentGroupCode = generateRandomCode(6);
        generatedInviteLink = "https://expensify-69ebb.web.app/?code=" + currentGroupCode;
        tvInviteLink.setText(generatedInviteLink);

        // 3. Start the save process (Fetch admin name first)
        fetchUsernameAndSaveGroup(currentGroupCode);

        // 4. Run the premium UI animations
        runPremiumAnimations(view);

        // 5. Click Listeners
        btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnCopy.setOnClickListener(v -> copyToClipboard(generatedInviteLink));
        btnShareWhatsapp.setOnClickListener(v -> shareToWhatsApp("Join my group on Expensify: " + generatedInviteLink));
        btnTimeline.setOnClickListener(v -> openTimeline());
    }

    private void fetchUsernameAndSaveGroup(String code) {
        Bundle args = getArguments();
        if (args == null) return;

        String phone = args.getString("creatorId", "");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(phone);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String realUsername = "Admin";
                if (snapshot.exists()) {
                    // Pulling "username" field as per your DB structure
                    realUsername = snapshot.child("username").getValue(String.class);
                }
                saveFinalData(code, realUsername != null ? realUsername : "Admin");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                saveFinalData(code, "Admin");
            }
        });
    }

    private void saveFinalData(String code, String adminName) {
        Bundle args = getArguments();
        if (args == null) return;

        String name = args.getString("groupName", "New Group");
        String desc = args.getString("groupDesc", "");
        String creator = args.getString("creatorId", "");

        // We ignore the 'passed' memberCount and set it to 1 initially
        // because only the Admin is present at creation time.
        Group newGroup = new Group(code, name, desc, 1, creator, adminName);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(code);
        ref.setValue(newGroup).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Success! The data is now in Firebase exactly like your screenshot.
            } else {
                Toast.makeText(getContext(), "Error saving group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runPremiumAnimations(View view) {
        View circle = view.findViewById(R.id.successCircle);
        ImageView tick = view.findViewById(R.id.ivSuccessTick);
        TextView t1 = view.findViewById(R.id.tvTitle);
        TextView t2 = view.findViewById(R.id.tvSubtitle);

        // Initial States
        circle.setTranslationY(150f); circle.setAlpha(0f);
        tick.setAlpha(0f);
        t1.setTranslationY(50f); t1.setAlpha(0f);
        t2.setTranslationY(50f); t2.setAlpha(0f);

        // Circle Entrance
        circle.animate().translationY(0f).alpha(1f).setDuration(600).setStartDelay(150)
                .setInterpolator(new OvershootInterpolator()).start();

        // Tick Throb/Scale effect
        PropertyValuesHolder pX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(.2f, 1.3f),
                Keyframe.ofFloat(.5f, 0.9f),
                Keyframe.ofFloat(1f, 1f));
        PropertyValuesHolder pY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(.2f, 1.3f),
                Keyframe.ofFloat(.5f, 0.9f),
                Keyframe.ofFloat(1f, 1f));

        ObjectAnimator throb = ObjectAnimator.ofPropertyValuesHolder(tick, pX, pY);
        throb.setDuration(800).setStartDelay(300);
        throb.start();

        tick.animate().alpha(1f).setDuration(200).setStartDelay(300).start();

        // Text fade-in
        t1.animate().translationY(0f).alpha(1f).setDuration(500).setStartDelay(500).start();
        t2.animate().translationY(0f).alpha(1f).setDuration(500).setStartDelay(650).start();
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < length) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager cb = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cb.setPrimaryClip(ClipData.newPlainText("Invite", text));
        Toast.makeText(getContext(), "Link Copied to Clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void shareToWhatsApp(String msg) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.setPackage("com.whatsapp");
        i.putExtra(Intent.EXTRA_TEXT, msg);
        try {
            startActivity(i);
        } catch (Exception e) {
            // Fallback to general share if WhatsApp is missing
            Intent generic = new Intent(Intent.ACTION_SEND);
            generic.setType("text/plain");
            generic.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(generic, "Share via"));
        }
    }

    private void openTimeline() {
        TimelineFragment frag = new TimelineFragment();
        Bundle b = new Bundle();
        b.putString("groupId", currentGroupCode);
        frag.setArguments(b);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit();
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            if (nav != null) nav.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);
        }
    }
}