package com.example.foodexpress;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.adapters.ProductAdapter;
import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.Product;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> favoriteProducts;
    private DatabaseHelper databaseHelper;
    private TextView textViewNoFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorites");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        textViewNoFavorites = findViewById(R.id.textViewNoFavorites);

        // Setup recycler view
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Load favorite products
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteProducts = databaseHelper.getFavoriteProducts();
        productAdapter = new ProductAdapter(this, favoriteProducts);
        recyclerView.setAdapter(productAdapter);

        if (favoriteProducts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textViewNoFavorites.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textViewNoFavorites.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to this activity
        loadFavorites();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
