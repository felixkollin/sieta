package com.sieta.game.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.sieta.game.utils.GMath;

public class Body {
	//For physics:
	private Vector2 pos = new Vector2(); //Position of top left corner
	
	//For rendering:
	private Vector2 renderPos = new Vector2(); //Render pos
	private Vector2 prevPos = new Vector2(); //Used when interpolating
		
	private Vector2 vel = new Vector2();
	private Object data;
	
	public Polygon hitbox;

	public float w;
	public float h;
	
	private short mask;
	private short category;
	
	private float density;
	private float mass;
	private float restitution; //Bouncyness
	
	private float gravityScale;
	
	private float rotation;
	private float renderRotation;
	private float prevRotation;
	
	public Body(float x, float y, float width, float height, float density, Object data){
		renderPos.set(x, y);
		pos.set(x,y);
		prevPos.set(x,y);

		this.density = density;
		setDim(width, height);
		this.data = data;
		gravityScale = 1f;
		mask = 0;
		category = 0;
		hitbox = new Polygon(new float[]{0,0,w,0,w,h,0,h});
		hitbox.setOrigin(w/2, h/2);
		
		setRotation(0);
		renderRotation = rotation;
		prevRotation = rotation;
		restitution = 0.5f;
	}
	
	public float getPhysRotation(){
		//Cant have same rotation as slopes, so not 45 or -45 => just add 1, doesn't matter
		//Must have something to do with how LIBGDX uses intersector
		if(Math.round(rotation) == 45 || Math.round(rotation) == -45){
			return rotation + 1;
		}
		return rotation;
	}
	
	public float getRestitution(){
		return restitution;
	}
	
	public float getRotation(){
		return rotation;
	}
	public void setRotation(float degrees){
		rotation = degrees % 360;
		hitbox.setRotation(rotation);
	}
	
	public void rotate(float degrees){
		setRotation(rotation + degrees);
	}
	
	public float getGravityScale(){
		return gravityScale;
	}
	
	public void setGravityScale(float scale){
		this.gravityScale = scale;
	}
	
	public void setFilter(short mask, short category){
		this.mask = mask;
		this.category = category;
	}
	
	public void setDim(float width, float height){
		this.w = width;
		this.h = height;
		mass = w * h * density;
	}
	
	public void setDensity(float density){
		this.density = density;
		mass = w * h * density;
	}
	
	public float getMass(){
		return mass;
	}
	
	public void setPos(float x, float y){
		pos.set(x, y);
		hitbox.setPosition(x - w/2f, y - h/2f);
	}
	
	public Vector2 getPos(){
		return pos;
	}
	
	public float getX(){
		return pos.x;
	}
	
	public float getY(){
		return pos.y;
	}
	
	public float renderX(){
		return GMath.roundAccurate(renderPos.x, 100f);
	}
	public float renderY(){
		return GMath.roundAccurate(renderPos.y, 100f) + 0.125f * 2f;
	}
	public float renderRotation(){
		return renderRotation;
	}
	
	public Object getData(){
		return data;
	}
	
	public void setData(Object data){
		this.data = data;
	}
	
	public Vector2 getVelocity(){
		return vel;
	}
	public void setVelocity(float x, float y){
		vel.set(x, y);
	}
	public void setVelocity(Vector2 vec){
		vel.set(vec);
	}
	
	public void updatePrevState(){
		prevPos.x = pos.x;
		prevPos.y = pos.y;
		prevRotation = rotation;
	}
	public void interpolate(float alpha){
		if(alpha >= 1f){
			renderPos.x = pos.x;
			renderPos.y = pos.y;
			renderRotation = rotation;
			return;
		}
		//---- interpolate: currentState*alpha + previousState * ( 1.0 - alpha ); ------------------
		renderPos.x = pos.x * alpha + prevPos.x * (1.0f - alpha);
		renderPos.y = pos.y * alpha + prevPos.y * (1.0f - alpha);
		renderRotation = MathUtils.lerpAngleDeg(prevRotation, rotation, alpha);
	}
}
