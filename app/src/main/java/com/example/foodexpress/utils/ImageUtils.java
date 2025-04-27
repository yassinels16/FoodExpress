package com.example.foodexpress.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.foodexpress.R;

public class ImageUtils {

    public static void loadImage(Context context, String imageName, ImageView imageView) {
        // For demo purposes, we'll load images from drawable resources
        // In a real app, you might load from a server or local storage
        int resourceId = context.getResources().getIdentifier(
                imageName.replace(".jpg", ""), 
                "drawable", 
                context.getPackageName());
        
        if (resourceId != 0) {
            Glide.with(context)
                    .load(resourceId)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageView);
        } else {
            // Fallback to placeholder
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(imageView);
        }
    }
}
