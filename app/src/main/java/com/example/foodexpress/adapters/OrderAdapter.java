package com.example.foodexpress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodexpress.R;
import com.example.foodexpress.models.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.textViewOrderId.setText("Order #" + order.getId());
        holder.textViewOrderDate.setText(order.getDate());
        holder.textViewOrderTotal.setText(String.format("%.2fTND", order.getTotal()));
        holder.textViewOrderStatus.setText(order.getStatus());
        holder.textViewOrderAddress.setText(order.getAddress());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOrderId;
        TextView textViewOrderDate;
        TextView textViewOrderTotal;
        TextView textViewOrderStatus;
        TextView textViewOrderAddress;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            textViewOrderTotal = itemView.findViewById(R.id.textViewOrderTotal);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewOrderAddress = itemView.findViewById(R.id.textViewOrderAddress);
        }
    }
}
