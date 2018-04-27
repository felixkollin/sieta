package com.sieta.game.items;

import com.sieta.game.world.Tile;

/**
 * The hotbar links to slots in the player's inventory.
 * @author felixkollin
 *
 */
public class Hotbar implements ItemContainerListener{
	private ItemContainer itemContainer;
	
	private int numSlots;
	private int numTabs;
	
	private int selectedSlot;
	private int selectedTab;
	
	private Item[][] slots;
	private String[] amountStrings;
	private int[] amounts;
	
	public Hotbar(ItemContainer itemContainer, int numSlots, int numTabs){
		this.itemContainer = itemContainer;
		itemContainer.addListener(this);
		selectedSlot = 0;
		selectedTab = 0;
		this.numSlots = numSlots;
		this.numTabs = numTabs;
		slots = new Item[numSlots][numTabs];
		amounts = new int[numSlots];
		amountStrings = new String[numSlots];
		addItem(new Item(Tile.dirt, Item.Type.TILE), 0);
	}
	
	public void addItem(Item item, int slotNum){
		if(containsItem(item)){
			return;
		}
		slots[slotNum][selectedTab] = item;
	}
	
	public boolean containsItem(Item item){
		for(int i = 0; i < numSlots; i++){
			if(slots[i][selectedTab] == null){
				continue;
			}
			if(slots[i][selectedTab].equals(item)){
				return true;
			}
		}
		return false;
	}
	
	public int getNumSlots(){
		return numSlots;
	}
		
    public void selectSlot(int n){
		if(n >= numSlots){
			selectedSlot = numSlots - 1;
		}else if(n <= 0){
			selectedSlot = 0;
		}else{
			selectedSlot = n - 1;
		}
	}
    
    public Item getItem(int slot){
    	return slots[slot][selectedTab];
    }
    public String getAmountString(int slot){
    	return amountStrings[slot];
    }
    public int getAmount(int slot){
    	return amounts[slot];
    }
    public int getSelectedSlot(){
    	return selectedSlot;
    }

	@Override
	public void amountModification(Item item) {
		for(int i = 0; i < numSlots; i++){
			if(slots[i][selectedTab] == null){
				continue;
			}
			if(slots[i][selectedTab].equals(item)){
				int amount = itemContainer.containsAmount(item);
				amounts[i] = amount;
				amountStrings[i] = Integer.toString(amount);
			}
		}
	}

	public Item getSelectedItem() {
		return slots[selectedSlot][selectedTab];
	}
}
