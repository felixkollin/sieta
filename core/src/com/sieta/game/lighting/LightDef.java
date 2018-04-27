package com.sieta.game.lighting;

import com.badlogic.gdx.graphics.Color;
import com.sieta.game.world.Tile;

public class LightDef {
	//Regular white light is 0.5f, all colors are multiplied by 2 in shader.
	//Any higher makes the light really bright

	public static LightSphere playerAmbient = new LightSphere(8, new Color(0.1f, 0.1f, 0.2f, 1f), 0.1f, 0.05f);
	public static LightSphere environment = new LightSphere(16, new Color(0.5f, 0.5f, 0.5f, 1f), 0.01f, 0.005f);
	public static final LightSphere torch = new LightSphere(32, new Color(0.75f, 0.6f, 0.5f, 1f), 0.01f, 0.0025f);
	public static final LightSphere lava = new LightSphere(4, new Color(0.5f, 0.3f, 0.2f, 1f), 0.01f, 0.5f);

	public static final byte ENVIRONMENT = 1, TORCH = Tile.torch, LAVA = Tile.lava, PLAYER_AMB = 2;

	public static LightSphere getLightSphere(byte lightID) {
		if (lightID == ENVIRONMENT) {
			return environment;
		} else if(lightID == TORCH){
			return torch;
		}else if(lightID == LAVA){
			return lava;
		}else if(lightID == PLAYER_AMB){
			return playerAmbient;
		}
		return null;
	}

	public static float getMidRelative(int x, int y, byte lightID) {
		return getLightSphere(lightID).getMidRelative(x,y);
	}
}
