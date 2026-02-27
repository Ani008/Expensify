package com.example.expensify;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Random;

public class GroupSuccessFragment extends Fragment {

    private String generatedInviteLink;
    private String currentGroupCode;

    public static class Group {
        public String groupId, groupName, description, creatorId, adminName;
        public int memberCount;
        public long createdAt;

        public Group() {}

        public Group(String groupId, String groupName, String description, int memberCount, String creatorId, String adminName) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.description = description;
            this.memberCount = memberCount;
            this.creatorId = creatorId;
            this.adminName = adminName;
            this.createdAt = System.currentTimeMillis();
        }
    }

    public GroupSuccessFragment() {
        super(R.layout.fragment_group_success);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideNavigation();

        // Bind Views
        ImageView btnClose = view.findViewById(R.id.btnClose);
        TextView btnCopy = view.findViewById(R.id.btnCopy);
        TextView tvInviteLink = view.findViewById(R.id.tvInviteLink);
        LinearLayout btnShareWhatsapp = view.findViewById(R.id.btnShareWhatsapp);
        AppCompatButton btnTimeline = view.findViewById(R.id.btnTimeline);

        // 1. Generate Link
        currentGroupCode = generateRandomCode(6);
        generatedInviteLink = "https://expensify-69ebb.web.app/?code=" + currentGroupCode;
        tvInviteLink.setText(generatedInviteLink);

        // 2. Fetch Username and Save (Crucial: This handles the Firebase Save)
        fetchUsernameAndSaveGroup(currentGroupCode);

        // 3. Animations
        runPremiumAnimations(view);

        // 4. Click Listeners
        btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnCopy.setOnClickListener(v -> copyToClipboard(generatedInviteLink));
        btnShareWhatsapp.setOnClickListener(v -> shareToWhatsApp("Join my group: " + generatedInviteLink));
        btnTimeline.setOnClickListener(v -> openTimeline());
    }

    private void fetchUsernameAndSaveGroup(String code) {
        Bundle args = getArguments();
        if (args == null) return;

        String phone = args.getString("creatorId", "");

        // Access the users collection using the phone number from your screenshot
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(phone);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String realUsername = "Admin"; // Fallback if user not found
                if (snapshot.exists()) {
                    // Extract "username" field (e.g., "mayank") from your screenshot
                    realUsername = snapshot.child("username").getValue(String.class);
                }

                // ONLY SAVE NOW that we have the real name
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
        String name = args.getString("groupName", "New Group");
        String desc = args.getString("groupDesc", "");
        int members = args.getInt("memberCount", 1);
        String creator = args.getString("creatorId", "");

        Group newGroup = new Group(code, name, desc, members, creator, adminName);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("groups").child(code);
        ref.setValue(newGroup);
    }

    private void runPremiumAnimations(View view) {
        View circle = view.findViewById(R.id.successCircle);
        ImageView tick = view.findViewById(R.id.ivSuccessTick);
        TextView t1 = view.findViewById(R.id.tvTitle);
        TextView t2 = view.findViewById(R.id.tvSubtitle);

        circle.setTranslationY(150f); circle.setAlpha(0f);
        tick.setAlpha(0f);
        t1.setTranslationY(50f); t1.setAlpha(0f);
        t2.setTranslationY(50f); t2.setAlpha(0f);

        circle.animate().translationY(0f).alpha(1f).setDuration(600).setStartDelay(150).setInterpolator(new OvershootInterpolator()).start();

        PropertyValuesHolder pX = PropertyValuesHolder.ofKeyframe(View.SCALE_X, Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(.25f, 1.3f), Keyframe.ofFloat(.5f, 0.9f), Keyframe.ofFloat(1f, 1f));
        PropertyValuesHolder pY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y, Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(.25f, 1.3f), Keyframe.ofFloat(.5f, 0.9f), Keyframe.ofFloat(1f, 1f));
        ObjectAnimator throb = ObjectAnimator.ofPropertyValuesHolder(tick, pX, pY);
        throb.setDuration(800).setStartDelay(200);
        throb.start();

        tick.animate().alpha(1f).setDuration(150).setStartDelay(200).start();
        t1.animate().translationY(0f).alpha(1f).setDuration(400).setStartDelay(350).start();
        t2.animate().translationY(0f).alpha(1f).setDuration(400).setStartDelay(450).start();
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
        Toast.makeText(getContext(), "Link Copied!", Toast.LENGTH_SHORT).show();
    }

    private void shareToWhatsApp(String msg) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain"); i.setPackage("com.whatsapp");
        i.putExtra(Intent.EXTRA_TEXT, msg);
        try { startActivity(i); } catch (Exception e) { Toast.makeText(getContext(), "WhatsApp not found", Toast.LENGTH_SHORT).show(); }
    }

    private void openTimeline() {
        TimelineFragment frag = new TimelineFragment();
        Bundle b = new Bundle(); b.putString("groupId", currentGroupCode);
        frag.setArguments(b);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).commit();
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
            getActivity().findViewById(R.id.fab_add).setVisibility(View.GONE);
        }
    }
}