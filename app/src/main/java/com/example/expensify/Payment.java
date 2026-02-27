package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Payment extends Fragment {

    RecyclerView recyclerView;
    upiAdapter adapter;
    List<String> upiList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        recyclerView = view.findViewById(R.id.upiRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔥 For hackathon – mock data
        upiList = new ArrayList<>();
        upiList.add("aditya@upi");
        upiList.add("om@upi");
        upiList.add("mayank@upi");

        adapter = new upiAdapter(getContext(), upiList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}