package com.github.adisann.pokemon.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.github.adisann.pokemon.PokemonGameMain;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration configuration = new GwtApplicationConfiguration(600, 400);
        return configuration;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new PokemonGameMain();
    }
}