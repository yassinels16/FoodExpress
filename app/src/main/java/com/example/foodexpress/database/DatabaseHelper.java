package com.example.foodexpress.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.foodexpress.models.CartItem;
import com.example.foodexpress.models.Category;
import com.example.foodexpress.models.Order;
import com.example.foodexpress.models.Product;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "foodexpress.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CART = "cart";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE = "image";

    // CATEGORIES table columns
    private static final String KEY_DESCRIPTION = "description";

    // PRODUCTS table columns
    private static final String KEY_CATEGORY_ID = "category_id";
    private static final String KEY_PRICE = "price";
    private static final String KEY_DETAILS = "details";

    // CART table columns
    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_QUANTITY = "quantity";

    // ORDERS table columns
    private static final String KEY_DATE = "date";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_STATUS = "status";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ADDRESS = "address";

    // ORDER_ITEMS table columns
    private static final String KEY_ORDER_ID = "order_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create CATEGORIES table
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_IMAGE + " TEXT" + ")";
        db.execSQL(CREATE_CATEGORIES_TABLE);

        // Create PRODUCTS table
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_CATEGORY_ID + " INTEGER,"
                + KEY_PRICE + " REAL,"
                + KEY_DETAILS + " TEXT,"
                + KEY_IMAGE + " TEXT" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);

        // Create CART table
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_PRODUCT_ID + " INTEGER,"
                + KEY_QUANTITY + " INTEGER" + ")";
        db.execSQL(CREATE_CART_TABLE);

        // Create ORDERS table
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DATE + " TEXT,"
                + KEY_TOTAL + " REAL,"
                + KEY_STATUS + " TEXT,"
                + KEY_LATITUDE + " REAL,"
                + KEY_LONGITUDE + " REAL,"
                + KEY_ADDRESS + " TEXT" + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Create ORDER_ITEMS table
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_ORDER_ID + " INTEGER,"
                + KEY_PRODUCT_ID + " INTEGER,"
                + KEY_QUANTITY + " INTEGER,"
                + KEY_PRICE + " REAL" + ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);

        // Create tables again
        onCreate(db);
    }

    // Initialize database with sample data
    public void initializeData() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if categories already exist
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CATEGORIES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        
        if (count > 0) {
            return; // Data already initialized
        }
        
        // Add sample categories
        addCategory(new Category(1, "Burgers", "Delicious burgers", "burger_category.jpg"));
        addCategory(new Category(2, "Pizzas", "Tasty pizzas", "pizza_category.jpg"));
        addCategory(new Category(3, "Sandwiches", "Fresh sandwiches", "sandwich_category.jpg"));
        addCategory(new Category(4, "Drinks", "Refreshing drinks", "drink_category.jpg"));
        
        // Add sample products
        addProduct(new Product(1, "Classic Burger", 1, 5.99, "Beef patty with lettuce, tomato, and special sauce", "classic_burger.jpg"));
        addProduct(new Product(2, "Cheese Burger", 1, 6.99, "Classic burger with cheese", "cheese_burger.jpg"));
        addProduct(new Product(3, "Double Burger", 1, 8.99, "Double beef patty with all the fixings", "double_burger.jpg"));
        
        addProduct(new Product(4, "Margherita Pizza", 2, 9.99, "Classic tomato and cheese pizza", "margherita_pizza.jpg"));
        addProduct(new Product(5, "Pepperoni Pizza", 2, 11.99, "Pizza with pepperoni toppings", "pepperoni_pizza.jpg"));
        addProduct(new Product(6, "Vegetarian Pizza", 2, 10.99, "Pizza with assorted vegetables", "vegetarian_pizza.jpg"));
        
        addProduct(new Product(7, "Club Sandwich", 3, 7.99, "Triple-decker sandwich with chicken, bacon, and vegetables", "club_sandwich.jpg"));
        addProduct(new Product(8, "Tuna Sandwich", 3, 6.99, "Tuna with mayo and vegetables", "tuna_sandwich.jpg"));
        
        addProduct(new Product(9, "Cola", 4, 1.99, "Refreshing cola drink", "cola.jpg"));
        addProduct(new Product(10, "Orange Juice", 4, 2.99, "Fresh orange juice", "orange_juice.jpg"));
    }

    // CRUD operations for Category
    public void addCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, category.getName());
        values.put(KEY_DESCRIPTION, category.getDescription());
        values.put(KEY_IMAGE, category.getImage());
        db.insert(TABLE_CATEGORIES, null, values);
        db.close();
    }

    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CATEGORIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                category.setDescription(cursor.getString(2));
                category.setImage(cursor.getString(3));
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryList;
    }

    // CRUD operations for Product
    public void addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, product.getName());
        values.put(KEY_CATEGORY_ID, product.getCategoryId());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_DETAILS, product.getDetails());
        values.put(KEY_IMAGE, product.getImage());
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, KEY_CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(0));
                product.setName(cursor.getString(1));
                product.setCategoryId(cursor.getInt(2));
                product.setPrice(cursor.getDouble(3));
                product.setDetails(cursor.getString(4));
                product.setImage(cursor.getString(5));
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productList;
    }

    public Product getProduct(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        Product product = null;
        if (cursor.moveToFirst()) {
            product = new Product();
            product.setId(cursor.getInt(0));
            product.setName(cursor.getString(1));
            product.setCategoryId(cursor.getInt(2));
            product.setPrice(cursor.getDouble(3));
            product.setDetails(cursor.getString(4));
            product.setImage(cursor.getString(5));
        }
        cursor.close();
        return product;
    }

    public List<Product> searchProducts(String query) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null, KEY_NAME + " LIKE ?",
                new String[]{"%" + query + "%"}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(0));
                product.setName(cursor.getString(1));
                product.setCategoryId(cursor.getInt(2));
                product.setPrice(cursor.getDouble(3));
                product.setDetails(cursor.getString(4));
                product.setImage(cursor.getString(5));
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productList;
    }

    // CRUD operations for Cart
    public void addToCart(int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if product already exists in cart
        Cursor cursor = db.query(TABLE_CART, null, KEY_PRODUCT_ID + "=?",
                new String[]{String.valueOf(productId)}, null, null, null);
        
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_ID, productId);
        
        if (cursor.moveToFirst()) {
            // Update quantity
            int currentQuantity = cursor.getInt(2);
            int newQuantity = currentQuantity + quantity;
            values.put(KEY_QUANTITY, newQuantity);
            db.update(TABLE_CART, values, KEY_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)});
        } else {
            // Add new item
            values.put(KEY_QUANTITY, quantity);
            db.insert(TABLE_CART, null, values);
        }
        cursor.close();
        db.close();
    }

    public void updateCartItemQuantity(int cartItemId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_QUANTITY, quantity);
        db.update(TABLE_CART, values, KEY_ID + "=?", new String[]{String.valueOf(cartItemId)});
        db.close();
    }

    public void removeFromCart(int cartItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, KEY_ID + "=?", new String[]{String.valueOf(cartItemId)});
        db.close();
    }

    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, null, null);
        db.close();
    }

    public List<CartItem> getCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT c." + KEY_ID + ", c." + KEY_PRODUCT_ID + ", c." + KEY_QUANTITY + ", " +
                "p." + KEY_NAME + ", p." + KEY_PRICE + ", p." + KEY_IMAGE +
                " FROM " + TABLE_CART + " c " +
                "JOIN " + TABLE_PRODUCTS + " p ON c." + KEY_PRODUCT_ID + " = p." + KEY_ID;
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setId(cursor.getInt(0));
                item.setProductId(cursor.getInt(1));
                item.setQuantity(cursor.getInt(2));
                item.setProductName(cursor.getString(3));
                item.setPrice(cursor.getDouble(4));
                item.setImage(cursor.getString(5));
                cartItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartItems;
    }

    public double getCartTotal() {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT SUM(p." + KEY_PRICE + " * c." + KEY_QUANTITY + ") " +
                "FROM " + TABLE_CART + " c " +
                "JOIN " + TABLE_PRODUCTS + " p ON c." + KEY_PRODUCT_ID + " = p." + KEY_ID;
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // CRUD operations for Order
    public long createOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, order.getDate());
        values.put(KEY_TOTAL, order.getTotal());
        values.put(KEY_STATUS, order.getStatus());
        values.put(KEY_LATITUDE, order.getLatitude());
        values.put(KEY_LONGITUDE, order.getLongitude());
        values.put(KEY_ADDRESS, order.getAddress());
        
        long orderId = db.insert(TABLE_ORDERS, null, values);
        
        // Add order items
        List<CartItem> cartItems = getCartItems();
        for (CartItem item : cartItems) {
            ContentValues itemValues = new ContentValues();
            itemValues.put(KEY_ORDER_ID, orderId);
            itemValues.put(KEY_PRODUCT_ID, item.getProductId());
            itemValues.put(KEY_QUANTITY, item.getQuantity());
            itemValues.put(KEY_PRICE, item.getPrice());
            db.insert(TABLE_ORDER_ITEMS, null, itemValues);
        }
        
        // Clear cart
        clearCart();
        
        db.close();
        return orderId;
    }

    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, KEY_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(0));
                order.setDate(cursor.getString(1));
                order.setTotal(cursor.getDouble(2));
                order.setStatus(cursor.getString(3));
                order.setLatitude(cursor.getDouble(4));
                order.setLongitude(cursor.getDouble(5));
                order.setAddress(cursor.getString(6));
                orderList.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderList;
    }
}
