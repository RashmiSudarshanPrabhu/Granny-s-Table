package com.example.grannystable;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserHomePage extends AppCompatActivity {

    private EditText searchEditText;
    private Button logoutButton;
    private ListView searchResultsListView;
    private ArrayAdapter<String> adapter;
    private List<String> recipeTitles = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_page);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        searchEditText = findViewById(R.id.searchEditText);
        searchResultsListView = findViewById(R.id.searchResultsListView); // ListView for search results
        logoutButton = findViewById(R.id.logoutButton);
        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recipeTitles);
        searchResultsListView.setAdapter(adapter);

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomePage.this, Login_Page.class);
            startActivity(intent);
        });

        findViewById(R.id.sideSaladLayout).setOnClickListener(view -> openCategory("Side Salad"));
        findViewById(R.id.herbalDrinksLayout).setOnClickListener(view -> openCategory("Herbal Drinks"));
        findViewById(R.id.homeRemediesLayout).setOnClickListener(view -> openCategory("Home Remedies"));
        findViewById(R.id.searchButton).setOnClickListener(v -> performSearch());

        searchEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        // 🔹 Live search while typing
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchMatchingRecipes(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 🔹 Click on a suggestion to perform search
        searchResultsListView.setOnItemClickListener((parent, view, position, id) -> {
            searchEditText.setText(recipeTitles.get(position));  // Fill search box with selected recipe
            performSearch();
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Intent intent = new Intent(UserHomePage.this, RecipeListActivity.class);
            intent.putExtra("searchQuery", query.toLowerCase());  // 🔹 Convert query to lowercase
            startActivity(intent);
        }
    }

    private void fetchMatchingRecipes(String query) {
        if (query.isEmpty()) {
            searchResultsListView.setVisibility(View.GONE);
            return;
        }

        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        recipeTitles.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            if (title.toLowerCase().contains(query.toLowerCase())) {
                                recipeTitles.add(title);
                            }
                        }

                        if (recipeTitles.isEmpty()) {
                            searchResultsListView.setVisibility(View.GONE);
                        } else {
                            adapter.notifyDataSetChanged();
                            searchResultsListView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void openCategory(String category) {
        Intent intent = new Intent(this, RecipeListActivity.class);
        intent.putExtra("category", category);  // This is for category-based search
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        searchEditText.setText("");  // 🔹 Clear the search bar
        recipeTitles.clear();        // 🔹 Clear the search results
        adapter.notifyDataSetChanged();
        searchResultsListView.setVisibility(View.GONE); // 🔹 Hide the list
    }

}
