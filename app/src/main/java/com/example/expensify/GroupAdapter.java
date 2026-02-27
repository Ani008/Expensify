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

    // Ensure this 'Group' class is the one we standardized
    // (with groupId, groupName, adminName, etc.)
    private List<GroupSuccessFragment.Group> groups;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(GroupSuccessFragment.Group group);
    }

    public GroupAdapter(List<GroupSuccessFragment.Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupSuccessFragment.Group group = groups.get(position);

        holder.tvTitle.setText(group.groupName != null ? group.groupName : "Unnamed Group");

        // UI Logic: Show the Admin name or Member count
        // If you want to show who created it:
        String subtitle = "By " + (group.adminName != null ? group.adminName : "Admin");
        holder.tvMembers.setText(subtitle);

        // Status logic (Can be made dynamic later when you add expenses)
        holder.tvStatus.setText("ACTIVE");
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