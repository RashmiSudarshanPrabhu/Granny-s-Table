package com.example.grannystable;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditRecipeActivity extends AppCompatActivity {

    private static final String TAG = "EditRecipeActivity";

    private EditText titleEditText, ingredientsEditText, stepsEditText, healthBenefitsEditText;
    private Spinner categorySpinner, subcategorySpinner;
    private ImageView selectedImageView;
    private Button selectImageButton, updateRecipeButton;

    private FirebaseFirestore db;
    private StorageReference storageReference;
    private String recipeId, imageUrl;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    // Mapping between categories and their subcategories
    private Map<String, String[]> categorySubcategoryMap = new HashMap<>();

    // Instance variables to store the saved category & subcategory
    private String savedCategory;
    private String savedSubcategory;
    // Flag to control whether we are still loading the initial data
    private boolean isInitialLoad = true;

    // Adapter for the category spinner
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("recipe_images");

        // Initialize UI elements
        titleEditText = findViewById(R.id.titleEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        subcategorySpinner = findViewById(R.id.subcategorySpinner);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        stepsEditText = findViewById(R.id.stepsEditText);
        healthBenefitsEditText = findViewById(R.id.healthBenefitsEditText);
        selectedImageView = findViewById(R.id.selectedImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        updateRecipeButton = findViewById(R.id.addRecipeButton); // Update Recipe button

        // Initialize the category–subcategory mapping
        categorySubcategoryMap.put("Side Salad", new String[]{"Sukka (Dry)", "Gravy", "Food", "Chutney"});
        categorySubcategoryMap.put("Herbal Drinks", new String[]{"Tea", "Juice"});
        categorySubcategoryMap.put("Home Remedies", new String[]{"Health Care", "Skin Care"});

        // Set up the category spinner adapter using the mapping keys
        String[] categories = categorySubcategoryMap.keySet().toArray(new String[0]);
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set a listener on the category spinner.
        // During initial load we do not trigger subcategory changes.
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "User selected category: " + selectedCategory);
                // Only update if initial load is complete
                if (!isInitialLoad) {
                    updateSubcategorySpinner(selectedCategory, null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Retrieve recipe ID from Intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId");
        Log.d(TAG, "Recipe ID Received: " + recipeId);

        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Error: Recipe ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRecipeData();

        // Set listener to choose a new image
        selectImageButton.setOnClickListener(v -> openImageChooser());

        // Update recipe when the button is clicked
        updateRecipeButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToFirebase();
            } else {
                updateRecipe(imageUrl); // update recipe without changing the image
            }
        });
    }

    /**
     * Helper method to update the subcategory spinner.
     * @param category The category for which to load subcategories.
     * @param preselectSubcategory Optionally pass a subcategory to preselect.
     */
    private void updateSubcategorySpinner(String category, @Nullable String preselectSubcategory) {
        String[] subcategories = categorySubcategoryMap.get(category);
        if (subcategories != null) {
            ArrayAdapter<String> subAdapter = new ArrayAdapter<>(EditRecipeActivity.this,
                    android.R.layout.simple_spinner_item, subcategories);
            subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subcategorySpinner.setAdapter(subAdapter);
            if (preselectSubcategory != null) {
                int subPosition = getMatchingPosition(subAdapter, preselectSubcategory);
                if (subPosition >= 0) {
                    subcategorySpinner.setSelection(subPosition);
                    Log.d(TAG, "Preselecting subcategory: " + preselectSubcategory);
                } else {
                    Log.d(TAG, "Saved subcategory (" + preselectSubcategory + ") not found in adapter.");
                }
            }
        } else {
            Log.d(TAG, "No subcategories found for category: " + category);
        }
    }

    /**
     * Searches for a matching position in the adapter using a case-insensitive and trimmed comparison.
     */
    private int getMatchingPosition(ArrayAdapter<String> adapter, String value) {
        if (value == null) return -1;
        String trimmedValue = value.trim();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null && item.trim().equalsIgnoreCase(trimmedValue)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Load existing recipe details from Firestore.
     */
    private void loadRecipeData() {
        db.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        titleEditText.setText(documentSnapshot.getString("title"));
                        ingredientsEditText.setText(documentSnapshot.getString("ingredients"));
                        stepsEditText.setText(documentSnapshot.getString("steps"));
                        healthBenefitsEditText.setText(documentSnapshot.getString("healthBenefits"));
                        imageUrl = documentSnapshot.getString("imageUrl");

                        // Retrieve saved category and subcategory from Firestore
                        savedCategory = documentSnapshot.getString("category");
                        savedSubcategory = documentSnapshot.getString("subcategory");
                        Log.d(TAG, "Saved category: " + savedCategory + ", Saved subcategory: " + savedSubcategory);

                        // If a saved category exists, update the spinners accordingly
                        if (savedCategory != null) {
                            int catPosition = categoryAdapter.getPosition(savedCategory);
                            if (catPosition >= 0) {
                                categorySpinner.setSelection(catPosition);
                                // Update the subcategory spinner using the saved category and preselect the saved subcategory
                                updateSubcategorySpinner(savedCategory, savedSubcategory);
                            } else {
                                Log.d(TAG, "Saved category (" + savedCategory + ") not found in adapter.");
                            }
                        }

                        // Finished initial load so that future spinner changes trigger the listener
                        isInitialLoad = false;

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(selectedImageView);
                        }
                    } else {
                        Toast.makeText(EditRecipeActivity.this, "Recipe not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditRecipeActivity.this, "Failed to load recipe!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading recipe: ", e);
                });
    }

    /**
     * Open image chooser to select a new image.
     */
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Recipe Image"), PICK_IMAGE_REQUEST);
    }

    /**
     * Handle image selection from the gallery.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            selectedImageView.setImageURI(selectedImageUri);
        }
    }

    /**
     * Upload the selected image to Firebase Storage.
     */
    private void uploadImageToFirebase() {
        if (selectedImageUri == null) return;

        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> updateRecipe(uri.toString())))
                .addOnFailureListener(e ->
                        Toast.makeText(EditRecipeActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show());
    }

    /**
     * Update the recipe details in Firestore.
     */
    private void updateRecipe(String newImageUrl) {
        // Get the current selections from the spinners
        String currentCategory = categorySpinner.getSelectedItem().toString();
        String currentSubcategory = subcategorySpinner.getSelectedItem().toString();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", titleEditText.getText().toString().trim());
        updatedData.put("ingredients", ingredientsEditText.getText().toString().trim());
        updatedData.put("steps", stepsEditText.getText().toString().trim());
        updatedData.put("healthBenefits", healthBenefitsEditText.getText().toString().trim());
        updatedData.put("imageUrl", newImageUrl);
        updatedData.put("category", currentCategory);
        updatedData.put("subcategory", currentSubcategory);

        db.collection("recipes").document(recipeId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditRecipeActivity.this, "Recipe updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(EditRecipeActivity.this, "Failed to update recipe!", Toast.LENGTH_SHORT).show());
    }
}
