package com.sieta.game.world;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.sieta.game.lighting.LightEngine;
import com.sieta.game.utils.GMath;
import com.sieta.game.world.Chunk.Layer;

public class Map {
	//Active chunks are the ones on the screen
	private static final int ACTIVE_X = 2;
	private static final int ACTIVE_Y = 1;
	private Array<Chunk> activeChunks = new Array<Chunk>();
	
	//Chunks loaded into RAM, outside this radius are saved to disk
	private static final int LOAD_X = 4;
	private static final int LOAD_Y = 3;
	private HashMap<IntPair, Chunk> loadedChunks = new HashMap<IntPair, Chunk>();

	public WorldCamera worldCam;
	
	//Loaded chunks has mobs, items and are updating water, growth etc
	//Active chunks are the chunks on screen, used to update lighting and placement
	
	private Vector2 lastMiddleCoords = new Vector2(0,0);
	private Vector2 middleCoords = new Vector2(0,0);
	private Vector2 middlePos = new Vector2();
	
	private final Pool<Chunk> chunkPool = new Pool<Chunk>() {
		@Override
		protected Chunk newObject() {
			return new Chunk();
		}
	};
		
	public Map(WorldCamera worldCam, ShaderProgram shader) {
		this.worldCam = worldCam;
		updateMiddlePos();
		loadAllChunks();
	}
	
	public void init(LightEngine lightEngine){
		updateActiveChunks();
		lightEngine.updateBounds(Math.round(middleCoords.x - ACTIVE_X), Math.round(middleCoords.y - ACTIVE_Y), Math.round(middleCoords.x + ACTIVE_X + 1), Math.round(middleCoords.y + ACTIVE_Y + 1), activeChunks);
				
		for (Chunk chunk : activeChunks) {
			chunk.calculateMasses();
		}
		for (Chunk chunk : activeChunks) {
			chunk.updateMassStates();
			chunk.resetModified();
		}
		
		lightEngine.update();
	}
	
	private void updateMiddlePos(){
		lastMiddleCoords.x = middleCoords.x;
		lastMiddleCoords.y = middleCoords.y;		
		middleCoords.x = worldToCoord(worldCam.getPosition().x);
		middleCoords.y = worldToCoord(worldCam.getPosition().y);
		middlePos.set(worldCam.getPosition().x, worldCam.getPosition().y);
	}
	
	public enum Slope{
		LEFT,RIGHT,NONE,PEAK;
	}
	
	//--- CHUNKS ---	
	
	//Load and unload
	private void loadVerticalChunks(boolean left) {
		if(left){
			for (int y = -LOAD_Y; y <= LOAD_Y; y++) {
				Chunk removed = loadedChunks.remove(new IntPair((int)middleCoords.x + LOAD_X + 1, (int)middleCoords.y + y));
				if(removed != null){
					chunkPool.free(removed);
				}
				
				loadChunk((int)middleCoords.x - LOAD_X, (int)middleCoords.y + y);
			}
		}else{
			for (int y = -LOAD_Y; y <= LOAD_Y; y++) {
				Chunk removed = loadedChunks.remove(new IntPair((int)middleCoords.x - LOAD_X - 1, (int)middleCoords.y + y));
				if(removed != null){
					chunkPool.free(removed);
				}
				
				loadChunk((int)middleCoords.x + LOAD_X, (int)middleCoords.y + y);
			}
		}
	}
	private void loadHorizontalChunks(boolean up) {
		if(up){
			for (int x = -LOAD_X; x <= LOAD_X; x++) {
				Chunk removed = loadedChunks.remove(new IntPair((int)middleCoords.x + x, (int)middleCoords.y - LOAD_Y - 1));
				if(removed != null){
					chunkPool.free(removed);
				}

				loadChunk((int)middleCoords.x - x, (int)middleCoords.y + LOAD_Y);
			}
		}else{
			for (int x = -LOAD_X; x <= LOAD_X; x++) {
				Chunk removed = loadedChunks.remove(new IntPair((int)middleCoords.x - x, (int)middleCoords.y + LOAD_Y + 1));
				if(removed != null){
					chunkPool.free(removed);
				}

				loadChunk((int)middleCoords.x + x, (int)middleCoords.y - LOAD_Y);
			}
		}
	}
	
	public void loadAllChunks() {
		for (int x = -LOAD_X; x <= LOAD_X; x++) {
			for (int y = -LOAD_Y; y <= LOAD_Y; y++) {
				loadChunk((int)middleCoords.x + x, (int)middleCoords.y + y);
			}
		}
	}

	public void loadChunk(int x, int y) {
		IntPair coords = new IntPair(x, y);
		if (loadedChunks.containsKey(coords)) {
			return;
		}
		Chunk loadChunk = chunkPool.obtain();
		loadChunk.init(this, Math.round(coords.x), Math.round(coords.y));
		loadedChunks.put(coords, loadChunk);
	}
	
	private void updateActiveChunks(){
		activeChunks.clear();
		for (int x = -ACTIVE_X; x <= ACTIVE_X; x++) {
			for (int y = -ACTIVE_Y; y <= ACTIVE_Y; y++) {
				activeChunks.add(loadedChunks.get(new IntPair((int)middleCoords.x + x, (int)middleCoords.y + y)));
			}
		}
	}
	
	private int worldToCoord(float world){
		return (int)(GMath.floor(world/Chunk.SIZE));
	}
	
	public Chunk getLoadedChunkCoords(int x, int y) {
		return loadedChunks.get(new IntPair(x,y));
	}
	public Chunk getLoadedChunk(int worldX, int worldY) {
		return loadedChunks.get(new IntPair(worldToCoord(worldX), worldToCoord(worldY)));
	}

	//--- TILES ---
	
	public boolean addTile(short id, int x, int y, Layer layer) {
		if(id == Tile.air){
			return false;
		}
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return false;
		}
		if(chunk.addTile(id, x, y, layer)){
			return true;
		}
		return false;
	}
	
	public short getTile(int x, int y, Layer layer) {
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return Tile.air;
		}
		return chunk.getTile(x, y, layer);
	}

	public boolean hasTile(int x, int y, Layer layer) {
		if(getTile(x, y, layer) != Tile.air){
			return true;
		}
		return false;
	}

	public boolean hasAnyTile(Chunk chunk, int x, int y){
		if (chunk != null) {
			if(chunk.getTile(x, y, Layer.BACK) != Tile.air || chunk.getTile(x, y, Layer.MID) != Tile.air){
				return true;
			}
		}
		return false;
	}
	
	public Slope getSlope(int x, int y){
		Chunk chunk = getLoadedChunk(x,y);
		if(chunk != null){
			return chunk.getSlope(x,y);
		}
		return Slope.NONE;
	}

	public boolean hasTileAround(int x, int y, Chunk.Layer layer) {
		Chunk chunk = getLoadedChunk(x, y);
		if(chunk != null){
			return chunk.hasTileAround(x, y, layer);
		}
		return false;
	}

	public boolean removeTile(int x, int y, Layer layer) {
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return false;
		}
		if(chunk.removeTile(x, y, layer)){
			return true;
		}
		return false;
	}
	
	public float getMass(int x, int y) {
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return 0;
		}
		return chunk.getMass(x, y);
	}
	public float flow(int x, int y, float remainingMass, float sourceMass, Chunk.Direction dir, short liquidId) {
		Chunk chunk = getActiveChunk(x, y);
		if (chunk == null) {
			return 0;
		}
		return chunk.flow(x, y, remainingMass, sourceMass, dir, liquidId);
	}
	
	public Chunk getActiveChunk(int x, int y){
		if(x < middlePos.x - ACTIVE_X * Chunk.SIZE || y < middlePos.y - ACTIVE_Y * Chunk.SIZE){
			return null;
		}
		if(x > middlePos.x + ACTIVE_X * Chunk.SIZE || y > middlePos.y + ACTIVE_Y * Chunk.SIZE){
			return null;
		}
		return getLoadedChunk(x,y);
	}
	
	//--- LIGHTING ---
	public boolean removeLight(int x, int y){
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return false;
		}
		if(chunk.removeLightSource(x, y)){
			return true;
		}
		return false;
	}
	public boolean addLight(byte id, int x, int y){
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return false;
		}
		chunk.addLightSource(id, x, y);
		return true;
	}
	public Color getLight(int x, int y, boolean frontbuffer) {
		Chunk chunk = getLoadedChunk(x, y);
		if (chunk == null) {
			return null;
		}
		return chunk.getLight(chunk.worldToLocalX(x), chunk.worldToLocalY(y), frontbuffer);
	}
	
	//--- RENDER/UPDATE ---
	
	public void update(LightEngine lightEngine) {
		updateMiddlePos();
		
		//Only update if changing chunk
		if(Math.abs(middleCoords.x - lastMiddleCoords.x) > 0){
			loadVerticalChunks(middleCoords.x - lastMiddleCoords.x == -1);
			updateActiveChunks();
			lightEngine.updateBounds(Math.round(middleCoords.x - ACTIVE_X), Math.round(middleCoords.y - ACTIVE_Y), Math.round(middleCoords.x + ACTIVE_X + 1), Math.round(middleCoords.y + ACTIVE_Y + 1), activeChunks);
		}else if(Math.abs(middleCoords.y - lastMiddleCoords.y) > 0){
			loadHorizontalChunks(middleCoords.y - lastMiddleCoords.y == 1);
			updateActiveChunks();
			lightEngine.updateBounds(Math.round(middleCoords.x - ACTIVE_X), Math.round(middleCoords.y - ACTIVE_Y), Math.round(middleCoords.x + ACTIVE_X + 1), Math.round(middleCoords.y + ACTIVE_Y + 1), activeChunks);
		}
		
		//Use tick timer to update loaded chunks, crops etc
		
		boolean anyModifiedChunk = false;
		
		for (Chunk chunk : activeChunks) {
			chunk.calculateMasses();
		}
		for (Chunk chunk : activeChunks) {
			chunk.updateMassStates();
			if(chunk.isModified()){
				anyModifiedChunk = true;
			}
			chunk.resetModified();
		}
		
		if(anyModifiedChunk){
			lightEngine.update();
		}
	}

	public void renderBackground(SpriteBatch batch) {
		for (Chunk chunk : activeChunks) {
			chunk.renderBackground(batch);
		}
	}
	public void renderForeground(SpriteBatch batch) {
		for (Chunk chunk : activeChunks) {
			chunk.renderForeground(batch);
		}
	}
	
	public void renderLightColors(SpriteBatch batch) {
		for (Chunk chunk : activeChunks) {
			chunk.renderLightColors(batch);
		}
	}

	// Return the last X (to the right side of the screen), used when chunk rendering
	//Add some amount of tiles to render a bit outside bounds
	public int getEndWorldX() {
		return Math.round(middlePos.x + (worldCam.getCam().viewportWidth * worldCam.getCam().zoom)/2f + 3f);
	}

	public int getEndWorldY() {
		return Math.round(middlePos.y + (worldCam.getCam().viewportHeight * worldCam.getCam().zoom)/2f + 2f);
	}

	public int getStartWorldX() {
		return Math.round(middlePos.x - (worldCam.getCam().viewportWidth * worldCam.getCam().zoom)/2f - 2f);
	}

	public int getStartWorldY() {
		return Math.round(middlePos.y - (worldCam.getCam().viewportHeight * worldCam.getCam().zoom)/2f - 1f);
	}
}
