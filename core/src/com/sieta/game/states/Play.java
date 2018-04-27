package com.sieta.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.sieta.game.entities.Player;
import com.sieta.game.handlers.GameStateManager;
import com.sieta.game.handlers.GestureProcessor;
import com.sieta.game.handlers.GestureProcessor.GestureHandler;
import com.sieta.game.handlers.GestureProcessor.TouchArea;
import com.sieta.game.handlers.MyInput;
import com.sieta.game.handlers.MyInputProcessor;
import com.sieta.game.handlers.GestureProcessor.TouchType;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.hud.GameHud;
import com.sieta.game.hud.HotbarElement;
import com.sieta.game.hud.ItemContainerElement;
import com.sieta.game.items.Item;
import com.sieta.game.items.Slot;
import com.sieta.game.lighting.LightDef;
import com.sieta.game.physics.CollisionHandler;
import com.sieta.game.utils.Graphics;
import com.sieta.game.world.GameWorld;
import com.sieta.game.world.Chunk.Layer;
import com.sieta.game.world.Tile;

public class Play extends GameState implements GestureHandler {	
	private GameWorld gameworld;
	private GameHud hud;
	private Sound backgroundMusic;

	private GestureProcessor gestureProcessor;
	private float destroyTimer = 0;
	private Vector3 lastDestroyPos;
	private long lastTouch = 0;
	
	private Slot cursorSlot;

	public Play(GameStateManager gsm) {
		super(gsm);
		ShaderProgram.pedantic = false;
		
		lastDestroyPos = new Vector3();
		backgroundMusic = Gdx.audio.newSound(Gdx.files.internal("music.mp3"));
		
		gameworld = new GameWorld(cam);
		gameworld.setContactListener(new CollisionHandler());
		cursorSlot = new Slot(null,0);
		
		Sprite invSprite = new Sprite(ResourceHandler.getTileTexture(0));
		invSprite.setColor(new Color(1f,1f,1f,0.5f));
		hud = new GameHud(hudCam, hudViewport, cursorSlot, gameworld.getPlayer());
		ItemContainerElement inventory = new ItemContainerElement(hudCam, -20, 0, true, invSprite, gameworld.getPlayer().getInventory(), cursorSlot, GameHud.P_INV);
		hud.addElement(inventory);
		
		HotbarElement hotbarElement = new HotbarElement(hudCam, 0, 0, true, hud.getHotbar(), invSprite, GameHud.HOTBAR);
		hud.addElement(hotbarElement);
		
		gameworld.getPlayer().getInventory().store(new Item(Tile.dirt, Item.Type.TILE), 200);
		gameworld.getPlayer().getInventory().store(new Item(Tile.steam, Item.Type.TILE), 200);
		gameworld.getPlayer().getInventory().store(new Item(Tile.water, Item.Type.TILE), 200);
		gameworld.getPlayer().getInventory().store(new Item(Tile.lava, Item.Type.TILE), 200);
		gameworld.getPlayer().getInventory().store(new Item(Tile.torch, Item.Type.TILE), 200);
	}

	@Override
	public void resume() {//TODO Good with new?
		MyInput.clear();
		if (game.getAppType() == ApplicationType.Desktop) {
			Gdx.input.setInputProcessor(new MyInputProcessor());
		} else {
			gestureProcessor = new GestureProcessor(this);
			Gdx.input.setInputProcessor(gestureProcessor);
		}
	}

	@Override
	public void handleDesktopInput() {
		
		Player player = gameworld.getPlayer();
		player.resetJumpFrame();

		if (MyInput.isPressed(MyInput.TAB)) {
			hud.toggleVisible(GameHud.P_INV);
		}
		//Using inventory:
		boolean interacted = hud.interactInput();
		if(!interacted){
			if(Gdx.input.isTouched() && !MyInput.isDown(MyInput.SHIFT)){
				tryPlacing(Gdx.input.getX(), Gdx.input.getY());
			}else if(Gdx.input.isTouched() && MyInput.isDown(MyInput.SHIFT)){
				destroy(Gdx.input.getX(), Gdx.input.getY());
			}
		}
		
		if (MyInput.isDown(MyInput.A)) {
			player.moveLeft();
		}
		if (MyInput.isDown(MyInput.D)) {
			player.moveRight();
		}
		if (MyInput.isPressed(MyInput.Q)) {
			player.dashLeft();;
		}
		if (MyInput.isPressed(MyInput.E)) {
			player.dashRight();
		}
		if (MyInput.isDown(MyInput.SPACE)) {
			player.jump();
		}
		if(MyInput.isReleased(MyInput.SPACE)){			
			player.releaseJump();
		}
		
		if (MyInput.isPressed(MyInput.SPACE)) {
			if(!player.tryWallJump()){
				player.doubleJump();
			}
		}
		if(MyInput.isPressed(MyInput.S)){
			player.crouch(true);
		}
		if(MyInput.isDown(MyInput.S)){
			player.dropDown();
		}else{
			player.crouch(false);
		}
		int numberDown = MyInput.getNumberDown();
		if(numberDown != -1){
			hud.getHotbar().selectSlot(numberDown);
		}
	}
	
	/**
	 * Returns true if destroy was successful
	 * @param worldpos
	 * @return 
	 */
	private boolean destroy(float mouseX, float mouseY){
		Vector3 worldpos = cam.unproject(new Vector3(mouseX, mouseY, 0));
		return gameworld.removeTile(Math.round(worldpos.x), Math.round(worldpos.y), Layer.MID);
	}
	
	private void tryPlacing(float mouseX, float mouseY){
		boolean fromCursor = true;
		Item item = cursorSlot.getItem();
		if(cursorSlot.getItem() == null){
			fromCursor = false;
			item = hud.getHotbar().getSelectedItem();
		}
		if(item == null){
			return;
		}
		place(item, mouseX, mouseY, fromCursor);
	}
	
	private void place(Item item, float mouseX, float mouseY, boolean fromCursor){
		if(!fromCursor){
			if(!gameworld.getPlayer().getInventory().hasItem(item)){
				return;
			}	
		}
		Vector3 worldpos = cam.unproject(new Vector3(mouseX, mouseY, 0));
		int worldX = Math.round(worldpos.x);
		int worldY = Math.round(worldpos.y);
		
		if (gameworld.hasTileAround(worldX, worldY, Layer.MID) || gameworld.hasTileAround(worldX, worldY, Layer.BACK)) {
			if(gameworld.placeTile(item.getPlaceId(), worldX, worldY)){
				if(fromCursor){
					cursorSlot.take(1);
				}else{
					gameworld.getPlayer().getInventory().remove(item);
				}
			}
			return;
		}
	}

	@Override
	public void handleGesture(TouchType mainType, TouchType secondaryType,
			TouchArea area, float x, float y, float angle) {
		
		Player player = gameworld.getPlayer();
		player.resetJumpFrame();
		TouchType type = mainType;
		if (mainType == TouchType.SWIPE) {
			type = secondaryType;
		}
		switch (area) {
		case GESTURE:
			if(player.inLiquid()){
				player.jump();
			}
			switch (type) {
			case SHORT_HOLD:
				player.jump();
				break;
			case TAP:
				player.jump();
				if(!player.tryWallJump()){
					player.doubleJump();
				}
				break;
			case SWIPE_LEFT:
				player.dashLeft();
				break;
			case SWIPE_RIGHT:
				player.dashRight();
				break;
			case SWIPE_DOWN:
				player.smash();
				//player.dropDown();
				break;
			case SWIPE_UP:
				player.jump();
				player.doubleJump();
				break;
			case SWIPE:
				break;
			default:
				break;
			}
			break;
		case MOVE_LEFT:
			if (type == TouchType.TOUCH) {
				player.moveLeft();
			}
			break;
		case MOVE_RIGHT:
			if (type == TouchType.TOUCH) {
				player.moveRight();
			}
			break;
		case INTERACT:
			// TODO or drag
			if (type == TouchType.TAP) {
				tryPlacing(x,y);
			}
			if (type == TouchType.TOUCH) {
				// If x,y changed or too much time has passed since last touch,
				// reset destroy timer
				Vector3 worldpos = cam.unproject(new Vector3(x, y, 0));
				int worldX = Math.round(worldpos.x);
				int worldY = Math.round(worldpos.y);
				if (Math.round(lastDestroyPos.x) != worldX
						|| Math.round(lastDestroyPos.y) != worldY
						|| System.currentTimeMillis() - lastTouch > 100f) {
					destroyTimer = 0;
				}
				lastDestroyPos = worldpos;
				lastTouch = System.currentTimeMillis();
				
				if (gameworld.hasTile(worldX, worldY, Layer.MID)) {
					destroyTimer += Gdx.graphics.getRawDeltaTime();
					if (destroyTimer >= 0.3f) { // TODO Depend on material
						destroyTimer = 0;
						destroy(worldpos.x, worldpos.y);
					}
				}
				return;
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void update(float deltaTime) {
		if (game.getAppType() == ApplicationType.Android) {
			gestureProcessor.update();
		}
		gameworld.update(deltaTime);
	}
	
	@Override
	public void resize(int width, int height){
		gameworld.resize(width, height);
		hud.resize(width, height);
	}

	@Override
	public void render() {
		gameworld.render(batch, cam, hudCam);
		hud.render(batch);
	}

	@Override
	public void dispose() {
		backgroundMusic.dispose();
		gameworld.dispose();
	}

	@Override
	public void pause() {

	}

	@Override
	public void updatePrevState() {
		gameworld.updatePrevState();
	}

	@Override
	public void interpolate(float alpha) {
		gameworld.interpolate(alpha);
	}

}
