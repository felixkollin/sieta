package com.sieta.game.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.items.ItemContainer;
import com.sieta.game.utils.Physics;
import com.sieta.game.world.GameWorld;
import com.sieta.game.world.Map;

public class Player extends Creature{	
	private boolean canDoubleJump;
	
	//TODO Scale friction/acc on movespeed (10f is good)
	private static final float MOVESPEED = 12.5f;
	private static final float DASH_SPEED = MOVESPEED * 5f;
	private static final int DASH_DURATION = 100;
	private static final int DASH_COOLDOWN = 75;
	private static final int DASH_GRAVITY_HOLD = 125;
	private static final int WALL_JUMP_FORGIVENESS = 150;
	
	private long lastDash = 0;
	private boolean dashing;
	private boolean smashing;
	private long smashHittime = 0;
	private boolean triggerSmash = false;
	
	private long leftWallTime = 0;
	private long rightWallTime = 0;
	private long leftWallJump = 0;
	private long rightWallJump = 0;
	private boolean hitWallLeft = false;
	private boolean hitWallRight = false;
	
	private TextureRegion wallTexture;
	
	private Vector2 dashLeft = new Vector2(-DASH_SPEED, 0);
	private Vector2 dashRight = new Vector2(DASH_SPEED, 0);
	private Vector2 smash = new Vector2(0, -DASH_SPEED);
	
	private boolean moveRightFrame = false;
	private boolean moveLeftFrame = false;
	
	private boolean variableJumpOngoing = false;
	private boolean crouched = false;
			
	private ItemContainer itemContainer;
	
	public Player(Vector2 spawnpos) {

		super(spawnpos, new Vector2(1.75f,3.875f), MOVESPEED, Physics.CATEGORY_PLAYER, Physics.MASK_PLAYER);
		leftWallTime = System.currentTimeMillis();
		rightWallTime = System.currentTimeMillis();
		rightWallJump = System.currentTimeMillis();
		leftWallJump = System.currentTimeMillis();
		wallTexture = ResourceHandler.getCharTexture("wall");
		
		dashing = false;
		body.setData(this);
		itemContainer = new ItemContainer();
	}
	
	protected void updateRenderTexture(){
		super.updateRenderTexture();
		if((hitWallLeft || hitWallRight) && !onGround && body.getVelocity().y <= 0){
			setTexture(wallTexture, flipTexture);
			runTimer = 0;
			jumpTimer = 0;
		}
	}
	
	public void resetHits(){
		if(hitWallLeft){
			leftWallTime = System.currentTimeMillis();
		}
		if(hitWallRight){
			rightWallTime = System.currentTimeMillis();
		}
		hitWallRight = false;
		hitWallLeft = false;
		
		moveRightFrame = false;
		moveLeftFrame = false;
		super.resetHits();
	}
	
	public long timeSinceLeftWallJump(){
		return System.currentTimeMillis() - leftWallJump;
	}
	public long timeSinceRightWallJump(){
		return System.currentTimeMillis() - rightWallJump;
	}
	
	public void moveLeft(){
		moveLeftFrame = true;
		super.moveLeft();
		
	}
	public void moveRight(){
		moveRightFrame = true;
		super.moveRight();
	}
	
	public void releaseJump(){
		variableJumpOngoing = false;
		if(!onGround && body.getVelocity().y > 0){
			body.setVelocity(body.getVelocity().x, body.getVelocity().y * jumpDec);
		}
	}
	
	public void hitWallLeft(){
		hitWallLeft = true;
	}
	public void hitWallRight(){
		hitWallRight = true;
	}
	
	public ItemContainer getInventory(){
		return itemContainer;
	}
	
	public long timeSinceLeftWall(){
		return System.currentTimeMillis() - leftWallTime;
	}
	public long timeSinceRightWall(){
		return System.currentTimeMillis() - rightWallTime;
	}
	
	@Override
	protected boolean canJump(){
		return variableJumpOngoing || (!dashing && !smashing && super.canJump()) || (timeSinceGrounded() < 150 && body.getVelocity().y <= 0);
	}
	
	public boolean wallJump(){
		if((hitWallRight || (timeSinceRightWall() < WALL_JUMP_FORGIVENESS && !moveRightFrame)) && !onGround){
			hitWallRight = false;
			rightWallJump = System.currentTimeMillis();
			rightWallTime = Integer.MAX_VALUE;
			if(!moveRightFrame){
				flipTexture = false;
			}
			body.setVelocity(-currentMaxSpeed, maxJumpForce);
			variableJumpOngoing = true;
			return true;
		}else if((hitWallLeft || (timeSinceLeftWall() < WALL_JUMP_FORGIVENESS && !moveLeftFrame)) && !onGround){
			hitWallLeft = false;
			leftWallJump = System.currentTimeMillis();
			leftWallTime = Integer.MAX_VALUE;
			if(!moveLeftFrame){
				flipTexture = true;
			}
			body.setVelocity(currentMaxSpeed, maxJumpForce);
			variableJumpOngoing = true;
			return true;
		}
		
		return false;
	}
	
	public boolean onWallLeft(){
		return hitWallLeft;
	}
	public boolean onWallRight(){
		return hitWallRight;
	}
	
	public boolean tryWallJump() {
		if((body.getVelocity().y + jumpAcc * body.getVelocity().y > maxJumpForce || body.getVelocity().y <= 0) && !onGround){
			variableJumpOngoing = false;
		}
		
		//First try jumping, if you can't, then walljump
		if(super.jump()){
			variableJumpOngoing = true;
			return true;
		}else{
			return wallJump();
		}
	}
	
	//TODO if nothing above, offset y a tiny bit upward
	@Override
	public boolean jump(){
		if((body.getVelocity().y + jumpAcc * body.getVelocity().y > maxJumpForce || body.getVelocity().y <= 0) && !onGround){
			variableJumpOngoing = false;
		}
		if(super.jump() || variableJumpOngoing){
			if(variableJumpOngoing){
				jumpFrame = false;
			}
			variableJumpOngoing = true;
			
			return true;
		}
		return false;
	}
	
	
	public void doubleJump(){
		if(!inLiquid && !dashing && !smashing && canDoubleJump && timeSinceJump() > 20f){
			canDoubleJump = false;
			dashing = false;
			body.setVelocity(body.getVelocity().x, maxJumpForce);
		}
	}
	
	@Override
	protected boolean canMove(){
		return (!dashing && !smashing);
	}
	
	private boolean canDash(){
		return (System.currentTimeMillis() - (lastDash + DASH_DURATION) > DASH_COOLDOWN && !smashing);
	}
	
	public void dashLeft() {
		if(canDash()){
			dashing = true;
			lastDash = System.currentTimeMillis();
			flipTexture(false);
			body.setVelocity(dashLeft);
		}
	}
	
	public void crouch(boolean down){
		if(down && onGround){
			body.setVelocity(0,0);
		}
		crouched = down;
	}
	
	public void dashRight() {
		if(canDash()){
			dashing = true;
			lastDash = System.currentTimeMillis();
			flipTexture(true);
			body.setVelocity(dashRight);
		}
	}
	public void smash() {
		if(!smashing && !onGround){
			smashing = true;
			lastDash = System.currentTimeMillis();
			body.setVelocity(smash);
		}
	}
	
	public boolean triggerSmash(){
		if(System.currentTimeMillis() - smashHittime > 50f && triggerSmash){
			triggerSmash = false;
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean applyFriction(){
		if(!dashing){
			return true;
		}
		return false;
	}
	
	@Override
	public void update(Map map, GameWorld world) {
		if(onGround){
			hitWallRight = false;
			hitWallLeft = false;
			if(hitWallLeft){
				leftWallTime = System.currentTimeMillis();
			}
			if(hitWallRight){
				rightWallTime = System.currentTimeMillis();
			}
		}
				
		if(hitWallLeft || hitWallRight){
			if(body.getVelocity().y < 0){
				body.setVelocity(body.getVelocity().x, Math.min(body.getVelocity().y * 0.9f, 0));
			}
		}
		if(System.currentTimeMillis() - lastDash > DASH_DURATION){
			dashing = false;
		}
		
		
		body.setGravityScale(1);
		if(dashing){
			//TODO have dashLeft and dashRight instead
			if(body.getVelocity().x == 0){
				dashing = false;
			}else if(body.getVelocity().x > 0){//dash right
				body.setVelocity(dashRight.x, 0);
			}else{
				body.setVelocity(dashLeft.x, 0);
			}
			
			body.setGravityScale(0);
		}
		
		if(System.currentTimeMillis() - lastDash < DASH_DURATION + DASH_GRAVITY_HOLD){
			body.setGravityScale(0);
		}
		
		if (onGround) {
			if(smashing){
				triggerSmash = true;
				smashing = false;
				smashHittime = System.currentTimeMillis();
			}
			canDoubleJump = true;
		}
		super.update(map, world);
	}
	
	@Override
	protected boolean limitMovement(){
		return !dashing;
	}
}
