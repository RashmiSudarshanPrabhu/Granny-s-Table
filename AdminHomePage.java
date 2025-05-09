package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminHomePage extends AppCompatActivity {

    private CardView addRecipeCard, viewRecipeCard, usersCard, logoutCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page); // Ensure this matches your layout file name
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Find CardViews using their correct IDs
        addRecipeCard = findViewById(R.id.add_recipe_card);
        viewRecipeCard = findViewById(R.id.view_recipe_card);
        usersCard = findViewById(R.id.users_card);
        logoutCard = findViewById(R.id.logout_card);

        // Set Click Listeners
        addRecipeCard.setOnClickListener(v -> openActivity(Add_Recipe.class));
        viewRecipeCard.setOnClickListener(v -> openActivity(ViewRecipe.class));
        usersCard.setOnClickListener(v -> openActivity(ViewUsers.class));
        logoutCard.setOnClickListener(v -> logout());
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(AdminHomePage.this, activityClass);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(AdminHomePage.this, Login_Page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
