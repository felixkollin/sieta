package com.sieta.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.sieta.game.handlers.FontLibrary;
import com.sieta.game.handlers.ResourceHandler;
import com.sieta.game.world.Tile;

/**
 * Utility class for handling some basic graphic features of LibGDX.
 */

public class Graphics {
	/* The virtual resolution everything will be programmed with, 
	 * other resolutions are handled with Viewports.
	 */
	
	private static Color convertingColor = new Color(0f,0f,0f,0f);
	
	public static final float VIRTUAL_WIDTH = 1280;
	public static final float VIRTUAL_HEIGHT = 720;
		
	private static BitmapFont font;
	private static GlyphLayout layout;
	
	public static final Color WHITE_COLOR = Color.WHITE;
			
	static{
		layout = new GlyphLayout();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font4.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.incremental = true;
		parameter.color = Color.WHITE;
		
		parameter.mono = true;
		//parameter.size = 10; //10,20
		parameter.size = 20;
		parameter.minFilter = TextureFilter.Nearest;
		parameter.magFilter = TextureFilter.Nearest;
		font = generator.generateFont(parameter);
		font.setUseIntegerPositions(false); //TODO test
	}
    
	public static void drawSmallText(SpriteBatch batch, String text, Color color, float x, float y, boolean world){
        //FontLibrary.drawFont("mono", batch, text, x, y);
		/*if(color == Color.RED){
			font.setColor(color);
			//layout.setText(font, text);
			font.draw(batch, text, x - layout.width/2, y);
		}*/
		//font.setColor(color);
		//layout.setText(font, text);
		//font.draw(batch, text, x - layout.width/2, y);
	}
	
	public static BitmapFont getFont(){
		return font;
	}
	
	public static float getAlpha(int color){
		Color.rgba4444ToColor(convertingColor, color);
		return convertingColor.a;
	}
}
