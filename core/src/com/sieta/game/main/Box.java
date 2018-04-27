package com.sieta.game.main;

public class Box{
	 // position of bottom-left corner
    public float x, y;

    // dimensions
    public float w, h;
    
    public Box(float x, float y, float w, float h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    
    public boolean inside(float x, float y){
    	return (Math.abs(x - centerX()) < w * 0.5f && Math.abs(y - centerY()) < h * 0.5f);
    }
    
    public float centerX(){
    	return x + w * 0.5f;
    }
    public float centerY(){
    	return y + h * 0.5f;
    }
    
    public boolean intersects(Box box){
    	if(Math.abs(centerX() - box.centerX()) < (w + box.w) * 0.5f){
	         if(Math.abs(centerY() - box.centerY()) < (h + box.h) * 0.5f){
	        	 return true;
	         }
	    }
    	return false;
    }
    
}

   
