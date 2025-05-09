package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(() -> {
            // Intent to navigate to NextActivity
            Intent intent = new Intent(MainActivity.this, Login_Page.class);
            startActivity(intent);
            finish(); // Close MainActivity so it doesn't stay in the back stack
        }, 3000);
    }
}