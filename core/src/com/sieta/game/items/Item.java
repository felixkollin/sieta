package com.sieta.game.items;

import com.sieta.game.world.IntPair;


public class Item {
	public static final int STONE_PICKAXE = 0;
	
	private int id;
	private Type type;
	
	public enum Type{
		TILE, TOOL;
	}
	
	public Item(int id, Type type){
		this.id = id;
		this.type = type;
	}
	
	public Type getType(){
		return type;
	}
	
	public int getId(){
		return id;
	}
	
	public byte getPlaceId(){
		if(type == Type.TILE){
			return (byte) id;
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object o){
		if (getClass() != o.getClass()) return false;
		if (id != ((Item)(o)).getId()) return false;
		if (type != ((Item)(o)).getType()) return false;
		return true;
		
	}
}
