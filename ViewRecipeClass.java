package com.example.grannystable;

public class ViewRecipeClass {
    private String id; // New field for document ID
    private String title;
    private String category;
    private String healthBenefits;
    private String imageUrl;
    private String ingredients;
    private String steps;
    private String subcategory;

    public ViewRecipeClass() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getHealthBenefits() { return healthBenefits; }
    public void setHealthBenefits(String healthBenefits) { this.healthBenefits = healthBenefits; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
}
