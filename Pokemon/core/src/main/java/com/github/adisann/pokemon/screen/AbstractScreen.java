package com.github.adisann.pokemon.screen;

import com.badlogic.gdx.Screen;

/**
 * Abstract screen interface that extends LibGDX Screen.
 * Adds update() method for game logic separated from rendering.
 */
public interface AbstractScreen extends Screen {

	public void update(float delta);

}
