package com.example.foodexpress;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.adapters.OrderAdapter;
import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.Order;

import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private DatabaseHelper databaseHelper;
    private TextView textViewNoOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Orders");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewOrders);
        textViewNoOrders = findViewById(R.id.textViewNoOrders);

        // Setup recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load orders
        loadOrders();
    }

    private void loadOrders() {
        orderList = databaseHelper.getAllOrders();
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(orderAdapter);

        if (orderList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textViewNoOrders.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textViewNoOrders.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
