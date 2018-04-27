package com.sieta.game.world;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sieta.game.entities.Mob;
import com.sieta.game.entities.Player;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.lighting.LightEngine;
import com.sieta.game.physics.ContactListener;
import com.sieta.game.physics.PhysicsEngine;
import com.sieta.game.world.Chunk.Layer;
import com.sieta.game.world.WorldCamera.ShakeType;

public class GameWorld{
    private Array<Mob> mobs = new Array<Mob>();
    private Player player;
        
    FrameBuffer colorFBO;
    private ShaderProgram shader;
    
    private WorldCamera worldCam;
    private Map map;
       
    private PhysicsEngine physicsEngine;
    public LightEngine lightEngine;
    
	public GameWorld(OrthographicCamera cam){
		//Format.RGBA4444 for android
		GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if(Gdx.app.getType() == ApplicationType.Android){
			frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA4, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		}else{
			frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		}
		colorFBO = frameBufferBuilder.build();
		
		player = new Player(new Vector2(5,30));
		worldCam = new WorldCamera(cam, player);
		
		/*for(int i = -1000; i < 1000; i++){
			spawnMob(new Vector2(10 + i * 0.001f,30));
		}*/
		
		shader = new ShaderProgram(Gdx.files.internal("shaders/color.vsh"), Gdx.files.internal("shaders/color.fsh"));
		shader.begin();
		shader.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shader.setUniformi("u_lightmap", 1);
		shader.end();
		
		worldCam.update(0f);
		map = new Map(worldCam, shader);
		lightEngine = new LightEngine(map);
		map.init(lightEngine);
		
		physicsEngine = new PhysicsEngine(map);
		for(Mob mob: mobs){
			physicsEngine.addBody(mob.getBody());
		}
		physicsEngine.addBody(player.getBody());
	}
	
	public void setContactListener(ContactListener listener){
		physicsEngine.setContactListener(listener);
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public void spawnMob(Vector2 spawnpos){
		mobs.add(new Mob(spawnpos));
	}
	
	public boolean addLight(byte id, int x, int y){
		return map.addLight(id, x, y);
	}
	
	public boolean placeTile(short id, int x, int y){
		return map.addTile(id, x, y, Layer.MID);
	}
	
	public short getTile(int x, int y, Layer layer){
		return map.getTile(x, y, layer);
	}
	
	public boolean hasTileAround(int x, int y, Layer layer){
		return map.hasTileAround(x, y, layer);
	}
	
	public boolean hasTile(int x, int y, Layer layer){
		return map.hasTile(x, y, layer);
	}
	
	public boolean removeTile(int x, int y, Layer layer){
		return map.removeTile(x, y, layer);
	}
	

	public void update(float deltaTime){
		player.resetHits();
		for(Mob mob: mobs){
			mob.resetHits();
		}
		physicsEngine.step(deltaTime); //Check collisions, player needs to know if grounded etc
		worldCam.update(deltaTime);
		map.update(lightEngine);
		
		player.update(map, this);
		//TODO only if move
		map.removeLight((int)player.getLastLightSourcePos().x, (int)player.getLastLightSourcePos().y);
		map.addLight(player.getLightSourceId(), (int)player.getBody().getX(), (int)player.getBody().getY());
		player.setLastLightSourcePos((int)player.getBody().getX(), (int)player.getBody().getY());
		
		for(Mob mob: mobs){
			mob.jump();
			if(mob.getPosition().x < player.getPosition().x - 1){
				mob.moveRight();
			}
			if(mob.getPosition().x > player.getPosition().x + 1){
				mob.moveLeft();
			}
			mob.update(map, this);
		}
		if (player.triggerSmash()) {
			worldCam.shake(1f, 0.02f, ShakeType.UP);
		}
	}
	
	public void render(SpriteBatch batch, OrthographicCamera cam, OrthographicCamera hudCam){
		worldCam.update(0f);
		batch.setProjectionMatrix(cam.combined);

		colorFBO.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		map.renderLightColors(batch);
		batch.flush();
		colorFBO.end();
		colorFBO.getColorBufferTexture().bind(1);
		ResourceHandler.getTileTexture(0).getTexture().bind(0); //Just bind some random texture (needed cus libgdx is weird)

		//TODO draw background
		batch.setShader(shader);
		map.renderBackground(batch);
		for(Mob mob: mobs){
			mob.render(batch);
		}
		player.render(batch);
		map.renderForeground(batch);
		batch.setShader(null);
	}
	
	public void resize(int width, int height){
		if(shader != null){
			shader.begin();
			shader.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			shader.end();
		}
	}
	
	public void dispose(){
		shader.dispose();
	}
	
	public void updatePrevState(){
		physicsEngine.updatePrevState();
	}
	public void interpolate(float alpha){
		physicsEngine.interpolate(alpha);
	}
}
