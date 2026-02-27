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
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you have a layout file named item_group.xml (or similar)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);

        // Use a null check or default value to prevent crashes
        holder.tvTitle.setText(group.groupName != null ? group.groupName : "Unnamed Group");

        // Safely set the text for the member count
        holder.tvMembers.setText(String.format("%d members", group.memberCount));

        // Hardcoded for now based on your snippet
        holder.tvStatus.setText("ALL SETTLED");
        holder.progressBar.setProgress(100);
        holder.tvPercent.setText("100%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups != null ? groups.size() : 0;
    }

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