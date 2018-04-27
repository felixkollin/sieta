package com.sieta.game.handlers;

import com.badlogic.gdx.Input.Buttons;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

/**
 * Class for processing inputs from the user.
 */
public class MyInputProcessor extends InputAdapter {
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		if(button == Buttons.LEFT){
			MyInput.setLeftClick(true);
			
			MyInput.leftDown = true;
			MyInput.leftUp = false;
		}
		if(button == Buttons.RIGHT){
			MyInput.setRightClick(true);
		}
		return true;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		if(button == Buttons.LEFT){
			MyInput.setLeftClick(false);
			
			MyInput.leftDown = false;
			MyInput.leftUp = true;
		}
		if(button == Buttons.RIGHT){
			MyInput.setRightClick(false);
		}
		return true;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public boolean keyDown(int key) {
		switch (key) {
		case Keys.TAB:
			MyInput.setKey(MyInput.TAB, true);
			break;
			
		case Keys.W:
			MyInput.setKey(MyInput.W, true);
			break;

		case Keys.S:
			MyInput.setKey(MyInput.S, true);
			break;

		case Keys.A:
			MyInput.setKey(MyInput.A, true);
			break;
		case Keys.Q:
			MyInput.setKey(MyInput.Q, true);
			break;
		case Keys.E:
			MyInput.setKey(MyInput.E, true);
			break;
		case Keys.D:
			MyInput.setKey(MyInput.D, true);
			break;
		case Keys.SHIFT_LEFT:
			MyInput.setKey(MyInput.SHIFT, true);
			break;
		case Keys.NUM_1:
			MyInput.setKey(MyInput.NUM_1, true);
			break;
		case Keys.NUM_2:
			MyInput.setKey(MyInput.NUM_2, true);
			break;
		case Keys.NUM_3:
			MyInput.setKey(MyInput.NUM_3, true);
			break;
		case Keys.NUM_4:
			MyInput.setKey(MyInput.NUM_4, true);
			break;
		case Keys.NUM_5:
			MyInput.setKey(MyInput.NUM_5, true);
			break;
		case Keys.NUM_6:
			MyInput.setKey(MyInput.NUM_6, true);
			break;
		case Keys.NUM_7:
			MyInput.setKey(MyInput.NUM_7, true);
			break;
		case Keys.NUM_8:
			MyInput.setKey(MyInput.NUM_8, true);
			break;
		case Keys.NUM_9:
			MyInput.setKey(MyInput.NUM_9, true);
			break;
		case Keys.NUM_0:
			MyInput.setKey(MyInput.NUM_0, true);
			break;
		case Keys.SPACE:
			MyInput.setKey(MyInput.SPACE, true);
			break;
		}
		
		return true;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public boolean keyUp(int key) {
		switch (key){
		case Keys.TAB:
			MyInput.setKey(MyInput.TAB, false);
			break;
		case Keys.W:
			MyInput.setKey(MyInput.W, false);
			break;
		case Keys.Q:
			MyInput.setKey(MyInput.Q, false);
			break;
		case Keys.E:
			MyInput.setKey(MyInput.E, false);
			break;

		case Keys.S:
			MyInput.setKey(MyInput.S, false);
			break;

		case Keys.A:
			MyInput.setKey(MyInput.A, false);
			break;

		case Keys.D:
			MyInput.setKey(MyInput.D, false);
			break;
		case Keys.SHIFT_LEFT:
			MyInput.setKey(MyInput.SHIFT, false);
			break;
		case Keys.NUM_1:
			MyInput.setKey(MyInput.NUM_1, false);
			break;
		case Keys.NUM_2:
			MyInput.setKey(MyInput.NUM_2, false);
			break;
		case Keys.NUM_3:
			MyInput.setKey(MyInput.NUM_3, false);
			break;
		case Keys.NUM_4:
			MyInput.setKey(MyInput.NUM_4, false);
			break;
		case Keys.NUM_5:
			MyInput.setKey(MyInput.NUM_5, false);
			break;
		case Keys.NUM_6:
			MyInput.setKey(MyInput.NUM_6, false);
			break;
		case Keys.NUM_7:
			MyInput.setKey(MyInput.NUM_7, false);
			break;
		case Keys.NUM_8:
			MyInput.setKey(MyInput.NUM_8, false);
			break;
		case Keys.NUM_9:
			MyInput.setKey(MyInput.NUM_9, false);
			break;
		case Keys.NUM_0:
			MyInput.setKey(MyInput.NUM_0, false);
			break;
		case Keys.SPACE:
			MyInput.setKey(MyInput.SPACE, false);
			break;
		}
		return true;
	}
}
