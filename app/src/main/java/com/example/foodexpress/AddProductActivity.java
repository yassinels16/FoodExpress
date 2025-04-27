package com.example.foodexpress;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.Category;
import com.example.foodexpress.models.Product;
import com.example.foodexpress.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_STORAGE_REQUEST_CODE = 101;

    private EditText editTextName;
    private EditText editTextPrice;
    private EditText editTextDetails;
    private Spinner spinnerCategory;
    private ImageView imageViewProduct;
    private Button buttonCamera;
    private Button buttonGallery;
    private Button buttonSave;

    private DatabaseHelper databaseHelper;
    private List<Category> categories;
    private String imageBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add New Product");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDetails = findViewById(R.id.editTextDetails);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonGallery = findViewById(R.id.buttonGallery);
        buttonSave = findViewById(R.id.buttonSave);

        // Load categories
        loadCategories();

        // Setup button listeners
        buttonCamera.setOnClickListener(v -> {
            checkCameraPermission();
        });

        buttonGallery.setOnClickListener(v -> {
            checkStoragePermission();
        });

        buttonSave.setOnClickListener(v -> {
            saveProduct();
        });
    }

    private void loadCategories() {
        categories = databaseHelper.getAllCategories();

        // Create adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);

        // Add category names to adapter
        for (Category category : categories) {
            adapter.add(category.getName());
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageViewProduct.setImageBitmap(imageBitmap);

                // Convert bitmap to Base64
                imageBase64 = ImageUtils.bitmapToBase64(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    imageViewProduct.setImageBitmap(bitmap);

                    // Convert bitmap to Base64
                    imageBase64 = ImageUtils.bitmapToBase64(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveProduct() {
        String name = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String details = editTextDetails.getText().toString().trim();
        int categoryPosition = spinnerCategory.getSelectedItemPosition();

        // Validate input
        if (name.isEmpty() || priceStr.isEmpty() || details.isEmpty() || imageBase64 == null) {
            Toast.makeText(this, "Please fill all fields and add an image", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int categoryId = categories.get(categoryPosition).getId();

            // Create new product
            Product product = new Product();
            product.setName(name);
            product.setCategoryId(categoryId);
            product.setPrice(price);
            product.setDetails(details);
            product.setImage(imageBase64);

            // Save to database
            databaseHelper.addProduct(product);

            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
