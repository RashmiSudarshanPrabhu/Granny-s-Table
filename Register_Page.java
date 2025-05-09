package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Register_Page extends AppCompatActivity {

    private EditText usernameEditText, phoneNumberEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputLayout passwordInputLayout, confirmPasswordInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username);
        phoneNumberEditText = findViewById(R.id.phone_number);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.signup_button);

        passwordInputLayout = findViewById(R.id.password_input_layout);
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout);

        signUpButton.setOnClickListener(v -> registerUser());
        TextView loginLink = findViewById(R.id.login_link);
        loginLink.setOnClickListener(v -> startActivity(new Intent(Register_Page.this, Login_Page.class)));
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validation checks
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
            phoneNumberEditText.setError("Enter a valid 10-digit phone number");
            return;
        }

        if (TextUtils.isEmpty(email) || !email.matches("^[a-zA-Z0-9._%+-]+@(gmail\\.com|yahoo\\.com)$")) {
            emailEditText.setError("Enter a valid email ending with @gmail.com or @yahoo.com");
            return;
        }

        if (TextUtils.isEmpty(password) || !isStrongPassword(password)) {
            passwordInputLayout.setError("Password must be 8+ chars, include uppercase, lowercase, number, special char");
            return;
        } else {
            passwordInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword) || !password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            return;
        } else {
            confirmPasswordInputLayout.setError(null);
        }

        // ✅ Register User in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            saveUserData(userId, username, phoneNumber, email);
                        }
                    } else {
                        Log.e("AuthError", "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(Register_Page.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * ✅ Save user data to Firestore with registration date
     */
    private void saveUserData(String userId, String username, String phoneNumber, String email) {
        String registrationDate = getCurrentDateTime(); // Get current date & time

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("phoneNumber", phoneNumber);
        user.put("email", email);
        user.put("registrationDate", registrationDate); // ✅ Save registration date

        db.collection("users").document(userId).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clearFields();
                        Toast.makeText(Register_Page.this, "Registration successful!", Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("FirestoreError", "Failed to save user data: " + task.getException());
                        Toast.makeText(Register_Page.this, "Error saving user data!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * ✅ Get current date & time in format "dd-MM-yyyy HH:mm:ss"
     */
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    private void clearFields() {
        usernameEditText.setText("");
        phoneNumberEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        Intent intent = new Intent(Register_Page.this, Login_Page.class);
        startActivity(intent);
    }
}
