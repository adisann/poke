package com.github.adisann.pokemon.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database for NPC data loaded from JSON.
 * NPCs include trainers, healers, and other interactable characters.
 * 
 * @author Antigravity
 */
public class NPCDatabase {

    private static final String NPC_FILE = "data/trainers/npcs.json";
    private static Map<String, NPCData> npcs = new HashMap<>();
    private static List<String> loadErrors = new ArrayList<>();
    private static boolean loaded = false;

    /**
     * Load all NPCs from JSON file.
     */
    public static void load() {
        if (loaded)
            return;

        FileHandle file = Gdx.files.internal(NPC_FILE);
        if (!file.exists()) {
            Gdx.app.error("NPCDatabase", "NPC file not found: " + NPC_FILE);
            loaded = true;
            return;
        }

        try {
            JsonValue root = new JsonReader().parse(file);

            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                try {
                    NPCData npc = parseNPC(entry);
                    npcs.put(npc.id, npc);
                } catch (Exception e) {
                    String id = entry.getString("id", "unknown");
                    loadErrors.add(id + ": " + e.getMessage());
                    Gdx.app.error("NPCDatabase", "Failed to load NPC: " + id, e);
                }
            }

            loaded = true;
            Gdx.app.log("NPCDatabase", "Loaded " + npcs.size() + " NPCs from JSON");

        } catch (Exception e) {
            Gdx.app.error("NPCDatabase", "Failed to parse npcs.json", e);
            loaded = true;
        }
    }

    /**
     * Get NPC data by ID.
     */
    public static NPCData getNPC(String id) {
        if (!loaded)
            load();
        return npcs.get(id);
    }

    /**
     * Check if an NPC exists.
     */
    public static boolean hasNPC(String id) {
        if (!loaded)
            load();
        return npcs.containsKey(id);
    }

    /**
     * Parse a single NPC from JSON.
     */
    private static NPCData parseNPC(JsonValue entry) {
        NPCData npc = new NPCData();
        npc.id = entry.getString("id");
        npc.displayName = entry.getString("displayName", npc.id);
        npc.sprite = entry.getString("sprite", null);
        npc.battleSprite = entry.getString("battleSprite", null);
        npc.isTrainer = entry.getBoolean("isTrainer", false);
        npc.isHealer = entry.getBoolean("isHealer", false);
        npc.dialogueBefore = entry.getString("dialogueBefore", null);
        npc.dialogueAfter = entry.getString("dialogueAfter", null);

        // Parse team
        JsonValue teamJson = entry.get("team");
        if (teamJson != null) {
            for (JsonValue pokemon = teamJson.child; pokemon != null; pokemon = pokemon.next) {
                TeamMember member = new TeamMember();
                member.species = pokemon.getString("species");
                member.level = pokemon.getInt("level", 5);

                // Parse moves
                JsonValue movesJson = pokemon.get("moves");
                if (movesJson != null) {
                    for (JsonValue move = movesJson.child; move != null; move = move.next) {
                        member.moves.add(move.asString());
                    }
                }
                npc.team.add(member);
            }
        }

        // Parse rewards
        JsonValue rewardsJson = entry.get("rewards");
        if (rewardsJson != null) {
            npc.rewardMoney = rewardsJson.getInt("money", 0);
        }

        return npc;
    }

    /**
     * NPC data container.
     */
    public static class NPCData {
        public String id;
        public String displayName;
        public String sprite;
        public String battleSprite;
        public boolean isTrainer;
        public boolean isHealer;
        public String dialogueBefore;
        public String dialogueAfter;
        public List<TeamMember> team = new ArrayList<>();
        public int rewardMoney;
    }

    /**
     * Pokemon team member data.
     */
    public static class TeamMember {
        public String species;
        public int level;
        public List<String> moves = new ArrayList<>();
    }
}
