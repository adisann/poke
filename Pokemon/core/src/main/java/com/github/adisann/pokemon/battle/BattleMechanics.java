package com.github.adisann.pokemon.battle;

import com.badlogic.gdx.math.MathUtils;
import com.github.adisann.pokemon.battle.moves.MOVE_CATEGORY;
import com.github.adisann.pokemon.battle.moves.Move;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.model.Type;

/**
 * Contains methods useful for calculations during battle. 
 * 
 * Some say this is a ShoddyBattle tactic, and they're probably right.
 * */
public class BattleMechanics {
	
	private String message = "";
	
	private boolean criticalHit(Move move, Pokemon user, Pokemon target) {
		float probability = 1f/16f;
		if (probability >= MathUtils.random(1.0f)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return True if the player goes first.
	 */
	public boolean goesFirst(Pokemon player, Pokemon opponent) {
		if (player.getStat(STAT.SPEED) > opponent.getStat(STAT.SPEED)) {
			return true;
		} else if (opponent.getStat(STAT.SPEED) > player.getStat(STAT.SPEED)) {
			return false;
		} else {
			return MathUtils.randomBoolean();
		}
	}
	
	public boolean attemptHit(Move move, Pokemon user, Pokemon target) {
		float random = MathUtils.random(1.0f);
		if (move.getAccuracy() >= random) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Formula found here {@link http://bulbapedia.bulbagarden.net/wiki/Damage#Damage_formula}
	 */
	public int calculateDamage(Move move, Pokemon user, Pokemon target) {
		message = "";
		
		float attack = 0f;
		if (move.getCategory() == MOVE_CATEGORY.PHYSICAL) {
			attack = user.getStat(STAT.ATTACK);
		} else {
			attack = user.getStat(STAT.SPECIAL_ATTACK);
		}
		
		float defence = 0f;
		if (move.getCategory() == MOVE_CATEGORY.PHYSICAL) {
			defence = target.getStat(STAT.DEFENCE);
		} else {
			defence = target.getStat(STAT.SPECIAL_DEFENCE);
		}
		
		boolean isCritical = criticalHit(move, user, target);
		
		int level = user.getLevel();
		float base = move.getPower();
		float modifier = MathUtils.random(0.85f, 1.00f);
		
		// Apply critical hit
		if (isCritical) {
			modifier = modifier * 2f;
			message = "A critical hit!";
		}
		
		// Apply type effectiveness
		float typeMultiplier = 1f;
		if (move.getType() != null && target.getTypes() != null) {
			for (Type targetType : target.getTypes()) {
				typeMultiplier *= Type.getEffectiveness(move.getType(), targetType);
			}
		}
		
		// Add effectiveness message
		if (typeMultiplier >= 2f) {
			if (!message.isEmpty()) message += " ";
			message += "It's super effective!";
		} else if (typeMultiplier > 0f && typeMultiplier < 1f) {
			if (!message.isEmpty()) message += " ";
			message += "It's not very effective...";
		} else if (typeMultiplier == 0f) {
			if (!message.isEmpty()) message += " ";
			message += "It doesn't affect " + target.getName() + "...";
			return 0;
		}
		
		modifier *= typeMultiplier;
		
		int damage = (int) ((  (2f*level+10f)/250f   *   (float)attack/defence   * base + 2   ) * modifier);
		
		return damage;
	}
	
	public boolean hasMessage() {
		return !message.isEmpty();
	}
	
	public String getMessage() {
		return message;
	}
}