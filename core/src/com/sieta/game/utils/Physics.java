package com.sieta.game.utils;


import com.badlogic.gdx.math.Polygon;

public class Physics {
	public static final float PPM = 8; //Pixels per meter
	public static final float GRAVITY = -125f; //Gravity
	
	public static final short CATEGORY_PLAYER = 0x0001;
	public static final short CATEGORY_PROJECTILE = 0x0002;
	public static final short CATEGORY_WORLD = 0x0004;
	public static final short CATEGORY_MOB = 0x0008;
	
	
	public static final short MASK_PLAYER_LIGHT = CATEGORY_MOB | CATEGORY_WORLD;
	//public static final short MASK_PLAYER = CATEGORY_MOB;
	public static final short MASK_PLAYER = CATEGORY_WORLD;
	public static final short MASK_MOB = CATEGORY_WORLD | CATEGORY_PROJECTILE;
	public static final short MASK_PROJECTILE = CATEGORY_WORLD | CATEGORY_MOB;
	public static final short MASK_PROJECTILE_P = CATEGORY_PLAYER | CATEGORY_WORLD | CATEGORY_MOB;
	public static final short MASK_WORLD = CATEGORY_PROJECTILE | CATEGORY_PLAYER | CATEGORY_MOB;

	public static final Polygon tilePolygon = new Polygon(new float[]{0,0,1f,0,1f,1f,0,1f});
	
	//Needs to be this shape because this determines how early player snaps to slopes, the top is for overlapping
	//			  /I               I\
	//			/  I			   I  \
	// Right: /    I		 Left: I    \
	//       I_____I			   I_____I
	//      
	//
	public static final Polygon rightSnapPoly = new Polygon(new float[]{0,0,0,0.2f,1f,1.2f,1f,0});
	public static final Polygon leftSnapPoly = new Polygon(new float[]{0,0,1f,0,1f,0.2f,0,1.2f});
	
	//For physics other than snapping, we want the shape you expect:
	public static final Polygon rightSlopePoly = new Polygon(new float[]{0,0,1f,1f,1f,0});
	public static final Polygon leftSlopePoly = new Polygon(new float[]{0,0,0f,1f,1f,0});
	
	//For peak, use this shape:
	//
	//       /\
	//		/__\
	//
	public static final Polygon peakSlopePoly = new Polygon(new float[]{0,0,0.5f,0.5f,1f,0});
}
