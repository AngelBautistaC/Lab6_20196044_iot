package com.example.lab6_iot_29106044;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab6_iot_29106044.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class StfMemoryClassicActivity extends AppCompatActivity {
    private Uri firstImageSelected;
    private Uri secondImageSelected;
    private int firstImageIndex = -1;
    private int secondImageIndex = -1;
    private int pairsFound = 0;
    private int helpUsedCount = 0;

    private static final int IMAGE_CHOOSE = 1001;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private LinearLayout imageContainer;
    private TextView imageCountView;
    private Button addImagesButton;
    private Button btnHelp;

    private Button btnShuffle;

    private HorizontalScrollView scrollView;
    private int numColumns;
    private int numRows;
    private ArrayList<Uri> boardImageUris = new ArrayList<>();
    private GridView gridView;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stf_memory_classic);



        imageContainer = findViewById(R.id.llImageContainer);
        imageCountView = findViewById(R.id.tvImageCount);
        btnHelp = findViewById(R.id.btnHelp);
        addImagesButton = findViewById(R.id.btnAddImages);
        scrollView = findViewById(R.id.scrollView);
        gridView = findViewById(R.id.gridView);

        btnShuffle = findViewById(R.id.btnShuffle);

        btnHelp.setVisibility(View.GONE);
        btnShuffle.setVisibility(View.GONE);

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHelpButtonClicked(v);
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShuffleBoardClicked(v);
            }
        });

        addImagesButton.setOnClickListener(v -> {
            if (imageUris.size() < 15) {
                chooseImage();
            } else {

            }
        });

        Button startGameButton = findViewById(R.id.btnStartGame);
        startGameButton.setOnClickListener(v -> {
            if (!imageUris.isEmpty()) {
                startGame();
            } else {
                Toast.makeText(this, "Por favor, agrega algunas imágenes primero.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void chooseImage() {
        Intent chooseImageIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(chooseImageIntent, IMAGE_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CHOOSE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null && !imageUris.contains(selectedImage)) {
                imageUris.add(selectedImage);
                addImageView(selectedImage);
            }
        }
    }

    private void addImageView(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setPadding(10, 10, 10, 10);

        // Add a way for the user to remove the image
        imageView.setOnLongClickListener(v -> {
            imageContainer.removeView(imageView);
            imageUris.remove(imageUri);
            updateImageCount();
            return true;
        });

        imageContainer.addView(imageView);
        updateImageCount();
        // Scroll to the right to show the newly added image
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_RIGHT));
    }

    private void updateImageCount() {
        imageCountView.setText(String.format("Total de imagenes seleccionadas: %d", imageUris.size()));
    }


    private void setupBoard() {
        Pair<Integer, Integer> dimensions = calculateBoardDimensions(imageUris.size());
        numRows = dimensions.first;
        numColumns = dimensions.second;

        gridView = findViewById(R.id.gridView);
        gridView.setNumColumns(numColumns);
        imageAdapter = new ImageAdapter(this, boardImageUris,this);
        gridView.setAdapter(imageAdapter);
    }

    private void shuffleImagesAndSetupBoard() {
        boardImageUris.clear();
        for (Uri uri : imageUris) {
            boardImageUris.add(uri);
            boardImageUris.add(uri);
        }

        Collections.shuffle(boardImageUris);
        if (boardImageUris.size() % 2 != 0) {
            boardImageUris.remove(boardImageUris.size() - 1);
        }
        Pair<Integer, Integer> dimensions = calculateBoardDimensions(boardImageUris.size() / 2);
        numRows = dimensions.first;
        numColumns = dimensions.second;

        setupBoard();
    }




    private Pair<Integer, Integer> calculateBoardDimensions(int numImages) {
        int totalTiles = numImages * 2;
        int sqrt = (int) Math.ceil(Math.sqrt(totalTiles));

        int rows, cols;
        if (sqrt * (sqrt - 1) >= totalTiles && (sqrt - 1) % 2 == 1) {

            rows = sqrt;
            cols = sqrt - 1;
        } else if (sqrt * sqrt >= totalTiles) {

            rows = cols = sqrt;
            if (cols % 2 == 0) {
                cols++;
            }
        } else {

            rows = cols = sqrt + 1;
            if (cols % 2 == 0) {
                cols++;
            }
        }

        return new Pair<>(rows, cols);
    }
    private void startGame() {
        btnShuffle.setVisibility(View.VISIBLE);
        btnHelp.setVisibility(View.VISIBLE);
        imageContainer.setVisibility(View.GONE);
        addImagesButton.setVisibility(View.GONE);
        shuffleImagesAndSetupBoard();
        firstImageSelected = null;
        secondImageSelected = null;
        firstImageIndex = -1;
        secondImageIndex = -1;
        pairsFound = 0;

        helpUsedCount = 0; // Restablecer el contador de uso de la ayuda

        // Habilitar el botón de ayuda si fue deshabilitado previamente
        btnHelp.setEnabled(true);

        imageAdapter = new ImageAdapter(this, boardImageUris, this);
        gridView.setAdapter(imageAdapter);
        gridView.setNumColumns(numColumns);
        imageAdapter.notifyDataSetChanged();
    }

    private void resetGame() {
        imageUris.clear();
        boardImageUris.clear();
        addImagesButton.setVisibility(View.VISIBLE);
        imageContainer.removeAllViews();
        updateImageCount();
        gridView.setAdapter(null);
        pairsFound = 0;
    }

    // Este método puede ser llamado para "aleatorizar" las imágenes en el tablero
// Este método puede ser llamado para "aleatorizar" las imágenes en el tablero
    public void onShuffleBoardClicked(View view) {
        if (gridView != null) {
            ArrayList<Uri> nonMatchedImages = new ArrayList<>();
            ArrayList<Integer> nonMatchedPositions = new ArrayList<>();

            // Recoger solo las imágenes que no han sido emparejadas aún.
            for (int i = 0; i < boardImageUris.size(); i++) {
                ImageView imageView = (ImageView) gridView.getChildAt(i);
                if (imageView.getAlpha() >= 1.0f) { // Esta imagen no es parte de un par encontrado
                    nonMatchedImages.add(boardImageUris.get(i));
                    nonMatchedPositions.add(i);
                }
            }

            // Barajar solo las imágenes no emparejadas.
            Collections.shuffle(nonMatchedImages);
            for (int i = 0; i < nonMatchedPositions.size(); i++) {
                int position = nonMatchedPositions.get(i);
                boardImageUris.set(position, nonMatchedImages.get(i));
            }

            // Notificar al adaptador del cambio
            imageAdapter.notifyDataSetChanged();
        }
    }


    // Este método puede ser llamado para proporcionar "ayuda" al usuario
    public void onHelpButtonClicked(View view) {
        if (helpUsedCount >= 2) {
            // El usuario ya ha utilizado la ayuda 2 veces, no hacer nada o deshabilitar el botón
            view.setEnabled(false);
            Toast.makeText(this, "No more help available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Incrementar el contador de uso de la ayuda
        helpUsedCount++;

        // Lógica para encontrar una pareja de imágenes que no ha sido encontrada aún
        Uri unmatchedImageUri = null;
        int unmatchedImageIndex = -1;
        for (int i = 0; i < boardImageUris.size(); i++) {
            ImageView imageView = (ImageView) gridView.getChildAt(i);
            if (imageView.getAlpha() >= 1.0f) { // Esta imagen no es parte de un par encontrado
                unmatchedImageUri = boardImageUris.get(i);
                unmatchedImageIndex = i;
                break;
            }
        }

        if (unmatchedImageUri != null) {
            // Mostrar temporalmente la pareja de la imagen no encontrada
            for (int i = 0; i < boardImageUris.size(); i++) {
                if (i != unmatchedImageIndex && boardImageUris.get(i).equals(unmatchedImageUri)) {
                    final ImageView firstImageView = (ImageView) gridView.getChildAt(unmatchedImageIndex);
                    final ImageView secondImageView = (ImageView) gridView.getChildAt(i);
                    firstImageView.setImageURI(unmatchedImageUri);
                    secondImageView.setImageURI(unmatchedImageUri);
                    firstImageView.setAlpha(0.5f);
                    secondImageView.setAlpha(0.5f);

                    // Ocultar las imágenes después de un tiempo
                    new Handler().postDelayed(() -> {
                        firstImageView.setAlpha(1.0f);
                        secondImageView.setAlpha(1.0f);
                        firstImageView.setImageResource(R.drawable.image_back);
                        secondImageView.setImageResource(R.drawable.image_back);
                    }, 1000);
                    break;
                }
            }
        }

        // Si el contador de ayuda es 2, deshabilitar el botón de ayuda
        if (helpUsedCount >= 2) {
            view.setEnabled(false);
        }
    }



    public void onImageSelected(int position, ImageView imageView) {
        if (position < 0 || position >= boardImageUris.size() * 2) {
            return;
        }
        handleImageSelection(position, imageView);
    }

    private void handleImageSelection(int position, ImageView imageView) {
        if (firstImageSelected != null && secondImageSelected != null) {
            return;
        }
        if (position < 0 || position >= boardImageUris.size() * 2) {
            return;
        }

        if (imageView.getAlpha() < 1.0f) {
            // Esta imagen ya es parte de un par encontrado, no hacer nada
            return;
        }

        Uri imageUri = boardImageUris.get(position / 2);
        imageView.setImageURI(imageUri);
        imageView.setTag(imageUri);

        if (firstImageSelected == null) {
            firstImageSelected = imageUri;
            firstImageIndex = position;
            imageView.setAlpha(0.5f);
        } else if (secondImageSelected == null && firstImageIndex != position) {
            secondImageSelected = imageUri;
            secondImageIndex = position;
            imageView.setAlpha(0.5f);

            imageView.postDelayed(this::checkForMatch, 1000);
        }
    }



    private void checkForMatch() {
        if (firstImageIndex == secondImageIndex) {
            // No permitir que el mismo índice sea seleccionado para ambos
            return;
        }

        ImageView firstImageView = (ImageView) gridView.getChildAt(firstImageIndex);
        ImageView secondImageView = (ImageView) gridView.getChildAt(secondImageIndex);

        if (firstImageSelected.equals(secondImageSelected)) {
            pairsFound++;
            firstImageView.setAlpha(0.3f); // Hacer las imágenes encontradas más transparentes
            secondImageView.setAlpha(0.3f);
            firstImageView.setBackgroundResource(R.drawable.border_correct);
            secondImageView.setBackgroundResource(R.drawable.border_correct);

            if (pairsFound * 2 == imageAdapter.getCount()) {
                Toast.makeText(StfMemoryClassicActivity.this, "¡Juego completado!", Toast.LENGTH_SHORT).show();
            }
        } else {
            firstImageView.setAlpha(1.0f);
            secondImageView.setAlpha(1.0f);
            firstImageView.setBackgroundResource(R.drawable.border_wrong);
            secondImageView.setBackgroundResource(R.drawable.border_wrong);
            new Handler().postDelayed(() -> {
                firstImageView.setImageResource(R.drawable.image_back);
                secondImageView.setImageResource(R.drawable.image_back);
                firstImageView.setBackgroundResource(0); // Remover el borde
                secondImageView.setBackgroundResource(0); // Remover el borde
                firstImageView.setTag(null);
                secondImageView.setTag(null);
            }, 1000);
        }

        firstImageSelected = null;
        secondImageSelected = null;
        firstImageIndex = -1;
        secondImageIndex = -1;
    }


}
