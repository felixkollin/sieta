package com.sieta.game.hud;

/**
 * For hotbar, itemcontainers, equipment etc
 * @author felixkollin
 *
 */
public class CellArea {
	private int cellSize;
	private int padding;
	private int borderPadding;
	private int cellsX;
	private int cellsY;
	
	private float x;
	private float y;
	
	public CellArea(int cellSize, int padding, int borderPadding, int cellsX, int cellsY, float x, float y){
		this.cellSize = cellSize;
		this.padding = padding;
		this.borderPadding = borderPadding;
		this.cellsX = cellsX;
		this.cellsY = cellsY;
		
		this.x = x;
		this.y = y;
	}
	
	private int getCellIndexXAt(float x){
		return 0;
    }
	
	private int getCellIndexYAt(float y){
		return 0;
    }
}
