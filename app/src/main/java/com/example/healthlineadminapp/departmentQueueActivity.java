package com.example.healthlineadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class departmentQueueActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private departmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_queue);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rvDepartments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDepartments();
    }

    private void loadDepartments() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("adminAccounts")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String hospitalName = documentSnapshot.getString("hospitalName");
                        db.collection("hospitals")
                                .document(hospitalName)
                                .collection("departments")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        List<Department> departments = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Department department = document.toObject(Department.class);
                                            departments.add(department);
                                        }

                                        adapter = new departmentAdapter(departments);
                                        recyclerView.setAdapter(adapter);

                                        adapter.setOnItemClickListener(position -> {
                                            Department clickedDepartment = departments.get(position);
                                            Intent intent = new Intent(departmentQueueActivity.this, manageQueueActivity.class);
                                            intent.putExtra("departmentName", clickedDepartment.getDepartmentName());
                                            intent.putExtra("hospitalName", hospitalName);

                                            startActivity(intent);
                                        });

                                    } else {
                                        Toast.makeText(departmentQueueActivity.this, "Error loading departments", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(departmentQueueActivity.this, "Hospital info not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
