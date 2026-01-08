package com.github.adisann.pokemon.model;

import com.github.adisann.pokemon.battle.STAT;
import java.util.Map;
import java.util.List;

/**
 * Immutable definition of a Pokemon species.
 * 
 * spriteName: Front-facing sprite (used for opponent Pokemon)
 * backSpriteName: Back-facing sprite (used for player's Pokemon in battle)
 */
public record PokemonSpecies(
        String name,
        Map<STAT, Integer> baseStats,
        List<Type> types,
        int baseExp,
        String spriteName,
        String backSpriteName) {
    
    /**
     * Constructor for backwards compatibility (no back sprite).
     */
    public PokemonSpecies(String name, Map<STAT, Integer> baseStats, List<Type> types, 
                          int baseExp, String spriteName) {
        this(name, baseStats, types, baseExp, spriteName, deriveBackSpriteName(spriteName));
    }
    
    /**
     * Derives back sprite name from front sprite name.
     * Example: "graphics/pokemon/bulbasaur.gif" -> "graphics/pokemon/bulbasaur_backside.gif"
     */
    private static String deriveBackSpriteName(String spriteName) {
        if (spriteName == null) return null;
        int dotIndex = spriteName.lastIndexOf('.');
        if (dotIndex > 0) {
            return spriteName.substring(0, dotIndex) + "_backside" + spriteName.substring(dotIndex);
        }
        return spriteName + "_backside";
    }
    
    public int getBaseStat(STAT stat) {
        return baseStats.getOrDefault(stat, 0);
    }
}

