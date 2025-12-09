package com.github.adisann.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.annotation.Singleton;
import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.adisann.pokemon.util.SkinGenerator;
import aurelienribon.tweenengine.TweenManager;

@Provider
public class AppConfig {

    @Singleton
    @Provider
    public AssetManager provideAssetManager() {
        return new AssetManager();
    }

    @Singleton
    @Provider
    public TweenManager provideTweenManager() {
        return new TweenManager();
    }
    
    @Singleton
    @Provider
    public MoveDatabase provideMoveDatabase() {
        return new MoveDatabase();
    }

    @Singleton
    @Provider
    public Skin provideSkin(AssetManager assetManager) {
        return SkinGenerator.generateSkin(assetManager);
    }
    
    @Provider(value = "overlay")
    @Singleton
    public ShaderProgram provideOverlayShader() {
        ShaderProgram.pedantic = false;
		ShaderProgram overlayShader = new ShaderProgram(
				Gdx.files.internal("shaders/overlay/vertexshader.txt"), 
				Gdx.files.internal("shaders/overlay/fragmentshader.txt"));
		if (!overlayShader.isCompiled()) {
			System.out.println(overlayShader.getLog());
            Gdx.app.exit();
		}
        return overlayShader;
    }

    @Provider(value = "transition")
    @Singleton
    public ShaderProgram provideTransitionShader() {
        ShaderProgram.pedantic = false;
        ShaderProgram transitionShader = new ShaderProgram(
				Gdx.files.internal("shaders/transition/vertexshader.txt"), 
				Gdx.files.internal("shaders/transition/fragmentshader.txt"));
		if (!transitionShader.isCompiled()) {
			System.out.println(transitionShader.getLog());
            Gdx.app.exit();
		}
        return transitionShader;
    }
}


