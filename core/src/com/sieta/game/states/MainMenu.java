package com.sieta.game.states;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sieta.game.handlers.GameStateManager;
import com.sieta.game.utils.Graphics;

public class MainMenu extends GameState{
	private Stage stage;
	private Skin skin;
	
	private static final float BUTTON_WIDTH = 800f;
    private static final float BUTTON_HEIGHT = 200f;
	
	public MainMenu(final GameStateManager gsm) {
		super(gsm);
		stage = new Stage();
		skin = new Skin();
		Gdx.input.setInputProcessor(stage);
		
		//Fonts
		BitmapFont font = Graphics.getFont();
		font.getData().setScale(0.5f);
		skin.add("default",font);
		
		Pixmap pixmap = new Pixmap(100, 100, Format.RGBA8888);
		pixmap.setColor(Color.CHARTREUSE);
		pixmap.fill();

		skin.add("white", new Texture(pixmap));
		
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		skin.add("default", textButtonStyle);
		
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = skin.getFont("default");
		labelStyle.fontColor = Color.WHITE;
		skin.add("default", labelStyle);
		
		
		float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        
        final float buttonX = ( width - BUTTON_WIDTH ) / 2;
        
        Label welcomeLabel = new Label( "Psieta: A sandbox game", getSkin() );
        welcomeLabel.setX(width/2 - welcomeLabel.getWidth()/2);
        welcomeLabel.setY(height/2 + height/4);
        stage.addActor(welcomeLabel);
        
        // button "start game"
        TextButton startGameButton = new TextButton( "Start game", getSkin() );
        startGameButton.setX(buttonX);
        startGameButton.setY(height/2 - BUTTON_HEIGHT/2);
        startGameButton.setWidth(BUTTON_WIDTH);
        startGameButton.setHeight(BUTTON_HEIGHT);
        stage.addActor( startGameButton );
        
        startGameButton.addCaptureListener( new ClickListener() {
            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y ){
                gsm.pushState(GameStateManager.PLAY);
            }
        } );
	}
	
	public Skin getSkin(){
		return skin;
	}

	@Override
	public void handleDesktopInput() {		
	}

	@Override
	public void update(float deltaTime) {
		stage.act(deltaTime);
	}

	@Override
	public void render() {
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}

	@Override
	public void resize(int width, int height) {		
	}
	
	@Override
	public void resume() {		
	}

	@Override
	public void pause() {		
	}

	@Override
	public void updatePrevState() {}

	@Override
	public void interpolate(float alpha) {}

}