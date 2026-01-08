package com.github.adisann.pokemon.battle.moves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.adisann.pokemon.battle.animation.ChargeAnimation;
import com.github.adisann.pokemon.model.Type;

/**
 * Collection of all moves known by the game.
 * */
public class MoveDatabase {

	private List<Move> moves = new ArrayList<Move>();
	private HashMap<String, Integer> mappings = new HashMap<String, Integer>();

	public MoveDatabase() {
		initializeMoves();
	}

	private void initializeMoves() {
		addMove(new DamageMove(
				new MoveSpecification(
						Type.NORMAL,
						MOVE_CATEGORY.PHYSICAL,
						50,
						1f,
						35,
						"Tackle",
						"Charges the foe with a full-body tackle."),
				ChargeAnimation.class));
		addMove(new DamageMove(
				new MoveSpecification(
						Type.WATER,
						MOVE_CATEGORY.SPECIAL,
						40,
						1f,
						25,
						"Water Gun",
						"Squirts water to attack the foe."),
				ChargeAnimation.class));
		addMove(new DamageMove(
				new MoveSpecification(
						Type.NORMAL,
						MOVE_CATEGORY.PHYSICAL,
						40,
						1f,
						35,
						"Scratch",
						"Scratches the foe with sharp claws."),
				ChargeAnimation.class));
		addMove(new DamageMove(
				new MoveSpecification(
						Type.DRAGON,
						MOVE_CATEGORY.PHYSICAL,
						80,
						1f,
						15,
						"Dragon Claw",
						"Hooks and slashes the foe with long, sharp claws."),
				ChargeAnimation.class));
		
		// ===== NEW MOVES WITH IMPLEMENTED TYPES =====
		
		// GRASS type moves
		addMove(new DamageMove(
				new MoveSpecification(
						Type.GRASS,
						MOVE_CATEGORY.PHYSICAL,
						45,
						1f,
						25,
						"Vine Whip",
						"Strikes the foe with slender, whiplike vines."),
				ChargeAnimation.class));
		
		// FIRE type moves
		addMove(new DamageMove(
				new MoveSpecification(
						Type.FIRE,
						MOVE_CATEGORY.SPECIAL,
						40,
						1f,
						25,
						"Ember",
						"Attacks the foe with small flames."),
				ChargeAnimation.class));
		
		// WATER type moves
		addMove(new DamageMove(
				new MoveSpecification(
						Type.WATER,
						MOVE_CATEGORY.SPECIAL,
						40,
						1f,
						30,
						"Bubble",
						"Sprays countless bubbles at the foe."),
				ChargeAnimation.class));
		
		// NORMAL type moves
		addMove(new DamageMove(
				new MoveSpecification(
						Type.NORMAL,
						MOVE_CATEGORY.PHYSICAL,
						40,
						1f,
						30,
						"Quick Attack",
						"An extremely fast attack that always strikes first."),
				ChargeAnimation.class));
		addMove(new DamageMove(
				new MoveSpecification(
						Type.NORMAL,
						MOVE_CATEGORY.PHYSICAL,
						20,
						1f,
						20,
						"Rage",
						"Raises Attack when hit. Lasts until the user switches."),
				ChargeAnimation.class));
		addMove(new DamageMove(
				new MoveSpecification(
						Type.NORMAL,
						MOVE_CATEGORY.PHYSICAL,
						70,
						1f,
						20,
						"Slash",
						"Slashes the foe with claws. High critical-hit ratio."),
				ChargeAnimation.class));
		addMove(new DamageMove(
				new MoveSpecification(
						Type.NORMAL,
						MOVE_CATEGORY.PHYSICAL,
						70,
						1f,
						15,
						"Headbutt",
						"A ramming attack that may cause flinching."),
				ChargeAnimation.class));
	}

	private void addMove(Move move) {
		moves.add(move);
		mappings.put(move.getName(), moves.size() - 1);
	}

	/**
	 * @param moveName Name of the Move you want
	 * @return Clone of the move
	 */
	public Move getMove(String moveName) {
		return moves.get(mappings.get(moveName)).clone();
	}

	/**
	 * @param index Index of wanted move
	 * @return Clone of move
	 */
	public Move getMove(int index) {
		return moves.get(index).clone();
	}
}