package com.example.foodexpress.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.foodexpress.R;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    public static void loadImage(Context context, String imageName, ImageView imageView) {
        // First check if it's a Base64 encoded image
        if (imageName != null && imageName.startsWith("data:image") || imageName != null && imageName.length() > 100) {
            try {
                // It's likely a Base64 image
                Bitmap bitmap = base64ToBitmap(imageName);
                if (bitmap != null) {
                    Glide.with(context)
                            .load(bitmap)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(imageView);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If not Base64 or conversion failed, try as a resource
        int resourceId = 0;
        try {
            if (imageName != null) {
                resourceId = context.getResources().getIdentifier(
                        imageName.replace(".jpg", ""),
                        "drawable",
                        context.getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    // Convert Bitmap to Base64 string
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Convert Base64 string to Bitmap
    public static Bitmap base64ToBitmap(String base64String) {
        try {
            // Remove data:image/jpeg;base64, prefix if it exists
            if (base64String.contains(",")) {
                base64String = base64String.split(",")[1];
            }

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
