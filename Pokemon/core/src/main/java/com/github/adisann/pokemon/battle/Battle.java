package com.github.adisann.pokemon.battle;

import java.util.ArrayList;
import java.util.List;

import com.github.adisann.pokemon.battle.animation.FaintingAnimation;
import com.github.adisann.pokemon.battle.animation.PokeballAnimation;
import com.github.adisann.pokemon.battle.animation.OpponentPokeballAnimation;
import com.github.adisann.pokemon.battle.animation.SlideInAnimation;
import com.github.adisann.pokemon.battle.animation.SlideOutAnimation;
import com.github.adisann.pokemon.battle.event.AnimationBattleEvent;
import com.github.adisann.pokemon.battle.event.BattleEvent;
import com.github.adisann.pokemon.battle.event.BattleEventQueuer;
import com.github.adisann.pokemon.battle.event.BattleEventPlayer;
import com.github.adisann.pokemon.battle.event.HPAnimationEvent;
import com.github.adisann.pokemon.battle.event.LearnMoveEvent;
import com.github.adisann.pokemon.battle.event.NameChangeEvent;
import com.github.adisann.pokemon.battle.event.PokeSpriteEvent;
import com.github.adisann.pokemon.battle.event.PokeballCatchEvent;
import com.github.adisann.pokemon.battle.event.EXPAnimationEvent;
import com.github.adisann.pokemon.battle.event.TextEvent;
import com.github.adisann.pokemon.battle.moves.Move;
import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.adisann.pokemon.model.Pokemon;

/**
 * A 100% real Pokemon fight! Right in your livingroom.
 */
public class Battle implements BattleEventQueuer {

	public enum STATE {
		READY_TO_PROGRESS,
		SELECT_NEW_POKEMON,
		RAN,
		WIN,
		LOSE,
		CAUGHT,
		;
	}

	private STATE state;

	private BattleMechanics mechanics;

	private Pokemon player;
	private Pokemon opponent;

	private Trainer playerTrainer;
	private Trainer opponentTrainer;

	private BattleEventPlayer eventPlayer;

	private MoveDatabase moveDatabase;

	public Battle(Trainer player, Pokemon opponent, MoveDatabase moveDatabase) {
		this.playerTrainer = player;
		this.player = player.getPokemon(0);
		this.opponent = opponent;
		this.moveDatabase = moveDatabase;
		mechanics = new BattleMechanics();
		this.state = STATE.READY_TO_PROGRESS;
	}

	/**
	 * Constructor for trainer battles (player vs NPC trainer).
	 * 
	 * @param player       Player's trainer
	 * @param opponent     Opponent trainer (NPC)
	 * @param moveDatabase Move database for move lookups
	 */
	public Battle(Trainer player, Trainer opponent, MoveDatabase moveDatabase) {
		this.playerTrainer = player;
		this.opponentTrainer = opponent;
		this.player = player.getPokemon(0);
		this.opponent = opponent.getPokemon(0);
		this.moveDatabase = moveDatabase;
		mechanics = new BattleMechanics();
		this.state = STATE.READY_TO_PROGRESS;
	}

	/**
	 * Check if this is a trainer battle (vs NPC trainer, not wild Pokemon).
	 * Used to disable catching and running.
	 * 
	 * @return true if this is a trainer battle
	 */
	public boolean isTrainerBattle() {
		return opponentTrainer != null;
	}

	/**
	 * Plays appropriate animation for starting a battle
	 * Pokemon Emerald style: opponent slides in from right, then player's Pokemon
	 * appears
	 */
	public void beginBattle() {
		if (isTrainerBattle()) {
			// TRAINER BATTLE INTRO
			// 1. Show Trainer Sprite
			String trainerSprite = opponentTrainer.getSpriteName();
			if (trainerSprite == null) {
				// Fallback if no sprite set
				trainerSprite = opponent.getSpriteName();
			}
			queueEvent(new PokeSpriteEvent(trainerSprite, BATTLE_PARTY.OPPONENT));

			// 2. Slide Trainer IN
			queueEvent(new AnimationBattleEvent(BATTLE_PARTY.OPPONENT, new SlideInAnimation(true)));

			// 3. Intro Text
			queueEvent(new TextEvent("Trainer May wants to battle!", 2f));

			// 4. Slide Trainer OUT
			queueEvent(new AnimationBattleEvent(BATTLE_PARTY.OPPONENT, new SlideOutAnimation(true)));

			// 5. Send out Pokemon (Switch sprite)
			queueEvent(new PokeSpriteEvent(opponent.getSpriteName(), BATTLE_PARTY.OPPONENT));

			// 6. Pokeball Animation IN
			queueEvent(new AnimationBattleEvent(BATTLE_PARTY.OPPONENT, new OpponentPokeballAnimation()));
			queueEvent(new TextEvent("Trainer May sent out " + opponent.getName() + "!", 1.5f));
		} else {
			// WILD POKEMON INTRO
			// Wild Pokemon appeared - slides in from right
			queueEvent(new PokeSpriteEvent(opponent.getSpriteName(), BATTLE_PARTY.OPPONENT));
			queueEvent(new AnimationBattleEvent(BATTLE_PARTY.OPPONENT, new SlideInAnimation(true)));
			queueEvent(new TextEvent("Wild " + opponent.getName() + " appeared!", 1f));
		}

		// Initialize opponent HP bar (Common)
		queueEvent(new HPAnimationEvent(
				BATTLE_PARTY.OPPONENT,
				opponent.getCurrentHitpoints(),
				opponent.getCurrentHitpoints(),
				opponent.getStat(STAT.HITPOINTS),
				0f));

		queueEvent(new TextEvent("Go " + player.getName() + "!", 0.8f));

		// Initialize player HP bar
		queueEvent(new HPAnimationEvent(
				BATTLE_PARTY.PLAYER,
				player.getCurrentHitpoints(),
				player.getCurrentHitpoints(),
				player.getStat(STAT.HITPOINTS),
				0f));
		queueEvent(new PokeSpriteEvent(player.getBackSpriteName(), BATTLE_PARTY.PLAYER));
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
		queueEvent(new PokeSpriteEvent(pokemon.getBackSpriteName(), BATTLE_PARTY.PLAYER));
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

	/**
	 * Called when player uses an item (Potion, etc).
	 * Gives opponent a free turn, then resets to player's turn.
	 */
	public void useItemTurn() {
		// Opponent gets a turn after player uses an item
		playTurn(BATTLE_PARTY.OPPONENT, 0);

		// Reset to player's turn if battle continues
		if (this.state != STATE.WIN && this.state != STATE.LOSE &&
				this.state != STATE.SELECT_NEW_POKEMON && this.state != STATE.CAUGHT) {
			this.state = STATE.READY_TO_PROGRESS;
		}
	}

	/**
	 * Attempts to catch the wild Pokemon with a Pokeball.
	 * 
	 * @param catchRate The catch rate modifier from the Pokeball
	 * @return true if catch was successful
	 */
	public boolean attemptCatch(float catchRate) {
		// Calculate catch rate based on HP
		float hpRatio = (float) opponent.getCurrentHitpoints() /
				opponent.getStat(STAT.HITPOINTS);

		// Base catch rate: Full HP = 30%, 1 HP = 90%
		float baseCatchRate = 0.3f + (1.0f - hpRatio) * 0.6f;
		float finalCatchRate = baseCatchRate * catchRate;

		// Calculate shakes (each shake has a chance to fail)
		// 3 successful shakes = caught!
		int shakeCount = 0;
		boolean caught = true;

		for (int i = 0; i < 3; i++) {
			float roll = (float) Math.random();
			if (roll < finalCatchRate) {
				shakeCount++;
			} else {
				caught = false;
				break;
			}
		}

		queueEvent(new TextEvent("You threw a Poke Ball!", 0.5f));

		// Queue the catch animation event with calculated values
		queueEvent(new PokeballCatchEvent(caught, shakeCount));

		if (caught) {
			queueEvent(new TextEvent("Gotcha! " + opponent.getName() + " was caught!", 3f));
			this.state = STATE.CAUGHT;
		} else {
			// Wiggle-based failure messages
			String failMessage;
			switch (shakeCount) {
				case 0:
					failMessage = "Oh no! The Pokemon broke free!";
					break;
				case 1:
					failMessage = "Aww! It appeared to be caught!";
					break;
				case 2:
					failMessage = "Aargh! Almost had it!";
					break;
				default:
					failMessage = "Gah! It was so close, too!";
					break;
			}
			queueEvent(new TextEvent(failMessage, 1.5f));
			// Opponent gets a turn after failed catch
			playTurn(BATTLE_PARTY.OPPONENT, 0);

			// Ensure state is READY_TO_PROGRESS if battle continues
			if (this.state != STATE.WIN && this.state != STATE.LOSE &&
					this.state != STATE.SELECT_NEW_POKEMON && this.state != STATE.CAUGHT) {
				this.state = STATE.READY_TO_PROGRESS;
			}
		}

		return caught;
	}

	/**
	 * Get the caught Pokemon (only valid if state is CAUGHT)
	 */
	public Pokemon getCaughtPokemon() {
		if (state == STATE.CAUGHT) {
			return opponent;
		}
		return null;
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
			int oldLevel = player.getLevel();
			int expBefore = player.getCurrentExp();
			int expGain = Pokemon.calculateExpGain(opponent);
			boolean leveledUp = player.gainExp(expGain);
			int newLevel = player.getLevel();
			int expAfter = player.getCurrentExp();
			int expToNext = player.getExpToNextLevel();

			queueEvent(new TextEvent(player.getName() + " gained " + expGain + " EXP!", 1.5f));
			// Animate EXP bar fill
			queueEvent(new EXPAnimationEvent(expBefore, expAfter, expToNext, player.getLevel(), 0.5f));

			if (leveledUp) {
				queueEvent(new TextEvent(player.getName() + " grew to Lv" + player.getLevel() + "!", 1.5f));

				// Check for learnable moves at each level gained
				for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
					List<String> learnableMoves = player.getLearnableMovesAtLevel(lvl);
					for (String moveName : learnableMoves) {
						queueEvent(new TextEvent(player.getName() + " wants to learn " + moveName + "!", 2f));

						if (player.hasEmptyMoveSlot()) {
							// Learn immediately if there's an empty slot
							Move newMove = moveDatabase.getMove(moveName);
							if (newMove != null) {
								player.learnMove(newMove);
							}
							queueEvent(new TextEvent(player.getName() + " learned " + moveName + "!", 2f));
						} else {
							// All 4 slots full - show move replacement UI
							queueEvent(new TextEvent("But " + player.getName() + " already knows 4 moves!", 2f));
							queueEvent(new LearnMoveEvent(player, moveName, moveDatabase));
						}
					}
				}
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