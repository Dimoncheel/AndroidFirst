package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton=findViewById(R.id.calcButton);
        addButton.setOnClickListener(this::btnCalcClick);

        findViewById(R.id.exitButton).setOnClickListener(this::btnExitClick);
    }
    private void btnCalcClick(View v){
        Intent calcIntent=new Intent(MainActivity.this,CalcActivity.class);
        startActivity(calcIntent);
    }
    private void btnExitClick(View v){
        finish();
    }
}