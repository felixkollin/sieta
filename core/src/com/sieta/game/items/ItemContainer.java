package com.sieta.game.items;

import com.badlogic.gdx.utils.Array;
import com.sieta.game.lighting.LightDef;
import com.sieta.game.world.Tile;

public class ItemContainer {	
	private int numRows;
	private int numColumns;

    private Slot[][] slots;
    private Array<ItemContainerListener> listeners;
        
    public ItemContainer() {
    	listeners = new Array<ItemContainerListener>();
    	numColumns = 9;
    	numRows = 4;
    	slots = new Slot[numColumns][numRows];
    	
    	for(int x = 0; x < numColumns; x++){
    		for(int y = 0; y < numRows; y++){
    			slots[x][y] = new Slot();
    		}
    	}
    }
    
    public int getRows(){
    	return numRows;
    }
    public int getCols(){
    	return numColumns;
    }
    
    public Item getHotbarItem(int x){
    	return slots[x][0].getItem();
    }

    public int containsAmount(Item item) {
        int amount = 0;
        for(int x = 0; x < numColumns; x++){
    		for(int y = 0; y < numRows; y++){
    			if(slots[x][y].getItem() == null){
    				continue;
    			}
    			if(slots[x][y].getItem().equals(item)){
    				amount += slots[x][y].getAmount();
    			}
    		}
    	}
        return amount;
    }
    
    public void addListener(ItemContainerListener listener){
    	listeners.add(listener);
    }
    
    public boolean hasItem(Item item){
    	for(int y = 0; y < numRows; y++){
    		for(int x = 0; x < numColumns; x++){
    			if(slots[x][y].getItem() == null){
    				continue;
    			}
    			if (slots[x][y].getItem().equals(item) && slots[x][y].getAmount() > 0) {
                    return true;
                }
    		}
    	}
    	return false;
	}
    
    public boolean remove(Item item, int amount){
    	Slot itemSlot = firstSlotWithItem(item);
    	if(itemSlot == null){
    		return false;
    	}
    	if(itemSlot.take(amount)){
    		notifyAmountModification(item);
    		return true;
    	}
    	return false;
    }
    public boolean remove(Item item){
    	return remove(item, 1);
    }
    
    private void notifyAmountModification(Item item){
    	for(ItemContainerListener listener : listeners){
    		listener.amountModification(item);
    	}
    }

    public boolean store(Item item, int amount) {
        // first check for a slot with the same item type
        Slot itemSlot = firstSlotWithItem(item);
        if (itemSlot != null) {
            itemSlot.add(item, amount);
            notifyAmountModification(item);
            return true;
        } else {
            // now check for an available empty slot
            Slot emptySlot = firstSlotWithItem(null);
            if (emptySlot != null) {
                emptySlot.add(item, amount);
                notifyAmountModification(item);
                return true;
            }
        }
        // no slot to add
        return false;
    }

    public Slot[][] getSlots() {
        return slots;
    }

    private Slot firstSlotWithItem(Item item) {
    	for(int y = 0; y < numRows; y++){
    		for(int x = 0; x < numColumns; x++){
    			if(item == null){
    				if(slots[x][y].getItem() == null){
    					return slots[x][y];
        			}
    				continue;
				}
    			
    			if(slots[x][y].getItem() == null){
    				continue;
    			}
    			if (slots[x][y].getItem().equals(item)) {
                    return slots[x][y];
                }
    		}
    	}
        return null;
    }
    
    public Slot getSlot(int x, int y){
    	if(x >= numColumns || y >= numRows || y < 0 || x < 0){
    		return null;
    	}
    	return slots[x][y];
    }
}