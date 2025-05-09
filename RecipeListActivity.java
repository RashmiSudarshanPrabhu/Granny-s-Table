package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class RecipeListActivity extends AppCompatActivity {//Adpter used RecipeAdapter, class is Recipe
    private ListView listView;
    private RecipeAdapter recipeAdapter;
    private List<Object> displayList;
    private FirebaseFirestore db;
    private String category;
    private String searchQuery;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.recipeListView);
        searchEditText = findViewById(R.id.searchEditText);

        // Get category and search query from intent
        category = getIntent().getStringExtra("category");
        searchQuery = getIntent().getStringExtra("searchQuery");

        // Initialize list and adapter
        displayList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, new ArrayList<>());
        listView.setAdapter(recipeAdapter);

        // If a search query is provided, perform the global search
        if (searchQuery != null && !searchQuery.isEmpty()) {
            performGlobalSearch(searchQuery);
        } else if (category != null) {
            loadRecipesByCategory(category);
        } else {
            loadAllRecipes(); // Load all recipes if no category is selected
        }

        // Enable search functionality (for updating the list based on search)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle recipe click to open details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Object item = recipeAdapter.getItem(position);
            if (item instanceof Recipe) {
                Recipe selectedRecipe = (Recipe) item;
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipeTitle", selectedRecipe.getTitle());
                intent.putExtra("recipeImageUrl", selectedRecipe.getImageUrl());
                intent.putExtra("recipeIngredients", selectedRecipe.getIngredients());
                intent.putExtra("recipeSteps", selectedRecipe.getSteps());
                intent.putExtra("recipeHealthBenefits", selectedRecipe.getHealthBenefits());
                startActivity(intent);
            }
        });
    }

    /**
     * Load all recipes and group them by subcategory.
     */
    private void loadAllRecipes() {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, List<Recipe>> subcategoryMap = new LinkedHashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = addRecipeFromDocument(document);
                            String subcategory = recipe.getSubcategory();
                            if (!subcategoryMap.containsKey(subcategory)) {
                                subcategoryMap.put(subcategory, new ArrayList<>());
                            }
                            subcategoryMap.get(subcategory).add(recipe);
                        }

                        // Prepare the display list with subcategory headings and recipes
                        displayList.clear();
                        for (Map.Entry<String, List<Recipe>> entry : subcategoryMap.entrySet()) {
                            displayList.add(entry.getKey()); // Add subcategory heading
                            displayList.addAll(entry.getValue()); // Add recipes under subcategory
                        }

                        recipeAdapter.updateList(displayList);
                    } else {
                        Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Load recipes by category and group them by subcategory.
     */
    private void loadRecipesByCategory(String category) {
        db.collection("recipes")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, List<Recipe>> subcategoryMap = new LinkedHashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = addRecipeFromDocument(document);
                            String subcategory = recipe.getSubcategory();
                            if (!subcategoryMap.containsKey(subcategory)) {
                                subcategoryMap.put(subcategory, new ArrayList<>());
                            }
                            subcategoryMap.get(subcategory).add(recipe);
                        }

                        // Prepare the display list with subcategory headings and recipes
                        displayList.clear();
                        for (Map.Entry<String, List<Recipe>> entry : subcategoryMap.entrySet()) {
                            displayList.add(entry.getKey()); // Add subcategory heading
                            displayList.addAll(entry.getValue()); // Add recipes under subcategory
                        }

                        recipeAdapter.updateList(displayList);
                    } else {
                        Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Perform a global search on all recipes.
     */
    private void performGlobalSearch(String query) {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Recipe> allRecipes = new ArrayList<>();
                        String queryLower = query.toLowerCase();  // 🔹 Convert query to lowercase

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = addRecipeFromDocument(document);

                            // 🔹 Search in title, ingredients, and subcategory
                            if (recipe.getTitle().toLowerCase().contains(queryLower) ||
                                    recipe.getIngredients().toLowerCase().contains(queryLower) ||
                                    recipe.getSubcategory().toLowerCase().contains(queryLower)) {

                                allRecipes.add(recipe);
                            }
                        }

                        // Group filtered recipes by subcategory
                        Map<String, List<Recipe>> subcategoryMap = new LinkedHashMap<>();
                        for (Recipe recipe : allRecipes) {
                            String subcategory = recipe.getSubcategory();
                            subcategoryMap.computeIfAbsent(subcategory, k -> new ArrayList<>()).add(recipe);
                        }

                        // Display results
                        displayList.clear();
                        for (Map.Entry<String, List<Recipe>> entry : subcategoryMap.entrySet()) {
                            displayList.add(entry.getKey());  // 🔹 Add subcategory title
                            displayList.addAll(entry.getValue());  // 🔹 Add recipes under the subcategory
                        }

                        recipeAdapter.updateList(displayList);
                    } else {
                        Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Filter recipes based on the search query.
     */
    private List<Recipe> filterRecipes(List<Recipe> recipes, String query) {
        List<Recipe> filteredRecipes = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();
        for (Recipe recipe : recipes) {
            if (recipe.getTitle().toLowerCase().contains(queryLower)) {
                filteredRecipes.add(recipe);
            }
        }
        return filteredRecipes;
    }

    /**
     * Add a recipe from Firestore document.
     */
    private Recipe addRecipeFromDocument(QueryDocumentSnapshot document) {
        String title = document.getString("title");
        String imageUrl = document.getString("imageUrl");
        String ingredients = document.getString("ingredients");
        String steps = document.getString("steps");
        String healthBenefits = document.getString("healthBenefits");
        String subcategory = document.getString("subcategory");

        return new Recipe(title, imageUrl, ingredients, steps, healthBenefits, subcategory);
    }

    /**
     * Perform search on recipes.
     */
    private void performSearch(String query) {
        if (displayList == null || displayList.isEmpty()) return;

        List<Object> filteredList = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        for (Object item : displayList) {
            if (item instanceof Recipe) {
                Recipe recipe = (Recipe) item;
                if (recipe.getTitle().toLowerCase().contains(queryLower)) {
                    filteredList.add(recipe);
                }
            } else {
                filteredList.add(item); // Keep subcategory headers
            }
        }

        recipeAdapter.updateList(filteredList);
    }
    @Override
    protected void onResume() {
        super.onResume();
        EditText searchEditText = findViewById(R.id.searchEditText);
        if (searchEditText != null) {
            searchEditText.setText("");  // 🔹 Clear the search bar
        }
    }

}
