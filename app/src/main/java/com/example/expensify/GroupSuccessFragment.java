package com.example.expensify;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.ActivityNotFoundException;
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

import java.util.Random;

public class GroupSuccessFragment extends Fragment {

    private String generatedInviteLink;
    private String currentGroupCode; // Store the raw code separately for easy passing

    public GroupSuccessFragment() {
        super(R.layout.fragment_group_success);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind all the interactive buttons
        ImageView btnClose = view.findViewById(R.id.btnClose);
        TextView btnCopy = view.findViewById(R.id.btnCopy);
        TextView tvInviteLink = view.findViewById(R.id.tvInviteLink);
        LinearLayout btnShareWhatsapp = view.findViewById(R.id.btnShareWhatsapp);
        AppCompatButton btnTimeline = view.findViewById(R.id.btnTimeline);

        // Bind the views needed for the premium animation
        View successCircle = view.findViewById(R.id.successCircle);
        ImageView successTick = view.findViewById(R.id.ivSuccessTick);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);

        // --- ANIMATION LOGIC START ---

        // 1. Prepare views: Push them down and make them invisible initially
        float startTranslationY = 150f;

        successCircle.setTranslationY(startTranslationY);
        successCircle.setAlpha(0f);

        // We only hide the rocket with Alpha, no translation, so it stays centered
        successTick.setAlpha(0f);

        tvTitle.setTranslationY(50f);
        tvTitle.setAlpha(0f);

        tvSubtitle.setTranslationY(50f);
        tvSubtitle.setAlpha(0f);

        // 2. Animate the Circle coming up from the bottom
        OvershootInterpolator overshoot = new OvershootInterpolator(1.2f);
        successCircle.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(150)
                .setInterpolator(overshoot)
                .start();

        // 3. BULLETPROOF JAVA ANIMATION FOR THE ROCKET THROB
        // We use Java Keyframes instead of XML to prevent resource crashes
        android.animation.Keyframe kf0 = android.animation.Keyframe.ofFloat(0f, 1.0f);
        android.animation.Keyframe kf1 = android.animation.Keyframe.ofFloat(0.25f, 1.3f);
        android.animation.Keyframe kf2 = android.animation.Keyframe.ofFloat(0.5f, 0.9f);
        android.animation.Keyframe kf3 = android.animation.Keyframe.ofFloat(0.75f, 1.15f);
        android.animation.Keyframe kf4 = android.animation.Keyframe.ofFloat(1f, 1.0f);

        android.animation.PropertyValuesHolder pvhX = android.animation.PropertyValuesHolder.ofKeyframe(View.SCALE_X, kf0, kf1, kf2, kf3, kf4);
        android.animation.PropertyValuesHolder pvhY = android.animation.PropertyValuesHolder.ofKeyframe(View.SCALE_Y, kf0, kf1, kf2, kf3, kf4);

        android.animation.ObjectAnimator throbAnimator = android.animation.ObjectAnimator.ofPropertyValuesHolder(successTick, pvhX, pvhY);
        throbAnimator.setDuration(800);
        throbAnimator.setStartDelay(200);
        throbAnimator.start();

        // 4. Fade in the successTick so it becomes visible while throbbing
        successTick.animate()
                .alpha(1f)
                .setDuration(150)
                .setStartDelay(200) // Start fading at the same time the throb starts
                .start();

        // 5. Stagger the text fading in right after the icon
        tvTitle.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(350)
                .start();

        tvSubtitle.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(450)
                .start();

        // --- ANIMATION LOGIC END ---


        // 1. Generate the random code and create the link
        currentGroupCode = generateRandomCode(6);
        generatedInviteLink = "https://expensify-69ebb.web.app/?code=" + currentGroupCode;

        // Update the UI
        tvInviteLink.setText(generatedInviteLink);

        // 2. Close Button Logic
        btnClose.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        // 3. Copy Button Logic
        btnCopy.setOnClickListener(v -> copyToClipboard(generatedInviteLink));

        // 4. WhatsApp Share Logic
        btnShareWhatsapp.setOnClickListener(v -> {
            String message = "Hey! I just created a group. Join me using this link:\n" + generatedInviteLink;
            shareToWhatsApp(message);
        });

        // 5. OK/Timeline Button Logic
        btnTimeline.setOnClickListener(v -> {
            // Create the TimelineFragment instance
            TimelineFragment timelineFrag = new TimelineFragment();

            // Pack the group code into a bundle
            Bundle args = new Bundle();
            args.putString("GROUP_CODE", currentGroupCode);
            timelineFrag.setArguments(args);

            // Navigate to the Timeline
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, timelineFrag)
                        // We don't add to backstack so the user doesn't
                        // "go back" to this success screen from the timeline.
                        .commit();
            }
        });
    }

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        Random rnd = new Random();
        while (codeBuilder.length() < length) {
            int index = (int) (rnd.nextFloat() * characters.length());
            codeBuilder.append(characters.charAt(index));
        }
        return codeBuilder.toString();
    }

    private void copyToClipboard(String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Group Invite Link", textToCopy);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Link copied!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToWhatsApp(String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "WhatsApp is not installed.", Toast.LENGTH_LONG).show();
        }
    }

    private void hideNavigation() {
        if (getActivity() != null) {
            View navBar = getActivity().findViewById(R.id.bottom_navigation);
            View fab = getActivity().findViewById(R.id.fab_add);
            View fragmentContainer = getActivity().findViewById(R.id.fragment_container);

            if (navBar != null) navBar.setVisibility(View.GONE);
            if (fab != null) fab.setVisibility(View.GONE);

            if (fragmentContainer != null && fragmentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
                params.bottomMargin = 0;
                fragmentContainer.requestLayout();
            }
        }
    }
}