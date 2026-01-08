package com.github.adisann.pokemon.battle.moves;

import com.github.adisann.pokemon.battle.BATTLE_PARTY;
import com.github.adisann.pokemon.battle.BattleMechanics;
import com.github.adisann.pokemon.battle.animation.BattleAnimation;
import com.github.adisann.pokemon.battle.event.BattleEventQueuer;
import com.github.adisann.pokemon.model.Pokemon;

/**
 * Represents a move a pokemon can do in battle.
 * 
 * Do not make new instances of these!
 * Instead, use {@link #clone()}.
 */
public abstract class Move {

	protected MoveSpecification spec;
	protected Class<? extends BattleAnimation> animationClass;

	public Move(MoveSpecification spec, Class<? extends BattleAnimation> animationClass) {
		this.spec = spec;
		this.animationClass = animationClass;
	}

	public int useMove(BattleMechanics mechanics, Pokemon user, Pokemon target, BATTLE_PARTY party,
			BattleEventQueuer broadcaster) {
		int damage = mechanics.calculateDamage(this, user, target, party == BATTLE_PARTY.PLAYER);
		target.applyDamage(damage);
		return damage;
	}

	public abstract BattleAnimation animation();

	public abstract String message();

	/**
	 * @return If this move deals damage
	 */
	public abstract boolean isDamaging();

	public String getName() {
		return spec.name(); // Record accessor
	}

	public com.github.adisann.pokemon.model.Type getType() {
		return spec.type();
	}

	public MOVE_CATEGORY getCategory() {
		return spec.category();
	}

	public int getPower() {
		return spec.power();
	}

	public float getAccuracy() {
		return spec.accuracy();
	}

	public MoveSpecification getMoveSpecification() {
		return spec;
	}

	/**
	 * @return A copy of this instance.
	 */
	public abstract Move clone();
}