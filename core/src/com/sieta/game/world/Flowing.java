package com.sieta.game.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.sieta.game.handlers.ResourceHandler;

//Used by liquids and steam

/**
 * Used to handle liquids and gases, things that flow through the map.
 * @author felixkollin
 *
 */
public abstract class Flowing {		
	public static final float MIN_MASS = 0.05f; //Dissapearance of liquids at this mass
	public static final float MAX_MASS = 1f; //The normal, un-pressurized mass of a full water cell
	public static final float MAX_COMP = 0.2f; //How much excess water a cell can store, compared to the cell above it
	public static final float MIN_DRAW = 0.01f;  //Ignore cells that are almost dry
	
	private static final float OPACITY = 0.75f;
	
	private static Sprite sprite;
	private static Sprite spriteUpper;
	
	public static float getStableState(float total_mass){
		if (total_mass <= MAX_MASS){
			return MAX_MASS;
		} else if (total_mass < 2f * MAX_MASS + MAX_COMP ){
			return (MAX_MASS * MAX_MASS + total_mass * MAX_COMP)/(MAX_MASS + MAX_COMP);
		} else {
			return (total_mass + MAX_COMP)/2f;
		}
	}
	
	public static float getHorizontalMaxSpeed(short id){
		if(id == Tile.lava){
			return 0.1f;
		}else{
			return 100f;
		}
	}
	
	public static float getHorizontalFlowSpeed(short id){
		if(Tile.isSteam(id)){
			return 1f;
		}
		if(id == Tile.lava){
			return 1f;
		}
		return 1f;
	}
	
	public static float getVerticalSpeed(short id){
		if(Tile.isSteam(id)){
			return 0.2f;
		}
		return 1f;
	}
		
	public static void render(SpriteBatch batch, short id, float x, float y, float mass, float aboveMass, float belowMass, short aboveId, short belowId) {
		sprite = ResourceHandler.getTileSprite(id);
		setColor(sprite, id, mass);
		float height = Math.min(mass, 1f);
		
		if(Tile.isSteam(id)){
			float mod = MathUtils.clamp(height * 3f, 0.125f, 1f);
			sprite.setSize(mod, mod);
			sprite.setPosition(x - mod * 0.5f, y - mod * 0.5f);
			sprite.draw(batch);
			return;
		}
		
		//Blending state, draw both tiles
		if(aboveId != id && Tile.isLiquid(aboveId)){ //Some other liquid above
			spriteUpper = ResourceHandler.getTileSprite(aboveId);
			setColor(spriteUpper, aboveId, aboveMass);
			spriteUpper.setPosition(x - 0.5f, y - 0.5f + Math.max(height, 0.05f));
			spriteUpper.setSize(1f, 1f - Math.max(height, 0.05f));
			spriteUpper.draw(batch);
			
			sprite.setPosition(x - 0.5f, y - 0.5f);
			sprite.setSize(1f, Math.max(height, 0.05f));
			sprite.draw(batch);
			return;
		}
		
		boolean otherLiquidUnder = belowId != id && Tile.isFlowing(belowId);
		if(aboveMass >= Flowing.MIN_DRAW && Tile.isLiquid(aboveId)){ //Droplet
			float mod = MathUtils.clamp(height * 3f, 0.125f, 1f);
			sprite.setSize(mod, mod);
			sprite.setPosition(x - mod * 0.5f, y - mod * 0.5f);
			sprite.draw(batch);
    	}else if((belowMass >= 1f || !Tile.isLiquid(belowId) || otherLiquidUnder) && belowId != Tile.air){ //If full liquid or solid below
			sprite.setPosition(x - 0.5f, y - 0.5f);
			sprite.setSize(1f, Math.max(height, 0.05f));
			sprite.draw(batch);
    	}
	}
	
	private static float colorModifier(float mass){
		if(mass > MAX_MASS + MAX_COMP){ //Over pressure
			return MathUtils.clamp((MAX_MASS * 2.5f)/mass, 0.4f, 1f);
		}
		return 1f;
	}
	
	private static void setColor(Sprite sprite, short id, float mass){
		Color baseColor = sprite.getColor();
		float colormod = colorModifier(mass);
		sprite.setColor(baseColor.r * colormod, baseColor.g * colormod, baseColor.b * colormod, OPACITY);
	}
}
