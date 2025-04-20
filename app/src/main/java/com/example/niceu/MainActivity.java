package com.example.niceu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import java.lang.*;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    //bluetooth variables
    ImageButton startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (ImageButton) findViewById(R.id.imageButton);

        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, connectingBTDevices.class);
            startActivity(intent);
        });
    }
}