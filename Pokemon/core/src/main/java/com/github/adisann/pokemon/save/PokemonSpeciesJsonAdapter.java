package com.github.adisann.pokemon.save;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.adisann.pokemon.battle.STAT;
import com.github.adisann.pokemon.model.PokemonSpecies;
import com.github.adisann.pokemon.model.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom JSON serializer for PokemonSpecies record.
 * 
 * LibGDX's default JSON serializer requires a no-arg constructor,
 * which Java records don't have. This adapter handles serialization
 * and deserialization manually to preserve the record's immutability.
 * 
 * @author Antigravity
 */
public class PokemonSpeciesJsonAdapter implements Json.Serializer<PokemonSpecies> {

    @Override
    public void write(Json json, PokemonSpecies species, Class knownType) {
        json.writeObjectStart();
        json.writeValue("name", species.name());

        // Write baseStats as nested object
        json.writeObjectStart("baseStats");
        for (Map.Entry<STAT, Integer> entry : species.baseStats().entrySet()) {
            json.writeValue(entry.getKey().name(), entry.getValue());
        }
        json.writeObjectEnd();

        // Write types as array of strings
        json.writeArrayStart("types");
        for (Type type : species.types()) {
            json.writeValue(type.name());
        }
        json.writeArrayEnd();

        json.writeValue("baseExp", species.baseExp());
        json.writeValue("spriteName", species.spriteName());
        json.writeValue("backSpriteName", species.backSpriteName());
        json.writeObjectEnd();
    }

    @Override
    public PokemonSpecies read(Json json, JsonValue data, Class type) {
        String name = data.getString("name");
        int baseExp = data.getInt("baseExp", 64);
        String spriteName = data.getString("spriteName", "");
        String backSpriteName = data.getString("backSpriteName", null);

        // Parse baseStats
        Map<STAT, Integer> baseStats = new HashMap<>();
        JsonValue statsJson = data.get("baseStats");
        if (statsJson != null) {
            for (JsonValue stat = statsJson.child; stat != null; stat = stat.next) {
                try {
                    STAT statEnum = STAT.valueOf(stat.name);
                    baseStats.put(statEnum, stat.asInt());
                } catch (IllegalArgumentException e) {
                    System.err.println("[PokemonSpeciesJsonAdapter] Unknown stat: " + stat.name);
                }
            }
        }

        // Parse types
        List<Type> types = new ArrayList<>();
        JsonValue typesJson = data.get("types");
        if (typesJson != null) {
            for (JsonValue typeVal = typesJson.child; typeVal != null; typeVal = typeVal.next) {
                try {
                    types.add(Type.valueOf(typeVal.asString()));
                } catch (IllegalArgumentException e) {
                    System.err.println("[PokemonSpeciesJsonAdapter] Unknown type: " + typeVal.asString());
                }
            }
        }

        // Construct immutable record
        return new PokemonSpecies(name, baseStats, types, baseExp, spriteName, backSpriteName);
    }
}
