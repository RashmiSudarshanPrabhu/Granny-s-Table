package com.example.grannystable;
public class Recipe {
    private String title;
    private String imageUrl;
    private String ingredients;
    private String steps;
    private String healthBenefits;
    private String subcategory;

    // 🔹 Constructor
    public Recipe(String title, String imageUrl, String ingredients, String steps, String healthBenefits, String subcategory) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.steps = steps;
        this.healthBenefits = healthBenefits;
        this.subcategory = subcategory;
    }

    // 🔹 Add Getter Methods
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public String getHealthBenefits() {
        return healthBenefits;
    }

    public String getSubcategory() {
        return subcategory;
    }
}
