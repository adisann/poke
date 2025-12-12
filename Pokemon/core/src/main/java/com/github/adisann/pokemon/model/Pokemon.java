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
	 * Helper to generate a standardized Pokemon for testing.
	 * Now takes String spriteName instead of Texture.
	 */
	public static Pokemon generatePokemon(String name, String spriteName, MoveDatabase moveDatabase) {
		Map<STAT, Integer> stats = new HashMap<>();
		List<Type> types = new ArrayList<>();
		int baseExp = 64;

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
		} else if (name.contains("Slow")) {
			stats.put(STAT.HITPOINTS, 90);
			stats.put(STAT.ATTACK, 65);
			stats.put(STAT.DEFENCE, 65);
			stats.put(STAT.SPECIAL_ATTACK, 40);
			stats.put(STAT.SPECIAL_DEFENCE, 40);
			stats.put(STAT.SPEED, 15);
			types.add(Type.WATER);
			types.add(Type.PSYCHIC);
			baseExp = 63;
		} else { // Generic / Charizard / Golem placeholders
			for (STAT s : STAT.values())
				stats.put(s, 50);
			if (name.contains("Char")) {
				types.add(Type.FIRE);
				types.add(Type.FLYING);
				baseExp = 240;
			} else {
				types.add(Type.NORMAL);
				baseExp = 50;
			}
		}

		PokemonSpecies spec = new PokemonSpecies(
				name,
				stats,
				types,
				baseExp,
				spriteName);

		Pokemon generated = new Pokemon(spec, 5); // Level 5
		generated.setMove(0, moveDatabase.getMove("Tackle"));
		if (types.contains(Type.WATER)) {
			generated.setMove(1, moveDatabase.getMove("Water Gun"));
		} else if (types.contains(Type.GRASS)) {
			generated.setMove(1, moveDatabase.getMove("Scratch")); // Placeholder for vine whip
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