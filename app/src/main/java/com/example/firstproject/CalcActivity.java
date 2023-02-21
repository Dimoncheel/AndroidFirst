package com.example.firstproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory ;
    private TextView tvResult  ;
    private String minusSign ;
    private boolean needClearResult ;
    private boolean needClearHistory ;
    private double operand1 ;
    private String operation ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvHistory = findViewById( R.id.tvHistory ) ;
        tvResult  = findViewById( R.id.tvResult  ) ;
        tvHistory.setText( "" ) ;
        tvResult.setText( "0" ) ;
        minusSign = getApplicationContext().getString( R.string.calc_minus_sign );
        for( int i = 0; i < 10; i++ ) {
            findViewById(
                    getResources().getIdentifier(
                            "button_digit_" + i,
                            "id",
                            getPackageName()
                    ) ).setOnClickListener( this::digitClick ) ;
        }
        findViewById(R.id.button_comma).setOnClickListener(this::digitClick);
        findViewById(R.id.button_squared).setOnClickListener(this::btnSquaredClick);
        findViewById( R.id.button_plus_minus ).setOnClickListener( this::pmClick ) ;
        findViewById( R.id.button_backspace ).setOnClickListener( this::backspaceClick ) ;
        findViewById( R.id.button_inverse ).setOnClickListener( this::inverseClick ) ;
        findViewById( R.id.button_clear_e ).setOnClickListener( this::clearEntryClick ) ;
        findViewById( R.id.button_clear_all ).setOnClickListener( this::clearAllClick ) ;
        findViewById( R.id.button_plus ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_minus ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_multiply ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_divide ).setOnClickListener( this::fnButtonClick ) ;
        findViewById( R.id.button_equal ).setOnClickListener( this::equalClick ) ;
        findViewById(R.id.button_sqrt).setOnClickListener(this::btnSqrtClick);
    }
    @Override
    protected void onSaveInstanceState( @NonNull Bundle outState ) {
        super.onSaveInstanceState( outState ) ;
        outState.putCharSequence( "history", tvHistory.getText() ) ;
        outState.putCharSequence( "result",  tvResult.getText()  ) ;
        Log.d( CalcActivity.class.getName(), "Данные сохранены" ) ;
    }
    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState ) ;
        tvHistory.setText( savedInstanceState.getCharSequence( "history" ) ) ;
        tvResult.setText(  savedInstanceState.getCharSequence( "result"  ) ) ;
        Log.d( CalcActivity.class.getName(), "Данные восстановлены" ) ;
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
        if( needClearHistory ) {
            tvHistory.setText( "" ) ;
            needClearHistory = false ;
        }
        if( needClearResult ) {
            needClearResult = false ;
        }
        String result = tvResult.getText().toString() ;
        int len = result.length() ;
        result = result.substring( 0, len - 1 ) ;
        if( result.equals( minusSign ) || len <= 1 ) {
            result = "0" ;
        }
        tvResult.setText( result ) ;
    }
    private  void btnSqrtClick(View v){
        String result = tvResult.getText().toString() ;
        String history = String.format( "%s %s","\u221A",result) ;
        tvHistory.setText( history ) ;
        operation = "\u221A" ;
        operand1 = parseResult(result) ;
    }
    private  void btnSquaredClick(View v){
        String result = tvResult.getText().toString() ;
        String history = String.format( "sqr(%s)",result) ;
        tvHistory.setText( history ) ;
        operation = "sqr" ;
        operand1 = parseResult(result) ;
    }

    private void digitClick( View v ) {
        String result = tvResult.getText().toString() ;
        if( needClearResult ) {
            needClearResult = false ;
            result = "0" ;
        }
        if( result.length() >= 10 ) return ;
        String digit = ((Button) v).getText().toString() ;
        if( result.equals( "0" ) ) {
            result = digit ;
        }
        else {
            result += digit ;
        }
        tvResult.setText( result ) ;
        if( needClearHistory ) {
            tvHistory.setText( "" ) ;
            needClearHistory = false ;
        }
    }
    private void inverseClick( View v ) {
        String result = tvResult.getText().toString() ;
        double arg = parseResult( result ) ;
        if( arg == 0 ) {
            alert( R.string.calc_divide_by_zero ) ;
            return ;
        }
        tvHistory.setText( String.format( "1/(%s) =", result ) ) ;
        showResult( 1 / arg ) ;
    }
    private double parseResult( String result ) {
        return Double.parseDouble( result.replace( minusSign, "-" ).replace(",",".") ) ;
    }
    private void showResult( double arg ) {
        String result = String.valueOf( arg ) ;
        int finLength = 10 ;
        if( result.startsWith( "-" ) )
            finLength++ ;
        if( result.contains( "." ) )
            finLength++ ;
        if( result.length() >= finLength ) {
            result = result.substring( 0, finLength ) ;
        }
        tvResult.setText( result.replace( "-", minusSign ) ) ;
    }
    private void alert( int stringId ) {
        Toast
                .makeText(
                        CalcActivity.this,
                        stringId,
                        Toast.LENGTH_SHORT
                )
                .show() ;

        Vibrator vibrator ;
        long[] vibratePattern = { 0, 200, 100, 200 } ;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
            VibratorManager vibratorManager = (VibratorManager)
                    getSystemService( Context.VIBRATOR_MANAGER_SERVICE ) ;
            vibrator = vibratorManager.getDefaultVibrator() ;
        }
        else {
            vibrator = (Vibrator) getSystemService(
                    Context.VIBRATOR_SERVICE ) ;
        }

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            vibrator.vibrate(
                    VibrationEffect.createWaveform( vibratePattern, -1 )
            ) ;
        }
        else {
            vibrator.vibrate( vibratePattern, -1 ) ;
        }
    }
    private void clearEntryClick( View v ) {   // CE button
        tvResult.setText( "0" ) ;
    }
    private void clearAllClick( View v ) {    // C button
        tvHistory.setText( "" ) ;
        tvResult.setText( "0" ) ;
    }
    private void fnButtonClick( View v ) {
        String fn = ((Button) v).getText().toString() ;
        String result = tvResult.getText().toString() ;
        String history = String.format( "%s %s", result, fn ) ;
        tvHistory.setText( history ) ;
        needClearResult = true ;
        operation = fn ;
        operand1 = parseResult( result ) ;
    }
    private void equalClick( View v ) {
        if(operation.equals("\u221a")){
            if(operand1>=0){
                showResult(Math.sqrt(operand1));
                needClearResult = true ;
                needClearHistory = true ;
            }
            else{
                Toast.makeText(this,"Negative number",Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if(operation.equals("sqr")){
            showResult(Math.pow(operand1,2));
            needClearResult = true ;
            needClearHistory = true ;
            return;
        }
        String result  = tvResult.getText().toString() ;
        String history = tvHistory.getText().toString() ;
        tvHistory.setText( String.format( "%s %s =", history, result ) ) ;
        double operand2 = parseResult( result ) ;
        if( operation.equals( getString( R.string.btn_calc_plus ) ) ) {
            showResult( operand1 + operand2 ) ;
        }
        if(operation.equals(getString(R.string.btn_calc_minus))){
            showResult(operand1-operand2);
        }
        if(operation.equals(getString(R.string.btn_calc_multiplication))){
            showResult(operand1*operand2);
        }
        if(operation.equals(getString(R.string.btn_calc_divide)))
        {
            if(operand2!=0){
                showResult(operand1/operand2);
            }
            else{
                Toast.makeText(this,"Divide by zero",Toast.LENGTH_SHORT).show();
            }
        }


        needClearResult = true ;
        needClearHistory = true ;
    }
}