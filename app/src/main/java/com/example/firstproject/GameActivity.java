package com.example.firstproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int[][] cells = new int[4][4] ;
    private final TextView[][] tvCells = new TextView[4][4] ;
    private final Random random = new Random() ;
    private Animation spawnCellAnimation ;
    private int score ;
    private TextView tvScore ;
    private int bestScore ;
    private TextView tvBestScore ;
    private final String bestScoreFilename = "best_score_192.txt" ;
    private boolean isContinuePlaying ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById( R.id.tv_score ) ;
        bestScore = loadBestScore() ;
        tvBestScore = findViewById( R.id.tv_best_score ) ;
        tvBestScore.setText( getString( R.string.tv_best_score_pattern, bestScore ) ) ;


        spawnCellAnimation = AnimationUtils.loadAnimation(
                GameActivity.this,
                R.anim.spawn_cell
        ) ;

        spawnCellAnimation.reset() ;


        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                tvCells[i][j] = findViewById(
                        getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        ) ) ;
            }
        }


        findViewById( R.id.game_layout )
                .setOnTouchListener( new OnSwipeListener( GameActivity.this ) {
                    @Override
                    public void onSwipeLeft() {
                        if( moveLeft() ) spawnCell() ;
                        else Toast.makeText(GameActivity.this, "No Left Move", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeRight() {
                        if( moveRight() ) spawnCell() ;
                        else Toast.makeText(GameActivity.this, "No Right Move", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeTop() {
                        Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeBottom() {
                        Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
                    }
                } ) ;
        findViewById( R.id.game_start_new )
                .setOnClickListener( this::newGameClick ) ;

        startNewGame() ;
    }

    private void newGameClick( View v ) {

    }

    private boolean saveBestScore() {

        try( FileOutputStream fos = openFileOutput( bestScoreFilename, Context.MODE_PRIVATE ) ) {
            DataOutputStream writer = new DataOutputStream( fos ) ;
            writer.writeInt( bestScore ) ;
            writer.flush() ;
            writer.close() ;
        }
        catch( IOException ex ) {
            Log.d( "saveBestScore", ex.getMessage() ) ;
            return false ;
        }
        return true ;
    }
    private int loadBestScore() {
        int best = 0 ;
        try( FileInputStream fis = openFileInput( bestScoreFilename ) ) {
            DataInputStream reader = new DataInputStream( fis ) ;
            best = reader.readInt() ;
            reader.close() ;
        }
        catch( IOException ex ) {
            Log.d( "loadBestScore", ex.getMessage() ) ;
        }
        return best ;
    }

    private boolean isWin() {
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                if( cells[i][j] == 8 ) {
                    return true ;
                }
            }
        }
        return false ;
    }
    private void showWinDialog() {
        new AlertDialog.Builder(
                GameActivity.this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert )
                .setTitle( R.string.game_victory_title )
                .setIcon( android.R.drawable.ic_dialog_info )
                .setMessage( "Ви зібрали 2048 та виграли!")
                .setCancelable( false )  // неможна закрити без вибору дії
                .setPositiveButton("Продовжити", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int whichButton ) {
                        isContinuePlaying = true ;
                    } } )
                .setNegativeButton( "Вихід", (dialog, whichButton) -> {
                    finish() ;
                } )
                .setNeutralButton( "Нова гра", (dialog, whichButton) -> {
                    startNewGame() ;
                } )
                .show() ;
    }

    private void startNewGame() {
        score = 0 ;
        isContinuePlaying = false ;

        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                cells[i][j] = 0 ;
            }
        }

        spawnCell() ;
        spawnCell() ;
    }


    private void showField() {
        Resources resources = getResources() ;
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].setTextAppearance(
                        resources.getIdentifier(
                                "GameCell_" + cells[i][j],
                                "style",
                                getPackageName()
                        ) ) ;


                tvCells[i][j].setBackgroundColor(
                        resources.getColor(
                                resources.getIdentifier(
                                        "game_bg_" + cells[i][j],
                                        "color",
                                        getPackageName()
                                ),
                                getTheme() ) ) ;
            }
        }

        tvScore.setText( getString( R.string.tv_score_pattern, score ) ) ;

        if( score > bestScore ) {
            bestScore = score ;
            if( saveBestScore() ) {
                tvBestScore.setText( getString( R.string.tv_best_score_pattern, bestScore ) ) ;
            }
        }

        if( ! isContinuePlaying ) {
            if( isWin() ) {
                showWinDialog() ;
            }
        }
    }


    private boolean spawnCell() {

        List<Integer> freeCellIndexes = new ArrayList<>() ;
        for( int i = 0; i < 4; ++i ) {
            for( int j = 0; j < 4; ++j ) {
                if( cells[i][j] == 0 ) {
                    freeCellIndexes.add( i * 10 + j ) ;
                }

            }
        }

        int cnt = freeCellIndexes.size() ;
        if( cnt == 0 ) return false ;

        int randIndex = random.nextInt( cnt ) ;

        int x = freeCellIndexes.get( randIndex ) / 10 ;
        int y = freeCellIndexes.get( randIndex ) % 10 ;

        cells[x][y] = random.nextInt(10) < 9 ? 2 : 4 ;

        tvCells[x][y].startAnimation( spawnCellAnimation ) ;

        showField() ;
        return true ;
    }

    private boolean moveLeft() {
        boolean result = false ;
        boolean needRepeat ;

        for( int i = 0; i < 4; ++i ) {
            do {
                needRepeat = false ;
                for( int j = 0; j < 3; ++j ) {
                    if( cells[i][j] == 0 ) {
                        for( int k = j + 1; k < 4; ++k ) {
                            if( cells[i][k] != 0 ) {
                                cells[i][j] = cells[i][k] ;
                                cells[i][k] = 0 ;
                                needRepeat = true ;
                                result = true ;
                                break ;
                            }
                        }
                    }
                }
            } while( needRepeat ) ;


            for( int j = 0; j < 3; ++j ) {
                if( cells[i][j] != 0 && cells[i][j] == cells[i][j + 1] ) {
                    cells[i][j] *= 2 ;
                    for( int k = j + 1; k < 3; ++k ) {
                        cells[i][k] = cells[i][k + 1] ;
                    }
                    cells[i][3] = 0 ;
                    result = true ;
                    score += cells[i][j] ;
                }
            }
        }
        return result ;
    }
    private boolean moveRight() {
        boolean result = false ;
        boolean needRepeat ;

        for( int i = 0; i < 4; ++i ) {
            do {
                needRepeat = false ;
                for( int j = 3; j > 0; --j ) {
                    if( cells[i][j] == 0 ) {
                        for( int k = j - 1; k >= 0; --k ) {
                            if( cells[i][k] != 0 ) {
                                cells[i][j] = cells[i][k] ;
                                cells[i][k] = 0 ;
                                needRepeat = true ;
                                result = true ;
                                break ;
                            }
                        }
                    }
                }
            } while( needRepeat ) ;


            for( int j = 3; j > 0; --j ) {
                if( cells[i][j] != 0 && cells[i][j] == cells[i][j - 1] ) {
                    cells[i][j] *= 2 ;
                    for( int k = j - 1; k > 0; --k ) {
                        cells[i][k] = cells[i][k - 1] ;
                    }
                    cells[i][0] = 0 ;
                    result = true ;
                    score += cells[i][j] ;
                }
            }
        }
        return result ;
    }
}