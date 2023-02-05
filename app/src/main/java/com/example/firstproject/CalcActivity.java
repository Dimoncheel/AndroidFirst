package com.example.firstproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory ;
    private TextView tvResult  ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvHistory = findViewById( R.id.tvHistory ) ;
        tvResult  = findViewById( R.id.tvResult  ) ;
        tvHistory.setText( "" ) ;
        tvResult.setText( "0" ) ;

        for( int i = 0; i < 10; i++ ) {
            findViewById(
                    getResources().getIdentifier(
                            "button_digit_" + i,
                            "id",
                            getPackageName()
                    ) ).setOnClickListener( this::digitClick ) ;
        }
        findViewById( R.id.button_plus_minus ).setOnClickListener( this::pmClick ) ;
        findViewById( R.id.button_backspace ).setOnClickListener( this::backspaceClick ) ;
    }
    private void pmClick( View v ) {
        String result = tvResult.getText().toString() ;
        if( result.startsWith( "-" ) ) {
            result = result.substring(1 ) ;
        }
        else {
            result = "-" + result ;
        }
        tvResult.setText( result ) ;
    }

    private void backspaceClick(View v){
        String line=tvResult.getText().toString();
        if (line == null || line.length() == 0) {
            return;
        }
        String result=line.substring(0, line.length() - 1);
        tvResult.setText(result);
    }
    private void digitClick( View v ) {
        String result = tvResult.getText().toString() ;
        if( result.length() >= 10 ) return ;

        String digit = ((Button) v).getText().toString() ;

        if( result.equals( "0" ) ) {
            result = digit ;
        }
        else {
            result += digit ;
        }
        tvResult.setText( result ) ;
    }
}