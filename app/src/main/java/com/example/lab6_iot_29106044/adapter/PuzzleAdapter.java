package com.example.lab6_iot_29106044.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class PuzzleAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Bitmap> puzzlePieces;
    private int pieceWidth, pieceHeight;

    public PuzzleAdapter(Context context, ArrayList<Bitmap> puzzlePieces, int pieceWidth, int pieceHeight) {
        this.context = context;
        this.puzzlePieces = puzzlePieces;
        this.pieceWidth = pieceWidth;
        this.pieceHeight = pieceHeight;
    }

    @Override
    public int getCount() {
        return puzzlePieces.size();
    }

    @Override
    public Object getItem(int position) {
        return puzzlePieces.get(position);
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
            imageView.setLayoutParams(new GridView.LayoutParams(pieceWidth, pieceHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(puzzlePieces.get(position));
        return imageView;
    }
}
