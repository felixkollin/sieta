package com.sieta.game.hud;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sieta.game.main.Box;
import com.sieta.game.utils.Graphics;

/**
 * Hud element that can be rendered on the screen.
 * It can also be interacted with.
 * @author felixkollin
 *
 */
abstract class InteractElement{
	protected Box interactBox;
	protected Array<InteractElement> attachments = new Array<InteractElement>();
	private boolean open;
	private OrthographicCamera cam; //Needed for clamping on screen
	
	private int id;
	
	//TODO
	boolean anchorX = false;
	boolean anchorY = false;
	
	public InteractElement(OrthographicCamera cam, int x, int y, int w, int h, boolean open, int id){
		this.interactBox = new Box(x, y, w, h);
		this.id = id;
		this.open = open;
		this.cam = cam;
	}
	
	public void anchorX(boolean anchor){
		anchorX = anchor;
	}
	
	public void anchorY(boolean anchor){
		anchorY = anchor;
	}
	
	public boolean isOpen(){
		return open;
	}
	
	public void show(){
		open = true;
	}
	public void hide(){
		open = false;
	}
	public void toggleVisible(){
		open = !open;
	}
	
	public void leftClick(float x, float y){
		for(InteractElement attachment : attachments){
			attachment.leftClick(x,y);
		}
		if(!isInsideElement(x, y)){
			return;
		}
		leftClickEvent(x,y);
	}
	public void rightClick(float x, float y){
		for(InteractElement attachment : attachments){
			attachment.rightClick(x,y);
		}
		if(!isInsideElement(x, y)){
			return;
		}
		rightClickEvent(x,y);
	}
	public void leftDown(float x, float y){
		for(InteractElement attachment : attachments){
			attachment.leftDown(x,y);
		}
		if(!isInsideElement(x, y)){
			return;
		}
		leftDownEvent(x,y);
	}
	public void leftUp(float x, float y){
		for(InteractElement attachment : attachments){
			attachment.leftUp(x,y);
		}
		if(!isInsideElement(x, y)){
			return;
		}
		leftUpEvent(x,y);
	}
	
	public void attach(InteractElement element){
		if(element == null){
			return;
		}
		attachments.add(element);
	}
	
	public void detach(InteractElement element){
		attachments.removeValue(element, false);
	}
	
	public void render(SpriteBatch batch, OrthographicCamera cam, Viewport viewport){
		if(open){
			for(InteractElement attachment : attachments){
				attachment.render(batch, cam, viewport);
			}
			draw(batch, cam, viewport);
		}
	}
	
	public void updateClamp(int diffX, int diffY){
		if(anchorX){
			interactBox.x -= diffX * cam.zoom * 0.5f;
		}
		if(anchorY){
			interactBox.y += diffY * cam.zoom * 0.5f;
		}
		
		interactBox.x = MathUtils.clamp(interactBox.x, -(Gdx.graphics.getWidth() * cam.zoom)/2, (Gdx.graphics.getWidth() * cam.zoom)/2 - interactBox.w);
		interactBox.y = MathUtils.clamp(interactBox.y,-(Gdx.graphics.getHeight() * cam.zoom)/2, (Gdx.graphics.getHeight() * cam.zoom)/2 - interactBox.h);
		
	}

	protected void setPos(float x, float y){
		//Clamp so it is inside screen
		interactBox.x = x;
		interactBox.y = y;
		updateClamp(0,0);
	}
	
	public boolean isInsideElement(float x, float y){
		return interactBox.inside(x, y);
	}
	
	public int getId(){
		return id;
	}
	
	abstract void mousePos(float x, float y);
	abstract void leftClickEvent(float x, float y);
	abstract void leftDownEvent(float x, float y);
	abstract void leftUpEvent(float x, float y);
	abstract void rightClickEvent(float x, float y);
	abstract void draw(SpriteBatch batch, OrthographicCamera cam, Viewport viewport);
}
