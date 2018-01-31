package kobi.chess;

/*
 Copyright 2011 by Kobi Krasnoff

 This file is part of Pocket Chess.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import engine.ChessEngine;
import engine.SimpleEngine;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

public class MainActivity extends Activity {
	public static final int ACITIVITY_OPTIONS = 1;
	public static final int ACITIVITY_HELP = 0;
	private BoardView boardView;
	private Point startPoint, endPoint, movePoint;
	private TextView txtStatus;
	
	private ChessEngine engine;
	private int ply = 0;
	private boolean moveEnabled = false;   
	
	
	private MyHandler mMyHandler = new MyHandler();
	
	
	/**
	 * Constructor class
	 */
	public MainActivity()
	{
		engine = new SimpleEngine();
		startPoint = new Point();
		endPoint = new Point();
		movePoint = new Point();
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // sets textView
        txtStatus = (TextView)this.findViewById(R.id.txtStatus);
        
        
        
        // sets view   
        final BoardView boardView = (BoardView)findViewById(R.id.BoardView);
        boardView.setFocusableInTouchMode(true);
        boardView.setFocusable(true);
        boardView.syncParent(this);
        this.boardView = boardView;
        
        this.boardView.displayPieces(engine.getBoard(ply));
        
        boardView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				
				if (engine.isWhiteTurn())
				{
					switch (event.getAction())
					{
					case MotionEvent.ACTION_DOWN:
						startPoint.x = (int)(event.getX() / boardView.getWidth() * 8);
						startPoint.y = (int)(event.getY() / boardView.getHeight() * 8);
						startPoint.fx = event.getX();
						startPoint.fy = event.getY();
						
						char temp = getSelectedPiece(engine.getBoard(ply), startPoint.x, startPoint.y);
						if (temp == 'P' || temp == 'K' || temp == 'Q' || temp == 'R' || temp == 'N' ||  temp == 'B')
							moveEnabled = true;
						else
							moveEnabled = false;
						
						if (moveEnabled)
							boardView.getMovePoint(startPoint, movePoint);
						break;
					case MotionEvent.ACTION_MOVE:
						movePoint.fx = event.getX();
						movePoint.fy = event.getY();
						if (moveEnabled)
							boardView.getMovePoint(startPoint, movePoint);
						break;
					case MotionEvent.ACTION_UP:
						endPoint.x = (int)(event.getX() / boardView.getWidth() * 8);
						endPoint.y = (int)(event.getY() / boardView.getHeight() * 8);
						if (moveEnabled)
						{
							boardView.getUpPoint();
							// TODO start thread from here!!!
							PlayerMove(setMove());
						}
						
						break;
					}
				}
				
				return true;
			}
		
			
        
        });
        
        
    }
    
    private void PlayerMove(String move)
    {
    	
    	char selectedPiece = getSelectedPiece(engine.getBoard(ply), move);
    	
    	if (move.charAt(move.length() - 1) == '8' && selectedPiece == 'P')
    	{
    		showPromotionPieceDialog(move);
    		return;
    	}
    	
    	char[][] myBoard = engine.getBoard(ply);
    	int[] rwm = engine.getBoard(myBoard);
    	
    	if (move.compareTo("e1-g1") == 0 && myBoard[7][4] == 'k')
    	{
    		move = "O-O";
    	}
    	else if ((move.compareTo("e1-b1") == 0 || move.compareTo("e1-c1") == 0) && myBoard[7][4] == 'k')
    	{
    		move = "O-O-O";
    	}
    	
    	
    	String res = engine.makeMove(move);
    	if (res.startsWith("ERROR")) {
    		String res2 = engine.makeMove(move.replace('-', 'x'));
    		if (res2.startsWith("ERROR")) {
	    		if (!(engine.isDraw() || engine.isMate()))
	    			showToastNotification(this.getString(R.string.illegal_move_try_again));
	    		else if (engine.isMate())
	    			showToastNotification(this.getString(R.string.activity_mate));
	    		else if (engine.isDraw())
	    			showToastNotification(this.getString(R.string.activity_draw));
	    	}
    		else
    		{
    			makeComputerMove();
    		}
    		
    	}
    	else 
    	{
    		makeComputerMove();
    	}
    	
    	//return res;
    }
    
    
    
    /**
     * gets my chosen piece name.
     */
    private char getSelectedPiece(char[][] bitBoard, String pgnLocation)
    {
    	int x = 0;
    	int y = 0;
    	
    	char[] myPGN =  pgnLocation.toCharArray();
    	
    	y = 8 - (int)(myPGN[1] - 48);
    	switch (myPGN[0])
    	{
	    	case 'a': x = 0; break;
	    	case 'b': x = 1; break;
	    	case 'c': x = 2; break;
	    	case 'd': x = 3; break;
	    	case 'e': x = 4; break;
	    	case 'f': x = 5; break;
	    	case 'g': x = 6; break;
	    	case 'h': x = 7; break;
    	}
    	
    	return getSelectedPiece(bitBoard, x, y);
    }
    
    
    /**
     * gets my chosen piece name.
     */
    private char getSelectedPiece(char[][] bitBoard, int x, int y)
    {
    	return bitBoard[7-y][x];
    }
    
    
    private void makeComputerMove()
    {
    	// TODO run in thread
    	this.boardView.displayPieces(engine.getBoard(ply));
		//mMyHandler.machineGO();
    	threadMove();
		txtStatus.setText(R.string.activity_thinking);
    	//new ComputerMoveTask().execute();

    }
    
	public void threadMove() {
		new Thread(new Runnable() {
			public void run() {
				boardView.post(new Runnable() {
					public void run() {
						computerMove();
					}
				});
			}
		}).start();

		/*runOnUiThread(new Runnable() {
			public void run() {
				computerMove();
			}
		});*/

	}
	
	private class ComputerMoveTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			threadMove();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			txtStatus.setText(R.string.activity_thinking);
			super.onPreExecute();
		}

		
	}
    
    /**
     * Actually handle the computer move.
     */
    private synchronized void computerMove()
    {
    	//Synchronized
    	String temp = engine.go();
    	this.boardView.displayPieces(engine.getBoard(ply));
    	txtStatus.setText(R.string.activity_yourMove);
    	
    	if (engine.isMate())
			showToastNotification(this.getString(R.string.activity_mate));
    	else if (engine.isCheck())
    		showToastNotification(this.getString(R.string.activity_check));
    	else if (engine.isDraw())
			showToastNotification(this.getString(R.string.activity_draw));
    }
    
    /**
     * returns move in the right engine format
     * @return
     */
    private String setMove()
    {
    	String res = "";
    	
    	res = res + String.valueOf((char)(startPoint.x + 97));
    	res = res + String.valueOf(8 - startPoint.y);
    	res = res + "-";
    	res = res + String.valueOf((char)(endPoint.x + 97));
    	res = res + String.valueOf(8 - endPoint.y);
    	
    	return res;
    }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.startGame:
			
			break;
		/*case R.id.options:
			Log.e("pointerSquare", "MENU_OPTIONS");
			Intent myIntent = new Intent(this.getBaseContext(), OptionsActivity.class);
			//myIntent.putExtra("level", level);
			//myIntent.putExtra("myColor", myColor);
			startActivityForResult(myIntent, ACITIVITY_OPTIONS);
			break;*/
		case R.id.Copyright:
			Intent myIntent3 = new Intent(this.getBaseContext(), CopyrightActivity.class);
			//Intent myIntent3 = new Intent(this.getBaseContext(), GNUActivity.class);
			startActivityForResult(myIntent3, ACITIVITY_HELP);
			break;
		case R.id.help:
			Log.e("pointerSquare", "MENU_HELP");
			Intent myIntent2 = new Intent(this.getBaseContext(), InstructionsActivity.class);
			startActivityForResult(myIntent2, ACITIVITY_HELP);
			break;
		case R.id.about:
			Log.e("pointerSquare", "MENU_ABOUT");
			new AlertDialog.Builder (MainActivity.this)
            .setTitle (R.string.dialog_alert)
            .setMessage (R.string.dialog_message)
            .setIcon(R.drawable.king_white)
            .setPositiveButton (R.string.dialog_ok_button, new DialogInterface.OnClickListener(){
                public void onClick (DialogInterface dialog, int whichButton){
                    setResult (RESULT_OK);
                    
                }
            })
            .show ();  
			break;
		case R.id.exit:
			Log.e("pointerSquare", "MENU_EXIT");
			this.finish();
			break;
		default:
			Log.e("pointerSquare", String.valueOf(item.getItemId()));
			break;
		}
		return super.onOptionsItemSelected(item);
    }
    
    /**
     * Show system alerts
     * @param text
     */
    public void showToastNotification(String text)
    {
    	Context context = this.getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER, 0, 0);
    	toast.show();
    }
    
    public Point getStartPoint()
    {
    	return startPoint;
    }
    
    public Point getEndPoint()
    {
    	return endPoint;
    }
    
    /**
     * shows dialog of promotion tools
     */
    public void showPromotionPieceDialog(String move)
    {
    	
    	final CharSequence[] items = {this.getString(R.string.showPromotionPieceDialog_queen), this.getString(R.string.showPromotionPieceDialog_rook), this.getString(R.string.showPromotionPieceDialog_bishop), this.getString(R.string.showPromotionPieceDialog_knight)};
    	
    	final String move2 = move;
    	
    	new AlertDialog.Builder (MainActivity.this)
    	.setTitle(this.getString(R.string.showPromotionPieceDialog_title))
    	.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        if (item == 0)
    	    	{
    	    		PlayerMove(move2 + "Q");
    	    	}
    	    	else if (item == 1)
    	    	{
    	    		PlayerMove(move2 + "R");
    	    	}
    	    	else if (item == 2)
    	    	{
    	    		PlayerMove(move2 + "B");
    	    	}
    	    	else if (item == 3)
    	    	{
    	    		PlayerMove(move2 + "N");
    	    	}
    	    }
    	})
    	.show();
    	
    	
    }
	
	
    
    class MyHandler extends Handler 
    {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			computerMove();
		}
		
		public void machineGO()
		{
			this.removeMessages(0);
			//this.sendMessage(obtainMessage(0));
			this.sendMessageDelayed(obtainMessage(0), 1);
		}
    	
    };
}