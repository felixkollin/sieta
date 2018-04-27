package com.sieta.game.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector;
import com.badlogic.gdx.utils.Array;
import com.sieta.game.entities.Creature;
import com.sieta.game.entities.Player;
import com.sieta.game.utils.Physics;
import com.sieta.game.world.Map;
import com.sieta.game.world.Tile;
import com.sieta.game.world.Chunk.Layer;
import com.sieta.game.world.Map.Slope;

public class PhysicsEngine {
	
	private static final float SUB_STEPS = 5;
	
	private Map map;
	private ContactListener contactListener;
	private Array<Body> bodies;
	
	private Polygon satPoly;
    private MinimumTranslationVector satMTV;
    private int slopeX;
    private int slopeY;
	
	public PhysicsEngine(Map map, Array<Body> bodies){
		satPoly = new Polygon();
		satMTV = new MinimumTranslationVector();
		
		this.map = map;
		this.bodies = bodies;
	}
	public PhysicsEngine(Map map){
		satPoly = new Polygon();
		satMTV = new MinimumTranslationVector();
		this.map = map;
		bodies = new Array<Body>();
	}
	
	public void addBodies(Array<Body> bodies){
		bodies.addAll(bodies);
	}
	public void addBody(Body body){
		bodies.add(body);
	}
	
	public void setContactListener(ContactListener contactListener){
		this.contactListener = contactListener;
	}
	
	public void step(float deltaTime){
		for(Body body: bodies){
			applyGravity(body, deltaTime);
			process(body, deltaTime);
		}
	}
	
	private void applyGravity(Body body, float deltaTime){
		body.setVelocity(body.getVelocity().x, body.getVelocity().y + Physics.GRAVITY * body.getGravityScale() * deltaTime);
	}
	
	private void process(Body body, float deltaTime){
		if(body.getData() instanceof Creature){ //TODO check if main body
			process((Creature) body.getData(), deltaTime);
		}else{
			if(body.getPhysRotation() == 0){ //TODO add support for right angles
				updateUnrotated(body, deltaTime);
			}else{
				updateRotated(body, deltaTime);
			}
		}
	}
	
	private void process(Creature creature, float deltaTime){
		if(creature.getBody().getPhysRotation() == 0){ //TODO add support for right angles
			updateUnrotated(creature, deltaTime);
		}else{
			updateRotated(creature.getBody(), deltaTime);
		}
	}
	
	//Works same for creatures, doesn't snap to slopes etc
	private void updateRotated(Body body, float deltaTime){
		float deltaStep = deltaTime / SUB_STEPS;
		satPoly.setVertices(body.hitbox.getVertices());
		satPoly.setOrigin(body.w/2, body.h/2);
		satPoly.setRotation(body.getPhysRotation());
		
		for(int i = 0; i < SUB_STEPS; i++){
			rotatedSubstep(body, deltaStep);
		}
	}
	
	private void updateUnrotated(Body body, float deltaTime){
		satPoly.setVertices(body.hitbox.getVertices());
		satPoly.setOrigin(body.w/2, body.h/2);
		satPoly.setRotation(body.getPhysRotation());
		
		float deltaStep = deltaTime / SUB_STEPS;	
		for(int i = 0; i < SUB_STEPS; i++){
			subStepUnrotated(body, deltaStep);
		}
	}
	
	private void updateUnrotated(Creature creature, float deltaTime){
		Body body = creature.getBody();
		satPoly.setVertices(body.hitbox.getVertices());
		satPoly.setOrigin(body.w/2, body.h/2);
		satPoly.setRotation(body.getPhysRotation());
		
		float deltaStep = deltaTime / SUB_STEPS;	
		for(int i = 0; i < SUB_STEPS; i++){
			subStepUnrotatedCreature(creature, body, deltaStep);
		}		
	}
	
	private void rotatedSubstep(Body body, float deltaStep){
		satPoly.setPosition(body.hitbox.getX() + body.getVelocity().x * deltaStep, body.hitbox.getY() + body.getVelocity().y * deltaStep);
		//Check all tiles around (Broadphase), make better later, account for rotation and velocity direction
		for(int x = Math.round(body.getX() - body.h * 2); x <= Math.round(body.getX() + body.h * 2); x++){
			for(int y = Math.round(body.getY() - body.h * 2); y <= Math.round(body.getY() + body.h * 2); y++){
				Slope slope = map.getSlope(x, y);
				switch(slope){
				case NONE:
					Physics.tilePolygon.setPosition(x - 0.5f, y - 0.5f);
					Intersector.overlapConvexPolygons(Physics.tilePolygon, satPoly, satMTV);
					break;
				case RIGHT:
					Physics.rightSlopePoly.setPosition(x - 0.5f, y - 0.5f);
					Intersector.overlapConvexPolygons(Physics.rightSlopePoly, satPoly, satMTV);
					break;
				case LEFT:
					Physics.leftSlopePoly.setPosition(x - 0.5f, y - 0.5f);
					Intersector.overlapConvexPolygons(Physics.leftSlopePoly, satPoly, satMTV);
					break;
				case PEAK:
					Physics.peakSlopePoly.setPosition(x - 0.5f, y - 0.5f);
					Intersector.overlapConvexPolygons(Physics.peakSlopePoly, satPoly, satMTV);
					break;
				default:
					Physics.tilePolygon.setPosition(x - 0.5f, y - 0.5f);
					Intersector.overlapConvexPolygons(Physics.tilePolygon, satPoly, satMTV);
					break;
				}
				
				if(satMTV.depth > 0){ //Collision detected
					short tile = map.getTile(x, y, Layer.MID);
					if(tile != Tile.air){
						float mass = map.getMass(x, y);
						if(contactListener.contact(body, tile, mass, x, y, deltaStep)){
							if(map.getSlope(x, y) != Slope.NONE){
								Player p = (Player) body.getData();
								p.hitGround();
							}
							satPoly.setPosition(satPoly.getX() - satMTV.normal.x * satMTV.depth, satPoly.getY() - satMTV.normal.y * satMTV.depth);

							Vector2 normal = satMTV.normal.rotate(180);
							Vector2 slide = normal.scl(Vector2.dot(body.getVelocity().x, body.getVelocity().y, normal.x, normal.y));
							body.setVelocity(body.getVelocity().x - slide.x, body.getVelocity().y - slide.y);
							
							contactListener.post(body, tile, mass, x, y, slope == Slope.NONE);
						}
					}
					satMTV.depth = 0; //Reset mtv, only depth > 0 matters
				}
			}
		}
		body.setPos(satPoly.getX() + body.w/2, satPoly.getY() + body.h/2);
	}
	
	private void subStepUnrotated(Body body, float deltaStep){
		float oldX = body.getX();
		float oldY = body.getY();
		//Step x
		body.setPos(body.getX() + body.getVelocity().x * deltaStep, body.getY());
		if(AABBCheck(body,deltaStep)){//Collides
			body.setPos(oldX, body.getY());
			body.setVelocity(0,body.getVelocity().y);
		}
		
		//Step y
		body.setPos(body.getX(), body.getY() + body.getVelocity().y * deltaStep);
		if(AABBCheck(body,deltaStep)){
			body.setPos(body.getX(), oldY);
			body.setVelocity(body.getVelocity().x, 0);
		}
	}
	
	//Simple AABB
	private boolean AABBCheck(Body body, float deltaStep){
		//TODO Only check tiles in the moving direction, not inside
		for(int x = Math.round(body.getX() - body.w/2f); x <= Math.round(body.getX() + body.w/2); x++){
			for(int y = Math.round(body.getY() - body.h/2); y <= Math.round(body.getY() + body.h/2); y++){
				//Simple AABB
				if(overlapsTile(body, x, y)){
					short tile = map.getTile(x, y, Layer.MID);
		        	 if(tile != Tile.air){
		        		 float mass = map.getMass(x, y);
	        			 if(contactListener.contact(body, tile, mass, x, y, deltaStep)){
	        				 contactListener.post(body, tile, mass, x, y, false);
	        				 return true;
			        	 }
		        	 }
				}
			}
		}
		return false;
	}
	
	private void subStepUnrotatedCreature(Creature creature, Body body, float deltaStep){
		float oldX = body.getX();
		float oldY = body.getY();
		
		Slope slope = checkSlopes(body);
		if(creature.isJumpFrame() && slope != Slope.NONE){
			body.setPos(body.getX(), body.getY() + 0.05f);
			slope = Slope.NONE;
		}
		//Step x
		body.setPos(body.getX() + body.getVelocity().x * deltaStep, body.getY());
		if(unrotatedCreatureCheck(body, creature, deltaStep, slope, true)){
			body.setVelocity(0,body.getVelocity().y);
			body.setPos(oldX, body.getPos().y);
		}else if(slope != Slope.NONE && !creature.isJumpFrame()){
			creature.hitGround();
			snapToSlope(creature, body, slope);
		}

		//Step y
		if(slope == Slope.NONE){
			body.setPos(body.getX(), body.getY() + body.getVelocity().y * deltaStep);
		}
		if(unrotatedCreatureCheck(body, creature, deltaStep, slope, false)){
			body.setVelocity(body.getVelocity().x, body.getVelocity().y * 0.01f); //TODO When head touches friction
			if((slope != Slope.NONE && slope != Slope.PEAK) || creature.isJumpFrame()){
				body.setPos(oldX, oldY);
			}else{
				body.setPos(body.getPos().x, oldY);
			}
			
		}
	}
	
	private boolean unrotatedCreatureCheck(Body body, Creature creature, float deltaStep, Slope slope, boolean xCheck){
		boolean collided = false;
		satPoly.setPosition(body.hitbox.getX(), body.hitbox.getY());
		for(int x = Math.round(body.getX() - body.w/2f) - 1; x <= Math.round(body.getX() + body.w/2) + 1; x++){
			for(int y = Math.round(body.getY() - body.h/2) - 1; y <= Math.round(body.getY() + body.h/2) + 1; y++){

				if(!creature.isJumpFrame()){
					if(body.getY() - body.h/2 >= y - 0.6f){
		       			 if(map.getSlope(x,y) != Slope.NONE)continue;
		       		}
						
						if(slope != Slope.NONE && slope != Slope.PEAK && (y == slopeY || y == slopeY + 1)){
							if(x == slopeX){
								continue; //Checking slope
							}
							//Checking something to left of slope
							if(slope == Slope.LEFT && x == slopeX - 1)continue;	
							//Checking something to right of slope
							if(slope == Slope.RIGHT && x == slopeX + 1)continue;
						}
				}
				
				
				float overlap = getOverlap(body,x,y,xCheck); //TODO satpoly?
				if(overlap > 0){
					short tile = map.getTile(x, y, Layer.MID);
					if(tile != Tile.air){
						Slope current = map.getSlope(x, y);
						float mass = map.getMass(x, y);
						if(current == Slope.NONE){
							if(contactListener.contact(body, tile, mass, x, y, deltaStep)){
								if(xCheck){
									body.setPos(body.getPos().x - Math.signum(body.getVelocity().x) * overlap, body.getPos().y);
								}else{
									body.setPos(body.getPos().x, body.getPos().y - Math.signum(body.getVelocity().y) * overlap);
								}
								contactListener.post(body, tile, mass, x, y, false);
								collided = true;
				        	}
						}else{
							satMTV.depth = 0;
							switch(current){
							case RIGHT:
								Physics.rightSlopePoly.setPosition(x - 0.5f, y - 0.5f);
								Intersector.overlapConvexPolygons(Physics.rightSlopePoly, satPoly, satMTV);
								break;
							case LEFT:
								Physics.leftSlopePoly.setPosition(x - 0.5f, y - 0.5f);
								Intersector.overlapConvexPolygons(Physics.leftSlopePoly, satPoly, satMTV);
								break;
							case PEAK:
								Physics.peakSlopePoly.setPosition(x - 0.5f, y - 0.5f);
								Intersector.overlapConvexPolygons(Physics.peakSlopePoly, satPoly, satMTV);
								break;
							default:
								break;
							}
							if(satMTV.depth > 0){
								if(contactListener.contact(body, tile, mass, x, y, deltaStep)){
									collided = true;
									satPoly.setPosition(satPoly.getX() - satMTV.normal.x * satMTV.depth, satPoly.getY() - satMTV.normal.y * satMTV.depth);
									contactListener.post(body, tile, mass, x, y, true);
								}
								satMTV.depth = 0;
							}
						}
						
					}
				}
			}
		}
		return collided;
	}
	
	private boolean overlapsTile(Body body, int x, int y){
		if(Math.abs(x - body.getX()) < 0.5f + body.w * 0.5f){
	         if(Math.abs(y - body.getY()) < 0.5f + body.h * 0.5f){
	        	 return true;
	         }
		}
		return false;
	}
	private float getOverlap(Body body, int x, int y, boolean stepX){
		if(stepX){
			if(Math.max(0, Math.min(body.hitbox.getY() + body.h, y + 0.5f) - Math.max(body.hitbox.getY(),y - 0.5f)) > 0){
				return Math.max(0, Math.min(body.hitbox.getX() + body.w, x + 0.5f) - Math.max(body.hitbox.getX(),x - 0.5f));
			}
			return 0;
		}
		if(Math.max(0, Math.min(body.hitbox.getX() + body.w, x + 0.5f) - Math.max(body.hitbox.getX(),x - 0.5f)) > 0){
			return Math.max(0, Math.min(body.hitbox.getY() + body.h, y + 0.5f) - Math.max(body.hitbox.getY(),y - 0.5f));
		}
		return 0;
	}
	
	private Slope checkSlopes(Body body){
		satPoly.setPosition(body.hitbox.getX(), body.hitbox.getY());
		
		Slope foundSlope = Slope.NONE;
		int maxX = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE;
		for(int y = Math.round(body.getY() - body.h/2) - 1; y <= Math.round(body.getY() - body.h/2) + 1; y++){
			//TODO Only check tiles in the moving direction, not inside
			for(int x = Math.round(body.getX() - body.w/2f); x <= Math.round(body.getX() + body.w/2f); x++){
				//Check x collision (y collision depends on slope, see Physics for slope polygon shape)
				if(Math.abs(x - body.getX()) < 0.49f + body.w * 0.5f){
					switch(map.getSlope(x,y)){
		        	 case RIGHT:
		        		 Physics.rightSnapPoly.setPosition(x - 0.5f, y - 0.5f);
		        		 if(Intersector.overlapConvexPolygons(Physics.rightSnapPoly, satPoly)){
		        			 if(x > maxX){
		        				 maxX = x;
		        			 }
		        			 slopeY = y;
		        			 foundSlope = Slope.RIGHT;
		        		 }
		        		 break;
		        	 case LEFT:
		        		 Physics.leftSnapPoly.setPosition(x - 0.5f, y - 0.5f);
		        		 if(Intersector.overlapConvexPolygons(Physics.leftSnapPoly, satPoly)){
		        			 if(x < minX){
		        				 minX = x;
		        			 }
		        			 slopeY = y;
		        			 foundSlope = Slope.LEFT;
		        		 }
		        		 break;
		        	 case PEAK:
				         if(Math.abs(y - body.getY()) < 0.5f + body.h * 0.5f){
				        	 float leftOfBody = body.getX() - body.w/2;
			        		 float rightOfBody = body.getX() + body.w/2;
			        		 if(rightOfBody < x + 0.5f && leftOfBody < x - 0.5f){//right
			        			 Physics.rightSnapPoly.setPosition(x - 0.5f, y - 0.5f);
			        			 if(Intersector.overlapConvexPolygons(Physics.rightSnapPoly, satPoly)){
			        				 slopeX = x;
			    					 slopeY = y;
			    					 return Slope.RIGHT;
			        			 }
			        		 }else if(rightOfBody > x + 0.5f && leftOfBody > x - 0.5f){ //left
			        			 Physics.leftSnapPoly.setPosition(x - 0.5f, y - 0.5f);
			        			 if(Intersector.overlapConvexPolygons(Physics.leftSnapPoly, satPoly)){
			        				 slopeX = x;
			    					 slopeY = y;
			    					 return Slope.LEFT;	
			        			 }
			        		 }else{
			        			 slopeX = x;
			        			 slopeY = y;
			        			 return Slope.PEAK;
			        		 }
				         }
		        		 break;
		        	 case NONE:
		        		 break;
		        	 default:
		        		 break;
		        	 }
				}
			}
		}
		if(foundSlope == Slope.LEFT){
			slopeX = minX;
		}else if(foundSlope == Slope.RIGHT){
			slopeX = maxX;
		}
		
		return foundSlope;
	}
	private void snapToSlope(Creature creature, Body body, Slope slope){
		float diffX;
		switch(slope){
		case RIGHT:
			creature.hitGround();
			diffX = MathUtils.clamp(body.getX() + body.w * 0.5f - (slopeX - 0.5f), 0f, 1f);
			body.setPos(body.getX(), slopeY - 0.5f + body.h * 0.5f + diffX);
			if(diffX < 1f && diffX > 0){
				body.setVelocity(body.getVelocity().x, body.getVelocity().x);
			}else{
				body.setVelocity(body.getVelocity().x, 0);
			}
			break;
		case LEFT:
			creature.hitGround();
			diffX = MathUtils.clamp(1 - (1 + body.getX() - body.w/2 - (slopeX + 0.5f)), 0f, 1f);
			body.setPos(body.getX(), slopeY - 0.5f + body.h/2f + diffX);
			if(diffX < 1f && diffX > 0){
				body.setVelocity(body.getVelocity().x, -body.getVelocity().x);
			}else{
				body.setVelocity(body.getVelocity().x, 0);
			}
			break;
		case PEAK:
			creature.hitGround();
			body.setVelocity(body.getVelocity().x, 0f);
			break;
		default:
			break;
		}
	}
	public void updatePrevState(){
		for(Body body: bodies){
			body.updatePrevState();
		}
	}
	public void interpolate(float alpha){
		for(Body body: bodies){
			body.interpolate(alpha);
		}
	}
}
