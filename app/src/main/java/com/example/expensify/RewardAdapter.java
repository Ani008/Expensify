package com.example.expensify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {
    private List<rewardStore.RewardModel> mList;

    public RewardAdapter(List<rewardStore.RewardModel> list) {
        this.mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        rewardStore.RewardModel item = mList.get(position);

        holder.name.setText(item.name);
        holder.offer.setText(item.offer);
        holder.pts.setText(item.pts);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, offer, pts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Matching the IDs from your item_reward_card.xml
            name = itemView.findViewById(R.id.tvRewardName);
            offer = itemView.findViewById(R.id.tvRewardOffer);
            pts = itemView.findViewById(R.id.tvPointCost);
        }
    }
}