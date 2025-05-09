package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class Forgot extends AppCompatActivity {
    private EditText emailEditText;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // UI elements
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        // Reset Password Button Click
        resetPasswordButton.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Please enter your registered email");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Forgot.this,
                                "Password reset link sent! Check your email.", Toast.LENGTH_LONG).show();
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(Forgot.this, Login_Page.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    } else {
                        Toast.makeText(Forgot.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendResetLink(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(resetTask -> {
                    if (resetTask.isSuccessful()) {
                        Toast.makeText(Forgot.this,
                                "Password reset link sent! Check your email.", Toast.LENGTH_LONG).show();

                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(Forgot.this, Login_Page.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    } else {
                        Toast.makeText(Forgot.this,
                                "Failed to send reset email: " + resetTask.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

}
