package com.sieta.game.hud;

import java.util.ListIterator;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sieta.game.entities.Player;
import com.sieta.game.handlers.MyInput;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.items.Hotbar;
import com.sieta.game.items.Item;
import com.sieta.game.items.Slot;
import com.sieta.game.utils.Graphics;

public class GameHud {
	private OrthographicCamera cam;
	private Viewport viewport;
	private Slot cursorSlot; //To render cursorSlot
	
	public static final int HOTBAR = 1;
	public static final int P_INV = 2;
	
	Sprite cursorSprite = new Sprite();
	
	private Stack<InteractElement> elements = new Stack<InteractElement>();
	
	private static StringBuilder cursorAmount = new StringBuilder();
	private Player player;
	private Hotbar hotbar;
	
	private int lastWidth;
	private int lastHeight;
	
	public GameHud(OrthographicCamera cam, Viewport viewport, Slot cursorSlot, Player player) {
		this.player = player;
		this.cam = cam;
		this.viewport = viewport;
		this.cursorSlot = cursorSlot;
		hotbar = new Hotbar(player.getInventory(), 9, 3);
		lastWidth = Gdx.graphics.getWidth();
		lastHeight = Gdx.graphics.getHeight();
	}
	
	public Hotbar getHotbar(){
		return hotbar;
	}
	
	public void addElement(InteractElement element){
		elements.push(element);
	}
	
	private InteractElement getElement(int id){
		for(InteractElement e : elements){
			if(e.getId() == id){
				return e;
			}
		}
		return null;
	}
	
	public void toggleVisible(int id){
		InteractElement e = getElement(id);
		if(e != null){
			e.toggleVisible();
		}
	}
	
	//private void bringToFront

	public void update() {
		// Update things on hud
	}
	
	/**
	 * Returns true if the hud is interacted with. Used to disable any in-game
	 * input handling.
	 * @return
	 */
	public boolean interactInput(){
		boolean interacted = false;
		//If interacts with element, bring it to front of stack
		InteractElement bringToFront = null;
		boolean front = true;
		ListIterator<InteractElement> it = elements.listIterator(elements.size());
		while(it.hasPrevious()){
			InteractElement e = it.previous();

			if(!e.isOpen())continue;
			
			Vector3 pos = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			
			if(MyInput.leftUp){
				e.leftUpEvent(pos.x,pos.y);
			}
			e.mousePos(pos.x, pos.y);
			
			if(!e.isInsideElement(pos.x, pos.y)){
				front = false;
				continue;
			}
			
			if(!interacted){
				if(MyInput.leftDown){
					e.leftDownEvent(pos.x,pos.y);
					if(!front){
						bringToFront = e;
					}
				}
				if(MyInput.isRightClick()){
					e.rightClick(pos.x, pos.y);
					if(!front){
						bringToFront = e;
					}
					
				}
				if(MyInput.isLeftClick()){
					e.leftClick(pos.x, pos.y);
					if(!front){
						bringToFront = e;
					}
				}
			}
			front = false;
			interacted = true;
		}
		
		if(bringToFront != null){
			elements.remove(bringToFront);
			elements.push(bringToFront);
		}
		return interacted;
	}

	public void dispose() {
	}
	
	private void renderCursorSlot(SpriteBatch batch){
		//TODO precalc this
		Item cursorItem = cursorSlot.getItem();
		Vector3 pos = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
		pos.x -= 6;
		pos.y -= 6;
		cursorItem = cursorSlot.getItem();
		if(cursorItem != null){
			if(cursorItem.getType() == Item.Type.TILE){
				cursorSprite.setRegion(ResourceHandler.getTileTexture(cursorItem.getId()));
			}else{
				//cursorSprite = ResourceHandler.getItemSprite(cursorItem.getId());
			}
			cursorSprite.setSize(12, 12);
			cursorSprite.setPosition(pos.x, pos.y);
			cursorSprite.draw(batch);
			Graphics.drawSmallText(batch, cursorSlot.getAmountString(), Graphics.WHITE_COLOR, pos.x + 12, pos.y + 17, false);
		}
	}

	// Call inside batch begin and end
	public void render(SpriteBatch batch) {
		batch.setProjectionMatrix(cam.combined);
		//TODO needs to fix texture swapping because of text, draw text last and cache static text

		// Iterate in reverse.
		for(InteractElement e : elements){
			if(!e.isOpen())continue;
			e.render(batch, cam, viewport);
		}
		
		renderCursorSlot(batch);
		Graphics.drawSmallText(batch, Integer.toString(Gdx.graphics.getFramesPerSecond()), Color.RED, -100, 0, false);
		batch.end();
	}
	
	public void resize(int width, int height){
		for(InteractElement e : elements){
			e.updateClamp(lastWidth - Gdx.graphics.getWidth(), lastHeight - Gdx.graphics.getHeight());
		}
		lastWidth = Gdx.graphics.getWidth();
		lastHeight = Gdx.graphics.getHeight();
	}
}
