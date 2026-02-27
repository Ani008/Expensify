package com.example.expensify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class Payment extends Fragment {

    RecyclerView recyclerView;

    ArrayList<String> usernames = new ArrayList<>();
    ArrayList<String> upiIds = new ArrayList<>();

    upiAdapter adapter;
    DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_payment,
                container,
                false);

        recyclerView = view.findViewById(R.id.upiRecyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        adapter = new upiAdapter(
                getContext(),
                usernames,
                upiIds
        );

        recyclerView.setAdapter(adapter);

        fetchUsers();

        return view;
    }

    private void fetchUsers() {

        usersRef = FirebaseDatabase
                .getInstance()
                .getReference("Users");

        usersRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        usernames.clear();
                        upiIds.clear();

                        for (DataSnapshot ds :
                                snapshot.getChildren()) {

                            String username =
                                    ds.child("username")
                                            .getValue(String.class);

                            String upi =
                                    ds.child("upiId")
                                            .getValue(String.class);

                            if (upi != null) {
                                usernames.add(username);
                                upiIds.add(upi);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {}
                });
    }
}