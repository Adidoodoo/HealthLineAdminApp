package com.example.healthlineadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class homeFragment extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button buttonDept = view.findViewById(R.id.buttonDept);
        Button buttonManageQueue = view.findViewById(R.id.buttonQueue);

        buttonDept.setOnClickListener(v -> startActivity(new Intent(getActivity(), departmentActivity.class)));

        buttonManageQueue.setOnClickListener(v -> startActivity(new Intent(getActivity(), departmentQueueActivity.class)));

        return view;
    }
}