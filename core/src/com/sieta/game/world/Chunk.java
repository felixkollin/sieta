package com.sieta.game.world;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.lighting.LightDef;
import com.sieta.game.utils.GMath;
import com.sieta.game.world.Map.Slope;

public class Chunk implements Poolable{
	public enum Layer {
		BACK, MID;
	}
	public enum Direction {
		NORTH, NORTH_E, EAST, SOUTH_E, SOUTH, SOUTH_W, WEST, NORTH_W;
	}
	
	public static final int SIZE = 32;
	private static Color[][] swapBuffer; // Helper for flipbuffer method
	private static OverlayPair[] overlayQueue = new OverlayPair[4];
	
	static{
		for(int i = 0; i < 4; i++){
			overlayQueue[i] = new OverlayPair();
		}
	}
		
	private byte[][] lightSources;
	
	private Color[][] lightFront;
	private Color[][] lightBack;
	
	private short[][] tileBack;
	private short[][] tileMid;
	
	private float[][] massData;
	private float[][] newMass;
				
	public int xCoord;
	public int yCoord;
		
	private Map map;
		
	//So we only send one modification call to lightEngine if multiple blocks are modified in one update
	private boolean modified;
	private boolean hasLight;
	
	//TODO when loading, retrieve neighbour list
	
	/**
     * Callback method when the object is freed. It is automatically called by Pool.free()
     * Must reset every meaningful field of this chunk.
     */
    @Override
    public void reset() {
    }
    
    /**
     * Initialize the chunk. Call this method after getting a chunk from the pool.
     * Clears all data from previous chunk
     */
    public void init(Map map, int xCoord, int yCoord) {
    	this.map = map;
    	
		modified = true;
		hasLight = false;
		
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		
		//TODO load from file if it exists
		generate();
	}
    
    private void resetValues(int x, int y){
    	lightBack[x][y].set(0, 0, 0, 0);
		lightFront[x][y].set(0, 0, 0, 0);
		massData[x][y] = 0;
		newMass[x][y] = 0;
		tileBack[x][y] = 0;
		tileMid[x][y] = 0;
		lightSources[x][y] = 0;
    }
    
    private void generate(){
    	double surfaceY;
		int worldY;
		double distanceMod;
		short id = Tile.dirt;
		for (int x = 0; x < SIZE; x++) {
			surfaceY = Terrain.getHeightValue(x + SIZE * xCoord); //Get from map?, dont calc for each chunk
			for (int y = 0; y < SIZE; y++) {
				resetValues(x,y);
				
				worldY = y + SIZE * yCoord;
				if (worldY <= surfaceY + 1) { // Under surface
					distanceMod = 1 - MathUtils.clamp(Math.abs(worldY - surfaceY)/10, 0.1, 1);
					if (Terrain.getCaveValue(x + SIZE * xCoord + 100, worldY)
							- distanceMod < -0.25) {
						if (worldY >= surfaceY - 1 && worldY <= 0) {
							id = Tile.sand;
						} else {
							id = Tile.dirt;
						}
					} else {
						id = Tile.stone;
					}

					if (Terrain.getCaveValue(x + SIZE * xCoord, worldY)
							- distanceMod <= -0.25f || (worldY >= surfaceY - 1 && worldY <= 0)) {
						_setTile(id, x, y, Layer.MID);
					}
					_setTile(id, x, y, Layer.BACK);
				}
				if (worldY <= 0) { // Under waterlevel
					if (tileMid[x][y] == Tile.air && tileBack[x][y] == Tile.air) {
						_setTile(Tile.water, x, y, Layer.MID);
						//TODO Ocean water should be fixed pressure (no wobbly when generating in)
					}
				}
			}
		}
    }
	
	public Chunk(){
		tileBack = new short[SIZE][SIZE];
		tileMid = new short[SIZE][SIZE];
		
		massData = new float[SIZE][SIZE];
		newMass = new float[SIZE][SIZE];
		
		lightFront = new Color[SIZE][SIZE];
		lightBack = new Color[SIZE][SIZE];
		lightSources = new byte[SIZE][SIZE];
		
		
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				lightBack[x][y] = new Color();
				lightFront[x][y] = new Color();
			}
		}
		
		modified = false;
		hasLight = false;
	}
	
	public boolean environmentLight(int x, int y){
		return tileMid[x][y] == Tile.air && tileBack[x][y] == Tile.air;
	}
	
	public boolean hasLight(){
		return hasLight;
	}
	
	public void resetModified(){
		modified = false;		
	}
	public boolean isModified(){
		return modified;
	}
	public void renderBackground(SpriteBatch batch){
		int xEnd = culledEnd(true);
		int yEnd = culledEnd(false);
		int xStart = culledStart(true);
		int yStart = culledStart(false);
		Sprite overlay;
		short midId;
		for (int x = xStart; x < xEnd; x++) {
			for (int y = yStart; y < yEnd; y++) {
				midId = tileMid[x][y];
				//TODO Check for "hasAlpha" on mid block instead
				if(tileBack[x][y] != Tile.air){
					if (!Tile.isPhysical(midId) || Tile.isOneWay(midId)) {
						Tile.render(batch, tileBack[x][y],localToWorldX(x), localToWorldY(y), true);
					}
				}
				
				if(lightFront[x][y].a <= 0 && tileBack[x][y] != Tile.air) continue;
				
				updateOverlayQueue(tileBack[x][y],x,y,Layer.BACK);
				GMath.sortOverlay(overlayQueue);
				for(int i = 0; i < 4; i++){
					if(overlayQueue[i].id > tileBack[x][y]){
						overlay = ResourceHandler.getOverlaySprite(overlayQueue[i].id, overlayQueue[i].direction, localToWorldX(x), localToWorldY(y));
						overlay.setColor(0.35f,0.35f,0.35f,1f);
						overlay.draw(batch);
					}
				}
				
				if(midId == Tile.air){
					continue;
				}
				//Render non physical (behind player, in front of background)
				if (Tile.isObject(midId) || Tile.isOneWay(midId)) {
					Tile.render(batch, tileMid[x][y],localToWorldX(x), localToWorldY(y), false);
				}
			}
		}
	}
	
	public void renderForeground(SpriteBatch batch){
		int xEnd = culledEnd(true);
		int yEnd = culledEnd(false);
		int xStart = culledStart(true);
		int yStart = culledStart(false);
		
		short midId;
		Sprite overlay;
		for (int x = xStart; x < xEnd; x++) {
			for (int y = yStart; y < yEnd; y++) {
				midId = tileMid[x][y];
				if (massData[x][y] >= Flowing.MIN_DRAW) {
					midId = tileMid[x][y];
					Flowing.render(batch, midId, localToWorldX(x), localToWorldY(y), massData[x][y], _getMassRec(x, y + 1), _getMassRec(x, y - 1), getNeighbourTile(x,y,1,false,Layer.MID), getNeighbourTile(x,y,-1,false,Layer.MID));
				}else if (!(Tile.isObject(midId) || midId == Tile.air || Tile.isOneWay(midId)) && !Tile.isFlowing(midId)){
					Tile.render(batch, midId,localToWorldX(x), localToWorldY(y), false);
				}
				//TODO removed
				if(lightFront[x][y].a <= 0){
					if(midId != Tile.air || tileBack[x][y] != Tile.air) continue;
				}
				
				updateOverlayQueue(midId,x,y,Layer.MID);
				GMath.sortOverlay(overlayQueue);
				for(int i = 0; i < 4; i++){
					if(overlayQueue[i].id > midId){
						if(!Tile.isFlowing(overlayQueue[i].id)){
							overlay = ResourceHandler.getOverlaySprite(overlayQueue[i].id, overlayQueue[i].direction, localToWorldX(x), localToWorldY(y));
							overlay.draw(batch);
						}
					}
				}
			}
		}
	}
	
	public Slope getSlope(int worldX, int worldY){
		int x = worldToLocalX(worldX);
		int y = worldToLocalY(worldY);
		
		short midId = tileMid[x][y];
		if(!Tile.isPhysical(midId) || Tile.isLiquid(midId)){ //TODO Use canSlope() later
			return Slope.NONE;
		}
		
		short leftId = getNeighbourTile(x,y,-1,true,Layer.MID);
		short upId = getNeighbourTile(x,y,1,false,Layer.MID);
		short rightId = getNeighbourTile(x,y,1,true,Layer.MID);
		
		if(!Tile.isPhysical(upId)){
			if(!Tile.isPhysical(leftId) && !Tile.isPhysical(rightId)){
				return Slope.PEAK;
			}else if(!Tile.isPhysical(leftId)){
				return Slope.RIGHT;
			}else if(!Tile.isPhysical(rightId)){
				return Slope.LEFT;
			}
		}
		return Slope.NONE;
	}
	
	private short getNeighbourTile(int x, int y, int offset, boolean offsetX, Layer layer){
		if(offsetX){
			int checkX = x + offset;
			if(checkX == -1){//Chunk to left
				Chunk left = map.getLoadedChunkCoords(xCoord - 1, yCoord);
				if(left == null) return 0;
				return left._getTile(SIZE - 1, y, layer);
			}else if(checkX == SIZE){//Chunk to right
				Chunk right = map.getLoadedChunkCoords(xCoord + 1, yCoord);
				if(right == null) return 0;
				return right._getTile(0, y, layer);
			}
			return layer == Layer.MID ? tileMid[checkX][y] : tileBack[checkX][y];
		}else{
			int checkY = y + offset;
			if(checkY == -1){//Chunk below
				Chunk below = map.getLoadedChunkCoords(xCoord, yCoord - 1);
				if(below == null) return 0;
				return below._getTile(x, SIZE - 1, layer);
			}else if(checkY == SIZE){//Chunk above
				Chunk above = map.getLoadedChunkCoords(xCoord, yCoord + 1);
				if(above == null) return 0;
				return above._getTile(x, 0, layer);
			}
			return layer == Layer.MID ? tileMid[x][checkY] : tileBack[x][checkY];
		}
	}
	
	public boolean hasTileAround(int worldX, int worldY, Layer layer){
		return _hasTileAround(worldToLocalX(worldX), worldToLocalY(worldY), layer);
	}
	
	public boolean _hasTileAround(int x, int y, Layer layer){
		short bottom = getNeighbourTile(x, y, -1, false, layer);
		if(bottom != Tile.air) return true;
		short top = getNeighbourTile(x, y, 1, false, layer);
		if(top != Tile.air) return true;
		short left = getNeighbourTile(x, y, -1, true, layer);
		if(left != Tile.air) return true;
		short right = getNeighbourTile(x, y, 1, true, layer);
		if(right != Tile.air) return true;
		
		return false;
	}
	public boolean _hasSolidTileAround(int x, int y, Layer layer){
		short bottom = getNeighbourTile(x, y, -1, false, layer);
		if(Tile.isPhysical(bottom)) return true;
		short top = getNeighbourTile(x, y, 1, false, layer);
		if(Tile.isPhysical(top)) return true;
		short left = getNeighbourTile(x, y, -1, true, layer);
		if(Tile.isPhysical(left)) return true;
		short right = getNeighbourTile(x, y, 1, true, layer);
		if(Tile.isPhysical(right)) return true;
		
		return false;
	}
	
	public short _getTileRec(int x, int y, Layer layer){
		if(isValidPos(x,y)){
			return _getTile(x,y,layer);
		}
		return map.getTile(localToWorldX(x), localToWorldY(y), layer);
	}
	public short _getTile(int x, int y, Layer layer){
		return layer == Layer.MID  ? tileMid[x][y] : tileBack[x][y];
	}
	public short getTile(int worldX, int worldY, Layer layer) {
		return _getTile(worldToLocalX(worldX), worldToLocalY(worldY), layer);
	}
	public float _getMass(int x, int y){
		if(isValidPos(x,y)){
			return massData[x][y];
		}
		return 0;
	}
	
	public float getMass(int worldX, int worldY) {
		return _getMass(worldToLocalX(worldX), worldToLocalY(worldY));
	}
	public float _getMassRec(int x, int y){
		if(isValidPos(x,y)){
			return massData[x][y];
		}
		return map.getMass(localToWorldX(x), localToWorldY(y));
	}
	private void _setTile(short id, int x, int y, Layer layer){
		switch(layer){
		case MID:
			if(Tile.isFlowing(id)){
				massData[x][y] = Flowing.MAX_MASS;
				newMass[x][y] = Flowing.MAX_MASS;
			}else{
				massData[x][y] = 0;
				newMass[x][y] = 0;
			}
			tileMid[x][y] = id;
			break;
		case BACK:
			tileBack[x][y] = id;
			break;
		}		
	}
	
	public boolean addTile(short id, int worldX, int worldY, Layer layer){
		if(id == Tile.air){
			return false;
		}
		
		int x = worldToLocalX(worldX);
		int y = worldToLocalY(worldY);
		short currentId = getTile(worldX, worldY, layer);
		if(Tile.isFlowing(currentId) && Tile.isFlowing(id) && currentId != id){
			return false; //Can't place liquid into other liquid
		}
		if(Tile.isFlowing(currentId) || currentId == Tile.air){
			if(Tile.isFlowing(id)){
				if(isValidPos(x,y)){
					massData[x][y] += Flowing.MAX_MASS;
					newMass[x][y] += Flowing.MAX_MASS;
					tileMid[x][y] = id;
				}
			}else{
				_setTile(id, x, y, layer);
			}
			modified = true;
			
			return true;
		}
		return false;
	}

	// Return true if a tile was removed
	public boolean removeTile(int worldX, int worldY, Layer layer) {
		short tileId = getTile(worldX, worldY, layer);
		if (tileId == Tile.air) {
			return false;
		}
		if(Tile.isFlowing(tileId)){
			return false;
		}
		_setTile(Tile.air, worldToLocalX(worldX), worldToLocalY(worldY), layer);
		if(layer == Layer.MID){ //Remove if torch
			lightSources[worldToLocalX(worldX)][worldToLocalY(worldY)] = 0;
		}
		modified = true;
		return true;
	}
	
	public void update(){
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				//TODO Spread grass and more
			}
		}
	}
	
	public float _flowRec(int x, int y, float remainingMass, float sourceMass, Direction dir, short liquidId){
		if(isValidPos(x,y)){
			return doFlow(x,y,remainingMass, sourceMass, dir, liquidId);
		}
		return map.flow(localToWorldX(x), localToWorldY(y), remainingMass, sourceMass, dir, liquidId);
	}
	public float _flow(int x, int y, float remainingMass, float sourceMass, Direction dir, short liquidId){
		if(isValidPos(x,y)){
			return doFlow(x,y,remainingMass, sourceMass, dir, liquidId);
		}
		return 0;
	}
	public float flow(int worldX, int worldY, float remainingMass, float sourceMass, Direction dir, short liquidId) {
		return _flow(worldToLocalX(worldX), worldToLocalY(worldY), remainingMass, sourceMass, dir, liquidId);
	}

	private float doFlow(int x, int y, float remainingMass, float sourceMass, Direction dir, short liquidId){
		short thisID = tileMid[x][y];
		
		//Only flow into same type liquid, or air
		if(thisID == Tile.air || thisID == liquidId){
			float flow = 0;
			
			if(dir == Direction.SOUTH){
				flow = Flowing.getStableState(remainingMass + massData[x][y]) - massData[x][y];
				flow *= Flowing.getVerticalSpeed(liquidId);
			}else if(dir == Direction.WEST || dir == Direction.EAST){
				flow = (remainingMass - massData[x][y])/2f;
				flow *= Flowing.getHorizontalFlowSpeed(liquidId);
				flow = MathUtils.clamp(flow, 0, Flowing.getHorizontalMaxSpeed(liquidId));
			}else if(dir == Direction.NORTH){
				flow = remainingMass - Flowing.getStableState(remainingMass + massData[x][y]);
				flow *= Flowing.getVerticalSpeed(liquidId);
			}
			flow = MathUtils.clamp(flow, 0, remainingMass);
			
			newMass[x][y] += flow;
			
			if(newMass[x][y] > 0){ //Pushed flow here
				tileMid[x][y] = liquidId;
			}
			
			return flow;		
		}
		return 0;
	}
	
	//http://w-shadow.com/blog/2009/09/01/simple-fluid-simulation/
	//TODO Bug: Water drains into unloaded chunks
	//TODO Make water drain into empty space in back layer
	public void calculateMasses(){
		float flow = 0;
		float remainingmass = 0;
		short id = Tile.air;
		int mod;
		//Calculate and apply flow for each block if water
		for (int x = 0; x < SIZE; x++){
			for(int y = 0; y < SIZE; y++){
				if(massData[x][y] < Flowing.MIN_MASS)continue;
				id = tileMid[x][y];
				if(!Tile.isFlowing(id)){ //Only flowing are relevant
					continue;
				}
				//Down
			   //Retrieves flow number and updates tile under
			   remainingmass = massData[x][y];

			   mod = Tile.isSteam(id) ? 1 : -1;
			   flow = _flowRec(x, y + mod, remainingmass, massData[x][y], Direction.SOUTH, id);
			   if(flow > 0){
				   newMass[x][y] -= flow;
				   remainingmass -= flow;
			   }
			   if(remainingmass <= 0)continue;
			   
			   if(GMath.rand.nextInt(2) == 0){
				   flow = _flowRec(x - 1, y, remainingmass, massData[x][y], Direction.WEST, id);
				   if(flow > 0){
					   newMass[x][y] -= flow;
					   remainingmass -= flow;
				   }
				   if(remainingmass <= 0)continue;
				   flow = _flowRec(x + 1, y, remainingmass, massData[x][y], Direction.EAST, id);
				   if(flow > 0){
					   newMass[x][y] -= flow;
					   remainingmass -= flow;
				   }
			   }else{
				   flow = _flowRec(x + 1, y, remainingmass, massData[x][y], Direction.EAST, id);
				   if(flow > 0){
					   newMass[x][y] -= flow;
					   remainingmass -= flow;
				   }
				   if(remainingmass <= 0)continue;
				   flow = _flowRec(x - 1, y, remainingmass, massData[x][y], Direction.WEST, id);
				   if(flow > 0){
					   newMass[x][y] -= flow;
					   remainingmass -= flow;
				   }
			   }
			   
			   if(remainingmass <= 0)continue;
			   flow = _flowRec(x, y - mod, remainingmass, massData[x][y], Direction.NORTH, id);
			   if(flow > 0){
				   newMass[x][y] -= flow;
			   }
			}
		}
	}
		
	//Return true if lightengine has to update
	public void updateMassStates(){
		short id;
		float oldMass;
		for (int x = 0; x < SIZE; x++){
			for (int y = 0; y < SIZE; y++){				
				if(newMass[x][y] == massData[x][y]){ //Nothing to update
					continue;
				}
				
				id = tileMid[x][y];
				if(id != Tile.air){ //Only handle flowing or air
					if(!Tile.isFlowing(id)){
						continue;
					}
				}
				
				oldMass = massData[x][y];
				massData[x][y] = MathUtils.lerp(massData[x][y], newMass[x][y], 0.75f);
				
				if(massData[x][y] >= Flowing.MIN_MASS){//Substance not dispersed
					if(oldMass >= Flowing.MAX_MASS && massData[x][y] < Flowing.MAX_MASS){ //Just got below max mass (affects lighting)
						modified = true;
					}else if(oldMass < Flowing.MAX_MASS && massData[x][y] >= Flowing.MAX_MASS){ //Just got above max mass (affects lighting)
						modified = true;
					}
				}else if(id != Tile.air){ // liquid turned into air
					newMass[x][y] = 0;
					massData[x][y] = 0;
					tileMid[x][y] = Tile.air;
				}
			}
		}
	}
	
	private void updateOverlayQueue(short id, int x, int y, Layer layer){
		overlayQueue[0].direction = 0;
		overlayQueue[0].id = getNeighbourTile(x,y,1,false,layer);
		
		overlayQueue[1].direction = 1;
		overlayQueue[1].id = getNeighbourTile(x,y,1,true, layer);
		
		overlayQueue[2].direction = 2;
		overlayQueue[2].id = getNeighbourTile(x,y,-1,false, layer);
		
		overlayQueue[3].direction = 3;
		overlayQueue[3].id = getNeighbourTile(x,y,-1,true, layer);
	}
	
	public void renderLightColors(SpriteBatch batch) {
		int xEnd = culledEnd(true);
		int yEnd = culledEnd(false);
		int xStart = culledStart(true);
		int yStart = culledStart(false);
		for (int x = xStart; x < xEnd; x++) {
			for (int y = yStart; y < yEnd; y++) {
				if(lightFront[x][y].a <= 0){
					continue;
				}
				Sprite light = ResourceHandler.getTileSprite(Tile.air);
				light.setColor(lightFront[x][y]);
				light.setPosition(localToWorldX(x) - 0.5f,
						localToWorldY(y) - 0.5f);
			    light.draw(batch);
			}
		}
	}
	
	//Swap and clear the back buffer for next render
	
	//Set front buffer to back buffer and clear back buffer
	public void swapLightBuffers() {
		swapBuffer = lightFront;
		lightFront = lightBack;
		lightBack = swapBuffer;
	}
	
	public void clearBackBuffer(){
		hasLight = false;
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				if(!hasLight){
					
					if(getLocalLightSource(x,y) != 0 || (Tile.isFlowing(tileMid[x][y]) || tileMid[x][y] == Tile.air) && tileBack[x][y] == Tile.air){
						hasLight = true;
					}
				}
				lightBack[x][y].set(0, 0, 0, 0);
			}
		}
	}

	public float _getWallReduction(int x, int y) {
		short id = tileMid[x][y];
		if (Tile.isPhysical(id) && !Tile.isOneWay(id)) {
			if (id == Tile.brick || id == Tile.stone) {
				return 0.2f;
			}else{
				return 0.15f;
			}
		}
		return 0f;
	}

	public byte getLocalLightSource(int x, int y) {
		if(lightSources[x][y] != 0){
			return lightSources[x][y];
		}else if(tileMid[x][y] == Tile.lava){
			return LightDef.LAVA;
		}else if(tileMid[x][y] == Tile.torch){
			return LightDef.TORCH;
		}
		return 0;
	}

	public void addLightSource(byte lightID, int x, int y) {
		lightSources[worldToLocalX(x)][worldToLocalY(y)] = lightID;
		modified = true;
	}
	public boolean removeLightSource(int x, int y) {
		if(lightSources[worldToLocalX(x)][worldToLocalY(y)] == LightDef.ENVIRONMENT){
			return false;
		}
		lightSources[worldToLocalX(x)][worldToLocalY(y)] = 0;
		modified = true;
		return true;
	}

	public void setLocalLightColor(int x, int y, Color color,
			boolean frontbuffer) {
		if (frontbuffer) {
			lightFront[x][y].set(color);
		} else {
			lightBack[x][y].set(color);
		}
	}

	public Color getLight(int x, int y, boolean frontbuffer) {
		if (!isValidPos(x,y)) {
			return map.getLight(localToWorldX(x), localToWorldY(y),
					frontbuffer);
		}
		return getLocalLight(x,y,frontbuffer);
	}
	public Color getLocalLight(int x, int y, boolean frontbuffer) {
		return frontbuffer ? lightFront[x][y] : lightBack[x][y];
	}

	public int worldToLocalX(float worldX) {
		return (int)(worldX - SIZE * xCoord);
	}

	public int worldToLocalY(float worldY) {
		return (int)(worldY - SIZE * yCoord);
	}

	public int localToWorldX(int localX) {
		return (int)(SIZE * xCoord + localX);
	}

	public int localToWorldY(int localY) {
		return (int)(SIZE * yCoord + localY);
	}
	
	private boolean isValidPos(int localX, int localY){
		return (localX >= 0 && localY >= 0 && localX < SIZE && localY < SIZE);
	}
	
	private int culledStart(boolean xStart){
		int start = xStart ? worldToLocalX(map.getStartWorldX()) : worldToLocalY(map.getStartWorldY());
		if (start < 0) {
			start = 0;
		}
		
		return start;
	}
	private int culledEnd(boolean xEnd){
		int end = xEnd ? worldToLocalX(map.getEndWorldX()) : worldToLocalY(map.getEndWorldY());
		if (end > SIZE) {
			end = SIZE;
		}
		return end;
	}
	
	public void mixNewLight(int bx, int by, Color color, float brightness){
		if(lightBack[bx][by].equals(color)){
			return;
		}
		
		lightBack[bx][by].r = Math.max(lightBack[bx][by].r, color.r * brightness);
		lightBack[bx][by].g = Math.max(lightBack[bx][by].g, color.g * brightness);
		lightBack[bx][by].b = Math.max(lightBack[bx][by].b, color.b * brightness);
		lightBack[bx][by].a = Math.max(lightBack[bx][by].a, brightness);
	}
}
