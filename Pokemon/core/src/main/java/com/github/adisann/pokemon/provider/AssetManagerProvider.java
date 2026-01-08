package com.github.adisann.pokemon.provider;

import com.badlogic.gdx.assets.AssetManager;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * Provides a singleton AssetManager instance for dependency injection.
 */
@Provider
public class AssetManagerProvider implements DependencyProvider<AssetManager> {

    private AssetManager assetManager;

    @Override
    public Class<AssetManager> getDependencyType() {
        return AssetManager.class;
    }

    @Override
    public AssetManager provide() {
        if (assetManager == null) {
            assetManager = new AssetManager();
        }
        return assetManager;
    }
}
