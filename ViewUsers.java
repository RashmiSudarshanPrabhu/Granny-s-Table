package com.example.grannystable;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewUsers extends AppCompatActivity {

    private ListView usersListView;
    private TextView userCountTextView, startDateTextView, endDateTextView;
    private Button startDateButton, endDateButton, filterUsersButton;
    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private String selectedStartDate = "", selectedEndDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        // Initialize UI components
        usersListView = findViewById(R.id.usersListView);
        userCountTextView = findViewById(R.id.userCountTextView);
        startDateTextView = findViewById(R.id.startDateTextView);
        endDateTextView = findViewById(R.id.endDateTextView);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        filterUsersButton = findViewById(R.id.filterUsersButton);

        db = FirebaseFirestore.getInstance();
        userAdapter = new UserAdapter(this, new ArrayList<>());
        usersListView.setAdapter(userAdapter);

        // Load all users initially
        loadUsers();

        // Select Start Date
        startDateButton.setOnClickListener(v -> showDatePickerDialog(true));

        // Select End Date
        endDateButton.setOnClickListener(v -> showDatePickerDialog(false));

        // Filter users between selected dates
        filterUsersButton.setOnClickListener(v -> {
            if (!selectedStartDate.isEmpty() && !selectedEndDate.isEmpty()) {
                filterUsersByDate(selectedStartDate, selectedEndDate);
            } else {
                Toast.makeText(this, "Please select both start and end dates!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Shows Date Picker Dialog for Start & End Date Selection
     */
    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
            if (isStartDate) {
                selectedStartDate = selectedDate;
                startDateTextView.setText("Start Date: " + selectedDate);
            } else {
                selectedEndDate = selectedDate;
                endDateTextView.setText("End Date: " + selectedDate);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * ✅ Loads All Users from Firestore
     */
    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            String phoneNumber = document.getString("phoneNumber");
                            String email = document.getString("email");
                            String registrationDate = document.getString("registrationDate");

                            if (username != null && phoneNumber != null && email != null && registrationDate != null) {
                                userList.add(new User(username, phoneNumber, email, registrationDate));
                            }
                        }
                        userAdapter.updateList(userList);
                        updateUserCount(userList.size());  // Update total user count
                    } else {
                        Toast.makeText(this, "Error loading users!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ✅ Filters Users Based on Selected Date Range
     */
    private void filterUsersByDate(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        List<User> filteredList = new ArrayList<>();

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            if (start == null || end == null) {
                Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
                return;
            }

            for (User user : userList) {
                Date userDate = dateFormat.parse(user.getRegistrationDate().split(" ")[0]); // Extract only date part
                if (userDate != null && !userDate.before(start) && !userDate.after(end)) {
                    filteredList.add(user);
                }
            }

            userAdapter.updateList(filteredList);
            updateUserCount(filteredList.size());

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No users found for the selected dates!", Toast.LENGTH_SHORT).show();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ Updates User Count Text
     */
    private void updateUserCount(int count) {
        userCountTextView.setText("Total Users: " + count);
    }
}
