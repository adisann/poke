package com.github.adisann.pokemon.battle;

import com.badlogic.gdx.math.MathUtils;
import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.model.Tile;
import com.github.adisann.pokemon.model.world.World;

public class EncounterManager {

    private MoveDatabase moveDatabase;

    public EncounterManager(MoveDatabase moveDatabase) {
        this.moveDatabase = moveDatabase;
    }

    public Pokemon checkEncounter(World world, int x, int y) {
        Tile tile = world.getMap().getTile(x, y);
        if (tile == null || tile.getTerrain() == null) {
            return null;
        }

        String terrainName = tile.getTerrain().getImageName();
        // Strict check: Only trigger on TALL_GRASS (which maps to "grass1")
        // "grass" is safe lawn.

        boolean isOldale = world.getName().toLowerCase().contains("oldale");

        if (!isOldale) { // Strict rules for everywhere else
            if (terrainName == null || !terrainName.equalsIgnoreCase("grass1")) {
                return null;
            }
        }
        // If Oldale, we ignore the grass check and allow encounters everywhere (as
        // requested)
        // But we still check for null terrain to avoid encounters in void
        if (terrainName == null) {
            return null;
        }

        // 10% chance
        if (MathUtils.randomBoolean(0.1f)) {
            int roll = MathUtils.random(4); // 0-4 for 5 Pokemon
            switch (roll) {
                case 0:
                    return Pokemon.generatePokemon("Bulbasaur", "graphics/pokemon/bulbasaur.png", moveDatabase);
                case 1:
                    return Pokemon.generatePokemon("Charmander", "graphics/pokemon/charmander.png", moveDatabase);
                case 2:
                    return Pokemon.generatePokemon("Squirtle", "graphics/pokemon/squirtle.png", moveDatabase);
                case 3:
                    return Pokemon.generatePokemon("Pidgey", "graphics/pokemon/pidgey.png", moveDatabase);
                default:
                    return Pokemon.generatePokemon("Slowpoke", "graphics/pokemon/slowpoke.png", moveDatabase);
            }
        }
        return null;
    }
}
