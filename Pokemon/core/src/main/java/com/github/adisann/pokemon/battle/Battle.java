package com.github.adisann.pokemon.battle;

import java.util.ArrayList;
import java.util.List;

import com.github.adisann.pokemon.battle.animation.FaintingAnimation;
import com.github.adisann.pokemon.battle.animation.PokeballAnimation;
import com.github.adisann.pokemon.battle.event.AnimationBattleEvent;
import com.github.adisann.pokemon.battle.event.BattleEvent;
import com.github.adisann.pokemon.battle.event.BattleEventQueuer;
import com.github.adisann.pokemon.battle.event.BattleEventPlayer;
import com.github.adisann.pokemon.battle.event.HPAnimationEvent;
import com.github.adisann.pokemon.battle.event.NameChangeEvent;
import com.github.adisann.pokemon.battle.event.PokeSpriteEvent;
import com.github.adisann.pokemon.battle.event.EXPAnimationEvent;
import com.github.adisann.pokemon.battle.event.TextEvent;
import com.github.adisann.pokemon.battle.moves.Move;
import com.github.adisann.pokemon.model.Pokemon;

/**
 * A 100% real Pokemon fight! Right in your livingroom.
 * */
public class Battle implements BattleEventQueuer {

	public enum STATE {
		READY_TO_PROGRESS,
		SELECT_NEW_POKEMON,
		RAN,
		WIN,
		LOSE,
		;
	}

	private STATE state;

	private BattleMechanics mechanics;

	private Pokemon player;
	private Pokemon opponent;

	private Trainer playerTrainer;
	private Trainer opponentTrainer;

	private BattleEventPlayer eventPlayer;

	public Battle(Trainer player, Pokemon opponent) {
		this.playerTrainer = player;
		this.player = player.getPokemon(0);
		this.opponent = opponent;
		mechanics = new BattleMechanics();
		this.state = STATE.READY_TO_PROGRESS;
	}

	/**
	 * Plays appropriate animation for starting a battle
	 */
	public void beginBattle() {
		// Initialize opponent HP bar
		queueEvent(new HPAnimationEvent(
				BATTLE_PARTY.OPPONENT,
				opponent.getCurrentHitpoints(),
				opponent.getCurrentHitpoints(),
				opponent.getStat(STAT.HITPOINTS),
				0f));
		queueEvent(new PokeSpriteEvent(opponent.getSpriteName(), BATTLE_PARTY.OPPONENT));

		queueEvent(new TextEvent("Go " + player.getName() + "!", 1f));

		// Initialize player HP bar
		queueEvent(new HPAnimationEvent(
				BATTLE_PARTY.PLAYER,
				player.getCurrentHitpoints(),
				player.getCurrentHitpoints(),
				player.getStat(STAT.HITPOINTS),
				0f));
		queueEvent(new PokeSpriteEvent(player.getSpriteName(), BATTLE_PARTY.PLAYER));
		queueEvent(new AnimationBattleEvent(BATTLE_PARTY.PLAYER, new PokeballAnimation()));
	}

	/**
	 * Progress the battle one turn.
	 * 
	 * @param input Index of the move used by the player
	 */
	public void progress(int input) {
		if (state != STATE.READY_TO_PROGRESS) {
			return;
		}
		if (mechanics.goesFirst(player, opponent)) {
			playTurn(BATTLE_PARTY.PLAYER, input);
			if (opponent.isFainted() || state != STATE.READY_TO_PROGRESS) {
				return;
			}
			playTurn(BATTLE_PARTY.OPPONENT, 0);
		} else {
			playTurn(BATTLE_PARTY.OPPONENT, 0);
			if (player.isFainted() || state != STATE.READY_TO_PROGRESS) {
				return;
			}
			playTurn(BATTLE_PARTY.PLAYER, input);
		}
		/*
		 * XXX: Status effects go here.
		 */
	}

	/**
	 * Sends out a new Pokemon, in the case that the old one fainted.
	 * This will NOT take up a turn.
	 * 
	 * @param pokemon Pokemon the trainer is sending in
	 */
	public void chooseNewPokemon(Pokemon pokemon) {
		this.player = pokemon;
		queueEvent(new HPAnimationEvent(
				BATTLE_PARTY.PLAYER,
				pokemon.getCurrentHitpoints(),
				pokemon.getCurrentHitpoints(),
				pokemon.getStat(STAT.HITPOINTS),
				0f));
		queueEvent(new PokeSpriteEvent(pokemon.getSpriteName(), BATTLE_PARTY.PLAYER));
		queueEvent(new NameChangeEvent(pokemon.getName(), BATTLE_PARTY.PLAYER));
		queueEvent(new TextEvent("Go get 'em, " + pokemon.getName() + "!"));
		queueEvent(new AnimationBattleEvent(BATTLE_PARTY.PLAYER, new PokeballAnimation()));
		this.state = STATE.READY_TO_PROGRESS;
	}

	/**
	 * Attempts to run away
	 */
	public void attemptRun() {
		queueEvent(new TextEvent("Got away safely!", 3f)); // Auto-dismiss after 3 seconds
		this.state = STATE.RAN;
	}

	private void playTurn(BATTLE_PARTY user, int input) {
		BATTLE_PARTY target = BATTLE_PARTY.getOpposite(user);

		Pokemon pokeUser = null;
		Pokemon pokeTarget = null;
		if (user == BATTLE_PARTY.PLAYER) {
			pokeUser = player;
			pokeTarget = opponent;
		} else if (user == BATTLE_PARTY.OPPONENT) {
			pokeUser = opponent;
			pokeTarget = player;
		}

		Move move = pokeUser.getMove(input);

		/* Broadcast the text graphics */
		queueEvent(new TextEvent(pokeUser.getName() + " used\n" + move.getName().toUpperCase() + "!", 0.5f));

		if (mechanics.attemptHit(move, pokeUser, pokeTarget)) {
			move.useMove(mechanics, pokeUser, pokeTarget, user, this);
		} else { // miss
			/* Broadcast the text graphics */
			queueEvent(new TextEvent(pokeUser.getName() + "'s\nattack missed!", 0.5f));
		}

		if (player.isFainted()) {
			queueEvent(new AnimationBattleEvent(BATTLE_PARTY.PLAYER, new FaintingAnimation()));
			boolean anyoneAlive = false;
			for (int i = 0; i < getPlayerTrainer().getTeamSize(); i++) {
				if (!getPlayerTrainer().getPokemon(i).isFainted()) {
					anyoneAlive = true;
					break;
				}
			}
			if (anyoneAlive) {
				queueEvent(new TextEvent(player.getName() + " fainted!", true));
				this.state = STATE.SELECT_NEW_POKEMON;
			} else {
				queueEvent(new TextEvent("You blacked out!", 3f)); // Auto-dismiss after 3 seconds
				this.state = STATE.LOSE;
			}
		} else if (opponent.isFainted()) {
			queueEvent(new AnimationBattleEvent(BATTLE_PARTY.OPPONENT, new FaintingAnimation()));

			// Grant EXP to player Pokemon
			int expBefore = player.getCurrentExp();
			int expGain = Pokemon.calculateExpGain(opponent);
			boolean leveledUp = player.gainExp(expGain);
			int expAfter = player.getCurrentExp();
			int expToNext = player.getExpToNextLevel();

			queueEvent(new TextEvent(player.getName() + " gained " + expGain + " EXP!", 1.5f));
			// Animate EXP bar fill
			queueEvent(new EXPAnimationEvent(expBefore, expAfter, expToNext, player.getLevel(), 0.5f));

			if (leveledUp) {
				queueEvent(new TextEvent(player.getName() + " grew to Lv" + player.getLevel() + "!", 1.5f));
			}

			queueEvent(new TextEvent("Congratulations! You Win!", 3f)); // Auto-dismiss after 3 seconds
			this.state = STATE.WIN;
		}
	}

	public Pokemon getPlayerPokemon() {
		return player;
	}

	public Pokemon getOpponentPokemon() {
		return opponent;
	}

	public Trainer getPlayerTrainer() {
		return playerTrainer;
	}

	public Trainer getOpponentTrainer() {
		return opponentTrainer;
	}

	public STATE getState() {
		return state;
	}

	public void setEventPlayer(BattleEventPlayer player) {
		this.eventPlayer = player;
	}

	@Override
	public void queueEvent(BattleEvent event) {
		eventPlayer.queueEvent(event);
	}
}