package com.sieta.game.items;

public class Slot {

    private Item item;
    private int amount;
    private String amountString;
    
    public Slot(){
    	item = null;
    	amount = 0;
    	amountString = "";
    }
    public Slot(Item item, int amount) {
        this.item = item;
        this.amount = amount;
        amountString = Integer.toString(amount);
    }

    public boolean isEmpty() {
        return item == null || amount <= 0;
    }

    public boolean add(Item item, int amount) {
    	if(item == null){
    		return false;
    	}
        if (this.item == null) {
            this.item = item;
            this.amount += amount;
            amountString = Integer.toString(this.amount);
            return true;
        }else if(this.item.getId() == item.getId()){
        	this.item = item;
            this.amount += amount;
            amountString = Integer.toString(this.amount);
            return true;
        }

        return false;
    }

    public boolean take(int amount) {
        if (this.amount >= amount) {
            this.amount -= amount;
            amountString = Integer.toString(this.amount);
            if (this.amount == 0) {
                item = null;
            }
            return true;
        }

        return false;
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }
    
    //String is stored for low string creation
    public String getAmountString() {
        return amountString;
    }
}
