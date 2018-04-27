package com.sieta.game.handlers;

import org.json.JSONException;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sieta.game.utils.Physics;

public abstract class ResourceHandler {
	
	//TODO pack everything into 1 atlas
	
	private static AssetManager assetManager = new AssetManager();
	private static TextureAtlas atlas;
	
	//Second dim is index for autotiling
	private static Sprite[] tileSprites;
	
	private static TextureRegion[] tileTextures;

	//Only 8 overlay textures, one in each direction
	private static Sprite[][] overlaySprites;
	
	//The offsets for different directions
	private static int[] xOffset = new int[4];
	private static int[] yOffset = new int[4];


			
	public static void loadAssets(){		
		assetManager.load("sprites/atlas.atlas", TextureAtlas.class);
		
		assetManager.load("cursor.png", Pixmap.class);
		
		assetManager.finishLoading();
		atlas = assetManager.get("sprites/atlas.atlas", TextureAtlas.class);

		FontLibrary.loadFonts(atlas);

		loadTiles();
		
		xOffset[0] = 0;
		yOffset[0] = 3;
		
		xOffset[1] = 3;
		yOffset[1] = 0;
		
		xOffset[2] = 0;
		yOffset[2] = -3;
		
		xOffset[3] = -3;
		yOffset[3] = 0;
	}
	
	private static void loadTiles(){
		FileHandle file = Gdx.files.internal("configs/tiles.json");
		String text = file.readString();
		
		try {
			JSONObject tiles = new JSONObject(text);
			
			JSONObject properties = tiles.getJSONObject("tileproperties");
			int tilecount = tiles.getInt("tilecount");
			tileSprites = new Sprite[tilecount];
			tileTextures = new TextureRegion[tilecount];
			//Test
			//cornerTextures = new TextureRegion[Tile.tilecount][4];
			overlaySprites = new Sprite[tilecount][4];
			for(int i = 0; i < tilecount; i++){
				JSONObject tileprops = properties.getJSONObject("" + i);
				String name = tileprops.getString("name");
				String description = tileprops.getString("description");
				String type = tileprops.getString("type");
				//TODO sort based on properties, final fields will not be used
				
				if(!type.equals("liquid") && !type.equals("gas") && !type.equals("air")){
					TextureRegion base = new TextureRegion(atlas.findRegion(name));
					TextureRegion n = new TextureRegion(base);
					n.setRegion(base.getRegionX() + 2, base.getRegionY() + 10, 8, 2);
					overlaySprites[i][0] = new Sprite(n);
					overlaySprites[i][0].setSize(overlaySprites[i][0].getRegionWidth()/Physics.PPM, overlaySprites[i][0].getRegionHeight()/Physics.PPM);
					
					TextureRegion e = new TextureRegion(base);
					e.setRegion(base.getRegionX(), base.getRegionY() + 2, 2, 8);
					overlaySprites[i][1] = new Sprite(e);
					overlaySprites[i][1].setSize(overlaySprites[i][1].getRegionWidth()/Physics.PPM, overlaySprites[i][1].getRegionHeight()/Physics.PPM);
					
					TextureRegion s = new TextureRegion(base);
					s.setRegion(base.getRegionX() + 2, base.getRegionY(), 8, 2);
					overlaySprites[i][2] = new Sprite(s);
					overlaySprites[i][2].setSize(overlaySprites[i][2].getRegionWidth()/Physics.PPM, overlaySprites[i][2].getRegionHeight()/Physics.PPM);
					
					TextureRegion w = new TextureRegion(base);
					w.setRegion(base.getRegionX() + 10, base.getRegionY() + 2, 2, 8);
					overlaySprites[i][3] = new Sprite(w);
					overlaySprites[i][3].setSize(overlaySprites[i][3].getRegionWidth()/Physics.PPM, overlaySprites[i][3].getRegionHeight()/Physics.PPM);
					
					TextureRegion regular = new TextureRegion(base);
					regular.setRegion(base.getRegionX() + 2, base.getRegionY() + 2, 8, 8);
					tileTextures[i] = regular;
					tileSprites[i] = new Sprite(tileTextures[i]);
					tileSprites[i].setSize(tileTextures[i].getRegionWidth()/Physics.PPM, tileTextures[i].getRegionHeight()/Physics.PPM);

				}else{
					tileTextures[i] = atlas.findRegion(name);
					tileSprites[i] = new Sprite(tileTextures[i]);
					tileSprites[i].setSize(tileTextures[i].getRegionWidth()/Physics.PPM, tileTextures[i].getRegionHeight()/Physics.PPM);
				}
			}
			    
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}
	
	public static Pixmap getCursor(){
		return assetManager.get("cursor.png", Pixmap.class);
	}
	
	public static void dispose(){
		assetManager.dispose();
	}
	
	public static TextureRegion getCharTexture(String name){
		return atlas.findRegion(name);
	}
	
	public static Sprite getOverlaySprite(int id, int direction, int x, int y){
		overlaySprites[id][direction].setColor(1f, 1f, 1f, 1f);
		overlaySprites[id][direction].setPosition(x - overlaySprites[id][direction].getWidth() * 0.5f + xOffset[direction]/Physics.PPM, y - overlaySprites[id][direction].getHeight() * 0.5f + yOffset[direction]/Physics.PPM);
		return overlaySprites[id][direction];
	}
	
	public static Sprite getTileSprite(int id){
		tileSprites[id].setColor(1f, 1f, 1f, 1f);
		return tileSprites[id];
	}
	public static TextureRegion getTileTexture(int id){
		return tileTextures[id];
	}
}
