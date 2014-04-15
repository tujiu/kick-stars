package com.example.stars;

import com.example.stars.GameBoard.OnInfoListener;
import com.example.stars.GameBoard.OnRecordListener;
import com.example.stars.GameBoard.OnScoreListener;
import com.example.stars.GameBoard.OnTargetListener;
import com.example.stars.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView score;
	private TextView info;
	private TextView target;
	private TextView record;
	private Button restartBtn;
	private GameBoard gameBoard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		score = (TextView) findViewById(R.id.score);
		info = (TextView) findViewById(R.id.info);
		target = (TextView) findViewById(R.id.target);
		record = (TextView) findViewById(R.id.record);
		gameBoard = (GameBoard) findViewById(R.id.gameBoard);
		restartBtn = (Button) findViewById(R.id.button);
		setData();
		
		gameBoard.setInfoListener(new OnInfoListener() {
			
			@Override
			public void onInfoChanged(View v, String info) {
				MainActivity.this.info.setText(info);
			}
		});
		gameBoard.setScoreListener(new OnScoreListener() {
			
			@Override
			public void onScoreChanged(View v, String score) {
				MainActivity.this.score.setText(score);
			}
		});
		gameBoard.setTargetListener(new OnTargetListener() {
			
			@Override
			public void onTargetChanged(View v, String target) {
				MainActivity.this.target.setText(target);
			}
		});
		gameBoard.setRecordListener(new OnRecordListener() {
			
			@Override
			public void onRecordChanged(View v, String record) {
				MainActivity.this.record.setText(record);
			}
		});
		restartBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gameBoard.restart();
				setData();
			}
		});
	}

	@Override
	protected void onPause() {
		gameBoard.saveGame();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void setData() {
		target.setText("第" + gameBoard.getTurn() + "关 目标分数:" + gameBoard.getTarget());
		score.setText("你的得分: " + gameBoard.getScore());
		record.setText("记录: " + gameBoard.getRecord());
		info.setText("");
	}
}
