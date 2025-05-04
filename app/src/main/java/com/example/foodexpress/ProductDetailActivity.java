package com.example.foodexpress;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.Product;
import com.example.foodexpress.utils.ImageUtils;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewProductPrice;
    private TextView textViewProductDetails;
    private Button buttonAddToCart;
    private Button buttonDecreaseQuantity;
    private Button buttonIncreaseQuantity;
    private TextView textViewQuantity;

    // Add a favorite button to the layout
    private Button buttonToggleFavorite;
    private boolean isFavorite = false;

    private DatabaseHelper databaseHelper;
    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get product ID from intent
        int productId = getIntent().getIntExtra("product_id", 0);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get product from database
        product = databaseHelper.getProduct(productId);

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(product.getName());

        // Initialize views
        imageViewProduct = findViewById(R.id.imageViewProduct);
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductDetails = findViewById(R.id.textViewProductDetails);
        buttonAddToCart = findViewById(R.id.buttonAddToCart);
        buttonDecreaseQuantity = findViewById(R.id.buttonDecreaseQuantity);
        buttonIncreaseQuantity = findViewById(R.id.buttonIncreaseQuantity);
        textViewQuantity = findViewById(R.id.textViewQuantity);

        // In the onCreate method, after initializing other views:
        buttonToggleFavorite = findViewById(R.id.buttonToggleFavorite);

        // Get favorite status from database
        isFavorite = product.isFavorite();
        updateFavoriteButtonText();

        // Set product details
        textViewProductName.setText(product.getName());
        textViewProductPrice.setText(String.format("%.2fTND", product.getPrice()));
        textViewProductDetails.setText(product.getDetails());
        ImageUtils.loadImage(this, product.getImage(), imageViewProduct);

        // Set quantity
        updateQuantityDisplay();

        // Setup buttons
        buttonDecreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityDisplay();
            }
        });

        buttonIncreaseQuantity.setOnClickListener(v -> {
            quantity++;
            updateQuantityDisplay();
        });

        buttonAddToCart.setOnClickListener(v -> {
            databaseHelper.addToCart(product.getId(), quantity);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Setup favorite button
        buttonToggleFavorite.setOnClickListener(v -> {
            // Toggle favorite status
            isFavorite = !isFavorite;
            product.setFavorite(isFavorite);

            // Update in database
            databaseHelper.toggleFavorite(product.getId());

            // Update button text
            updateFavoriteButtonText();

            // Show message
            String message = isFavorite ? "Added to favorites" : "Removed from favorites";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateQuantityDisplay() {
        textViewQuantity.setText(String.valueOf(quantity));
    }

    // Add this method to update the favorite button text
    private void updateFavoriteButtonText() {
        if (isFavorite) {
            buttonToggleFavorite.setText("Remove from Favorites");
        } else {
            buttonToggleFavorite.setText("Add to Favorites");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
