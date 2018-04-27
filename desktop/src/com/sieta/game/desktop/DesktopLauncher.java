package com.sieta.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.sieta.game.SietaGame;
import com.sieta.game.utils.Graphics;

public class DesktopLauncher {
	private static Settings texturePackerSettings;

	/**
	 * Launch a desktop version of the game.
	 */
	public static void main(String[] args){
		texturePackerSettings = new Settings();
		texturePackerSettings.maxWidth = 1024;
		texturePackerSettings.maxHeight = 1024;
		texturePackerSettings.paddingX = 4;
		texturePackerSettings.paddingY = 4;
		texturePackerSettings.duplicatePadding = true;
		texturePackerSettings.bleed = true;
		texturePackerSettings.filterMin = TextureFilter.MipMapLinearNearest;
		texturePackerSettings.filterMag = TextureFilter.Nearest;
		texturePackerSettings.premultiplyAlpha = true;

		packAtlas("/Users/felixkollin/Downloads/sieta/android/assets/sprites/atlas-input", "/Users/felixkollin/Downloads/sieta/android/assets/sprites", "atlas");

		LwjglApplicationConfiguration cfg = setUpConfiguration();
		new LwjglApplication(new SietaGame(), cfg);
	}

	private static LwjglApplicationConfiguration setUpConfiguration() {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = SietaGame.TITLE + " | " + SietaGame.DESCRIPTION + " | " + SietaGame.VERSION;
		cfg.width = (int) Graphics.VIRTUAL_WIDTH;
		cfg.height = (int) Graphics.VIRTUAL_HEIGHT;
		cfg.allowSoftwareMode = true;
		cfg.foregroundFPS = 0;
		cfg.backgroundFPS = 60;
		cfg.vSyncEnabled = true;
		cfg.useHDPI = false;

		return cfg;
	}

	private static void packAtlas(String inputPath, String outputPath, String atlasName){
		TexturePacker.process(texturePackerSettings, inputPath, outputPath, atlasName);
	}
}