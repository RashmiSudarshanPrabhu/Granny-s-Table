package com.example.grannystable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class Add_Recipe extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private EditText titleEditText, ingredientsEditText, stepsEditText, healthBenefitsEditText;
    private Spinner categorySpinner, subcategorySpinner;
    private Button selectImageButton, addRecipeButton;
    private ImageView selectedImageView;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Remove the background color of the status bar
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        titleEditText = findViewById(R.id.titleEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        stepsEditText = findViewById(R.id.stepsEditText);
        healthBenefitsEditText = findViewById(R.id.healthBenefitsEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        subcategorySpinner = findViewById(R.id.subcategorySpinner);
        selectImageButton = findViewById(R.id.selectImageButton);
        selectedImageView = findViewById(R.id.selectedImageView);
        addRecipeButton = findViewById(R.id.addRecipeButton);

        setupCategorySpinner();

        selectImageButton.setOnClickListener(v -> openFileChooser());
        addRecipeButton.setOnClickListener(v -> uploadRecipeToFirestore());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Side Salad", "Herbal Drinks", "Home Remedies"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.BLACK); // Set selected item text color to black
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextColor(Color.BLACK); // Set dropdown item text color to black
                return textView;
            }
        };

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK); // Ensure selected text is black
                }
                updateSubcategorySpinner(categories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateSubcategorySpinner(String category) {
        String[] subcategories;
        if (category.equals("Side Salad")) {
            subcategories = new String[]{"Sukka(Dry)", "Gravy", "Food", "Chutney"};
        } else if (category.equals("Herbal Drinks")) {
            subcategories = new String[]{"Tea", "Juice"};
        } else {
            subcategories = new String[]{"Health Care", "Skin Care"};
        }

        ArrayAdapter<String> subcategoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subcategories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.BLACK); // Set selected item text color to black
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextColor(Color.BLACK); // Set dropdown item text color to black
                return textView;
            }
        };

        subcategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subcategorySpinner.setAdapter(subcategoryAdapter);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri); // Display the selected image
        }
    }

    private void uploadRecipeToFirestore() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String subcategory = subcategorySpinner.getSelectedItem().toString();
        String ingredients = ingredientsEditText.getText().toString().trim();
        String steps = stepsEditText.getText().toString().trim();
        String healthBenefits = healthBenefitsEditText.getText().toString().trim();

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "All fields are required except Health Benefits.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String storagePath = "recipes/" + System.currentTimeMillis() + ".jpg";
        FirebaseStorage.getInstance().getReference(storagePath).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            Map<String, Object> recipe = new HashMap<>();
                            recipe.put("title", title);
                            recipe.put("category", category);
                            recipe.put("subcategory", subcategory);
                            recipe.put("ingredients", ingredients);
                            recipe.put("steps", steps);
                            recipe.put("healthBenefits", healthBenefits);
                            recipe.put("imageUrl", downloadUri.toString());

                            db.collection("recipes").add(recipe).addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Recipe added successfully!", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                } else {
                                    Toast.makeText(this, "Failed to add recipe.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        titleEditText.setText("");
        ingredientsEditText.setText("");
        stepsEditText.setText("");
        healthBenefitsEditText.setText("");
        categorySpinner.setSelection(0);
        subcategorySpinner.setSelection(0);
        selectedImageView.setImageResource(0);
    }
}
