package com.example.grannystable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ViewRecipe extends AppCompatActivity {

    private RecyclerView recipeRecyclerView;
    private RecipeAdapter recipeAdapter;
    private List<ViewRecipeClass> recipeList = new ArrayList<>();
    private List<ViewRecipeClass> filteredRecipeList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        db = FirebaseFirestore.getInstance();

        searchBar = findViewById(R.id.searchBar);
        recipeRecyclerView = findViewById(R.id.recipeRecyclerView);
        recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        recipeAdapter = new RecipeAdapter(filteredRecipeList, this);
        recipeRecyclerView.setAdapter(recipeAdapter);

        loadRecipes();

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadRecipes() {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recipeList.clear();
                        filteredRecipeList.clear();

                        if (task.getResult() == null || task.getResult().isEmpty()) {
                            Toast.makeText(ViewRecipe.this, "No recipes found!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ViewRecipeClass recipe = document.toObject(ViewRecipeClass.class);
                            recipe.setId(document.getId());
                            recipeList.add(recipe);
                        }

                        filteredRecipeList.addAll(recipeList);
                        recipeAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewRecipe.this, "Error fetching recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterRecipes(String query) {
        filteredRecipeList.clear();

        if (query.isEmpty()) {
            filteredRecipeList.addAll(recipeList);
        } else {
            for (ViewRecipeClass recipe : recipeList) {
                if (recipe.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredRecipeList.add(recipe);
                }
            }
        }
        recipeAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // ✅ Refresh recipes when returning from EditRecipeActivity
            loadRecipes();
        }
    }

    // Adapter for RecyclerView
    public static class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

        private final List<ViewRecipeClass> recipes;
        private final Context context;
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        public RecipeAdapter(List<ViewRecipeClass> recipes, Context context) {
            this.recipes = recipes;
            this.context = context;
        }

        @NonNull
        @Override
        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewrecipe_item, parent, false);
            return new RecipeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
            ViewRecipeClass recipe = recipes.get(position);
            holder.recipeName.setText(recipe.getTitle());
            holder.recipeCategory.setText(recipe.getCategory());
            holder.recipeSubcategory.setText(recipe.getSubcategory());
            holder.recipeIngredients.setText(recipe.getIngredients());

            Picasso.get().load(recipe.getImageUrl()).into(holder.recipeImage);

            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditRecipeActivity.class);
                intent.putExtra("recipeId", recipe.getId());
                ((Activity) context).startActivityForResult(intent, 1001);
            });

            holder.deleteButton.setOnClickListener(v -> deleteRecipe(recipe));
        }

        private void deleteRecipe(ViewRecipeClass recipe) {
            db.collection("recipes").document(recipe.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        recipes.remove(recipe);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show();

                        // ✅ Reload the ViewRecipe Activity
                        Intent intent = new Intent(context, ViewRecipe.class);
                        ((Activity) context).finish();
                        context.startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error deleting recipe", Toast.LENGTH_SHORT).show());
        }


        @Override
        public int getItemCount() {
            return recipes.size();
        }

        public static class RecipeViewHolder extends RecyclerView.ViewHolder {
            TextView recipeName, recipeCategory, recipeSubcategory, recipeIngredients;
            ImageView recipeImage;
            Button editButton, deleteButton;

            public RecipeViewHolder(View itemView) {
                super(itemView);
                recipeName = itemView.findViewById(R.id.recipeName);
                recipeCategory = itemView.findViewById(R.id.recipeCategory);
                recipeSubcategory = itemView.findViewById(R.id.recipeSubcategory);
                recipeIngredients = itemView.findViewById(R.id.recipeIngredients);
                recipeImage = itemView.findViewById(R.id.recipeImage);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        // 🔹 Clear the search bar when returning
        searchBar.setText("");

        // 🔹 Reload recipes to refresh the page
        loadRecipes();
    }

}
