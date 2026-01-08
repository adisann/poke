package com.github.adisann.pokemon.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.adisann.pokemon.battle.STAT;
import com.github.adisann.pokemon.model.PokemonSpecies;
import com.github.adisann.pokemon.model.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database for Pokemon species loaded from JSON.
 * Provides graceful error handling - individual species failures won't crash
 * the game.
 * 
 * @author Antigravity
 */
public class SpeciesDatabase {

    private static final String SPECIES_FILE = "data/pokemon/species.json";
    private static Map<String, PokemonSpecies> species = new HashMap<>();
    private static List<String> loadErrors = new ArrayList<>();
    private static boolean loaded = false;

    /** Feature flag for gradual rollout */
    public static boolean USE_JSON_SPECIES = true;

    /**
     * Load all species from JSON file.
     * Should be called during game initialization.
     */
    public static void load() {
        if (loaded)
            return;

        FileHandle file = Gdx.files.internal(SPECIES_FILE);
        if (!file.exists()) {
            Gdx.app.error("SpeciesDB", "Species file not found: " + SPECIES_FILE);
            loadFallback();
            return;
        }

        try {
            JsonValue root = new JsonReader().parse(file);

            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                try {
                    // Validate required fields
                    ValidationResult vr = validateSpecies(entry);
                    if (!vr.isValid) {
                        String id = entry.getString("id", "unknown");
                        loadErrors.add(id + ": " + String.join(", ", vr.errors));
                        Gdx.app.error("SpeciesDB", "Skipping invalid species: " + id);
                        continue;
                    }

                    // Parse and store
                    PokemonSpecies sp = parseSpecies(entry);
                    species.put(entry.getString("id"), sp);

                } catch (Exception e) {
                    String id = entry.getString("id", "unknown");
                    loadErrors.add(id + ": " + e.getMessage());
                    Gdx.app.error("SpeciesDB", "Failed to load species: " + id, e);
                }
            }

            loaded = true;
            Gdx.app.log("SpeciesDB", "Loaded " + species.size() + " species from JSON");

            if (!loadErrors.isEmpty()) {
                Gdx.app.error("SpeciesDB", loadErrors.size() + " species failed to load. Check logs.");
            }

            // Ensure MissingNo exists as fallback
            if (!species.containsKey("missingno")) {
                loadFallback();
            }

        } catch (Exception e) {
            Gdx.app.error("SpeciesDB", "Failed to parse species.json", e);
            loadFallback();
        }
    }

    /**
     * Get a Pokemon species by ID.
     * Returns MissingNo if species not found.
     */
    public static PokemonSpecies getSpecies(String id) {
        if (!loaded)
            load();

        PokemonSpecies sp = species.get(id);
        if (sp == null) {
            Gdx.app.error("SpeciesDB", "Unknown species: " + id + " - using MissingNo");
            return species.get("missingno");
        }
        return sp;
    }

    /**
     * Check if a species exists.
     */
    public static boolean hasSpecies(String id) {
        if (!loaded)
            load();
        return species.containsKey(id);
    }

    /**
     * Get all load errors for debugging.
     */
    public static List<String> getLoadErrors() {
        return new ArrayList<>(loadErrors);
    }

    /**
     * Create MissingNo fallback species.
     */
    private static void loadFallback() {
        Map<STAT, Integer> stats = new HashMap<>();
        stats.put(STAT.HITPOINTS, 33);
        stats.put(STAT.ATTACK, 136);
        stats.put(STAT.DEFENCE, 0);
        stats.put(STAT.SPECIAL_ATTACK, 6);
        stats.put(STAT.SPECIAL_DEFENCE, 6);
        stats.put(STAT.SPEED, 29);

        List<Type> types = new ArrayList<>();
        types.add(Type.NORMAL);

        PokemonSpecies missingNo = new PokemonSpecies(
                "MissingNo.", stats, types, 0, "graphics/pokemon/bulbasaur.png");
        species.put("missingno", missingNo);

        Gdx.app.log("SpeciesDB", "Loaded fallback MissingNo species");
    }

    /**
     * Parse a single species from JSON.
     */
    private static PokemonSpecies parseSpecies(JsonValue entry) {
        String name = entry.getString("name");
        int baseExp = entry.getInt("baseExp", 64);
        String spriteName = entry.getString("spriteName", "graphics/pokemon/bulbasaur.png");

        // Parse baseStats
        Map<STAT, Integer> baseStats = new HashMap<>();
        JsonValue statsJson = entry.get("baseStats");
        if (statsJson != null) {
            for (JsonValue stat = statsJson.child; stat != null; stat = stat.next) {
                try {
                    STAT statEnum = STAT.valueOf(stat.name);
                    baseStats.put(statEnum, stat.asInt());
                } catch (IllegalArgumentException e) {
                    Gdx.app.error("SpeciesDB", "Unknown stat: " + stat.name);
                }
            }
        }

        // Parse types
        List<Type> types = new ArrayList<>();
        JsonValue typesJson = entry.get("types");
        if (typesJson != null) {
            for (JsonValue typeVal = typesJson.child; typeVal != null; typeVal = typeVal.next) {
                try {
                    types.add(Type.valueOf(typeVal.asString()));
                } catch (IllegalArgumentException e) {
                    Gdx.app.error("SpeciesDB", "Unknown type: " + typeVal.asString());
                }
            }
        }

        return new PokemonSpecies(name, baseStats, types, baseExp, spriteName);
    }

    /**
     * Validate a species JSON entry.
     */
    private static ValidationResult validateSpecies(JsonValue json) {
        ValidationResult result = new ValidationResult();

        // Required fields
        if (!json.has("id")) {
            result.errors.add("Missing: id");
            result.isValid = false;
        }
        if (!json.has("name")) {
            result.errors.add("Missing: name");
            result.isValid = false;
        }
        if (!json.has("baseStats")) {
            result.errors.add("Missing: baseStats");
            result.isValid = false;
        }

        // Validate sprite exists
        String sprite = json.getString("spriteName", "");
        if (!sprite.isEmpty() && !Gdx.files.internal(sprite).exists()) {
            result.errors.add("Sprite not found: " + sprite);
            result.isValid = false;
        }

        // Warnings for optional fields
        if (!json.has("learnset")) {
            result.warnings.add("No learnset - Pokemon won't learn moves");
        }

        return result;
    }

    /**
     * Validation result container.
     */
    public static class ValidationResult {
        public boolean isValid = true;
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
    }
}
