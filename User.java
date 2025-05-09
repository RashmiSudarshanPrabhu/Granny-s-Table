package com.example.grannystable;

public class User {
    private String username;
    private String phoneNumber;
    private String email;
    private String registrationDate;

    // Default constructor (needed for Firestore)
    public User() {}

    // ✅ Constructor with all four fields
    public User(String username, String phoneNumber, String email, String registrationDate) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    // ✅ Getter methods
    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }
}
