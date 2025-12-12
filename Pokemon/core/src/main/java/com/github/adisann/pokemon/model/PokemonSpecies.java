package com.github.adisann.pokemon.model;

import com.github.adisann.pokemon.battle.STAT;
import java.util.Map;
import java.util.List;

/**
 * Immutable definition of a Pokemon species.
 */
public record PokemonSpecies(
        String name,
        Map<STAT, Integer> baseStats,
        List<Type> types,
        int baseExp,
        String spriteName) {
    public int getBaseStat(STAT stat) {
        return baseStats.getOrDefault(stat, 0);
    }
}
