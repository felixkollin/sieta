package com.sieta.game.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.items.ItemContainer;
import com.sieta.game.items.Item;
import com.sieta.game.items.Slot;
import com.sieta.game.utils.Graphics;

public class ItemContainerElement extends WindowElement{
	public static final int PADDING= 4; //Pixels between the inventory spaces
	public static final int ICON_SIZE = 12;
	
	ItemContainer itemContainer;
	Slot cursorSlot; // The inventory interactions behave differently depending on cursorSlot
	
	private Sprite icon;

	public ItemContainerElement(OrthographicCamera cam, int x, int y, boolean visible,
			Sprite sprite, ItemContainer itemContainer, Slot cursorSlot, int id) {
		super(cam, x, y, PADDING + itemContainer.getCols() * (PADDING + ICON_SIZE), PADDING + itemContainer.getRows() * (PADDING + ICON_SIZE), visible, sprite, id);
		this.itemContainer = itemContainer;
		this.cursorSlot = cursorSlot;
		icon = new Sprite();
	}
	
	public Slot getSlotAtPos(float x, float y){
    	Vector2 index = getSlotIndexAt(x, y);
    	return itemContainer.getSlot((int)index.x,(int)index.y);
    }
	
	private Vector2 getSlotIndexAt(float x, float y){
    	//Correct for 0,0
    	float slotX = x - interactBox.x;
    	float slotY = y - interactBox.y;
    	slotX = slotX/(ICON_SIZE + PADDING);
    	slotY = slotY/(ICON_SIZE + PADDING);
    	return new Vector2((int)slotX,(int)slotY);
    }

	@Override
	void leftClickEvent(float x, float y) {
		Slot slot = getSlotAtPos(x, y);
		if(slot == null){
			return;
		}
		if(cursorSlot.getItem() == null){
			cursorSlot.add(slot.getItem(), slot.getAmount());
			slot.take(slot.getAmount());
		}else{
			if(slot.add(cursorSlot.getItem(), cursorSlot.getAmount())){ //Group together
				cursorSlot.take(cursorSlot.getAmount());
			}else{ // Switch the slots
				Item tmpItem = slot.getItem();
				int tmpAmount = slot.getAmount();
				
				slot.take(slot.getAmount());
				slot.add(cursorSlot.getItem(), cursorSlot.getAmount());
				
				cursorSlot.take(cursorSlot.getAmount());
				cursorSlot.add(tmpItem, tmpAmount);
			}
		}
	}

	@Override
	void rightClickEvent(float x, float y) {
		Slot slot = getSlotAtPos(x, y);
		if(slot == null){
			return;
		}
		
		if(cursorSlot.getItem() == null){
			cursorSlot.add(slot.getItem(), slot.getAmount()/2);
			slot.take(slot.getAmount()/2);
		}else if(slot.getItem() != null){
			if(cursorSlot.getItem().getId() == slot.getItem().getId()){
				slot.add(cursorSlot.getItem(), 1);
				cursorSlot.take(1);
			}
		}else{//Cursor != null, slot == null
			slot.add(cursorSlot.getItem(), 1);
			cursorSlot.take(1);
		}
	}
	
	@Override
	void draw(SpriteBatch batch, OrthographicCamera cam, Viewport viewport){
		super.draw(batch, cam, viewport); //Draw the window
  		for(int y = 0; y < itemContainer.getRows(); y++){
			for(int x = 0; x < itemContainer.getCols(); x++){
				Slot slot = itemContainer.getSlot(x, y);
				if(slot.getItem() != null){
					if(slot.getItem().getType() == Item.Type.TILE){
						icon.setRegion(ResourceHandler.getTileTexture(slot.getItem().getId()));
					}else{
						//icon = ResourceHandler.getItemSprite(slot.getItem().getId());
					}
					icon.setPosition(PADDING + interactBox.x + x * (ICON_SIZE + PADDING), PADDING + interactBox.y + y * (ICON_SIZE + PADDING));
					icon.setSize(ICON_SIZE, ICON_SIZE);
					icon.draw(batch);
					Graphics.drawSmallText(batch, slot.getAmountString(), Graphics.WHITE_COLOR, PADDING + interactBox.x + x * (ICON_SIZE + PADDING) + ICON_SIZE, PADDING + interactBox.y + y * (ICON_SIZE + PADDING) + ICON_SIZE + 7.5f/2 + 1, false);
				}
			}
		}
	}

}
