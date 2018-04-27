package com.sieta.game.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.physics.Body;
import com.sieta.game.utils.Physics;
import com.sieta.game.world.GameWorld;

public class WorldObject implements Renderable{
	protected Sprite sprite;
	protected Body body;
	private TextureRegion texture;
	private float lightLevel = 1;
	
	public WorldObject(Vector2 pos, Vector2 hitboxDim, float density, short category, short mask) {		
		body = new Body(pos.x, pos.y, hitboxDim.x, hitboxDim.y, density, this);
		body.setFilter(mask, category);
		sprite = new Sprite();
		setTexture(ResourceHandler.getCharTexture("player"), false);
	}
	
	public void resize(float width, float height){
		body.w = width;
		body.h = height;
		body.hitbox.setScale(width/body.w, height/body.h);		
	}
	
	public float getWidth(){
		return body.w;
	}
	public float getHeight(){
		return body.h;
	}
	
	public void setLightLevel(float light){
		this.lightLevel = light;
	}
	
	protected void setTexture(TextureRegion texture, boolean flipx){
		this.texture = texture;
		sprite.setSize((float) texture.getRegionWidth() / Physics.PPM,
				(float) texture.getRegionHeight() / Physics.PPM);
		sprite.setRegion(texture);
		sprite.setFlip(flipx, false);
		sprite.setOrigin(sprite.getWidth()/2f - 0.125f/2f, sprite.getHeight()/2f - 1f - 0.125f);
	}
	
	protected TextureRegion getTexture(){
		return texture;
	}
	
	public void render(SpriteBatch batch) {
		sprite.setRotation(body.renderRotation());
		sprite.setSize((float) texture.getRegionWidth() / Physics.PPM,
				(float) texture.getRegionHeight() / Physics.PPM);
		float offset = -0.25f;
		sprite.setPosition(body.renderX() - sprite.getWidth() / 2, body.renderY() - body.h/2 + offset);
		sprite.setColor(lightLevel, lightLevel, lightLevel, 1f);
		sprite.draw(batch);
	}
	
	public Vector2 getPosition() {
		return body.getPos();
	}

	public Sprite getSprite() {
		return sprite;
	}

	public Body getBody() {
		return body;
	}
}
