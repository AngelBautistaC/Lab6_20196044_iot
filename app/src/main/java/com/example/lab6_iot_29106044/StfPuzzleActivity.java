package com.example.lab6_iot_29106044;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_iot_29106044.adapter.PuzzleAdapter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StfPuzzleActivity extends AppCompatActivity {

    private static final int IMAGE_CHOOSE = 1000;
    private GridView gridView;
    private ArrayList<Bitmap> puzzlePieces = new ArrayList<>(); // Inicializa aquí
    private ArrayList<Bitmap> yourOriginalImagePieces = new ArrayList<>(); // Inicializa aquí
    private Button btnStartGame;
    private int gridSize = 3;
    private int emptySpaceIndex=-1; // Indice del espacio vacío
    private boolean gameStarted = false;
    private int pieceWidth, pieceHeight; // Dimensiones de las piezas del rompecabezas

    private void chooseImage() {
        Intent chooseImageIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(chooseImageIntent, IMAGE_CHOOSE);
    }

    // Sobrescribe este método para manejar la imagen elegida
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CHOOSE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                // Antes de crear nuevas piezas, limpiamos las anteriores
                puzzlePieces.clear();
                if (yourOriginalImagePieces != null) {
                    yourOriginalImagePieces.clear();
                }

                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);
                createPuzzlePieces(selectedBitmap);

                // Reiniciamos el estado del juego
                gameStarted = false;
                btnStartGame.setText("Start Game");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stf_puzzle);

        gridView = findViewById(R.id.gridViewPuzzle);
        btnStartGame = findViewById(R.id.btnStartGame);

        Button btnChooseImage = findViewById(R.id.btnChooseImage); // Make sure you have a button with this ID in your layout
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); // Call chooseImage when the button is clicked
            }
        });

        gridView.setNumColumns(gridSize);
        puzzlePieces = new ArrayList<>();
        yourOriginalImagePieces = new ArrayList<>();
        // This assumes the player has already chosen an image and it's stored locally
        // For actual image selection from gallery, you'll need to implement an image chooser
        if (!yourOriginalImagePieces.isEmpty()) {
            loadSavedGame();
        }

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameStarted) {
                    gameStarted = true;
                    shufflePuzzle();
                    btnStartGame.setText("Reset Game");
                } else {
                    resetGame();
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isAdjacentToEmptySpace(position)) {
                    swap(position, emptySpaceIndex);
                    emptySpaceIndex = position; // Actualiza el índice del espacio vacío
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
                    checkForWin(); // Verifica si el juego ha sido ganado después de cada movimiento
                }
            }
        });
    }


    private void createPuzzlePieces(Bitmap image) {
        int piecesNumber = gridSize * gridSize;
        pieceWidth = image.getWidth() / gridSize;
        pieceHeight = image.getHeight() / gridSize;

        // Asegurarse de que las listas estén vacías antes de agregar nuevas piezas
        puzzlePieces.clear();
        yourOriginalImagePieces.clear();

        int yCoord = 0;
        for (int row = 0; row < gridSize; row++) {
            int xCoord = 0;
            for (int col = 0; col < gridSize; col++) {
                Bitmap pieceBitmap = Bitmap.createBitmap(image, xCoord, yCoord, pieceWidth, pieceHeight);
                puzzlePieces.add(pieceBitmap);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        // Remove the bottom right corner piece to leave space on the board
        // This is done before adding the pieces to the original list to ensure the last piece is null
        Bitmap lastPiece = puzzlePieces.remove(piecesNumber - 1);
        puzzlePieces.add(null); // Add a null piece to represent the empty space

        // Add the non-null pieces to the original list
        yourOriginalImagePieces.addAll(puzzlePieces);
        // Add the last piece (which was removed) at the end of the original pieces list
        yourOriginalImagePieces.add(lastPiece);

        // Set up the adapter for the GridView
        gridView.setAdapter(new PuzzleAdapter(this, puzzlePieces, pieceWidth, pieceHeight));
    }



    private void shufflePuzzle() {
        if (puzzlePieces.size() == 0) return;
        Random random = new Random();
        // El índice del espacio vacío debe ser el último elemento en la lista de piezas
        emptySpaceIndex = puzzlePieces.size() - 1;

        // Definimos un número de pasos para barajar basado en el tamaño del rompecabezas
        int shuffleSteps = gridSize * gridSize * 2;

        for (int step = 0; step < shuffleSteps; step++) {
            List<Integer> validMoves = new ArrayList<>();

            // Verificamos si podemos mover una ficha hacia el espacio vacío desde arriba, abajo, izquierda o derecha
            if (emptySpaceIndex - gridSize >= 0) validMoves.add(emptySpaceIndex - gridSize); // Mover desde arriba
            if (emptySpaceIndex + gridSize < puzzlePieces.size()) validMoves.add(emptySpaceIndex + gridSize); // Mover desde abajo
            if (emptySpaceIndex % gridSize > 0) validMoves.add(emptySpaceIndex - 1); // Mover desde la izquierda
            if (emptySpaceIndex % gridSize < gridSize - 1) validMoves.add(emptySpaceIndex + 1); // Mover desde la derecha

            // Elegimos un movimiento aleatorio de los movimientos válidos y realizamos el intercambio
            int toSwapWithIndex = validMoves.get(random.nextInt(validMoves.size()));
            Collections.swap(puzzlePieces, emptySpaceIndex, toSwapWithIndex);
            emptySpaceIndex = toSwapWithIndex; // Actualizamos el índice del espacio vacío
        }

        // Refrescamos el GridView para mostrar el rompecabezas barajado
        gridView.invalidateViews();

        // Guardamos el estado barajado
        saveGameState();
    }

    private void swap(int indexOne, int indexTwo) {
        Collections.swap(puzzlePieces, indexOne, indexTwo);
    }



    private void resetGame() {
        // Ahora realmente restablecemos a su estado original y actualizamos la vista
        puzzlePieces = new ArrayList<>(yourOriginalImagePieces);
        emptySpaceIndex = puzzlePieces.size() - 1;
        puzzlePieces.set(emptySpaceIndex, null); // Establece el espacio vacío
        gridView.setAdapter(new PuzzleAdapter(this, puzzlePieces, pieceWidth, pieceHeight));
        gameStarted = false;
        btnStartGame.setText("Start Game");
        saveGameState(); // Guarda el estado inicial de nuevo
    }

    private boolean isAdjacentToEmptySpace(int position) {
        // Get the row and column of the empty space
        int emptyRow = emptySpaceIndex / gridSize;
        int emptyCol = emptySpaceIndex % gridSize;

        // Get the row and column of the clicked position
        int clickedRow = position / gridSize;
        int clickedCol = position % gridSize;

        // Check if the clicked position is adjacent to the empty space
        return (emptyRow == clickedRow && Math.abs(emptyCol - clickedCol) == 1) ||
                (emptyCol == clickedCol && Math.abs(emptyRow - clickedRow) == 1);
    }

    private void checkForWin() {
        for (int i = 0; i < puzzlePieces.size() - 1; i++) {
            // Assuming the original image pieces are in the correct order from 0 to size - 2
            // If any piece is not in the correct position, return
            if (puzzlePieces.get(i) != yourOriginalImagePieces.get(i)) {
                return;
            }
        }
        // If all pieces are in the correct position, the user has won
        Toast.makeText(this, "Se culminó el juego", Toast.LENGTH_SHORT).show();
        // Reset the game or navigate the user to a new screen
    }

    private void saveGameState() {
        if (puzzlePieces.isEmpty() || yourOriginalImagePieces.isEmpty()) return;
        // Guarda el estado actual del rompecabezas
        SharedPreferences prefs = getSharedPreferences("StfPuzzle", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder sb = new StringBuilder();
        for (Bitmap piece : puzzlePieces) {
            int originalIndex = yourOriginalImagePieces.indexOf(piece);
            sb.append(originalIndex).append(",");
        }

        editor.putString("puzzleState", sb.toString());
        editor.putInt("emptySpaceIndex", emptySpaceIndex);
        editor.apply();
    }
    private void loadSavedGame() {
        // Carga el estado guardado del rompecabezas
        SharedPreferences prefs = getSharedPreferences("StfPuzzle", MODE_PRIVATE);
        String savedState = prefs.getString("puzzleState", null);
        emptySpaceIndex = prefs.getInt("emptySpaceIndex", -1);

        if (savedState != null && emptySpaceIndex != -1 && !yourOriginalImagePieces.isEmpty()) {
            String[] pieceIndexes = savedState.split(",");
            puzzlePieces.clear();
            for (String index : pieceIndexes) {
                if (!index.isEmpty()) {
                    int originalIndex = Integer.parseInt(index);
                    puzzlePieces.add(yourOriginalImagePieces.get(originalIndex));
                }
            }
            puzzlePieces.add(null); // Añadir el espacio vacío
            gridView.setAdapter(new PuzzleAdapter(this, puzzlePieces, pieceWidth, pieceHeight));
        }
    }

}
