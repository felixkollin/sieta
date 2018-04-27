package com.sieta.game.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.items.Hotbar;
import com.sieta.game.items.Item;
import com.sieta.game.items.Slot;
import com.sieta.game.utils.Graphics;

public class HotbarElement extends WindowElement{
	public static final int PADDING = 4; //Pixels between the inventory spaces
	public static final int ICON_SIZE = 12;
	
	private Sprite icon;
	
	private Hotbar hotbar;
	
	public HotbarElement(OrthographicCamera cam, int x, int y, boolean visible, Hotbar hotbar, Sprite sprite, int id) {
		super(cam, x, y, PADDING + hotbar.getNumSlots() * (PADDING + ICON_SIZE), PADDING + (PADDING + ICON_SIZE), visible, sprite, id);
		this.hotbar = hotbar;
		icon = new Sprite();
	}
	
	public Item getItemAtPos(float x, float y){
    	int index = getSlotIndexAt(x, y);
    	return hotbar.getItem(index);
    }
	
	private int getSlotIndexAt(float x, float y){
    	//Correct for 0,0
    	float slotX = x - interactBox.x;
    	slotX = slotX/(ICON_SIZE + PADDING);
    	return (int)slotX;
    }
	
	@Override
	void draw(SpriteBatch batch, OrthographicCamera cam, Viewport viewport){
		super.draw(batch, cam, viewport); //Draw the window
		//Draw the slots
  		for(int n = 0; n < hotbar.getNumSlots(); n++){
  			Item item = hotbar.getItem(n);
  			if(item != null){
  				if(item.getType() == Item.Type.TILE){
					icon.setRegion(ResourceHandler.getTileTexture(item.getId()));
				}else{
					//icon = ResourceHandler.getItemSprite(slot.getItem().getId());
				}
  				icon.setPosition(PADDING + interactBox.x + n * (ICON_SIZE + PADDING), PADDING + interactBox.y);
				icon.setSize(ICON_SIZE, ICON_SIZE);
				icon.draw(batch);
				Graphics.drawSmallText(batch, hotbar.getAmountString(n), Graphics.WHITE_COLOR, PADDING + interactBox.x + n * (ICON_SIZE + PADDING) + ICON_SIZE, PADDING + interactBox.y + ICON_SIZE + 7.5f/2 + 1, false);
  			}
		}
	}

}
