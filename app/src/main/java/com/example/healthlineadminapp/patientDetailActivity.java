package com.example.healthlineadminapp;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class patientDetailActivity extends AppCompatActivity {

    private TextView patientName, email, address, mobile, status, comments;
    private Button deleteQueue, completeQueue;
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

        patientName = findViewById(R.id.tvPatientFullName);
        email = findViewById(R.id.tvPatientEmail);
        address = findViewById(R.id.tvPatientAddress);
        mobile = findViewById(R.id.tvPatientMobile);
        status = findViewById(R.id.tvPatientStatus);
        comments = findViewById(R.id.tvPatientComments);
        deleteQueue = findViewById(R.id.btnDeleteQueue);
        completeQueue = findViewById(R.id.btnCompleteQueue);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");
        hospitalName = getIntent().getStringExtra("hospitalName");
        departmentName = getIntent().getStringExtra("departmentName");

        if (userId != null) {
            loadPatientDetails();
        } else {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
        }
        deleteQueue.setOnClickListener(v -> deleteWarning());
        completeQueue.setOnClickListener(v -> completeQueueWarning());
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

                        patientName.setText(fullName);
                        this.comments.setText(comments);
                        this.status.setText(status);

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

                        this.email.setText(email != null ? email : "N/A");
                        this.address.setText(address != null ? address : "N/A");
                        this.mobile.setText(mobile != null ? mobile : "N/A");
                    } else {
                        Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading user details", Toast.LENGTH_SHORT).show());
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
        progressDialog.setContentView(R.layout.loading_progressbar);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> {
            action.run();
            progressDialog.dismiss();
        }, 1000);
    }


    private void deleteQueue() {
        if (queueId != null) {
            db.collection("userInformation").document(userId)
                    .get()
                    .addOnSuccessListener(v -> {
                        if (v.exists()) {
                            String globalQueueId = v.getString("activeGlobalQueueId");

                            db.runTransaction(transaction -> {
                                if (globalQueueId != null) {
                                    transaction.delete(db.collection("queues").document(globalQueueId));
                                }

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("activeQueueId", FieldValue.delete());
                                updates.put("activeHospitalId", FieldValue.delete());
                                updates.put("activeGlobalQueueId", FieldValue.delete());
                                updates.put("activeDepartmentName", FieldValue.delete());
                                transaction.update(db.collection("userInformation").document(userId), updates);

                                return null;
                            }).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("UserClear", "cleard");
                                } else {
                                    Toast.makeText(this, "Removal failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

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
                    "currentQueue", FieldValue.increment(-1),
                    "queuesCancelled", FieldValue.increment(1)
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

    private void completeQueueWarning(){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.complete_queue_warning);
        dialog.setCancelable(true);

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            showLoadingDialog(this::completeQueue);
        });

        dialog.show();
    }

    private void completeQueue(){
        if (queueId != null) {
            db.collection("userInformation").document(userId)
                    .get()
                    .addOnSuccessListener(v -> {
                        if (v.exists()) {
                            String globalQueueId = v.getString("activeGlobalQueueId");

                            db.runTransaction(transaction -> {
                                if (globalQueueId != null) {
                                    transaction.delete(db.collection("queues").document(globalQueueId));
                                }

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("activeQueueId", FieldValue.delete());
                                updates.put("activeHospitalId", FieldValue.delete());
                                updates.put("activeGlobalQueueId", FieldValue.delete());
                                updates.put("activeDepartmentName", FieldValue.delete());
                                transaction.update(db.collection("userInformation").document(userId), updates);

                                return null;
                            }).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("UserClear", "cleard");
                                } else {
                                    Toast.makeText(this, "Removal failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

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
                    "currentQueue", FieldValue.increment(-1),
                    "queuesCompleted", FieldValue.increment(1)
            );

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Patient processed", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to process patient", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}