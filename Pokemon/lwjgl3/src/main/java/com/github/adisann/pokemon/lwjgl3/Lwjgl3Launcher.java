package com.github.adisann.pokemon.lwjgl3;

import com.github.adisann.pokemon.PokemonGameMain;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new PokemonGameMain(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("pokemon");
        configuration.setWindowedMode(600, 400);
        configuration.useVsync(false);
        configuration.setForegroundFPS(60);
        configuration.setWindowIcon("graphics/pokeball_icon.png");
        return configuration;
    }
}