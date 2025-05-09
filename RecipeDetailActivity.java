package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView titleTextView, ingredientsTextView, stepsTextView, healthBenefitsTextView;
    private ImageView recipeImageView;
    private Button shareButton;
    private String title, ingredients, steps, healthBenefits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        titleTextView = findViewById(R.id.recipeDetailTitle);
        recipeImageView = findViewById(R.id.recipeDetailImage);
        ingredientsTextView = findViewById(R.id.recipeIngredients);
        stepsTextView = findViewById(R.id.recipeSteps);
        healthBenefitsTextView = findViewById(R.id.recipeHealthBenefits);
        shareButton = findViewById(R.id.shareButton);

        Intent intent = getIntent();
        title = intent.getStringExtra("recipeTitle");
        String imageUrl = intent.getStringExtra("recipeImageUrl");
        ingredients = intent.getStringExtra("recipeIngredients");
        steps = intent.getStringExtra("recipeSteps");
        healthBenefits = intent.getStringExtra("recipeHealthBenefits");

        titleTextView.setText(title);
        ingredientsTextView.setText(ingredients);
        stepsTextView.setText(steps);
        healthBenefitsTextView.setText(healthBenefits);

        Glide.with(this).load(imageUrl).into(recipeImageView);

        // Hide the share button for admin users
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail().equals("grannystable25@gmail.com")) {
            shareButton.setVisibility(View.GONE);
        } else {
            shareButton.setVisibility(View.VISIBLE);
            shareButton.setOnClickListener(v -> shareRecipe());
        }
    }

    /**
     * ✅ Share Recipe via WhatsApp, Instagram, Facebook, Messages, etc.
     */
    private void shareRecipe() {
        String shareMessage = "🍽 " + title + "\n\n" +
                "🌿 Ingredients:\n" + ingredients + "\n\n" +
                "👩‍🍳 Steps:\n" + steps + "\n\n" +
                "💚 Health Benefits:\n" + healthBenefits + "\n\n" +
                "Shared via Granny's Table App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        startActivity(Intent.createChooser(shareIntent, "Share Recipe via"));
    }
}
