package com.sieta.game.entities;

import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.lighting.LightDef;
import com.sieta.game.utils.Physics;
import com.sieta.game.world.GameWorld;
import com.sieta.game.world.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Creature extends WorldObject{	
	private String chatText = "Task: Clean";
	
	protected boolean flipTexture;
	private TextureRegion defaultTexture;
	private TextureRegion fallTexture;
	private TextureRegion freeFallTexture;
	
	private float acceleration = 0.6f;
	private float friction = 1f;
	
	protected float baseJumpForce;
	protected float maxJumpForce;
	protected float jumpAcc = 1.185f;
	protected float jumpDec = 0.8f;
	
	protected boolean jumpFrame = false;
	private long jumpTime = 0;
	private long groundedTime = 0;
	private long dropTime = 0;
	private long insideBlockTime = 0;
	
	private final float maxSpeed;
	private final float maxFallSpeed = 75f;
	protected float currentMaxSpeed;
	
	protected boolean inLiquid = false;
	protected boolean onGround = false;
	protected boolean onBlock = false; //Block = not slope
	
	private boolean insideBlock = false;
	private boolean detectedBlock = false;
	
	protected boolean moveLeft;
	protected boolean moveRight;
	
	protected byte lightSourceId;
	protected Vector2 lastLightSourcePos = new Vector2();
	
	protected Animation<TextureRegion> runAnimation;
	protected Animation<TextureRegion> jumpAnimation;
	protected float runTimer = 0f;
	protected float jumpTimer = 0f;
	
	public Creature(Vector2 spawnpos, Vector2 dimensions, float maxSpeed, short categoryBits, short maskBits) {
		super(spawnpos, dimensions, 1f, categoryBits, maskBits);
		baseJumpForce = 1f;
		maxJumpForce = baseJumpForce * 10f;
		this.maxSpeed = maxSpeed;
		currentMaxSpeed = maxSpeed;
		flipTexture = false;
		
		defaultTexture = ResourceHandler.getCharTexture("player");
		fallTexture = ResourceHandler.getCharTexture("fall");
		freeFallTexture = ResourceHandler.getCharTexture("freefall");
		
		setTexture(defaultTexture, false);
		runAnimation = new Animation(1f/8f, ResourceHandler.getCharTexture("run1"),ResourceHandler.getCharTexture("run2"),ResourceHandler.getCharTexture("run3"), ResourceHandler.getCharTexture("run4"),ResourceHandler.getCharTexture("run5"),ResourceHandler.getCharTexture("run6"), ResourceHandler.getCharTexture("run7"), ResourceHandler.getCharTexture("run8"));
		jumpAnimation = new Animation(1f/12f, ResourceHandler.getCharTexture("jump1"),ResourceHandler.getCharTexture("jump2"),ResourceHandler.getCharTexture("jump3"),ResourceHandler.getCharTexture("jump4"));
		lightSourceId = LightDef.PLAYER_AMB;
	}
	
	public Vector2 getLastLightSourcePos(){
		return lastLightSourcePos;
	}
	public void setLastLightSourcePos(float x, float y){
		lastLightSourcePos.set(x,y);
	}
	public byte getLightSourceId(){
		return lightSourceId;
	}
	

	public void setSpeed(float percentage) {
		currentMaxSpeed = maxSpeed * percentage;
	}
	
	public boolean onGround() {
		return onGround;
	}
	
	//Drop down through platforms
	public void dropDown(){
		dropTime = System.currentTimeMillis();
	}
	
	public boolean jump() {
		if(inLiquid){
			jumpFrame = true;
			jumpTime = System.currentTimeMillis();
			body.setVelocity(body.getVelocity().x, Math.min(body.getVelocity().y + baseJumpForce * 2f, maxJumpForce * 0.25f));
		}else if(canJump()){
			jumpTime = System.currentTimeMillis();
			if(body.getVelocity().y <= 0 || onGround){
				if(onGround && body.getVelocity().y > 0 && !jumpFrame){
					body.setVelocity(body.getVelocity().x, Math.min(body.getVelocity().y + baseJumpForce * 10f, maxJumpForce));
				}else{
					body.setVelocity(body.getVelocity().x, Math.max(baseJumpForce * 10f, body.getVelocity().y));
				}
			}else{
				body.setVelocity(body.getVelocity().x, Math.min(body.getVelocity().y + jumpAcc,maxJumpForce));
			}
			jumpFrame = true;
			return true;
		}
		return false;
	}
	
	public void resetJumpFrame(){
		jumpFrame = false;
	}
	public boolean isJumpFrame(){
		return jumpFrame;
	}
	
	protected void flipTexture(boolean flip){
		flipTexture = flip;
	}
	
	public boolean inLiquid(){
		return inLiquid;
	}
	
	public void hitBlock(){
		onBlock = true;
	}
	
	public void moveLeft() {
		if(canMove()){
			flipTexture = false;
			moveLeft = true;
		}
	}

	public void moveRight() {
		if(canMove()){
			flipTexture = true;
			moveRight = true;
		}
	}
	
	private void rightMovement(){
		body.setVelocity(body.getVelocity().add(currentMaxSpeed * acceleration,0));
	}
	
	private void leftMovement(){
		body.setVelocity(body.getVelocity().add(-currentMaxSpeed * acceleration,0));
	}
	
	protected void checkMoveLimit() {
		body.setVelocity(MathUtils.clamp(body.getVelocity().x, -currentMaxSpeed, currentMaxSpeed), MathUtils.clamp(body.getVelocity().y, -maxFallSpeed, maxFallSpeed));
	}
	
	public boolean isInsideBlock(){
		return insideBlock;
	}
	
	public long timeSinceJump(){
		return System.currentTimeMillis() - jumpTime;
	}
	public long timeSinceInsideBlock(){
		return System.currentTimeMillis() - insideBlockTime;
	}
	public long timeSinceGrounded(){
		return System.currentTimeMillis() - groundedTime;
	}
	public long timeSinceDrop(){
		return System.currentTimeMillis() - dropTime;
	}
	public void resetHits(){
		onBlock = false;
		inLiquid = false;
		onGround = false;
		detectedBlock = false;
	}
	public void detectedBlock(){
		detectedBlock = true;
	}
	public void insideBlock(){
		insideBlock = true;
	}

	public void hitLiquid(){
		inLiquid = true;
	}
	public void hitGround(){
		onGround = true;
	}

	public boolean onBlock(){
		return onBlock;
	}
	
	public void update(Map map, GameWorld world) {
		baseJumpForce = 3f;
		jumpAcc = 1.1f;
		maxJumpForce =  3f * 20f;
		
		if(!detectedBlock){
			insideBlock = false;
			detectedBlock = false;
		}
		
		if(timeSinceDrop() < 50){
			insideBlock = true;
		}
		if(onGround){
			groundedTime = System.currentTimeMillis();
		}
		if(insideBlock){
			insideBlockTime = System.currentTimeMillis();
		}
		if(inLiquid){
			acceleration = 0.13f * 0.75f;
			friction = 0.9f * 0.75f;
			currentMaxSpeed = maxSpeed;
			body.setVelocity(body.getVelocity().x - body.getVelocity().x * 0.1f, body.getVelocity().y - body.getVelocity().y * 0.1f);
		}else{
			if(onGround){
				friction = 0.95f;
				acceleration = 0.13f;
			}else{
				friction = 0.15f;
				acceleration = 0.06f;
			}
			
			currentMaxSpeed = maxSpeed;
			
		}
		
		if(moveRight){
			rightMovement();
			moveRight = false;
		}
		if(moveLeft){
			leftMovement();
			moveLeft = false;
		}
		
		if(applyFriction()){
			if(Math.abs(body.getVelocity().x) > friction){
				body.setVelocity(body.getVelocity().x + friction * -Math.signum(body.getVelocity().x), body.getVelocity().y);
			}else{
				body.setVelocity(0, body.getVelocity().y);
			}
		}

		if(limitMovement()){
			checkMoveLimit();
		}
	}
	
	public String getChatText(){
		return chatText;
	}
	
	//Overridden when needed
	protected boolean limitMovement(){
		return true;
	}
	protected boolean applyFriction(){
		return true;
	}
	protected boolean canMove(){
		return true;
	}
	protected boolean canJump(){
		return timeSinceJump() > 200 && onGround;
	}
	
	protected void updateRenderTexture(){
		runTimer += Gdx.graphics.getDeltaTime();
		jumpTimer += Gdx.graphics.getDeltaTime();
		
		if(Math.abs(body.getVelocity().x) > 0 && onGround){ //Running
			jumpTimer = 0;
			setTexture(runAnimation.getKeyFrame(runTimer, true), flipTexture);
		}else if(body.getVelocity().y > 0 && !onGround){ //Jumping
			runTimer = 0;
			setTexture(jumpAnimation.getKeyFrame(jumpTimer, false), flipTexture);
		}else if(body.getVelocity().y <= 0 && !onGround){ //Falling
			runTimer = 0;
			jumpTimer = 0;
			if(body.getVelocity().y <= maxFallSpeed * 0.9f && timeSinceGrounded() > 1000){
				setTexture(freeFallTexture, flipTexture);
			}else{
				setTexture(fallTexture, flipTexture);
			}
		}else{ //Idle
			runTimer = 0;
			jumpTimer = 0;
			setTexture(defaultTexture, flipTexture);
		}
	}
	
	@Override
	public void render(SpriteBatch batch){
		updateRenderTexture();
		super.render(batch);
	}
}
