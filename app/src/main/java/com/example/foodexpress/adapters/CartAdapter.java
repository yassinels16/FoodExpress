package com.example.foodexpress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.R;
import com.example.foodexpress.database.DatabaseHelper;
import com.example.foodexpress.models.CartItem;
import com.example.foodexpress.utils.ImageUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private DatabaseHelper databaseHelper;
    private CartItemListener listener;

    public interface CartItemListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.databaseHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        
        holder.textViewProductName.setText(item.getProductName());
        holder.textViewPrice.setText(String.format("$%.2f", item.getPrice()));
        holder.textViewQuantity.setText(String.valueOf(item.getQuantity()));
        holder.textViewSubtotal.setText(String.format("$%.2f", item.getSubtotal()));
        
        ImageUtils.loadImage(context, item.getImage(), holder.imageViewProduct);
        
        holder.buttonIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            databaseHelper.updateCartItemQuantity(item.getId(), newQuantity);
            item.setQuantity(newQuantity);
            notifyItemChanged(position);
            if (listener != null) {
                listener.onCartUpdated();
            }
        });
        
        holder.buttonDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                databaseHelper.updateCartItemQuantity(item.getId(), newQuantity);
                item.setQuantity(newQuantity);
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onCartUpdated();
                }
            }
        });
        
        holder.buttonRemove.setOnClickListener(v -> {
            databaseHelper.removeFromCart(item.getId());
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            if (listener != null) {
                listener.onCartUpdated();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewPrice;
        TextView textViewQuantity;
        TextView textViewSubtotal;
        ImageButton buttonIncrease;
        ImageButton buttonDecrease;
        ImageButton buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
