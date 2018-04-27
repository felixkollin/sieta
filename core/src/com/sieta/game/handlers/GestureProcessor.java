package com.sieta.game.handlers;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class GestureProcessor extends InputAdapter {
	// TODO pinching?
	// TODO long press?
	// TODO Swiping between areas messed up

	// TODO let the player decide gesture sensitivity
	// Low: 2f
	// Normal: 1f
	// High: 0.5f
	// Extreme: 0f, Actually nice (when not playing on the train)

	private float sensitivity = 1f;
	private static final float DEFAULT_GESTURE_DIST = 50f;
	
	//Support 3 touches because of weird people using more than two fingers:
	private static final int supportedTouches = 3;

	public enum TouchArea {
		MOVE_LEFT, MOVE_RIGHT, GESTURE, INTERACT, HOTBAR, NONE;
	}

	public enum TouchType {
		SWIPE, SWIPE_LEFT, SWIPE_RIGHT, SWIPE_UP, SWIPE_DOWN, TOUCH, TAP, CLICK, SHORT_HOLD, NONE;
	}
	
	private static Vector2 movement = new Vector2();
	private static Vector2 velocity = new Vector2();

	private class TouchInfo {
		
		public long startTime = 0;
		public long releaseTime = 0;
		public float startX = 0;
		public float startY = 0;
		public float currentX = 0;
		public float currentY = 0;
		public boolean down = false;
		public VelocityTracker tracker = new VelocityTracker();

		public float deltaX() {
			return startX - currentX;
		}

		public float deltaY() {
			return startY - currentY;
		}

		public boolean traveledHorizontal() {
			movement.set(startX - currentX, startY - currentY);
			if (Math.abs(movement.x) > Math.abs(movement.y)) {
				return true;
			}
			return false;
		}

		public void startTracker() {
			tracker.start(startX, startY, startTime);
		}

		public long timeSincePressed() {
			return TimeUtils.nanosToMillis(TimeUtils.nanoTime() - startTime);
		}

		public long timeSinceReleased() {
			return TimeUtils.nanosToMillis(TimeUtils.nanoTime() - releaseTime);
		}

		public boolean minimalMovement() {
			return Math.abs(deltaX()) < DEFAULT_GESTURE_DIST * sensitivity
					&& Math.abs(deltaY()) < DEFAULT_GESTURE_DIST * sensitivity;
		}
	}

	private GestureHandler handler;
	private TouchInfo[] touches;

	public GestureProcessor(GestureHandler handler) {
		this.handler = handler;
		touches = new TouchInfo[supportedTouches];
		for (int i = 0; i < supportedTouches; i++) {
			touches[i] = new TouchInfo();
		}
	}

	public void update() {
		determineTouches();
	}
	
	private void determineTouches() {
		for (int i = 0; i < supportedTouches; i++) {
			TouchArea area = TouchArea.NONE;
			TouchType type = TouchType.NONE;
			TouchType secondaryType = TouchType.NONE;
			float angle = 0;
			
			TouchInfo info = touches[i];
			if (info.currentX > (Gdx.graphics.getWidth() * 2)/3) {
				area = TouchArea.GESTURE;
			} else if (info.currentX <= (Gdx.graphics.getWidth() * 2)/3
					&& info.currentX >= Gdx.graphics.getWidth() / 3) {
				area = TouchArea.INTERACT;
			} else if (info.currentX < Gdx.graphics.getWidth()/8) {
				area = TouchArea.MOVE_LEFT;
			} else if (info.currentX >= Gdx.graphics.getWidth() / 8
					&& info.currentX < Gdx.graphics.getWidth() / 3) {
				area = TouchArea.MOVE_RIGHT;
			}
			if (area == TouchArea.NONE) {
				continue;
			}

			switch (area) {
			case GESTURE:
				if (checkShortHold(info)) {
					type = TouchType.SHORT_HOLD;
				}
				if (checkTap(info)) {
					type = TouchType.TAP;
				}
				if (info.down && info.timeSincePressed() > 70 && (Math.abs(info.deltaX()) > DEFAULT_GESTURE_DIST || Math.abs(info.deltaY()) > DEFAULT_GESTURE_DIST)) {
					type = TouchType.SWIPE;
					float vX = info.tracker.getVelocityX();
					float vY = info.tracker.getVelocityY();
					
					//Calculate angle
					velocity.set(-info.deltaX(), info.deltaY());
					angle = velocity.angle();
					
					if (info.traveledHorizontal()) {
						if (vX > 0
								&& info.deltaX() < -DEFAULT_GESTURE_DIST
										* sensitivity) {
							secondaryType = TouchType.SWIPE_RIGHT;
						} else if (info.deltaX() > DEFAULT_GESTURE_DIST
								* sensitivity) {
							secondaryType = TouchType.SWIPE_LEFT;
						}
					} else {
						if (vY > 0
								&& info.deltaY() < -DEFAULT_GESTURE_DIST
										* sensitivity) {
							secondaryType = TouchType.SWIPE_DOWN;
						} else if (info.deltaY() > DEFAULT_GESTURE_DIST
								* sensitivity) {
							secondaryType = TouchType.SWIPE_UP;
						}
					}
				}
				break;
			case MOVE_LEFT:
				if (info.down) {
					type = TouchType.TOUCH;
				}
				break;
			case MOVE_RIGHT:
				if (info.down) {
					type = TouchType.TOUCH;
				}
				break;
			case INTERACT:
				if(checkClick(info)){
					type = TouchType.CLICK;
				}else if (checkTap(info)) {
					type = TouchType.TAP;
				}else if (info.down) {
					type = TouchType.TOUCH;
				}
				break;
			default:
				break;
			}
			if (type == TouchType.NONE) {
				continue;
			}
			handler.handleGesture(type, secondaryType, area, info.currentX, info.currentY, angle);
		}
	}

	private boolean checkTap(TouchInfo info) {
		if (!info.down && info.timeSincePressed() < 300
				&& info.timeSinceReleased() < 50 && info.minimalMovement()) {
			return true;
		}else if(info.down && info.timeSincePressed() > 100 && info.timeSincePressed() < 300){
			return true;
		}
		return false;
	}
	
	private boolean checkClick(TouchInfo info) {
		if (!info.down && info.timeSinceReleased() < 15 && info.minimalMovement()) {
			return true;
		}
		return false;
	}

	private boolean checkShortHold(TouchInfo info) {
		if (info.down && info.timeSincePressed() > 100 && info.timeSincePressed() < 300
				&& info.minimalMovement()) {
			return true;
		}
		return false;
	}
	
	//Get the touch index with the last pos closest to the new one (x,y)
	private int getClosestIndex(float x, float y){
		int index = 0;
		float[] distances = new float[supportedTouches];
		for(int i = 0; i < supportedTouches; i++){
			TouchInfo info = touches[i];
			distances[i] = Math.abs(info.currentX - x) * Math.abs(info.currentX - x) + Math.abs(info.currentY - y) *Math.abs(info.currentY - y);
		}
		for(int i = 1; i < supportedTouches; i++){
			if(distances[i] < distances[index]){
				index = i;
			}
		}
		return index;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		return touchDown((float) x, (float) y, pointer, button);
	}

	private boolean touchDown(float x, float y, int pointer, int button) {		
		if (pointer < supportedTouches) {
			int index = getClosestIndex(x,y);
			TouchInfo info = touches[index];
			info.startTime = Gdx.input.getCurrentEventTime();
			info.startTracker();
			info.startX = x;
			info.startY = y;
			info.currentX = x;
			info.currentY = y;
			info.down = true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return touchUp((float) x, (float) y, pointer, button);
	}

	private boolean touchUp(float x, float y, int pointer, int button) {
		if (pointer < supportedTouches) {			
			int index = getClosestIndex(x,y);
			TouchInfo info = touches[index];
			info.currentX = x;
			info.currentY = y;
			info.down = false;
			
			//Continue to update velocity, for flinging
			info.releaseTime = Gdx.input.getCurrentEventTime();
			if (info.releaseTime - info.tracker.lastTime < (0.15f * 1000000000l)) {
				info.tracker.update(x, y, info.releaseTime);
			}
		}
		return false;
	}

	public boolean touchDragged(int x, int y, int pointer) {
		return touchDragged((float) x, (float) y, pointer);
	}

	private boolean touchDragged(float x, float y, int pointer) {
		if (pointer < supportedTouches) {
			int index = getClosestIndex(x,y);
			TouchInfo info = touches[index];
			info.currentX = x;
			info.currentY = y;
			info.tracker.update(x, y, Gdx.input.getCurrentEventTime());
		}
		return false;
	}

	public static interface GestureHandler {
		public void handleGesture(TouchType mainType, TouchType secondaryType, TouchArea area, float x, float y, float angle);
	}

	// Used from libgdx source
	static class VelocityTracker {
		int sampleSize = 10;
		float lastX, lastY;
		float deltaX, deltaY;
		long lastTime;
		int numSamples;
		float[] meanX = new float[sampleSize];
		float[] meanY = new float[sampleSize];
		long[] meanTime = new long[sampleSize];

		public void start(float x, float y, long timeStamp) {
			lastX = x;
			lastY = y;
			deltaX = 0;
			deltaY = 0;
			numSamples = 0;
			for (int i = 0; i < sampleSize; i++) {
				meanX[i] = 0;
				meanY[i] = 0;
				meanTime[i] = 0;
			}
			lastTime = timeStamp;
		}

		public void update(float x, float y, long timeStamp) {
			long currTime = timeStamp;
			deltaX = x - lastX;
			deltaY = y - lastY;
			lastX = x;
			lastY = y;
			long deltaTime = currTime - lastTime;
			lastTime = currTime;
			int index = numSamples % sampleSize;
			meanX[index] = deltaX;
			meanY[index] = deltaY;
			meanTime[index] = deltaTime;
			numSamples++;
		}

		public float getVelocityX() {
			float meanX = getAverage(this.meanX, numSamples);
			float meanTime = getAverage(this.meanTime, numSamples) / 1000000000.0f;
			if (meanTime == 0)
				return 0;
			return meanX / meanTime;
		}

		public float getVelocityY() {
			float meanY = getAverage(this.meanY, numSamples);
			float meanTime = getAverage(this.meanTime, numSamples) / 1000000000.0f;
			if (meanTime == 0)
				return 0;
			return meanY / meanTime;
		}

		private float getAverage(float[] values, int numSamples) {
			numSamples = Math.min(sampleSize, numSamples);
			float sum = 0;
			for (int i = 0; i < numSamples; i++) {
				sum += values[i];
			}
			return sum / numSamples;
		}

		private long getAverage(long[] values, int numSamples) {
			numSamples = Math.min(sampleSize, numSamples);
			long sum = 0;
			for (int i = 0; i < numSamples; i++) {
				sum += values[i];
			}
			if (numSamples == 0)
				return 0;
			return sum / numSamples;
		}
	}
}
