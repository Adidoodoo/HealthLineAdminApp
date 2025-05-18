package com.example.healthlineadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class departmentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private departmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_department);

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
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        List<Department> departments = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc : querySnapshot) {
                                            String departmentName = doc.getString("departmentName");
                                            String doctorName = doc.getString("doctorName");
                                            Long currentQueue = doc.getLong("currentQueue");
                                            boolean isOpen = doc.getBoolean("isOpen");
                                            if (currentQueue == null) {
                                                currentQueue = 0L;
                                            }
                                            departments.add(new Department(departmentName, doctorName, currentQueue.intValue(), isOpen));
                                        }

                                        adapter = new departmentAdapter(departments);
                                        recyclerView.setAdapter(adapter);

                                        adapter.setOnItemClickListener(position -> {
                                            Department clickedDepartment = departments.get(position);
                                            Intent intent = new Intent(departmentActivity.this, departmentStatusActivity.class);
                                            intent.putExtra("departmentName", clickedDepartment.getDepartmentName());
                                            intent.putExtra("hospitalName", hospitalName);
                                            startActivity(intent);
                                        });

                                    } else {
                                        Toast.makeText(departmentActivity.this, "Error loading departments", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(departmentActivity.this, "Hospital info not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(departmentActivity.this, "Failed to load hospital data", Toast.LENGTH_SHORT).show();
                });
    }
}
