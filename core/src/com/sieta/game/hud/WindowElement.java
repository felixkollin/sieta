package com.sieta.game.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;

abstract class WindowElement extends SpriteElement{
	private boolean dragged;
	private float lastX;
	private float lastY;
	
	public WindowElement(OrthographicCamera cam, int x, int y, int w, int h, boolean visible, Sprite sprite, int id) {
		super(cam, x, y, w, h, visible, sprite, id);
		dragged = false;
		lastX = interactBox.x;
		lastY = interactBox.y;
	}

	@Override
	void leftDownEvent(float x, float y) {
		if(!interactBox.inside(x, y)){
			dragged = false;
			return;
		}
		dragged = true;
	}

	@Override
	void leftUpEvent(float x, float y) {
		dragged = false;
	}
	
	@Override
	public void mousePos(float x, float y){
		if(dragged){
			setPos(interactBox.x - (lastX - x), interactBox.y - (lastY - y));
			for(InteractElement e : attachments){
				e.setPos(e.interactBox.x - (lastX - x), e.interactBox.y - (lastY - y));
			}
			lastX = x;
			lastY = y;
		}
		lastX = x;
		lastY = y;
	}
}
