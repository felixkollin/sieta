package com.sieta.game.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SpriteElement extends InteractElement{
	
	private Sprite sprite;
	
	public SpriteElement(OrthographicCamera cam, int x, int y, int w, int h, boolean open, Sprite sprite, int id) {
		super(cam, x, y, w, h, open, id);
		this.sprite = sprite;
		sprite.setPosition(x, y);
		sprite.setSize(interactBox.w, interactBox.h);
	}

	@Override
	public void mousePos(float x, float y) {}

	@Override
	void leftClickEvent(float x, float y) {}

	@Override
	void leftDownEvent(float x, float y) {}

	@Override
	void leftUpEvent(float x, float y) {}

	@Override
	void rightClickEvent(float x, float y) {}

	@Override
	void draw(SpriteBatch batch, OrthographicCamera cam, Viewport viewport) {
		sprite.setPosition(interactBox.x, interactBox.y);
		sprite.setSize(interactBox.w, interactBox.h);
		sprite.draw(batch);
	}

	@Override
	protected void setPos(float x, float y) {
		super.setPos(x,y);
		sprite.setPosition(interactBox.x, interactBox.y);
	}

}
