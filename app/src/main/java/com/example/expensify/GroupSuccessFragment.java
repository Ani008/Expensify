package com.example.expensify;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        ImageView btnClose = view.findViewById(R.id.btnClose);
        TextView btnCopy = view.findViewById(R.id.btnCopy);
        TextView tvInviteLink = view.findViewById(R.id.tvInviteLink);
        LinearLayout btnShareWhatsapp = view.findViewById(R.id.btnShareWhatsapp);
        AppCompatButton btnTimeline = view.findViewById(R.id.btnTimeline);

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
}