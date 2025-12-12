package com.github.adisann.pokemon.provider;

import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.czyzby.autumn.annotation.Provider;
import com.github.czyzby.autumn.provider.DependencyProvider;

/**
 * Provides MoveDatabase instance for dependency injection.
 */
@Provider
public class MoveDatabaseProvider implements DependencyProvider<MoveDatabase> {

    @Override
    public Class<MoveDatabase> getDependencyType() {
        return MoveDatabase.class;
    }

    @Override
    public MoveDatabase provide() {
        return new MoveDatabase();
    }
}
