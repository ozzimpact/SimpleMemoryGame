package com.example.ozi.hw4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class Manager extends Activity {
    private static int ROW_COUNT = -1;
    private static int COL_COUNT = -1;
    private Context context;
    private Drawable backImage;
    private int [] [] cards;
    private List<Drawable> images;
    private Card firstCard;
    private Card secondCard;
    private ButtonListener buttonListener;
    private int player = 0;
    private EditText player1Edittext;
    private EditText player2Edittext;
    private Button submitBtn;
    private TextView player1Textview;
    private TextView player2Textview;
    private static Object lock = new Object();
    private int player1Score;
    private int player2Score;
    private String player1;
    private String player2;
    private Intent winner;
    private int[] imageArray= {
            R.drawable.a,
            R.drawable.b,
            R.drawable.c,
            R.drawable.d,
            R.drawable.e,
            R.drawable.f,
            R.drawable.g,
            R.drawable.h,
            R.drawable.i,
            R.drawable.j};


    int turns;
    private TableLayout mainTable;
    private UpdateCardsHandler handler;

    public Manager() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        handler = new UpdateCardsHandler();
        loadImages();
        setContentView(R.layout.mainscreen);


        player1Edittext = (EditText)findViewById(R.id.editText);
        player2Edittext = (EditText)findViewById(R.id.editText2);
        submitBtn = (Button)findViewById(R.id.button);

        submitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                if(!player1Edittext.getText().toString().equals("") && !player2Edittext.getText().toString().equals("")) {

                    setContentView(R.layout.main);
                    backImage = getResources().getDrawable(R.drawable.icon);
                    buttonListener = new ButtonListener();
                    mainTable = (TableLayout) findViewById(R.id.TableLayout03);
                    context = mainTable.getContext();
                    newGame();
                }
            }
        });



    }

    private void newGame() {
        ROW_COUNT = 5;
        COL_COUNT = 4;
        player1Textview = (TextView)findViewById(R.id.player1Edittext);
        player2Textview = (TextView)findViewById(R.id.player2Edittext);
        player1 = player1Edittext.getText().toString();
        player2 = player2Edittext.getText().toString();

        player1Textview.setText(player1 + " : " + player1Score);
        player2Textview.setText(player2 + " : " + player2Score);
        cards = new int [COL_COUNT] [ROW_COUNT];

        TableRow tr = ((TableRow)findViewById(R.id.TableRow03));
        tr.removeAllViews();

        mainTable = new TableLayout(context);
        tr.addView(mainTable);

        for (int y = 0; y < ROW_COUNT; y++) {
            mainTable.addView(createRow(y));
        }

        firstCard=null;
        loadCards();

        turns=0;
        ((TextView)findViewById(R.id.tv1)).setText("Tries: "+turns);


    }

    private void loadImages() {
        images = new ArrayList<Drawable>();

        for(int index=0;index<imageArray.length;index++)
            images.add(getResources().getDrawable(imageArray[index]));


    }

    private void loadCards(){
        try{
            int size = ROW_COUNT*COL_COUNT;

            ArrayList<Integer> list = new ArrayList<Integer>();

            for(int i=0;i<size;i++){
                list.add(new Integer(i));
            }


            Random r = new Random();

            for(int i=size-1;i>=0;i--){
                int t=0;

                if(i>0){
                    t = r.nextInt(i);
                }

                t=list.remove(t).intValue();
                cards[i%COL_COUNT][i/COL_COUNT]=t%(size/2);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private TableRow createRow(int y){
        TableRow row = new TableRow(context);
        row.setHorizontalGravity(Gravity.CENTER);

        for (int x = 0; x < COL_COUNT; x++) {
            row.addView(createImageButton(x,y));
        }
        return row;
    }

    private View createImageButton(int x, int y){
        Button button = new Button(context);
        button.setBackgroundDrawable(backImage);
        button.setId(100*x+y);
        button.setOnClickListener(buttonListener);
        return button;
    }

    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if(player == 0)
                Toast.makeText(getApplicationContext(),player1+ " makes the move.",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(),player2+ " makes the move.",Toast.LENGTH_SHORT).show();

            synchronized (lock) {
                if(firstCard!=null && secondCard != null){
                    return;
                }

                int id = v.getId();
                int x = id/100;
                int y = id%100;
                turnCard((Button)v,x,y);
            }

        }

        private void turnCard(Button button,int x, int y) {
            button.setBackgroundDrawable(images.get(cards[x][y]));

            if(firstCard==null){
                firstCard = new Card(button,x,y);
            }
            else{
                if(firstCard.x == x && firstCard.y == y){
                    return; //the user pressed the same card
                }

                secondCard = new Card(button,x,y);

                turns++;
                ((TextView)findViewById(R.id.tv1)).setText("Tries: "+turns);


                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        try{
                            synchronized (lock) {
                                handler.sendEmptyMessage(0);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                Timer t = new Timer(false);
                t.schedule(tt, 1300);
            }


        }

    }

    class UpdateCardsHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                checkCards();
            }
        }
        public void checkCards(){


            if(cards[secondCard.x][secondCard.y] == cards[firstCard.x][firstCard.y]){
                firstCard.button.setVisibility(View.INVISIBLE);
                secondCard.button.setVisibility(View.INVISIBLE);
                switch (player){
                    case 0:{
                        player1Score++;
                        player1Textview.setText(player1Edittext.getText().toString() + " : " + player1Score);
                        player=1;

                    }
                    break;
                    case 1:{
                        player2Score++;
                        player2Textview.setText(player2Edittext.getText().toString() + " : " + player2Score);
                        player=0;
                    }
                    break;
                }

                if(player1Score>5 || player2Score>5 || (player1Score==5 && player2Score==5) ) {
                    setContentView(R.layout.winner);
                    TextView winner = (TextView)findViewById(R.id.winnerTextview);
                    String matchWinner = player1;
                    if(player1Score<player2Score) {
                        matchWinner = player2;
                        winner.setText("Winner: " + matchWinner);
                    }
                    else if(player1Score==5 && player2Score==5){
                        winner.setText("Draw!");

                }
                }

            }
            else {
                secondCard.button.setBackgroundDrawable(backImage);
                firstCard.button.setBackgroundDrawable(backImage);
            }

            if(player==0)
                player = 1;
            else
                player = 0;

            firstCard=null;
            secondCard =null;
        }
    }




}