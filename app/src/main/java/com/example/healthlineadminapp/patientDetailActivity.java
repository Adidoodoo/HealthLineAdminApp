package com.example.healthlineadminapp;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class patientDetailActivity extends AppCompatActivity {

    private TextView tvPatientFullName, tvPatientEmail, tvPatientAddress, tvPatientMobile, tvPatientStatus, tvPatientComments;
    private Button btnMoveUp, btnMoveDown, btnDeleteQueue;
    private FirebaseFirestore db;
    private String userId;
    private String hospitalName;
    private String departmentName;
    private String queueId;
    private int currentQueueNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        tvPatientFullName = findViewById(R.id.tvPatientFullName);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientAddress = findViewById(R.id.tvPatientAddress);
        tvPatientMobile = findViewById(R.id.tvPatientMobile);
        tvPatientStatus = findViewById(R.id.tvPatientStatus);
        tvPatientComments = findViewById(R.id.tvPatientComments);
        btnMoveUp = findViewById(R.id.btnMoveUp);
        btnMoveDown = findViewById(R.id.btnMoveDown);
        btnDeleteQueue = findViewById(R.id.btnDeleteQueue);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");
        hospitalName = getIntent().getStringExtra("hospitalName");
        departmentName = getIntent().getStringExtra("departmentName");

        if (userId != null) {
            loadPatientDetails();
        } else {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
        }

        btnMoveUp.setOnClickListener(v -> moveUpWarning());
        btnMoveDown.setOnClickListener(v -> moveDownWarning());
        btnDeleteQueue.setOnClickListener(v -> deleteWarning());
    }

    private void loadPatientDetails() {
        db.collection("hospitalQueues")
                .document(hospitalName)
                .collection("departments")
                .document(departmentName)
                .collection("queues")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String fullName = documentSnapshot.getString("patientName");
                        String comments = documentSnapshot.getString("patientComments");
                        String status = documentSnapshot.getString("status");
                        currentQueueNumber = documentSnapshot.getLong("queueNumber").intValue();
                        queueId = documentSnapshot.getId();

                        tvPatientFullName.setText(fullName);
                        tvPatientComments.setText(comments);
                        tvPatientStatus.setText(status);

                        loadUserDetails();
                    } else {
                        Toast.makeText(this, "Patient details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading queue details", Toast.LENGTH_SHORT).show());
    }

    private void loadUserDetails() {
        db.collection("userInformation")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        String address = documentSnapshot.getString("address");
                        String mobile = documentSnapshot.getString("mobileNumber");

                        tvPatientEmail.setText(email != null ? email : "N/A");
                        tvPatientAddress.setText(address != null ? address : "N/A");
                        tvPatientMobile.setText(mobile != null ? mobile : "N/A");
                    } else {
                        Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading user details", Toast.LENGTH_SHORT).show());
    }

    private void moveUpWarning() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.move_up_warning);
        dialog.setCancelable(true);

        Button btnYes = dialog.findViewById(R.id.btnCancel);
        Button btnNo = dialog.findViewById(R.id.btnRemove);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(() -> moveQueuePosition(-1));
        });

        dialog.show();
    }

    private void moveDownWarning() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.move_down_warning);
        dialog.setCancelable(true);

        Button btnYes = dialog.findViewById(R.id.btnCancel);
        Button btnNo = dialog.findViewById(R.id.btnRemove);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(() -> moveQueuePosition(1));
        });

        dialog.show();
    }

    private void deleteWarning() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.remove_queue_warning);
        dialog.setCancelable(true);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnRemove = dialog.findViewById(R.id.btnRemove);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnRemove.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(this::deleteQueue);
        });

        dialog.show();
    }

    private void showLoadingDialog(Runnable action) {
        Dialog progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.remove_queue_progressbar);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> {
            action.run();
            progressDialog.dismiss();
        }, 1000);
    }

    private void moveQueuePosition(int direction) {
        int newPosition = currentQueueNumber + direction;

        if (newPosition < 1) {
            Toast.makeText(this, "Patient is already at the top of the queue", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("hospitalQueues")
                .document(hospitalName)
                .collection("departments")
                .document(departmentName)
                .collection("queues")
                .whereEqualTo("queueNumber", newPosition)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot otherPatientDoc = querySnapshot.getDocuments().get(0);
                        String otherPatientId = otherPatientDoc.getId();

                        WriteBatch batch = db.batch();

                        batch.update(
                                db.collection("hospitalQueues")
                                        .document(hospitalName)
                                        .collection("departments")
                                        .document(departmentName)
                                        .collection("queues")
                                        .document(queueId),
                                "queueNumber", newPosition
                        );

                        batch.update(
                                db.collection("hospitalQueues")
                                        .document(hospitalName)
                                        .collection("departments")
                                        .document(departmentName)
                                        .collection("queues")
                                        .document(otherPatientId),
                                "queueNumber", currentQueueNumber
                        );

                        // Commit the batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Queue position updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(getIntent());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update queue position", Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(this, "Invalid queue position", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error finding queue position", Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteQueue() {
        if (queueId != null) {
            WriteBatch batch = db.batch();

            batch.delete(
                    db.collection("hospitalQueues")
                            .document(hospitalName)
                            .collection("departments")
                            .document(departmentName)
                            .collection("queues")
                            .document(queueId)
            );

            batch.update(
                    db.collection("hospitals")
                            .document(hospitalName)
                            .collection("departments")
                            .document(departmentName),
                    "currentQueue", FieldValue.increment(-1)
            );

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Patient removed from queue", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to remove patient", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}