package com.example.lab6_iot_29106044;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnStfPuzzleSimplified, btnStfMemoryClassic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        btnStfPuzzleSimplified = findViewById(R.id.btnStfPuzzleSimplified);
        btnStfMemoryClassic = findViewById(R.id.btnStfMemoryClassic);

        // Set onClickListener for Stf-Puzzle Simplified button
        btnStfPuzzleSimplified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Stf-Puzzle Simplified Activity
                Intent intent = new Intent(MainActivity.this, StfPuzzleActivity.class);
                startActivity(intent);
            }
        });

        // Set onClickListener for Stf-Memory Classic button
        btnStfMemoryClassic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Stf-Memory Classic Activity
                Intent intent = new Intent(MainActivity.this, StfMemoryClassicActivity.class);
                startActivity(intent);
            }
        });
    }
}
