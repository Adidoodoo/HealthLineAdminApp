package com.example.healthlineadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth authen;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authen = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        FirebaseUser currentUser = authen.getCurrentUser();
        if (currentUser != null) {
            showLoading();
            fetchHospitalAndRedirect(currentUser.getUid());
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email or Password cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                showLoading();
                loginAdmin(email, password);
            }
        });
    }

    private void showLoading() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void loginAdmin(String email, String password) {
        authen.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authen.getCurrentUser();
                        if (user != null) {
                            fetchHospitalAndRedirect(user.getUid());
                        }
                    } else {
                        hideLoading();
                        Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchHospitalAndRedirect(String uid) {
        db.collection("adminAccounts")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideLoading();
                    if (documentSnapshot.exists()) {
                        String hospitalName = documentSnapshot.getString("hospitalName");
                        Intent intent = new Intent(MainActivity.this, homeActivity.class);
                        intent.putExtra("assignedHospital", hospitalName);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Admin account not registered in Firestore", Toast.LENGTH_SHORT).show();
                        authen.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(MainActivity.this, "Failed to fetch admin info", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
    }
}