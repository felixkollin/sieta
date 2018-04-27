package com.sieta.game.physics;

import com.badlogic.gdx.math.Vector2;

import com.sieta.game.entities.Creature;
import com.sieta.game.entities.Player;
import com.sieta.game.world.Tile;

public class CollisionHandler implements ContactListener {
	
	@Override
	public boolean contact(Body b, short id, float mass, int x, int y, float deltaTime){
		Vector2 pos = b.getPos();
		Vector2 vel = b.getVelocity();
		boolean contactEnabled = Tile.isPhysical(id);
		
		if(Tile.isOneWay(id)){
			//TODO use overlap instead of deltaTime to get precise
			if(pos.y - b.h/2 - vel.y * deltaTime >= y + 0.5f){//If body is above
				contactEnabled = true;
			}else{
				contactEnabled = false;
			}
		}
		
		if(b.getData() instanceof Creature){
			Creature e = (Creature) b.getData();
			
			if(!Tile.stopsFall(id)){
				e.detectedBlock();
			}
			if(Tile.isLiquid(id) && mass >= 0.1f && y - Math.min(mass, 1f) > b.getY() - b.h/2){
				e.hitLiquid();
			}
			
			if(e.timeSinceDrop() < 50 && Tile.isOneWay(id)){
				contactEnabled = false;
			}
		}
		return contactEnabled;
	}
	@Override
	public void contact(Body a, Body b) {}
	
	/*
	 * Called after resolving a collision
	 */
	
	//If two values are equal, the edges are touching
	@Override
	public void post(Body b, short id, float mass, int x, int y, boolean slope) {
		if(b.getData() instanceof Creature){
			Creature e = (Creature) b.getData();

			Vector2 vel = e.getBody().getVelocity();
			
			if(Tile.isPhysical(id) && vel.y < 0){
				e.hitBlock();
			}
			
		    if(e instanceof Player && Math.abs(y - b.getY()) < 0.5f + b.h * 0.5 && !slope){
		    	Player p = (Player) e;
				if(Tile.isPhysical(id)){
					if(b.getX() > x){
						if(x > b.getX()){
		 					p.hitWallRight();
		 				}else{
		 					p.hitWallLeft();
		 				}
		        	}else{
		        		 if(x > b.getX()){
			 				p.hitWallRight();
			 			}else{
			 				p.hitWallLeft();
			 			}
		        	}
				}
			}

			if(Tile.isPhysical(id) && b.getY() - b.h * 0.5f >= y + 0.5f && (Math.abs(x - (b.getX())) < 0.5f  + b.w * 0.5f)){
				e.hitGround();
			}
		}
	}

	/*private void checkProjectileCollision(Contact contact) {
		Object a = contact.getFixtureA().getUserData();
		Object b = contact.getFixtureB().getBody().getUserData();
		if (a instanceof Tile && b instanceof Projectile) {
			Tile t = (Tile) a;
			if(t.getFixture() == null || TileDef.isSensor(t.id)){
				return;
			}
			Projectile p = (Projectile) b;
			
			Vector2 velocity = p.getBody().getLinearVelocity();
			float modifier = Math.abs(p.getBody().getAngularVelocity()) * 0.1f;
			if ((Math.abs(velocity.x) - modifier > TileDef.getHardness(t.id) || Math
					.abs(velocity.y) - modifier > TileDef.getHardness(t.id))
					&& (contact.getFixtureB() == p.getTip())) {
				p.stick(t.getFixture());
			} else {
				p.hit();
			}
		}
	}*/

}
