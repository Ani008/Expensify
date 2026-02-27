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

    public upiAdapter(Context context,
                      ArrayList<String> usernames,
                      ArrayList<String> upiIds) {

        this.context = context;
        this.usernames = usernames;
        this.upiIds = upiIds;
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

        holder.payBtn.setOnClickListener(v ->
                payUsingUpi(upiId, username));
    }

    @Override
    public int getItemCount() {
        return upiIds.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView upiText;
        Button payBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            upiText = itemView.findViewById(R.id.upiIdText);
            payBtn = itemView.findViewById(R.id.payButton);
        }
    }

    private void payUsingUpi(String upiId, String name) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", "Expense Payment")
                .appendQueryParameter("am", "1")
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        context.startActivity(
                Intent.createChooser(intent, "Pay using")
        );
    }
}