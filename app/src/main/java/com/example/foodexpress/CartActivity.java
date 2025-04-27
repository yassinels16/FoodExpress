package com.example.foodexpress;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.adapters.CartAdapter;
import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.CartItem;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private DatabaseHelper databaseHelper;
    private TextView textViewTotal;
    private Button buttonCheckout;
    private TextView textViewEmptyCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Shopping Cart");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewCart);
        textViewTotal = findViewById(R.id.textViewTotal);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        textViewEmptyCart = findViewById(R.id.textViewEmptyCart);

        // Setup recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load cart items
        loadCartItems();

        // Setup checkout button
        buttonCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                return;
            }
            startActivity(new Intent(this, CheckoutActivity.class));
        });
    }

    private void loadCartItems() {
        cartItems = databaseHelper.getCartItems();
        cartAdapter = new CartAdapter(this, cartItems, this);
        recyclerView.setAdapter(cartAdapter);

        updateUI();
    }

    private void updateUI() {
        double total = databaseHelper.getCartTotal();
        textViewTotal.setText(String.format("Total: $%.2f", total));

        if (cartItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textViewEmptyCart.setVisibility(View.VISIBLE);
            buttonCheckout.setEnabled(false);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
            buttonCheckout.setEnabled(true);
        }
    }

    @Override
    public void onCartUpdated() {
        updateUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
