package com.sieta.game.states;

import com.sieta.game.handlers.GameStateManager;

import com.sieta.game.SietaGame;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The GameState class implements the methods used by GameStateManager.
 */
public abstract class GameState {
	protected GameStateManager gsm;
	protected SietaGame game;
	protected SpriteBatch batch;
	protected OrthographicCamera cam;
	protected OrthographicCamera hudCam;
	protected Viewport viewport;
	protected Viewport hudViewport;

	protected GameState(GameStateManager gsm) {
		this.gsm = gsm;
		game = gsm.game();
		batch = game.getSpriteBatch();
		cam = game.getCamera();
		hudCam = game.getHudCamera();
		viewport = game.getViewport();
		hudViewport = game.getHudViewport();
	}

	/**
	 * Check for key or mouse inputs to the GameState and perform the
	 * appropriate action depending on the input.
	 */
	public abstract void handleDesktopInput();

	/**
	 * Update the values and objects associated with the GameState.
	 * 
	 * @param deltaTime
	 */
	public abstract void update(float deltaTime);

	/**
	 * Render and draw the objects inside the GameState.
	 * 
	 * @param deltaTime
	 */
	public abstract void render();

	/**
	 * Dispose the GameState and destroy any objects created inside.
	 */
	public abstract void dispose();

	/**
	 * Initialize the GameState, must be called after creation or when values
	 * needs to be refreshed.
	 */
	public abstract void resume();

	public abstract void pause();
	public abstract void resize(int width, int height);

	public abstract void updatePrevState();
	public abstract void interpolate(float alpha);
}