package com.example.expensify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class upiAdapter extends RecyclerView.Adapter<upiAdapter.ViewHolder> {

    Context context;
    ArrayList<String> usernames;
    ArrayList<String> upiIds;
    PayClickListener listener;

    // ✅ Interface to send click back to Fragment
    public interface PayClickListener {
        void onPayClick(String upiId, String username);
    }

    // ✅ Constructor
    public upiAdapter(Context context,
                      ArrayList<String> usernames,
                      ArrayList<String> upiIds,
                      PayClickListener listener) {

        this.context = context;
        this.usernames = usernames;
        this.upiIds = upiIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_upi, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        String username = usernames.get(position);
        String upiId = upiIds.get(position);

        holder.upiText.setText(username + "\n" + upiId);

        // ✅ SINGLE CLICK LISTENER
        holder.payBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPayClick(upiId, username);
            }
        });
    }

    @Override
    public int getItemCount() {
        return upiIds.size();
    }

    // ✅ ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView upiText;
        Button payBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            upiText = itemView.findViewById(R.id.upiIdText);
            payBtn = itemView.findViewById(R.id.payButton);
        }
    }
}