package com.sieta.game.handlers;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.sieta.game.SietaGame;
import com.sieta.game.states.GameState;
import com.sieta.game.states.MainMenu;
import com.sieta.game.states.Play;
import com.sieta.game.utils.Graphics;

/**
 * A manager that handles all GameStates in a stack.
 */
public class GameStateManager {
	private SietaGame game;
	private Stack<GameState> gameStates;

	public static final int PLAY = 1242626;
	public static final int MENU = 42424;

	/**
	 * Constructor for the GameStateManager. Pushes the menu state onto the
	 * GameState stack.
	 * 
	 * @param game
	 */
	public GameStateManager(SietaGame game) {
		this.game = game;
		gameStates = new Stack<GameState>();
		pushState(MENU);
	}

	public GameState peek(){
		return gameStates.peek();
	}
	/**
	 * Retrieve the Game.
	 * 
	 * @return Game
	 */
	public SietaGame game() {
		return game;
	}

	/**
	 * Update the current GameState.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime) {
		if(game.getAppType() == ApplicationType.Desktop){
			gameStates.peek().handleDesktopInput();
		}
		gameStates.peek().update(deltaTime);
	}
	
	public void updatePrevState(){
		gameStates.peek().updatePrevState();
	}
	public void interpolate(float alpha){
		gameStates.peek().interpolate(alpha);
	}

	/**
	 * Render the current GameState.
	 * 
	 * @param deltaTime
	 */
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		gameStates.peek().render();
	}
	
	public void resize(int width, int height) {
		gameStates.peek().resize(width, height);
	}
	

	/*
	 * Retrieve a gamestate from an int
	 */
	private GameState getState(int state) {
		if (state == PLAY)
			return new Play(this);
		if (state == MENU)
			return new MainMenu(this);
		return null;
	}

	/**
	 * Replaces the current state on top GameState stack with chosen state.
	 * 
	 * @param state
	 * @return
	 */
	public void setState(int state) {
		popState();
		pushState(state);
	}

	/**
	 * Push a state onto the GameState stack.
	 * 
	 * @param state
	 */
	public void pushState(int state) {
		gameStates.push(getState(state));
		gameStates.peek().resume();
	}

	/**
	 * Pop a state from the GameState stack.
	 */
	public void popState() {
		GameState g = gameStates.pop();
		g.dispose();
		gameStates.peek().resume();
	}
	
	

	/**
	 * Purge and dispose all remaining GameStates.
	 */
	public void purgeStates() {
		while (!gameStates.isEmpty()) {
			GameState g = gameStates.pop();
			g.dispose();
		}
	}

}
