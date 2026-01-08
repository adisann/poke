package com.github.adisann.pokemon.save;

/**
 * Data class representing a game save.
 * All fields are public for easy JSON serialization.
 * 
 * @author Antigravity
 */
public class GameSaveData {

    // Player position
    public String worldName;
    public int playerX;
    public int playerY;
    public String playerFacing;

    // Game progress
    public int version = 1; // Save format version for migration support
    public long playtimeSeconds;
    public long saveTimestamp;

    // Future expandable fields for pokemon team, items, etc.
    public java.util.List<com.github.adisann.pokemon.model.Pokemon> team;
    // public Map<String, Integer> items;
    // public int money;
    // public List<String> badges;

    // Defeated trainers for trainer battle persistence
    public java.util.Set<String> defeatedTrainers = new java.util.HashSet<>();

    /**
     * Mark a trainer as defeated.
     * 
     * @param trainerId Unique identifier for the trainer (e.g., "may")
     */
    public void markDefeated(String trainerId) {
        // Ensure defeatedTrainers is initialized (for old saves that may have null)
        if (defeatedTrainers == null) {
            defeatedTrainers = new java.util.HashSet<>();
        }
        defeatedTrainers.add(trainerId);
        System.out.println("[GameSaveData] Marked " + trainerId + " as defeated. Total: " + defeatedTrainers);
    }

    /**
     * Check if a trainer has been defeated.
     * 
     * @param trainerId Unique identifier for the trainer
     * @return true if the trainer has been defeated
     */
    public boolean isDefeated(String trainerId) {
        if (defeatedTrainers == null) {
            return false;
        }
        boolean defeated = defeatedTrainers.contains(trainerId);
        System.out.println(
                "[GameSaveData] isDefeated(" + trainerId + ") = " + defeated + " (list: " + defeatedTrainers + ")");
        return defeated;
    }

    /**
     * Default constructor required for JSON deserialization.
     */
    public GameSaveData() {
        // Required for JSON
    }

    /**
     * Create a new save data with the given values.
     * 
     * @param worldName    Name of the current world
     * @param playerX      Player X coordinate
     * @param playerY      Player Y coordinate
     * @param playerFacing Direction the player is facing (NORTH, SOUTH, EAST, WEST)
     */
    public GameSaveData(String worldName, int playerX, int playerY, String playerFacing) {
        this.worldName = worldName;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerFacing = playerFacing;
        this.playtimeSeconds = 0;
        this.saveTimestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "GameSaveData{" +
                "worldName='" + worldName + '\'' +
                ", playerX=" + playerX +
                ", playerY=" + playerY +
                ", playerFacing='" + playerFacing + '\'' +
                ", playtimeSeconds=" + playtimeSeconds +
                ", saveTimestamp=" + saveTimestamp +
                '}';
    }
}
