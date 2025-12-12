package com.github.adisann.pokemon.battle.moves;

import com.github.adisann.pokemon.model.Type;

/**
 * Immutable specification for a Move.
 */
public record MoveSpecification(
		Type type,
		MOVE_CATEGORY category,
		int power,
		float accuracy,
		int pp,
		String name,
		String description) {
}