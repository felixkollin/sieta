package com.sieta.game.lighting;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.utils.Array;
import com.sieta.game.world.Chunk;
import com.sieta.game.world.IntPair;
import com.sieta.game.world.Map;
import com.sieta.game.world.Chunk.Layer;
import com.sieta.game.world.Tile;

public class LightEngine {
	private class UpdaterThread extends Thread {
		
		UpdaterThread(Map map) {						
			setDaemon(true);
			start();
		}

		private Array<Chunk> visibleChunks = new Array<Chunk>();
		
		public void updateActiveChunks(Array<Chunk> activeChunks){
			visibleChunks.clear();
			for(Chunk chunk : activeChunks){
				visibleChunks.add(chunk);
			}
		}

		@Override
		public synchronized void run() {
			while(true){
				boolean sleep = true;
				while(sleep){
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (InterruptedException e) {
						sleep = false;
					}
				}
				//Clear whole scene
				for (Chunk chunk : visibleChunks) {
					chunk.clearBackBuffer();
				}
				
				for (Chunk chunk : visibleChunks) {
					castLights(chunk);// Add lights to back buffer
				}
				
				// Swap buffers (Finished whole light calculation)
				for (Chunk chunk : visibleChunks) {
					chunk.swapLightBuffers();
				}
				sleep = true;
			}
		}
	}
	
	private Map map;
	private UpdaterThread updater;
	
	private int endXPos;
    private int endYPos;
    private int beginXPos;
    private int beginYPos;
	
	private int castNum = 0;
	
	private int[][] visited = new int[LightSphere.MAX_RADIUS * 2][LightSphere.MAX_RADIUS * 2];
	private int visitedSize = LightSphere.MAX_RADIUS * 2;
	
	public boolean hasVisited(int lx, int ly, int castNumber) {
		int x = lx + LightSphere.MAX_RADIUS;
		int y = ly + LightSphere.MAX_RADIUS;
		if ((x >= 0 && y >= 0 && x < visitedSize && y < visitedSize)) {
			return visited[x][y] == castNumber;
		}
		return true;
	}
	public void visit(int lx, int ly){
		int x = lx + LightSphere.MAX_RADIUS;
		int y = ly + LightSphere.MAX_RADIUS;
		if ((x >= 0 && y >= 0 && x < visitedSize && y < visitedSize)) {
			visited[x][y] = castNum;
		}
		return;
	}
	
	public LightEngine(Map map) {
		this.map = map;
		updater = new UpdaterThread(map);
	}

	public void update(){
		updater.interrupt();
	}
	
	public void updateBounds(int beginx, int beginy, int endx, int endy, Array<Chunk> activeChunks) {
		endXPos = endx * Chunk.SIZE;
		endYPos = endy * Chunk.SIZE;
	    beginXPos = beginx * Chunk.SIZE;
	    beginYPos = beginy * Chunk.SIZE;  
		updater.updateActiveChunks(activeChunks);
	}
	
	public void castLight(byte lightID, int x, int y, Chunk chunk) {
		
		Color color = LightDef.getLightSphere(lightID).color;
		float brightness = color.a;
		
		if(castNum == Integer.MAX_VALUE){
			castNum = 0;
		}else{
			castNum++;
		}
		
		for(IntPair offset : LightDef.getLightSphere(lightID).getEndPointOffsets()){
			rayOfLight(x, y, x - offset.x, y + offset.y, brightness, color, lightID, chunk);
			rayOfLight(x, y, x - offset.y, y - offset.x, brightness, color, lightID, chunk);
			rayOfLight(x, y, x + offset.x, y - offset.y, brightness, color, lightID, chunk);
			rayOfLight(x, y, x + offset.y, y + offset.x, brightness, color, lightID, chunk);
		}
	}
	
	private void rayOfLight(int x0, int y0, int x1, int y1, float brightness, Color color, byte lightID, Chunk chunk) {
		int dx = Math.abs(x1 - x0);
	    int dy = Math.abs(y1 - y0);
	    int x = x0;
	    int y = y0;
	    int sx = (x1 > x0) ? 1 : -1;
	    int sy = (y1 > y0) ? 1 : -1;
	    
	    int err = dx - dy;
	    
	    float walls = 0;
	    
	    int bx;
	    int by;
	    
	    int err2;
	    while(x != x1 || y != y1){
	    	if (x >= endXPos || x < beginXPos || y >= endYPos || y < beginYPos) {
				return;
			}
	    	
			bx = chunk.worldToLocalX(x);
			by = chunk.worldToLocalY(y);

			//If passing into new chunk
			if(bx < 0 || bx >= Chunk.SIZE || by < 0 || by >= Chunk.SIZE){
				chunk = map.getLoadedChunk(x, y);
				if (chunk == null) {
					return;
				}
				bx = chunk.worldToLocalX(x);
				by = chunk.worldToLocalY(y);
			}
			
	    	if(!hasVisited(x0 - x, y0 - y, castNum)){
				visit(x0 - x, y0 - y);
				chunk.mixNewLight(bx,by,color,brightness);
	    	}
	    	
	    	walls += chunk._getWallReduction(bx, by);
			brightness = LightDef.getMidRelative(x0 - x, y0 - y, lightID) - walls;
			if(brightness <= 0){
				return;
			}
			
			//Prevent diagonal steps:
			err2 = 2 * err;
			if (err2 + dy > dx - err2) {
	            // horizontal step
	            err -= dy;
	            x += sx;
	        } else {
	            // vertical step
	            err += dx;
	            y += sy;
	        }
	    }
	}

	private void castLights(Chunk c) {
		if(!c.hasLight()){
			return;
		}
		if(visited[visitedSize/2][visitedSize/2] == Integer.MAX_VALUE){
			for (int i = 0; i < visitedSize; i++) {
				for (int j = 0; j < visitedSize; j++) {
					visited[i][j] = 0;
				}
			}
		}
		
		byte light;
		for (int x = 0; x < Chunk.SIZE; x++) {
			for (int y = 0; y < Chunk.SIZE; y++) {
				short tileMid = c._getTile(x, y, Layer.MID);
				short tileBack = c._getTile(x, y, Layer.BACK);
				
				//Environment
				if((Tile.isFlowing(tileMid) || tileMid == Tile.air) && tileBack == Tile.air){
					if ((c._hasSolidTileAround(x, y, Layer.MID) || c._hasSolidTileAround(x, y, Layer.BACK)) && (tileMid == Tile.air || Tile.isFlowing(tileMid))) {
						castLight(LightDef.ENVIRONMENT, c.localToWorldX(x), c.localToWorldY(y), c);
					}else{
						c.mixNewLight(x,y,LightDef.environment.color,LightDef.environment.color.a);
					}
				}
				
				light = c.getLocalLightSource(x, y);
				//TODO can be multiple in same location
				if(light != 0){
					castLight(light, c.localToWorldX(x), c.localToWorldY(y), c);
				}
			}
		}
	}

	public void destroy() {
		updater.interrupt();
	}
}
