package com.example.expensify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<Group> groups;

    public GroupAdapter(List<Group> groups) { this.groups = groups; }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_group, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.tvTitle.setText(group.groupName);
        holder.tvMembers.setText(group.memberCount + " members");

        // Since this is a hackathon, we can set default "settled" status for new groups
        holder.tvStatus.setText("ALL SETTLED");
        holder.progressBar.setProgress(100);
        holder.tvPercent.setText("100%");
    }

    @Override
    public int getItemCount() { return groups.size(); }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMembers, tvStatus, tvPercent;
        ProgressBar progressBar;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
