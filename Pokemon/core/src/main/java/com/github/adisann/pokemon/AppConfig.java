package com.github.adisann.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.mvc.config.AutumnActionPriority;

/**
 * Application configuration component that provides shader programs.
 * Shaders don't require assets so they can be initialized early.
 */
@Component
public class AppConfig {

    private ShaderProgram overlayShader;
    private ShaderProgram transitionShader;

    /**
     * Initialize shaders - this can run at any time since shaders don't depend on
     * loaded assets.
     */
    @Initiate(priority = AutumnActionPriority.HIGH_PRIORITY)
    public void initShaders() {
        ShaderProgram.pedantic = false;

        // Initialize overlay shader
        overlayShader = new ShaderProgram(
                Gdx.files.internal("shaders/overlay/vertexshader.txt"),
                Gdx.files.internal("shaders/overlay/fragmentshader.txt"));
        if (!overlayShader.isCompiled()) {
            System.out.println("Overlay shader compilation failed: " + overlayShader.getLog());
            Gdx.app.exit();
        }

        // Initialize transition shader
        transitionShader = new ShaderProgram(
                Gdx.files.internal("shaders/transition/vertexshader.txt"),
                Gdx.files.internal("shaders/transition/fragmentshader.txt"));
        if (!transitionShader.isCompiled()) {
            System.out.println("Transition shader compilation failed: " + transitionShader.getLog());
            Gdx.app.exit();
        }

        System.out.println("Shaders initialized");
    }

    public ShaderProgram getOverlayShader() {
        return overlayShader;
    }

    public ShaderProgram getTransitionShader() {
        return transitionShader;
    }
}