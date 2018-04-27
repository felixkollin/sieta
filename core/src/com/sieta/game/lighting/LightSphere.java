package com.sieta.game.lighting;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sieta.game.world.Chunk;
import com.sieta.game.world.IntPair;

public class LightSphere {
	
		public static final int MAX_RADIUS = Chunk.SIZE;
		private float[][] vals;
		private int size;
		private int radius;
		
		private Array<IntPair> preCalc; //Precalc all endpoints for light rays
		
		public Color color;

		private float attenuationLinear;
		private float attenuationQuadratic;

		public LightSphere(int radius, Color color, float attenuationLinear, float attenuationQuadratic) {
			if(radius > MAX_RADIUS){
				radius = MAX_RADIUS;
			}
			if(radius < 0){
				radius = 0;
			}
			this.attenuationQuadratic = attenuationQuadratic;
			this.attenuationLinear = attenuationLinear;
			this.color = color;
			this.radius = radius;
			size = radius * 2;
			vals = new float[radius * 2][radius * 2];
			gen(radius);
			preCalc();
		}
		
		//TODO change light falloff calculation
		private void gen(int radius) {
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					float dist = Vector2.dst(radius, radius, x, y);
					vals[x][y] = 1.0f / (1.0f + attenuationLinear * dist + attenuationQuadratic * dist * dist);
				}
			}
		}
		
		//0 = middle, x + precalc.x is endpoint
		private void preCalc(){
			preCalc = new Array<IntPair>();
			
			int xc = -radius;
			int yc = 0;
			int err = 2 - 2 * radius;
			
			while(xc < 0){
				preCalc.add(new IntPair(xc, yc));
				
				radius = err;
	            if (radius <= yc){
	            	yc++;
	            	err += yc * 2 + 1;
	            }          
	            if (radius > xc || err > yc){
	            	xc++;
	            	err += xc * 2 + 1;
	            }
			}
		}
		
		public Array<IntPair> getEndPointOffsets(){
			return preCalc;
		}
		
		public float getMidRelative(int lx, int ly) {
			int x = lx + size/2;
			int y = ly + size/2;
			if (isValid(x, y)) {
				return vals[x][y];
			}
			return 0f;
		}
		public boolean isValid(int x, int y) {
			return (x >= 0 && y >= 0 && x < size && y < size);
		}
		public float get(int x, int y) {
			return vals[x][y];
		}
		public int getSize() {
			return size;
		}

		public int getRadius() {
			return radius;
		}

}
