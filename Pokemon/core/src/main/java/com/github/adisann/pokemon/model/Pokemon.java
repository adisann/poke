package com.github.adisann.pokemon.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.adisann.pokemon.battle.STAT;
import com.github.adisann.pokemon.battle.moves.Move;
import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.adisann.pokemon.battle.moves.MoveSpecification;

public class Pokemon implements Json.Serializable {

	private PokemonSpecies species;
	private int level;
	private StatusCondition status = StatusCondition.NONE;

	private Map<STAT, Integer> stats;
	private int currentHitpoints;
	private int currentExp;

	private Move[] moves = new Move[4];

	// Temporary storage for reloading moves from JSON
	private List<String> moveNamesForReload;

	public Pokemon(PokemonSpecies species, int level) {
		this.species = species;
		this.level = level;

		calculateStats();
		this.currentHitpoints = stats.get(STAT.HITPOINTS);
	}

	@Deprecated
	public Pokemon() {
		// For JSON Serialization
	}

	private void calculateStats() {
		stats = new HashMap<STAT, Integer>();
		for (STAT stat : STAT.values()) {
			int base = species.getBaseStat(stat);
			// Simplified formula: Base * Level / 50 + 5
			int val = (base * level) / 50 + 5;
			if (stat == STAT.HITPOINTS) {
				val = (base * level) / 50 + 10 + level;
			}
			stats.put(stat, val);
		}
	}

	public String getSpriteName() {
		return species.spriteName();
	}

	/**
	 * Get back-facing sprite name (for player's Pokemon in battle).
	 */
	public String getBackSpriteName() {
		return species.backSpriteName();
	}

	public int getCurrentHitpoints() {
		return currentHitpoints;
	}

	public void setCurrentHitpoints(int currentHitpoints) {
		this.currentHitpoints = currentHitpoints;
	}

	public String getName() {
		return species.name();
	}

	public int getLevel() {
		return level;
	}

	public java.util.List<Type> getTypes() {
		return species.types();
	}

	public void setMove(int index, Move move) {
		moves[index] = move;
	}

	public Move getMove(int index) {
		if (moves[index] == null)
			return null;
		return moves[index].clone();
	}

	public MoveSpecification getMoveSpecification(int index) {
		if (moves[index] == null) {
			return null;
		}
		return moves[index].getMoveSpecification();
	}

	public int getStat(STAT stat) {
		return stats.get(stat);
	}

	public void applyDamage(int amount) {
		currentHitpoints -= amount;
		if (currentHitpoints < 0) {
			currentHitpoints = 0;
		}
	}

	public boolean isFainted() {
		return currentHitpoints == 0;
	}

	/**
	 * Calculates EXP needed for next level using simplified cubic formula.
	 */
	public int getExpToNextLevel() {
		int nextLevel = level + 1;
		return (int) Math.pow(nextLevel, 3);
	}

	/**
	 * Get current EXP.
	 */
	public int getCurrentExp() {
		return currentExp;
	}

	/**
	 * Gain EXP and handle level up. Returns true if leveled up.
	 */
	public boolean gainExp(int amount) {
		if (amount <= 0)
			return false;

		currentExp += amount;
		boolean leveledUp = false;

		// Level up loop (can gain multiple levels)
		while (currentExp >= getExpToNextLevel() && level < 100) {
			currentExp -= getExpToNextLevel();
			level++;
			calculateStats();
			// Heal to new max HP on level up (optional, common in games)
			currentHitpoints = stats.get(STAT.HITPOINTS);
			leveledUp = true;
		}

		// Clamp exp to prevent overflow
		if (currentExp < 0)
			currentExp = 0;
		return leveledUp;
	}

	/**
	 * Calculates EXP gained from defeating an opponent.
	 * Safe formula avoiding NaN/overflow.
	 */
	public static int calculateExpGain(Pokemon defeated) {
		if (defeated == null || defeated.species == null) {
			return 1; // Fallback to minimum EXP
		}
		int baseExp = defeated.species.baseExp();
		int level = defeated.getLevel();
		if (baseExp <= 0 || level <= 0) {
			return 1; // Fallback to minimum EXP
		}
		// Simplified formula: (baseExp * level) / 7
		int exp = (baseExp * level) / 7;
		return Math.max(1, exp); // At least 1 EXP
	}

	public StatusCondition getStatus() {
		return status;
	}

	public void setStatus(StatusCondition status) {
		this.status = status;
	}

	/**
	 * Get moves learnable at a specific level for this Pokemon species.
	 * Returns list of move names, empty if none.
	 * Only includes moves with implemented type effectiveness.
	 */
	public List<String> getLearnableMovesAtLevel(int targetLevel) {
		String name = species.name();
		List<String> moves = new ArrayList<>();
		
		// Bulbasaur learnset (GRASS/NORMAL moves only)
		if (name.contains("Bulba")) {
			switch (targetLevel) {
				case 1: moves.add("Tackle"); break;
				case 9: moves.add("Vine Whip"); break;
			}
		}
		// Charmander learnset (FIRE/NORMAL moves only)
		else if (name.contains("Char")) {
			switch (targetLevel) {
				case 1: moves.add("Scratch"); break;
				case 7: moves.add("Ember"); break;
				case 13: moves.add("Rage"); break;
				case 19: moves.add("Slash"); break;
			}
		}
		// Squirtle learnset (WATER/NORMAL moves only)
		else if (name.contains("Squirt")) {
			switch (targetLevel) {
				case 1: moves.add("Tackle"); break;
				case 7: moves.add("Bubble"); break;
				case 10: moves.add("Water Gun"); break;
			}
		}
		// Pidgey learnset (NORMAL moves only - no FLYING implemented)
		else if (name.contains("Pidg")) {
			switch (targetLevel) {
				case 1: moves.add("Tackle"); break;
				case 9: moves.add("Quick Attack"); break;
			}
		}
		// Slowpoke learnset (WATER/NORMAL moves only)
		else if (name.contains("Slow")) {
			switch (targetLevel) {
				case 1: moves.add("Tackle"); break;
				case 6: moves.add("Water Gun"); break;
				case 15: moves.add("Headbutt"); break;
			}
		}
		// Default - generic Pokemon
		else {
			if (targetLevel == 1) moves.add("Tackle");
		}
		
		return moves;
	}

	/**
	 * Check if this Pokemon has an empty move slot.
	 */
	public boolean hasEmptyMoveSlot() {
		for (Move m : moves) {
			if (m == null) return true;
		}
		return false;
	}

	/**
	 * Add a move to the first empty slot. Returns true if successful.
	 */
	public boolean learnMove(Move move) {
		for (int i = 0; i < 4; i++) {
			if (moves[i] == null) {
				moves[i] = move;
				return true;
			}
		}
		return false; // No empty slots
	}

	/**
	 * Helper to generate a standardized Pokemon for testing.
	 * Now takes String spriteName instead of Texture.
	 */
	public static Pokemon generatePokemon(String name, String spriteName, MoveDatabase moveDatabase) {
		Map<STAT, Integer> stats = new HashMap<>();
		List<Type> types = new ArrayList<>();
		int baseExp = 64;

		// ===== BULBASAUR (Grass/Poison) =====
		if (name.contains("Bulba")) {
			stats.put(STAT.HITPOINTS, 45);
			stats.put(STAT.ATTACK, 49);
			stats.put(STAT.DEFENCE, 49);
			stats.put(STAT.SPECIAL_ATTACK, 65);
			stats.put(STAT.SPECIAL_DEFENCE, 65);
			stats.put(STAT.SPEED, 45);
			types.add(Type.GRASS);
			types.add(Type.POISON);
			baseExp = 64;
		}
		// ===== CHARMANDER (Fire) =====
		else if (name.contains("Char")) {
			stats.put(STAT.HITPOINTS, 39);
			stats.put(STAT.ATTACK, 52);
			stats.put(STAT.DEFENCE, 43);
			stats.put(STAT.SPECIAL_ATTACK, 60);
			stats.put(STAT.SPECIAL_DEFENCE, 50);
			stats.put(STAT.SPEED, 65);
			types.add(Type.FIRE);
			baseExp = 62;
		}
		// ===== SQUIRTLE (Water) =====
		else if (name.contains("Squirt")) {
			stats.put(STAT.HITPOINTS, 44);
			stats.put(STAT.ATTACK, 48);
			stats.put(STAT.DEFENCE, 65);
			stats.put(STAT.SPECIAL_ATTACK, 50);
			stats.put(STAT.SPECIAL_DEFENCE, 64);
			stats.put(STAT.SPEED, 43);
			types.add(Type.WATER);
			baseExp = 63;
		}
		// ===== PIDGEY (Normal/Flying) =====
		else if (name.contains("Pidg")) {
			stats.put(STAT.HITPOINTS, 40);
			stats.put(STAT.ATTACK, 45);
			stats.put(STAT.DEFENCE, 40);
			stats.put(STAT.SPECIAL_ATTACK, 35);
			stats.put(STAT.SPECIAL_DEFENCE, 35);
			stats.put(STAT.SPEED, 56);
			types.add(Type.NORMAL);
			types.add(Type.FLYING);
			baseExp = 50;
		}
		// ===== SLOWPOKE (Water/Psychic) =====
		else if (name.contains("Slow")) {
			stats.put(STAT.HITPOINTS, 90);
			stats.put(STAT.ATTACK, 65);
			stats.put(STAT.DEFENCE, 65);
			stats.put(STAT.SPECIAL_ATTACK, 40);
			stats.put(STAT.SPECIAL_DEFENCE, 40);
			stats.put(STAT.SPEED, 15);
			types.add(Type.WATER);
			types.add(Type.PSYCHIC);
			baseExp = 63;
		}
		// ===== DEFAULT (Normal) =====
		else {
			for (STAT s : STAT.values())
				stats.put(s, 50);
			types.add(Type.NORMAL);
			baseExp = 50;
		}

		PokemonSpecies spec = new PokemonSpecies(
				name,
				stats,
				types,
				baseExp,
				spriteName);

		Pokemon generated = new Pokemon(spec, 5); // Level 5
		generated.setMove(0, moveDatabase.getMove("Tackle"));
		
		// Assign type-appropriate second move
		if (types.contains(Type.WATER)) {
			generated.setMove(1, moveDatabase.getMove("Water Gun"));
		} else if (types.contains(Type.FIRE)) {
			generated.setMove(1, moveDatabase.getMove("Scratch")); // Ember when available
		} else if (types.contains(Type.GRASS)) {
			generated.setMove(1, moveDatabase.getMove("Scratch")); // Vine Whip when available
		} else {
			generated.setMove(1, moveDatabase.getMove("Scratch"));
		}

		return generated;
	}

	public void reloadMoves(MoveDatabase db) {
		if (moveNamesForReload != null) {
			for (int i = 0; i < moveNamesForReload.size(); i++) {
				if (i < 4 && moveNamesForReload.get(i) != null) {
					moves[i] = db.getMove(moveNamesForReload.get(i));
				}
			}
			moveNamesForReload = null;
		}
	}

	@Override
	public void write(Json json) {
		json.writeValue("species", species);
		json.writeValue("level", level);
		json.writeValue("stats", stats);
		json.writeValue("currentHitpoints", currentHitpoints);
		json.writeValue("currentExp", currentExp);

		List<String> moveNames = new ArrayList<>();
		for (Move m : moves) {
			if (m != null)
				moveNames.add(m.getName());
		}
		json.writeValue("moveNames", moveNames);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		species = json.readValue("species", PokemonSpecies.class, jsonData);
		level = jsonData.getInt("level");
		stats = json.readValue("stats", HashMap.class, jsonData);
		currentHitpoints = jsonData.getInt("currentHitpoints");
		currentExp = jsonData.getInt("currentExp");

		moveNamesForReload = json.readValue("moveNames", ArrayList.class, jsonData);
		calculateStats(); // Recalculate derived stats just in case
	}
}