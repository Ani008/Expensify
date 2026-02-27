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

import java.util.List;

public class upiAdapter extends RecyclerView.Adapter<upiAdapter.ViewHolder> {

    private Context context;
    private List<String> upiList;

    public upiAdapter(Context context, List<String> upiList) {
        this.context = context;
        this.upiList = upiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_upi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String upiId = upiList.get(position);
        holder.upiIdText.setText(upiId);

        holder.payButton.setOnClickListener(v -> {
            payUsingUpi(upiId, "Hackathon Payment", "Test Payment", "1");
        });
    }

    @Override
    public int getItemCount() {
        return upiList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView upiIdText;
        Button payButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            upiIdText = itemView.findViewById(R.id.upiIdText);
            payButton = itemView.findViewById(R.id.payButton);
        }
    }

    private void payUsingUpi(String upiId, String name, String note, String amount) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        if (null != chooser.resolveActivity(context.getPackageManager())) {
            context.startActivity(chooser);
        }
    }
}