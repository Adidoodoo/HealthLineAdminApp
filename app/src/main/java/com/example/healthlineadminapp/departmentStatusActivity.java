package com.example.healthlineadminapp;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class departmentStatusActivity extends AppCompatActivity {

    private Button btnMoveUp, btnMoveDown;
    private TextView tvDepartmentName, tvDoctorName, tvCurrentQueue;
    private FirebaseFirestore db;
    private String hospitalName;
    private String departmentName;
    private DocumentReference departmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_department_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDepartmentName = findViewById(R.id.tvDepartmentName);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvCurrentQueue = findViewById(R.id.tvCurrentQueue);
        btnMoveUp = findViewById(R.id.btnMoveUp);
        btnMoveDown = findViewById(R.id.btnMoveDown);

        db = FirebaseFirestore.getInstance();
        hospitalName = getIntent().getStringExtra("hospitalName");
        departmentName = getIntent().getStringExtra("departmentName");

        if (hospitalName != null && departmentName != null) {
            departmentRef = db.collection("hospitals")
                    .document(hospitalName)
                    .collection("departments")
                    .document(departmentName);

            loadDepartmentDetails();

            btnMoveUp.setOnClickListener(v -> showMoveUpWarning());
            btnMoveDown.setOnClickListener(v -> showMoveDownWarning());
        } else {
            Toast.makeText(this, "Missing hospital or department information", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDepartmentDetails() {
        departmentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Update UI with department details
                String deptName = documentSnapshot.getString("departmentName");
                String doctorName = documentSnapshot.getString("doctorName");
                Long currentQueue = documentSnapshot.getLong("currentQueue");

                tvDepartmentName.setText(deptName);
                tvDoctorName.setText(doctorName);
                tvCurrentQueue.setText(String.valueOf(currentQueue));
            } else {
                Toast.makeText(this, "Department details not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading department details", Toast.LENGTH_SHORT).show();
        });
    }

    private void showMoveUpWarning() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.move_up_warning);
        dialog.setCancelable(true);

        Button btnYes = dialog.findViewById(R.id.btnCancel);
        Button btnNo = dialog.findViewById(R.id.btnRemove);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(() -> adjustCurrentQueue(1));
        });

        dialog.show();
    }

    private void showMoveDownWarning() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.move_down_warning);
        dialog.setCancelable(true);

        Button btnYes = dialog.findViewById(R.id.btnCancel);
        Button btnNo = dialog.findViewById(R.id.btnRemove);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(() -> adjustCurrentQueue(-1));
        });

        dialog.show();
    }

    private void showLoadingDialog(Runnable action) {
        Dialog progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.loading_progressbar);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> {
            action.run();
            progressDialog.dismiss();
        }, 1000);
    }

    private void adjustCurrentQueue(int change) {
        departmentRef.update("currentQueue", FieldValue.increment(change))
                .addOnSuccessListener(aVoid -> {
                    departmentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long updatedQueue = documentSnapshot.getLong("currentQueue");
                            tvCurrentQueue.setText(String.valueOf(updatedQueue));

                            String message = change < 0 ? "Queue moved up successfully" : "Queue moved down successfully";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update queue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}