package com.github.adisann.pokemon.provider;

import aurelienribon.tweenengine.TweenManager;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * Provides TweenManager instance for dependency injection.
 */
@Provider
public class TweenManagerProvider implements DependencyProvider<TweenManager> {

    @Override
    public Class<TweenManager> getDependencyType() {
        return TweenManager.class;
    }

    @Override
    public TweenManager provide() {
        return new TweenManager();
    }
}
