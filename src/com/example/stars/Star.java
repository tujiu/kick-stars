package com.example.stars;

import java.util.Random;

import android.graphics.Color;

public class Star {
	public static final int UNSELECTED = -2;
	public static int SELECTED = -1;
	public static final int NOSTAR = 5;
	public static final int RED = 0;
	public static final int BLUE = 1;
	public static final int GREEN = 2;
	public static final int YELLOW = 3;
	public static final int PURPLE = 4;
	public static final int MAX_COLOR = 6;
	public static final int[] colors = {Color.RED, Color.BLUE, 
		Color.GREEN, Color.YELLOW, Color.CYAN, Color.WHITE};
	public static final int[] colorsSelected = {0xffaa1111, 0xff1111aa, 
		0xff11aa11, 0xffaaaa11, 0xff11aaaa, Color.WHITE};
	public static final int SCORE_FACTOR = 5;
	
	public static final int SCORE_LEFT = 2000;
	public static final int SCORE_LEFT_FACTOR = 20;
	public static final int TARGET = 1000;
	public static final int TARGET_FACTOR = 500;
	
	public static int getColor(int i) {
		i = i > MAX_COLOR? 0: i;
		return colors[i];
	}

	public static int getSelectedColor(int i) {
		i = i > MAX_COLOR? 0: i;
		return colorsSelected[i];
	}
	
	public static int getScore(int count) {
		if (count <= 1) return 0;
		return count * count * SCORE_FACTOR;
	}
	
	public static int getLeftSocre(int count) {
		int score = SCORE_LEFT - count * count * SCORE_LEFT_FACTOR;
		return score > 0 ? score: 0;
	}
	
	public static int getTarget(int turn) {
		return (TARGET + TARGET_FACTOR + TARGET_FACTOR * turn) * turn / 2;
	}
	
	public static int[][] createStars(int row) {
		Random random = new Random();
		int[][] data = new int [row][row];
		for (int i = 0; i < row; i++) {
			for (int j =0; j < row; j++) {
				data[i][j] = random.nextInt(MAX_COLOR - 1);
			}
		}
		return data;
	}
	
	public static int[][] createEmptyStars(int row) {
		int[][] data = new int [row][row];
		for (int i = 0; i < row; i++) {
			for (int j =0; j < row; j++) {
				data[i][j] = UNSELECTED;
			}
		}
		return data;
	}
	
	public static int[][] setDataString(String savedData, int row) {
		int[][] data = new int [row][row];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < row; j++) {
				data[i][j] = Integer.parseInt(savedData.substring(i * row + j, i * row + j + 1));
			}
		}
		return data;
	}
}
