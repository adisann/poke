package com.github.adisann.pokemon.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.github.adisann.pokemon.model.PokemonSpecies;
import com.github.czyzby.autumn.annotation.Component;

/**
 * Manager class for saving and loading game progress.
 * Uses LibGDX Preferences with JSON serialization for cross-platform
 * compatibility.
 * 
 * Save location varies by platform:
 * - Windows: %USERPROFILE%/.prefs/
 * - Linux: ~/.prefs/
 * - macOS: ~/Library/Application Support/
 * 
 * @author Antigravity
 */
@Component
public class SaveManager {

    private static final String PREFERENCES_NAME = "pokemon_save";
    private static final String SAVE_KEY_PREFIX = "save_slot_";
    private static final String CURRENT_SLOT_KEY = "current_slot";
    private static final int MAX_SAVE_SLOTS = 3;

    private final Json json = new Json();
    private long sessionStartTime;
    private long previousPlaytime;

    public SaveManager() {
        // Initialize session tracking
        this.sessionStartTime = System.currentTimeMillis();
        this.previousPlaytime = 0;

        // Register custom serializer for PokemonSpecies record (fixes no-arg
        // constructor issue)
        json.setSerializer(PokemonSpecies.class, new PokemonSpeciesJsonAdapter());
    }

    /**
     * Save the game to the specified slot.
     * 
     * @param slot The save slot (0-2)
     * @param data The game data to save
     */
    public void saveGame(int slot, GameSaveData data) {
        if (slot < 0 || slot >= MAX_SAVE_SLOTS) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }

        // Update playtime
        long currentSessionTime = (System.currentTimeMillis() - sessionStartTime) / 1000;
        data.playtimeSeconds = previousPlaytime + currentSessionTime;
        data.saveTimestamp = System.currentTimeMillis();

        String jsonData = json.toJson(data);
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        prefs.putString(SAVE_KEY_PREFIX + slot, jsonData);
        prefs.putInteger(CURRENT_SLOT_KEY, slot);
        prefs.flush();

        System.out.println("Game saved to slot " + slot + ": " + data);
    }

    /**
     * Load the game from the specified slot.
     * 
     * @param slot The save slot (0-2)
     * @return The loaded game data, or null if no save exists
     */
    public GameSaveData loadGame(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_SLOTS) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }

        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        String jsonData = prefs.getString(SAVE_KEY_PREFIX + slot, null);

        if (jsonData == null || jsonData.isEmpty()) {
            return null;
        }

        GameSaveData data = json.fromJson(GameSaveData.class, jsonData);

        // Track previous playtime for this session
        this.previousPlaytime = data.playtimeSeconds;
        this.sessionStartTime = System.currentTimeMillis();

        System.out.println("Game loaded from slot " + slot + ": " + data);
        return data;
    }

    /**
     * Quick save to the last used slot.
     * 
     * @param data The game data to save
     */
    public void quickSave(GameSaveData data) {
        int slot = getCurrentSlot();
        saveGame(slot, data);
    }

    /**
     * Quick load from the last used slot.
     * 
     * @return The loaded game data, or null if no save exists
     */
    public GameSaveData quickLoad() {
        int slot = getCurrentSlot();
        return loadGame(slot);
    }

    /**
     * Check if a save exists in the specified slot.
     * 
     * @param slot The save slot (0-2)
     * @return true if a save exists
     */
    public boolean hasSaveGame(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_SLOTS) {
            return false;
        }
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        String jsonData = prefs.getString(SAVE_KEY_PREFIX + slot, null);
        return jsonData != null && !jsonData.isEmpty();
    }

    /**
     * Check if any save exists.
     * 
     * @return true if any save slot has data
     */
    public boolean hasAnySave() {
        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            if (hasSaveGame(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delete the save in the specified slot.
     * 
     * @param slot The save slot (0-2)
     */
    public void deleteSave(int slot) {
        if (slot < 0 || slot >= MAX_SAVE_SLOTS) {
            return;
        }
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        prefs.remove(SAVE_KEY_PREFIX + slot);
        prefs.flush();

        System.out.println("Save deleted from slot " + slot);
    }

    /**
     * Get the current/last used save slot.
     * 
     * @return The current save slot (defaults to 0)
     */
    public int getCurrentSlot() {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        return prefs.getInteger(CURRENT_SLOT_KEY, 0);
    }

    /**
     * Get save info for display in a menu.
     * 
     * @param slot The save slot (0-2)
     * @return A summary string for display, or null if no save
     */
    public String getSaveInfo(int slot) {
        GameSaveData data = loadGame(slot);
        if (data == null) {
            return null;
        }

        long hours = data.playtimeSeconds / 3600;
        long minutes = (data.playtimeSeconds % 3600) / 60;

        return String.format("Slot %d: %s (%d:%02d playtime)",
                slot + 1, data.worldName, hours, minutes);
    }

    /**
     * Get the maximum number of save slots.
     * 
     * @return The number of available save slots
     */
    public int getMaxSlots() {
        return MAX_SAVE_SLOTS;
    }
}
