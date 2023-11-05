package com.example.lab6_iot_29106044.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.lab6_iot_29106044.R;
import com.example.lab6_iot_29106044.StfMemoryClassicActivity;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Uri> imageUris;
    private StfMemoryClassicActivity activity;
    public ImageAdapter(Context context, ArrayList<Uri> imageUris, StfMemoryClassicActivity activity) {
        this.context = context;
        this.imageUris = imageUris;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(400, 400)); // Tamaño ajustado
        } else {
            imageView = (ImageView) convertView;
        }

        // Establecer el reverso de la tarjeta por defecto
        imageView.setImageResource(R.drawable.image_back);

        imageView.setOnClickListener(v -> activity.onImageSelected(position, imageView));

        return imageView;
    }






}
