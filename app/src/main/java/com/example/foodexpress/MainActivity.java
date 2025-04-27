package com.example.foodexpress;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.adapters.CategoryAdapter;
import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.Category;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private DatabaseHelper databaseHelper;
    private SearchView searchView;
    private SwitchMaterial switchDarkMode;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load theme preference
        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Set the theme based on preference
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Ensure database is populated with initial data
        databaseHelper.initializeData();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup search view
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Setup recycler view for categories
        recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Load categories
        loadCategories();

        // Add dark mode switch to the navigation header
        View headerView = navigationView.getHeaderView(0);
        switchDarkMode = headerView.findViewById(R.id.switchDarkMode);

        if (switchDarkMode != null) {
            switchDarkMode.setChecked(isDarkMode);
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Save preference
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();

                // Apply theme
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                // Recreate activity to apply theme
                recreate();
            });
        }
    }

    private void loadCategories() {
        categoryList = databaseHelper.getAllCategories();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerView.setAdapter(categoryAdapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home, just close drawer
        } else if (id == R.id.nav_categories) {
            // Already showing categories
        } else if (id == R.id.nav_cart) {
            startActivity(new Intent(this, CartActivity.class));
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(this, OrdersActivity.class));
        } else if (id == R.id.nav_add_product) {
            startActivity(new Intent(this, AddProductActivity.class));
        } else if (id == R.id.nav_map) {
            startActivity(new Intent(this, MapActivity.class));
        } else if (id == R.id.nav_qr_scanner) {
            startActivity(new Intent(this, QRScannerActivity.class));
        } else if (id == R.id.nav_bluetooth) {
            startActivity(new Intent(this, BluetoothActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
