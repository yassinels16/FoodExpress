package com.example.foodexpress;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.CartItem;
import com.example.foodexpress.models.Order;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int QR_SCANNER_REQUEST_CODE = 101;
    private static final int SMS_PERMISSION_REQUEST_CODE = 102;
    private static final int CALL_PERMISSION_REQUEST_CODE = 103;
    
    private static final String RESTAURANT_PHONE = "1234567890"; // Example phone number

    private TextView textViewSubtotal;
    private TextView textViewDiscount;
    private TextView textViewTotal;
    private TextView textViewLocation;
    private Button buttonSelectLocation;
    private Button buttonScanQR;
    private Button buttonPlaceOrder;
    private Button buttonCall;
    private EditText editTextNotes;

    private DatabaseHelper databaseHelper;
    private List<CartItem> cartItems;
    private double subtotal;
    private double discount = 0;
    private double total;
    private double latitude;
    private double longitude;
    private String address = "";
    private String qrCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewLocation = findViewById(R.id.textViewLocation);
        buttonSelectLocation = findViewById(R.id.buttonSelectLocation);
        buttonScanQR = findViewById(R.id.buttonScanQR);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        buttonCall = findViewById(R.id.buttonCall);
        editTextNotes = findViewById(R.id.editTextNotes);

        // Load cart items and calculate total
        loadCartItems();

        // Setup button listeners
        buttonSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            startActivityForResult(intent, LOCATION_REQUEST_CODE);
        });

        buttonScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivityForResult(intent, QR_SCANNER_REQUEST_CODE);
        });

        buttonPlaceOrder.setOnClickListener(v -> {
            if (address.isEmpty()) {
                Toast.makeText(this, "Please select a delivery location", Toast.LENGTH_SHORT).show();
                return;
            }
            placeOrder();
        });

        buttonCall.setOnClickListener(v -> {
            callRestaurant();
        });
    }

    private void loadCartItems() {
        cartItems = databaseHelper.getCartItems();
        subtotal = databaseHelper.getCartTotal();
        updateTotals();
    }

    private void updateTotals() {
        textViewSubtotal.setText(String.format("$%.2f", subtotal));
        textViewDiscount.setText(String.format("-$%.2f", discount));
        total = subtotal - discount;
        textViewTotal.setText(String.format("$%.2f", total));
    }

    private void applyDiscount(String code) {
        // Simple discount logic - in a real app, this would validate against a database
        if (code.equals("DISCOUNT20")) {
            discount = subtotal * 0.2; // 20% discount
            Toast.makeText(this, "20% discount applied!", Toast.LENGTH_SHORT).show();
        } else if (code.equals("DISCOUNT10")) {
            discount = subtotal * 0.1; // 10% discount
            Toast.makeText(this, "10% discount applied!", Toast.LENGTH_SHORT).show();
        } else if (code.equals("FREESHIP")) {
            discount = 5; // $5 off for free shipping
            Toast.makeText(this, "$5 shipping discount applied!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid discount code", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateTotals();
    }

    private void placeOrder() {
        // Create order
        Order order = new Order();
        order.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        order.setTotal(total);
        order.setStatus("Pending");
        order.setLatitude(latitude);
        order.setLongitude(longitude);
        order.setAddress(address);
        
        // Save order to database
        long orderId = databaseHelper.createOrder(order);
        
        if (orderId > 0) {
            // Send SMS with order details
            sendOrderSMS(orderId);
            
            // Show success message
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
            
            // Go to orders activity
            Intent intent = new Intent(this, OrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOrderSMS(long orderId) {
        // Check SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
            return;
        }
        
        // Create SMS message
        String message = "New order #" + orderId + " for $" + String.format("%.2f", total) + 
                ". Delivery to: " + address + 
                ". Notes: " + editTextNotes.getText().toString();
        
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(RESTAURANT_PHONE, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void callRestaurant() {
        // Check call permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST_CODE);
            return;
        }
        
        // Make phone call
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + RESTAURANT_PHONE));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get location from map activity
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            
            // Get address from coordinates
            getAddressFromLocation();
        } else if (requestCode == QR_SCANNER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get QR code from scanner activity
            qrCode = data.getStringExtra("qr_code");
            
            // Apply discount
            applyDiscount(qrCode);
        }
    }

    private void getAddressFromLocation() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address addr = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                
                for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                    sb.append(addr.getAddressLine(i));
                    if (i < addr.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                
                address = sb.toString();
                textViewLocation.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
            address = "Lat: " + latitude + ", Long: " + longitude;
            textViewLocation.setText(address);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try again
                placeOrder();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try again
                callRestaurant();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
