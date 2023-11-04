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
    private ArrayList<Bitmap> puzzlePieces = new ArrayList<>();
    private ArrayList<Bitmap> yourOriginalImagePieces = new ArrayList<>();
    private Button btnStartGame;
    private int gridSize = 3;
    private int emptySpaceIndex=-1;
    private boolean gameStarted = false;
    private int pieceWidth, pieceHeight;

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
            try {
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

        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        gridView.setNumColumns(gridSize);
        puzzlePieces = new ArrayList<>();
        yourOriginalImagePieces = new ArrayList<>();
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
                    emptySpaceIndex = position;
                    ((BaseAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    checkForWin();
                }
            }
        });
    }


    private void createPuzzlePieces(Bitmap image) {
        int piecesNumber = gridSize * gridSize;
        pieceWidth = image.getWidth() / gridSize;
        pieceHeight = image.getHeight() / gridSize;

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

        Bitmap lastPiece = puzzlePieces.remove(piecesNumber - 1);
        puzzlePieces.add(null);

        yourOriginalImagePieces.addAll(puzzlePieces);
        yourOriginalImagePieces.add(lastPiece);

        gridView.setAdapter(new PuzzleAdapter(this, puzzlePieces, pieceWidth, pieceHeight));
    }



    private void shufflePuzzle() {
        if (puzzlePieces.size() == 0) return;
        Random random = new Random();

        emptySpaceIndex = puzzlePieces.size() - 1;

        int shuffleSteps = gridSize * gridSize * 2;

        for (int step = 0; step < shuffleSteps; step++) {
            List<Integer> validMoves = new ArrayList<>();

            if (emptySpaceIndex - gridSize >= 0) validMoves.add(emptySpaceIndex - gridSize);
            if (emptySpaceIndex + gridSize < puzzlePieces.size()) validMoves.add(emptySpaceIndex + gridSize);
            if (emptySpaceIndex % gridSize > 0) validMoves.add(emptySpaceIndex - 1);
            if (emptySpaceIndex % gridSize < gridSize - 1) validMoves.add(emptySpaceIndex + 1);

            int toSwapWithIndex = validMoves.get(random.nextInt(validMoves.size()));
            Collections.swap(puzzlePieces, emptySpaceIndex, toSwapWithIndex);
            emptySpaceIndex = toSwapWithIndex;
        }

        gridView.invalidateViews();

        saveGameState();
    }

    private void swap(int indexOne, int indexTwo) {
        Collections.swap(puzzlePieces, indexOne, indexTwo);
    }



    private void resetGame() {
        gridSize = 3;
        gridView.setNumColumns(gridSize);

        puzzlePieces.clear();
        puzzlePieces.addAll(yourOriginalImagePieces.subList(0, gridSize * gridSize));
        emptySpaceIndex = gridSize * gridSize - 1;
        puzzlePieces.set(emptySpaceIndex, null);

        gridView.setAdapter(new PuzzleAdapter(this, puzzlePieces, pieceWidth, pieceHeight));

        gameStarted = false;
        btnStartGame.setText("Start Game");
        saveGameState();
    }



    private boolean isAdjacentToEmptySpace(int position) {

        int emptyRow = emptySpaceIndex / gridSize;
        int emptyCol = emptySpaceIndex % gridSize;

        int clickedRow = position / gridSize;
        int clickedCol = position % gridSize;

        return (emptyRow == clickedRow && Math.abs(emptyCol - clickedCol) == 1) ||
                (emptyCol == clickedCol && Math.abs(emptyRow - clickedRow) == 1);
    }

    private void checkForWin() {
        for (int i = 0; i < puzzlePieces.size() - 1; i++) {

            if (puzzlePieces.get(i) != yourOriginalImagePieces.get(i)) {
                return;
            }
        }

        Toast.makeText(this, "Se culminó el juego", Toast.LENGTH_SHORT).show();

    }

    private void saveGameState() {
        if (puzzlePieces.isEmpty() || yourOriginalImagePieces.isEmpty()) return;

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
