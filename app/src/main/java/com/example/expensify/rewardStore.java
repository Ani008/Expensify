package com.example.expensify;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class rewardStore extends Fragment {

    private int currentPoints = 25; // This would normally come from Firebase

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reward_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBalance = view.findViewById(R.id.tvCurrentBalance);
        tvBalance.setText(currentPoints + " Points");

        RecyclerView rvRewards = view.findViewById(R.id.rvRewards);
        rvRewards.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<RewardModel> list = new ArrayList<>();
        // Note: You can change the PTS values here to test the "Locked" vs "Available" UI
        list.add(new RewardModel("Zomato", "Flat 30% Off", "10 PTS", R.drawable.zomatocoupons));
        list.add(new RewardModel("Coffee Co.", "Flat 10% Off", "20 PTS", R.drawable.zomatocoupons));
        list.add(new RewardModel("Prime Mart", "₹100 Voucher", "30 PTS", R.drawable.zomatocoupons));
        list.add(new RewardModel("Swift Rides", "₹50 off ride", "50 PTS", R.drawable.zomatocoupons));

        RewardAdapter adapter = new RewardAdapter(list);
        rvRewards.setAdapter(adapter);

        view.findViewById(R.id.btnActiveCoupons).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening your active coupons...", Toast.LENGTH_SHORT).show();
        });
    }

    public static class RewardModel {
        String name, offer, pts;
        int imageResId;
        public RewardModel(String name, String offer, String pts, int imageResId) {
            this.name = name; this.offer = offer; this.pts = pts; this.imageResId = imageResId;
        }
    }

    private class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {
        private List<RewardModel> rewardList;

        public RewardAdapter(List<RewardModel> rewardList) {
            this.rewardList = rewardList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RewardModel reward = rewardList.get(position);
            holder.tvName.setText(reward.name);
            holder.tvOffer.setText(reward.offer);
            holder.tvPts.setText(reward.pts);
            holder.ivLogo.setImageResource(reward.imageResId);

            // LOGIC: Check if user has enough points
            int cost = Integer.parseInt(reward.pts.replaceAll("[^0-9]", ""));

            if (currentPoints < cost) {
                holder.itemView.setAlpha(0.5f); // Make it look "Locked"
                holder.itemView.setOnClickListener(v ->
                        Toast.makeText(getContext(), "You need " + (cost - currentPoints) + " more points!", Toast.LENGTH_SHORT).show()
                );
            } else {
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setOnClickListener(v -> showRedeemDialog(reward, cost));
            }
        }

        @Override
        public int getItemCount() {
            return rewardList.size();
        }

        private void showRedeemDialog(RewardModel reward, int cost) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Redeem Reward")
                    .setMessage("Spend " + cost + " points for " + reward.name + "?")
                    .setPositiveButton("Redeem", (dialog, which) -> {
                        processRedemption(reward);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void processRedemption(RewardModel reward) {
            // Generate a random coupon code
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder code = new StringBuilder("EXP-");
            Random rnd = new Random();
            while (code.length() < 10) {
                code.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            String finalCode = code.toString();

            // Success Dialog
            new AlertDialog.Builder(getContext())
                    .setTitle("Redeemed! 🎉")
                    .setMessage("Your code: " + finalCode + "\n\nIt has been saved to your active coupons.")
                    .setPositiveButton("Copy Code", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Coupon", finalCode);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Close", null)
                    .show();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvOffer, tvPts;
            ImageView ivLogo;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvRewardName);
                tvOffer = itemView.findViewById(R.id.tvRewardOffer);
                tvPts = itemView.findViewById(R.id.tvPointCost);
                ivLogo = itemView.findViewById(R.id.ivRewardLogo);
            }
        }
    }
}