package com.sieta.game.utils;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.sieta.game.world.OverlayPair;

public class GMath {
	
	public static Random rand = new Random();
	
	/**
	 * Value between 0 (inclusive) and limit (exclusive)
	 * @param limit
	 * @return
	 */
	public static int randomInt(int limit){
		return rand.nextInt(limit);
	}
	
	public static int roundClosest(float a){
		return (int) (a + 0.5);
	}
	
	public static float roundAccurate(float n, float accuracy) {
		return (int)Math.round(n * accuracy) / accuracy;
	}
	
	public static int floor(float x){
	    if (x >= 0){
	        return (int) x;
	    }else{
	        int y = (int)x;
	        return ((float)y == x) ? y : y - 1;
	    }
	}
	
	public static void sortOverlay(OverlayPair[] v){
		OverlayPair tmp;
	    if ( v[0].id > v[1].id ){
	    	tmp = v[1];
	    	v[1] = v[0];
	    	v[0] = tmp;
	    }
	    if ( v[0].id > v[2].id ){
	    	tmp = v[0];
	    	v[0] = v[2];
	    	v[2] = tmp;
	    }
	    if ( v[0].id > v[3].id ){
	    	tmp = v[3];
	    	v[3] = v[0];
	    	v[0] = tmp;
	    }
	    if ( v[1].id > v[2].id ){
	    	tmp = v[1];
	    	v[1] = v[2];
	    	v[2] = tmp;
	    }
	    if ( v[1].id > v[3].id ){
	    	tmp = v[3];
	    	v[3] = v[1];
	    	v[1] = tmp;
	    }
	    if ( v[2].id > v[3].id ){
	    	tmp = v[3];
	    	v[3] = v[2];
	    	v[2] = tmp;
	    }
	    return;
	}
}
