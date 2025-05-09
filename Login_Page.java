package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Login_Page extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private CheckBox showPasswordCheckbox;
    private Button loginButton;
    private SignInButton googleSignInButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 123;
    private static final String ADMIN_EMAIL = "grannystable25@gmail.com"; // ✅ Admin Email Set Here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        loginButton = findViewById(R.id.login_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button);

        setupGoogleSignIn();

        // Show/Hide Password Checkbox
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setTransformationMethod(null);
            } else {
                passwordEditText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            }
            passwordEditText.setSelection(passwordEditText.getText().length()); // Move cursor to end
        });

        // Email-Password Login
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login_Page.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            loginUser(email, password);
        });

        // Google Sign-In Button Click
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Navigate to Sign-Up Page
        TextView signUpLink = findViewById(R.id.sign_up_link);
        signUpLink.setOnClickListener(v -> startActivity(new Intent(Login_Page.this, Register_Page.class)));

        // Navigate to Forgot Password Page
        TextView forgotLink = findViewById(R.id.forgot);
        forgotLink.setOnClickListener(v -> startActivity(new Intent(Login_Page.this, Forgot.class)));
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user);
                        }
                    } else {
                        Log.e("FirebaseAuth", "Login failed: " + task.getException());
                        Toast.makeText(Login_Page.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("GoogleSignIn", "Google Sign-In failed: " + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user);
                        }
                    } else {
                        Log.e("FirebaseAuth", "Google Sign-In failed: " + task.getException());
                        Toast.makeText(Login_Page.this, "Google Authentication Failed.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        if (user == null) {
            Log.e("Firestore", "❌ User is null, cannot save to Firestore.");
            return;
        }

        String userId = user.getUid();
        String userEmail = user.getEmail() != null ? user.getEmail().toLowerCase() : "N/A";
        String username = user.getDisplayName() != null ? user.getDisplayName() : "N/A";
        String phoneNumber = user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A";
        String registrationDate = getCurrentDateTime();

        Log.d("Firestore", "📌 Checking user before saving: " + userEmail);

        // ✅ Do NOT save admin in Firestore
        if (userEmail.equals(ADMIN_EMAIL.toLowerCase())) {
            Log.d("Firestore", "⚠ Admin login detected. Skipping Firestore save.");
            checkUserRoleAndRedirect(user);  // Directly redirect admin
            return;
        }

        // ✅ Prepare user data
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", username);
        newUser.put("email", userEmail);
        newUser.put("phoneNumber", phoneNumber);
        newUser.put("registrationDate", registrationDate);
        newUser.put("role", "user"); // ✅ Assign role as "user"

        // ✅ Save Google Sign-In users to Firestore
        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Google Sign-In user added: " + userEmail);
                    checkUserRoleAndRedirect(user);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Firestore write failed", e);
                    Toast.makeText(Login_Page.this, "Error saving user!", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkUserRoleAndRedirect(FirebaseUser user) {
        if (user == null) {
            Log.e("Auth", "❌ User is null, cannot redirect.");
            return;
        }

        String userEmail = user.getEmail() != null ? user.getEmail().toLowerCase() : "N/A";

        Log.d("Auth", "📌 Checking role for: " + userEmail);

        // ✅ Directly redirect admin without checking Firestore
        if (userEmail.equals(ADMIN_EMAIL.toLowerCase())) {
            Log.d("Auth", "✅ Admin login detected. Redirecting to AdminHomePage.");
            startActivity(new Intent(Login_Page.this, AdminHomePage.class));
        } else {
            Log.d("Auth", "✅ Normal user detected. Redirecting to UserHomePage.");
            startActivity(new Intent(Login_Page.this, UserHomePage.class));
        }
        finish();
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
