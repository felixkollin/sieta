package com.sieta.game.world;

import com.badlogic.gdx.graphics.g2d.Sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sieta.game.handlers.ResourceHandler;

/**
 * Used to retrieve information from tiles, only from their id.
 * 
 * Used to ease the process when managing tiles.
 *
 */

public abstract class Tile {
	
	//Get data from string
	
	//TODO save a class TileProperties instead. containing id, type and more
	
	public static final short air=0,water=1,steam=2,lava=3,poison=4,poisongas=5,torch=6,platform=7,stone=8,dirt=9,sand=10,brick=12;
	
	//TODO Establish conventions of IDs? Faster checks
	// 0-X is Solid
	// X-Y is Liquid
	// Y-Z is Steam
	// One way, falling, objects
		
	public static boolean isPhysical(short id){
		if(id == air || isFlowing(id) || isObject(id)){
			return false;
		}
		return true;
	}
	
	//First 4 is meta
	//Meta: 0 - 15
	//Id: 0 - 4095
	
	public static boolean isLiquid(short id){
		if(id == water || id == lava || id == poison){
			return true;
		}
		return false;
	}
	
	public static boolean isSteam(short id){
		return id == steam || id == poisongas;
	}
	
	public static boolean isFlowing(short id){
		return id >= 1 && id <= 5;
	}
	
	//To display behind player
	public static boolean isObject(short id){
		return id == Tile.torch;
	}
	public static boolean isOneWay(short id){
		if(id == platform){
			return true;
		}
		return false;
	}
	
	public static boolean isFalling(short id){
		if(id == sand){
			return true;
		}
		return false;
	}
	
	public static boolean stopsFall(short id){
		if(!isOneWay(id)){
			return false;
		}
		if(id == platform){
			return true;
		}
		return false;
	}
	
	public static float getHardness(short id){
		if (id == brick || id == stone) {
			return 1000f;
		} else {
			return 0.8f;
		}
	}

	public static void render(SpriteBatch batch, short id, int x, int y, boolean background) {
		if(Tile.isLiquid(id)){
			return;
		}
		
		Sprite sprite = ResourceHandler.getTileSprite(id);
		if(background){
			sprite.setColor(0.35f, 0.35f, 0.35f, 1f);
		}
		if(id == dirt || id == stone){
			sprite.setPosition(x - sprite.getWidth() * 0.5f, y - sprite.getHeight() * 0.5f);
		}else{
			sprite.setPosition(x - sprite.getWidth() * 0.5f, y - sprite.getHeight() * 0.5f);

		}
		sprite.draw(batch);
	}
}
