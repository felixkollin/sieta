package com.sieta.game.entities;

import com.badlogic.gdx.math.Vector2;
import com.sieta.game.utils.Physics;
import com.sieta.game.world.GameWorld;
import com.sieta.game.world.Map;

public class Mob extends Creature{
	
	private static final float MOVESPEED = 15f;
	
	public Mob(Vector2 spawnpos){
		super(spawnpos, new Vector2(1f,2.6f), MOVESPEED, Physics.CATEGORY_MOB, Physics.MASK_MOB);
		body.setData(this);
	}
	
	@Override
	public void update(Map map, GameWorld world){
		body.setGravityScale(1);
		super.update(map, world);
	}
}
