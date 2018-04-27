package com.sieta.game.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.HashMap;

/**
 * Created by felixkollin on 16/12/17.
 */

public abstract class FontLibrary {
    private static final String[] fontNames = {"mono"};

    private static HashMap<String,BitmapFont> fonts;

    public static void loadFonts(TextureAtlas atlas){
        fonts = new HashMap<String, BitmapFont>();
        for(int i = 0; i < fontNames.length; i++){
            fonts.put(fontNames[i], new BitmapFont(Gdx.files.internal(fontNames[i] + ".fnt"), atlas.findRegion(fontNames[i])));
        }
    }

    public static BitmapFont getFont(String name){
        return fonts.get(name);
    }

    public static void drawFont(String fontName, Color color, SpriteBatch batch, String text, float x, float y){
        BitmapFont selected = fonts.get(fontName);
        if(selected != null){
            selected.setColor(color);
            selected.draw(batch, text, x, y);

        }

    }
}
