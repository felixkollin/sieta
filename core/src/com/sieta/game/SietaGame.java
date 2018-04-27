package com.sieta.game;

import com.sieta.game.handlers.GameStateManager;
import com.sieta.game.handlers.MyInput;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.states.Play;
import com.sieta.game.utils.Graphics;
import com.sieta.game.utils.Physics;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The main game class.
 */
public class SietaGame extends ApplicationAdapter {

	public static final String TITLE = "Psieta";
	public static final String DESCRIPTION = "A Sandbox 2D Game";
	public static final String VERSION = "Dev Build 0.1";

	private static final float STEP = 1.0f/60.0f;
	private static final double MAX_FRAMEDELTA = 0.25;
	private float accumulator;
	private double currentTime;

	private SpriteBatch batch;
	private OrthographicCamera cam;
	private OrthographicCamera hudCam;
	public static final float ZOOM_LEVEL = 1f;
	//1 zoom is farthest, 1.5 is nice, 1.25 normal,
	public static final float DEFAULT_ZOOM = 0.5f;
	private float zoom;

	private GameStateManager gsm;

	private ApplicationType appType;

	private static Viewport viewport;
	private static Viewport viewportHud;

	private double newTime;
	private double deltaTime;


	/**
	 * Called when the game is started
	 */
	public void create() {
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);

		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		ResourceHandler.loadAssets();
		setCursor();
		batch = new SpriteBatch(5000);
		appType = Gdx.app.getType();
		createCams();

		int width = Math.round(Graphics.VIRTUAL_WIDTH / Physics.PPM);
		int height = Math.round(Graphics.VIRTUAL_HEIGHT / Physics.PPM);
		viewport = new ExtendViewport(width, height, cam);
		viewportHud = new ScreenViewport(hudCam);
		viewport.apply();
		viewportHud.apply();

		gsm = new GameStateManager(this);

		accumulator = 0.0f;
		currentTime = TimeUtils.millis() / 1000.0;
	}

	private void createCams() {
		if(appType == ApplicationType.Android){
			zoom = (DEFAULT_ZOOM * 0.5f)/ZOOM_LEVEL;
		}else{
			zoom = DEFAULT_ZOOM/ZOOM_LEVEL;
		}
		cam = new OrthographicCamera();
		cam.zoom = zoom;
		cam.update();

		hudCam = new OrthographicCamera();
		hudCam.zoom = zoom * ZOOM_LEVEL;
		hudCam.update();
	}

	public ApplicationType getAppType(){
		return appType;
	}

	/**
	 * Set the zoom of the game.
	 *
	 * @param zoom
	 */
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	/**
	 * Get the zoom of the game.
	 *
	 * @param zoom
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * Set the current cursor image used in the game.
	 *
	 */
	//TODO allow changing cursor other than default
	public void setCursor() {
		Gdx.graphics.setCursor(Gdx.graphics.newCursor(ResourceHandler.getCursor(), 0, 0));
	}

	/**
	 * Update and render the game.
	 */
	/*
	 * Looping calls during runtime
	 */
	public void render() {
		newTime = TimeUtils.millis() / 1000.0;
		deltaTime = Math.min(newTime - currentTime, MAX_FRAMEDELTA);
		currentTime = newTime;

		accumulator += (float)deltaTime;
		while (accumulator >= STEP) {
			//TODO tie in before update
			gsm.updatePrevState(); //Save prev of dynamic moving objects
			gsm.update(STEP);
			if(appType == ApplicationType.Desktop){
				MyInput.update();
			}
			accumulator -= STEP;
		}
		gsm.interpolate(accumulator/STEP);//TODO tie in before render
		gsm.render();


	}

	/**
	 * Dispose the game and any resources needing to dispose.
	 */
	/*
	 * Called when game ends.
	 */
	public void dispose() {
		gsm.purgeStates();
		batch.dispose();
		ResourceHandler.dispose();
	}

	/**
	 * Called when the game window is hidden on the screen.
	 */
	public void pause() {
		gsm.peek().pause();
	}

	/**
	 * Called when the game window is resized.
	 */
	public void resize(int width, int height) {
		viewport.update(width, height);
		viewportHud.update(width, height);
		Vector3 oldpos = new Vector3(cam.position);
		cam.setToOrtho(false,
				width/Physics.PPM,
				height/Physics.PPM);
		cam.translate(oldpos.x-cam.position.x,oldpos.y-cam.position.y);
		gsm.resize(width, height);
	}

	/**
	 * Called when the game window is visible on the screen again.
	 */
	public void resume() {
		// Not needed right now
	}

	/**
	 * Retrieve the game's SpriteBatch.
	 *
	 * @return SpriteBatch batch
	 */
	public SpriteBatch getSpriteBatch() {
		return batch;
	}

	public OrthographicCamera getCamera() {
		return cam;
	}

	public OrthographicCamera getHudCamera() {
		return hudCam;
	}

	public Viewport getViewport() {
		return viewport;
	}

	public Viewport getHudViewport() {
		return viewportHud;
	}
}
