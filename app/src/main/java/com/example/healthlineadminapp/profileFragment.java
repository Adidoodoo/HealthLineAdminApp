package com.example.healthlineadminapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class profileFragment extends Fragment {

    private TextView nameDisplay, emailDisplay, mobNumDisplay, addressDisplay;
    private Button logoutButton, aboutUsButton;
    private FirebaseFirestore db;
    private FirebaseAuth authen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutButton = view.findViewById(R.id.buttonLogout);
        aboutUsButton = view.findViewById(R.id.buttonAboutUs);
        nameDisplay = view.findViewById(R.id.textHospitalName);
        emailDisplay = view.findViewById(R.id.textHospitalEmail);
        mobNumDisplay = view.findViewById(R.id.textHospitalMobileNumber);
        addressDisplay = view.findViewById(R.id.textHospitalAddress);

        db = FirebaseFirestore.getInstance();
        authen = FirebaseAuth.getInstance();

        FirebaseUser currentUser = authen.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            fetchUserData(currentUser.getEmail());
        } else {
            Toast.makeText(getActivity(), "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }

        logoutButton.setOnClickListener(v -> {logoutDialog();});
        aboutUsButton.setOnClickListener(v -> {aboutUsDialog();});

        return view;
    }

    private void fetchUserData(String userEmail) {
        db.collection("hospitals")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        String hospitalName = document.getString("name");
                        String address = document.getString("hospitalAddress");
                        String mobile = document.getString("hospitalContactNum");
                        String email = document.getString("email");

                        nameDisplay.setText(hospitalName != null ? hospitalName : "N/A");
                        emailDisplay.setText(email != null ? email : "N/A");
                        mobNumDisplay.setText(mobile != null ? mobile : "N/A");
                        addressDisplay.setText(address != null ? address : "N/A");
                    } else {
                        nameDisplay.setText("User data not found");
                        Toast.makeText(getActivity(), "Please complete your profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.logout_warning);
        dialog.setCancelable(true);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            logoutProgress();
        });

        dialog.show();
    }

    private void logoutProgress() {
        Dialog progressDialog = new Dialog(requireContext());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.logging_out_progressbar);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            performLogout();
        }, 1500);
    }

    private void performLogout(){
        authen.signOut();
        Toast.makeText(getActivity(), "Logout Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    private void aboutUsDialog(){
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.about_us);
        dialog.setCancelable(true);

        Button close = dialog.findViewById(R.id.btnClose);
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
