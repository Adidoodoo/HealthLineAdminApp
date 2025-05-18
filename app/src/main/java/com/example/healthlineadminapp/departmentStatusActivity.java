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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class departmentStatusActivity extends AppCompatActivity {

    private Button moveUp, closeDepartment, reset;
    private TextView depName, docName, currentQueue, queueCount, queuesCancelled, queuesCompleted;
    private FirebaseFirestore db;
    private String hospitalName;
    private String departmentName;
    private DocumentReference departmentRef;
    private boolean isOpen = true;

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

        depName = findViewById(R.id.tvDepartmentName);
        docName = findViewById(R.id.tvDoctorName);
        currentQueue = findViewById(R.id.tvCurrentQueue);
        moveUp = findViewById(R.id.btnMoveUp);
        closeDepartment = findViewById(R.id.btnCloseDepartment);
        reset = findViewById(R.id.btnResetCounters);
        queueCount = findViewById(R.id.tvQueueCount);
        queuesCancelled = findViewById(R.id.tvQueuesCancelled);
        queuesCompleted = findViewById(R.id.tvQueuesCompleted);

        db = FirebaseFirestore.getInstance();
        hospitalName = getIntent().getStringExtra("hospitalName");
        departmentName = getIntent().getStringExtra("departmentName");

        if (hospitalName != null && departmentName != null) {
            departmentRef = db.collection("hospitals")
                    .document(hospitalName)
                    .collection("departments")
                    .document(departmentName);

            loadDepartmentDetails();

            moveUp.setOnClickListener(v -> showMoveUpWarning());
            closeDepartment.setOnClickListener(v -> showCloseDepartmentDialog());
            reset.setOnClickListener(v -> showResetCountersDialog());
        } else {
            Toast.makeText(this, "Missing hospital or department information", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDepartmentDetails() {
        departmentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String deptName = documentSnapshot.getString("departmentName");
                String doctorName = documentSnapshot.getString("doctorName");
                Boolean open = documentSnapshot.getBoolean("isOpen");

                depName.setText(deptName);
                docName.setText(doctorName);
                currentQueue.setText(String.valueOf(documentSnapshot.getLong("currentQueue")));
                queueCount.setText(String.valueOf(documentSnapshot.getLong("queueCount")));
                queuesCancelled.setText(String.valueOf(documentSnapshot.getLong("queuesCancelled")));
                queuesCompleted.setText(String.valueOf(documentSnapshot.getLong("queuesCompleted")));
                if (open != null) {
                    isOpen = open;
                }
                if (!isOpen) {
                    closeDepartment.setText("Open Department");
                    currentQueue.setText("Closed");
                } else {
                    closeDepartment.setText("Close Department");
                }

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
        Button btnCancel = dialog.findViewById(R.id.btnNo);
        Button btnMove = dialog.findViewById(R.id.btnYes);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnMove.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(() -> adjustCurrentQueue(1));
        });

        dialog.show();
    }

    private void showCloseDepartmentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.close_department_warning);
        dialog.setCancelable(true);

        TextView dialogTitle = dialog.findViewById(R.id.title);
        TextView dialogMessage = dialog.findViewById(R.id.message);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        String title = isOpen ? "Close Department" : "Open Department";
        String message = isOpen ? "Are you sure you want to close this department?" : "Are you sure you want to open this department?";
        String openOrClose = isOpen? "Close" : "Open";

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        btnClose.setText(openOrClose);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(this::toggleDepartmentStatus);
        });

        dialog.show();
    }

    private void showResetCountersDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.reset_counters_warning);
        dialog.setCancelable(true);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnReset = dialog.findViewById(R.id.btnReset);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnReset.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(this::resetCounters);
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

    private void adjustCurrentQueue(int change) { //This system initially had a moveDown button meant to decrement the queue number. It was deemed redudant by our instructor
        WriteBatch batch = db.batch();
        batch.update(departmentRef, "currentQueue", FieldValue.increment(change));
        batch.update(departmentRef, "queueCount", FieldValue.increment(change));

        batch.commit().addOnSuccessListener(aVoid -> {
            departmentRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long updatedQueue = documentSnapshot.getLong("currentQueue");
                    currentQueue.setText(String.valueOf(updatedQueue));

                    String message = change > 0 ? "Queue moved up successfully" : "Queue moved down successfully";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update queue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleDepartmentStatus() {
        isOpen = !isOpen;
        departmentRef.update("isOpen", isOpen)
                .addOnSuccessListener(aVoid -> {
                    String message = isOpen ? "Department opened" : "Department closed";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    if (!isOpen) {
                        closeDepartment.setText("Open Department");

                    } else {
                        closeDepartment.setText("Close Department");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to toggle department status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetCounters() {
        departmentRef.update("currentQueue", 1, "queueCount", 0, "queuesCancelled", 0, "queuesCompleted", 0)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Counters reset", Toast.LENGTH_SHORT).show();
                    currentQueue.setText("1");
                    queueCount.setText("0");
                    queuesCancelled.setText("0");
                    queuesCompleted.setText("0");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to reset counters: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}