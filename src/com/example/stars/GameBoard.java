package com.example.stars;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.EncodingUtils;

import com.example.stars.R;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.CycleInterpolator;

public class GameBoard extends View {

	public final int ROW = 10;
	private final String GAME_TAG = "STAR";
	private final String GAME_PREF = "STAR_PREF";
	private final String GAME_TURN = "STAR_TURN";
	private final String GAME_SCORE = "STAR_SCORE";
	private final String GAME_RECORD = "STAR_RECORD";
	private final String DATA_FILE = "starData.txt";
	
	private int row;
	
	private Canvas canvas;
	private Paint[] paints;
	private Paint[] selectedPaints;
	private int screenWidth;
	private int starSize;
	private Bitmap bitmap;
	
	private int[][] stars;
	private int[][] selectedStars;
	private OnInfoListener infoListener;
	private OnScoreListener scoreListener;
	private OnTargetListener targetListener;
	private OnRecordListener recordListener;
	
	private int score;
	private int target;
	private int record;

	private int turn;
	private int selectedCount;
	private int selectedScore;
	
	private Context context;
	
	private int myAlpha;
	private Paint tempPaint;
	
	public interface OnInfoListener {
        void onInfoChanged(View v, String info);
    }
	
	public void setInfoListener(OnInfoListener infoListener) {
		this.infoListener = infoListener;
	}
	
	public interface OnScoreListener {
		void onScoreChanged(View v, String score);
	}
	
	public void setScoreListener(OnScoreListener scoreListener) {
		this.scoreListener = scoreListener;
	}
	
	public interface OnTargetListener {
		void onTargetChanged(View v, String target);
	}
	
	public void setTargetListener(OnTargetListener targetListener) {
		this.targetListener = targetListener;
	}
	
	public interface OnRecordListener {
		void onRecordChanged(View v, String record);
	}
	
	public void setRecordListener(OnRecordListener recordListener) {
		this.recordListener = recordListener;
	}
	
	public int getScore() {
		return score;
	}

	public int getTarget() {
		return target;
	}
	
	public int getTurn() {
		return turn;
	}
	
	public int getRecord() {
		return record;
	}
	
	public GameBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		
		screenWidth = ScreenHelper.getScreenWidth((Activity) context);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.gameBoard);
		row = a.getInt(R.styleable.gameBoard_row, ROW);
		a.recycle();
		
		initGameBoard();
		initGameLogic();
		
		createAnimation();
		
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						touchStar(event.getX(), event.getY());
						break;
					default:
						break;
				}
				return false;
			}
		});
	}

	private void initGameLogic() {
		getGameStatus();
		if (score == 0 || !getGameData()) {
			stars = Star.createStars(row);
			score = 0;
			turn = 1;
		}
		
		selectedStars = Star.createEmptyStars(row);
		target = Star.getTarget(turn);
	}

	private boolean getGameData() {
		try {
			File file = new File(context.getFilesDir() + DATA_FILE);    
			FileInputStream fis = new FileInputStream(file);
	        byte[] buffer = new byte[ROW * ROW + 1];
	        fis.read(buffer);
	        String data = EncodingUtils.getString(buffer, "UTF-8");
	        stars = Star.setDataString(data, row);
			fis.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void saveGameData() {
		try {
			File file = new File(context.getFilesDir() + DATA_FILE);
	        FileOutputStream fos = new FileOutputStream(file);
	        String data = getDataString();
	        byte[] buffer = EncodingUtils.getBytes(data, "UTF-8");
			fos.write(buffer);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getDataString() {
		String line = "";
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				line += stars[i][j];
			}
		}
		return line;
	}

	private void getGameStatus() {
		SharedPreferences  pref = context.getSharedPreferences(GAME_PREF, Context.MODE_PRIVATE);
		turn = pref.getInt(GAME_TURN, 1);
		score = pref.getInt(GAME_SCORE, 0);
		record = pref.getInt(GAME_RECORD, 0);
	}
	
	private void saveGameStatus() {
		SharedPreferences  pref = context.getSharedPreferences(GAME_PREF, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(GAME_TURN, turn);
		editor.putInt(GAME_SCORE, score);
		editor.putInt(GAME_RECORD, record);
		editor.commit();
	}

	private void touchStar(float x, float y) {
		int touchedRow = (int) (y / starSize);
		int touchedColumn = (int) (x / starSize);
		
		if (touchedColumn < row && touchedRow < row && stars[touchedRow][touchedColumn] != Star.NOSTAR) {
			if (selectedStars[touchedRow][touchedColumn] == Star.SELECTED) {
				if (selectedCount > 1) {
					kickStar(touchedRow, touchedColumn);
					if (checkOver()) {
						setLeftScore();
						turnOver();
					} else {
						printStars();
						clearSelected();
						score += selectedScore;
					}
					scoreListener.onScoreChanged(this, "你的得分:" + score);
					if (score > record) {
						record = score;
						recordListener.onRecordChanged(this, "新纪录:" + record);
					}
					invalidate();
				}
			} else {
				clearSelected();
				select(touchedRow, touchedColumn);
				invalidate();
				selectedScore = Star.getScore(selectedCount);
				infoListener.onInfoChanged(this, "连击" + selectedCount + "个 "
				+ "得分:" + selectedScore);
			}
		}
	}

	private void setLeftScore() {
		int leftCount = getLeftCount();
		int leftScore = Star.getLeftSocre(leftCount);
		score += leftScore;
		infoListener.onInfoChanged(this, "剩余" + leftCount + "个 "
				+ "奖励:" + leftScore + "分");
	}

	private int getLeftCount() {
		int left = 0;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				if (stars[i][j] != Star.NOSTAR) {
					left++;
				}
			}
		}
		return left;
	}

	private void turnOver() {
		if (score > target) {
			nextTurn();
		} else {
			infoListener.onInfoChanged(this, "分数不够，游戏结束");
			saveGameStatus();
		}
	}

	private void nextTurn() {
		turn++;
		stars = Star.createStars(row);
		selectedStars = Star.createEmptyStars(row);
		target = Star.getTarget(turn);
		targetListener.onTargetChanged(this, "第" + getTurn() + "关 目标分数:" + getTarget());
	}
	
	public void restart() {
		turn = 1;
		score = 0;
		stars = Star.createStars(row);
		selectedStars = Star.createEmptyStars(row);
		target = Star.getTarget(turn);
		targetListener.onTargetChanged(this, "第" + getTurn() + "关 目标分数:" + getTarget());
		invalidate();
	}

	private void kickStar(int touchedRow, int touchedColumn) {
		starAnimation();
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				if (selectedStars[i][j] == Star.SELECTED) {
					
					stars[i][j] = Star.NOSTAR;
					
					// star is gone, move down
					for (int k = i - 1; k >= 0; k--) {
						if (stars[k][j] == Star.NOSTAR) {
							break;
						}
						stars[k+1][j] = stars[k][j];
						stars[k][j] = Star.NOSTAR;
					}
				}
			}
		}
		
		// all column is gone, move left
		for (int j = row - 2; j >= 0; j--) {
			if (stars[row-1][j] == Star.NOSTAR) {
				if (stars[row-1][j+1] == Star.NOSTAR) {
					continue;
				}
				for (int k = j + 1; k < row; k++) {
					for (int i = 0; i < row; i++) {
						stars[i][k-1] = stars[i][k];
						stars[i][k] = Star.NOSTAR;
					}
				}
			}
		}
	}

	private boolean checkOver() {
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				clearSelected();
				if (stars[i][j] != Star.NOSTAR) {
					select(i, j);
					if (selectedCount > 1) {
						clearSelected();
						return false;
					}
				}
			}
		}
		clearSelected();
		return true;
	}

	private void printStars() {
		String line = "";
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				line += stars[i][j] + " ";
			}
			Log.d(GAME_TAG, line);
			line = "";
		}
	}

	private void select(int touchedRow, int touchedColumn) {
		if (selectedStars[touchedRow][touchedColumn] == Star.SELECTED) {
			return;
		}
		selectedStars[touchedRow][touchedColumn] = Star.SELECTED;
		selectedCount++;
		selectAroundSameColor(touchedRow, touchedColumn);
	}

	private void clearSelected() {
		selectedCount = 0;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				selectedStars[i][j] = Star.UNSELECTED;
			}
		}
	}

	private void selectAroundSameColor(int row, int column) {
		int color = stars[row][column];
		if (row != 0) {
			if (checkSelect(color, row - 1, column)) {
				select(row - 1, column);
			}
		}
		if (column != 0) {
			if (checkSelect(color, row, column - 1)) {
				select(row, column - 1);
			}
		}
		if (row != this.row - 1) {
			if (checkSelect(color, row + 1, column)) {
				select(row + 1, column);
			}
		}
		if (column != this.row - 1) {
			if (checkSelect(color, row, column + 1)) {
				select(row, column + 1);;
			}
		}
	}
	
	private boolean checkSelect(int color, int row, int column) {
		if (selectedStars[row][column] == Star.SELECTED) {
			return false;
		}
		if (stars[row][column] != color) {
			return false;
		}
		return true;
	}

	private void initGameBoard() {
		starSize = screenWidth / row;
		
		bitmap = Bitmap.createBitmap(screenWidth, screenWidth, Config.ARGB_4444);
		canvas = new Canvas(bitmap);
		
		paints = new Paint[Star.MAX_COLOR];
		selectedPaints = new Paint[Star.MAX_COLOR];
		for (int i = 0; i < Star.MAX_COLOR; i++) {
			paints[i] = new Paint();
			paints[i].setColor(Star.getColor(i));
			paints[i].setXfermode(new PorterDuffXfermode(Mode.SRC));
			paints[i].setAntiAlias(true);
			paints[i].setStyle(Style.FILL);
			selectedPaints[i] = new Paint();
			selectedPaints[i].setColor(Star.getSelectedColor(i));
			selectedPaints[i].setXfermode(new PorterDuffXfermode(Mode.SRC));
			selectedPaints[i].setAntiAlias(true);
			selectedPaints[i].setStyle(Style.FILL);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawBoard();
		canvas.drawBitmap(bitmap, 0, 0, tempPaint);
		super.onDraw(canvas);
	}

	private void drawBoard() {
		// draw Stars
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				canvas.drawRect(j * starSize, i * starSize, 
						(j + 1) * starSize - 1, (i + 1) * starSize - 1, paints[stars[i][j]]);
			}
		}
		// draw selected Stars
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				if (selectedStars[i][j] != Star.UNSELECTED) {
					canvas.drawRect(j * starSize, i * starSize, 
							(j + 1) * starSize - 1, (i + 1) * starSize - 1, 
							selectedPaints[stars[i][j]]);
				}
			}
		}
	}

	public void saveGame() {
		saveGameStatus();
		saveGameData();
	}
	
	// test code for animation
	private void starAnimation() {
		ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
		animation.setDuration(1000);
		animation.addUpdateListener(new AnimatorUpdateListener() {
		    @Override
		    public void onAnimationUpdate(ValueAnimator animation) {
		        Log.i("update", ((Float) animation.getAnimatedValue()).toString());
		    }
		});
		animation.start();
	}
	
	private void createAnimation() {
		myAlpha = 0;
		tempPaint = new Paint();
		ObjectAnimator oa=ObjectAnimator.ofInt(this, "myAlpha", 0, 255);
		oa.setDuration(3000);
		oa.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				invalidate();
			}
		});
		oa.start();
	}

	public int getMyAlpha() {
		return myAlpha;
	}

	public void setMyAlpha(int myAlpha) {
		this.myAlpha = myAlpha;
		tempPaint.setAlpha(myAlpha);
		Log.i("update", this.myAlpha + "");
	}
}
