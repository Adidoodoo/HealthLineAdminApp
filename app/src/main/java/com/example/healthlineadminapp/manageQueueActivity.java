package com.example.healthlineadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class manageQueueActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private queueAdapter adapter;
    private List<queueItem> queueList;
    private FirebaseFirestore db;
    private String departmentName;
    private String hospitalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_queue);

        recyclerView = findViewById(R.id.rvQueue);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        queueList = new ArrayList<>();
        adapter = new queueAdapter(queueList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        departmentName = getIntent().getStringExtra("departmentName");
        hospitalName = getIntent().getStringExtra("hospitalName");

        adapter.setOnItemClickListener(queueItem -> {
            Intent intent = new Intent(manageQueueActivity.this, patientDetailActivity.class);
            intent.putExtra("userId", queueItem.getUserId());
            intent.putExtra("hospitalName", hospitalName);
            intent.putExtra("departmentName", departmentName);
            startActivity(intent);
        });

        if (departmentName != null && hospitalName != null) {
            loadQueue(hospitalName, departmentName);
        } else {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadQueue(String hospitalName, String departmentName) {
        db.collection("hospitalQueues")
                .document(hospitalName)
                .collection("departments")
                .document(departmentName)
                .collection("queues")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    queueList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        queueItem item = doc.toObject(queueItem.class);
                        queueList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load queue", Toast.LENGTH_SHORT).show()
                );
    }
}