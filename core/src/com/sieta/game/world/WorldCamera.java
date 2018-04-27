package com.sieta.game.world;

import java.util.Random;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sieta.game.entities.Creature;
import com.sieta.game.utils.Physics;


public class WorldCamera {
	private Vector3 cameraTarget = new Vector3();
	private Vector3 cameraPosition;
	public static final float CAMERA_SPEED = 0.3f;
	private OrthographicCamera cam;
	private Rectangle view;
	
	private ShakeType shakeType;
	private Random random;
	private float power, duration, currentPower;
	private float currentTime;

	private Creature target;
	
	public enum ShakeType{
		RUMBLE, DOWN, UP, VERTICAL, HORIZONTAL, NONE;
	}

	public WorldCamera(OrthographicCamera cam, Creature target) {
		random = new Random();
		this.target = target;
		view = new Rectangle(0, 0, Gdx.graphics.getWidth() / Physics.PPM,
				Gdx.graphics.getHeight() / Physics.PPM);
		this.cam = cam;
		cameraPosition = cam.position;
		view.setCenter(new Vector2(cam.position.x, cam.position.y));
		shakeType = ShakeType.NONE;
	}
	
	public OrthographicCamera getCam(){
		return cam;
	}

	public void update(float deltaTime) {
		if(shakeType != ShakeType.NONE){
			currentTime += deltaTime;
			if (currentTime <= duration) {
				currentPower = power * ((duration - currentTime) / duration);
				// generate random new x and y values taking into account
				// how much force was passed in
				float x = 0;
				float y = 0;
				switch(shakeType){
				case RUMBLE:
					x = (random.nextFloat() - 0.5f) * 2 * currentPower;
					y = (random.nextFloat() - 0.5f) * 2 * currentPower;
					break;
				case DOWN:
					y = (0.5f) * 2 * currentPower;
					break;
				case UP:
					y = (-0.5f) * 2 * currentPower;
					break;
				case HORIZONTAL:
					x = (random.nextFloat() - 0.5f) * 2 * currentPower;
					break;
				case VERTICAL:
					y = (random.nextFloat() - 0.5f) * 2 * currentPower;
					break;
				default:
					break;
				}
				// Set the camera to this new x/y position
				cam.translate(-x, -y);
			}else{
				shakeType = ShakeType.NONE;
			}
		} else {
			// When the shaking is over move the camera back to the player
			// position
			//cameraTarget.set(target.getBody().renderPos(), 0);
			//cameraPosition = cam.position;
			//cameraPosition.scl(1f - CAMERA_SPEED);
			//cameraTarget.scl(CAMERA_SPEED);
			//cameraPosition.add(cameraTarget);
			//cam.position.set(cameraPosition);
			//cam.position.x = GMath.roundAccurate(cam.position.x, 100f);
			//cam.position.y = GMath.roundAccurate(cam.position.y, 100f);
			cam.position.x = target.getBody().renderX();
			cam.position.y = target.getBody().renderY();
		}
		
		cam.update();
		view.setCenter(cam.position.x, cam.position.y);
	}

	public void shake(float power, float time, ShakeType type) {
		shakeType = type;
		this.power = power;
		duration = time;
		this.currentTime = 0;
	}
	
	public Vector3 getPosition(){
		return cam.position;
	}
}
