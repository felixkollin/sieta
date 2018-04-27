package com.sieta.game.handlers;


/**
 * Class to handle the states of the current inputs.
 *
 */
public class MyInput {
	public static boolean[] keys;
	public static boolean[] pastKeys;
	
	//TODO Support for double tapping

	public static final int NUM_KEYS = 19;
	public static final int NUM_0 = 0;
	public static final int NUM_1 = 1;
	public static final int NUM_2 = 2;
	public static final int NUM_3 = 3;
	public static final int NUM_4 = 4;
	public static final int NUM_5 = 5;
	public static final int NUM_6 = 6;
	public static final int NUM_7 = 7;
	public static final int NUM_8 = 8;
	public static final int NUM_9 = 9;
	public static final int W = 10;
	public static final int S = 11;
	public static final int A = 12;
	public static final int D = 13;
	public static final int SHIFT = 14;
	public static final int SPACE = 15;
	public static final int Q = 16;
	public static final int E = 17;
	public static final int TAB = 18;
	
	public static boolean leftClick;
	public static boolean pastLeftClick;
	public static boolean rightClick;
	public static boolean pastRightClick;
	
	public static boolean leftDown;
	public static boolean leftUp;
	
	static {
		keys = new boolean[NUM_KEYS];
		pastKeys = new boolean[NUM_KEYS];
	}

	/**
	 * Update the current state of key inputs.
	 */
	public static void update() {
		for (int i = 0; i < NUM_KEYS; i++) {
			pastKeys[i] = keys[i];
		}
		pastRightClick = rightClick;
		pastLeftClick = leftClick;
		leftUp = false;
		leftDown = false;
	}

	/**
	 * Set a key to be down or up.
	 * 
	 * @param key
	 * @param isDown
	 */
	public static void setKey(int key, boolean isDown) {
		keys[key] = isDown;
	}
	
	/**
	 * Gets the first number held down.
	 * @return - Number (0-9), if none, then -1.
	 */
	public static int getNumberDown(){
		for(int i = 0; i < 10; i++){
			if(isDown(i)){
				return i;
			}
		}
		return -1;
	}
	
	public static void setTouch(int pointer, float x, float y, boolean touched){
		
	}

	/**
	 * Check if a key is held down.
	 * 
	 * @param key
	 * @return
	 */
	public static boolean isDown(int key) {
		return keys[key];
	}

	/**
	 * Check if a key is pressed.
	 * 
	 * @param key
	 * @return
	 */
	public static boolean isPressed(int key) {
		return keys[key] && !pastKeys[key];
	}
	
	/**
	 * Check if a key is released.
	 * 
	 * @param key
	 * @return
	 */
	public static boolean isReleased(int key) {
		return !keys[key] && pastKeys[key];
	}

	/**
	 * Clear all previous inputs.
	 */
	public static void clear() {
		keys = new boolean[NUM_KEYS];
		pastKeys = new boolean[NUM_KEYS];
		leftClick = false;
		rightClick = false;
		pastLeftClick = false;
		pastRightClick = false;
	}
	
	public static void setLeftClick(boolean isDown){
		leftClick = isDown;
	}
	public static void setRightClick(boolean isDown){
		rightClick = isDown;
	}
	
	public static boolean isLeftClick(){
		return leftClick && !pastLeftClick;
	}
	public static boolean isRightClick(){
		return rightClick && !pastRightClick;
	}

}
