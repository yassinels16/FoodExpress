package com.example.foodexpress;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.adapters.ProductAdapter;
import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.Product;

import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private DatabaseHelper databaseHelper;
    private String query;
    private TextView textViewNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Get query from intent
        query = getIntent().getStringExtra("query");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search: " + query);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewSearchResults);
        textViewNoResults = findViewById(R.id.textViewNoResults);

        // Setup recycler view
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Search products
        searchProducts();
    }

    private void searchProducts() {
        productList = databaseHelper.searchProducts(query);
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);

        if (productList.isEmpty()) {
            textViewNoResults.setText("No results found for \"" + query + "\"");
            textViewNoResults.setVisibility(TextView.VISIBLE);
        } else {
            textViewNoResults.setVisibility(TextView.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
