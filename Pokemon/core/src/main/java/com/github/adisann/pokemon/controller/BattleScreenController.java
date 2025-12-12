package com.github.adisann.pokemon.controller;

import java.util.Queue;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.github.adisann.pokemon.battle.Battle;
import com.github.adisann.pokemon.battle.event.BattleEvent;
import com.github.adisann.pokemon.battle.moves.MoveSpecification;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.MoveSelectBox;
import com.github.adisann.pokemon.ui.OptionBox;

/**
 * Controller for battle screen input handling.
 * Uses keyDown events only (no continuous keyHeld).
 * Z = confirm, X = cancel/back.
 * Arrow keys = navigate menus.
 */
public class BattleScreenController extends InputAdapter {

	public enum STATE {
		USE_NEXT_POKEMON, // Prompt to send next Pokemon
		SELECT_ACTION, // Fight/Run menu
		SELECT_MOVE, // Move selection
		DEACTIVATED, // No input accepted
	}

	private STATE state = STATE.DEACTIVATED;

	private Queue<BattleEvent> queue;
	private Battle battle;

	private DialogueBox dialogue;
	private OptionBox optionBox;
	private MoveSelectBox moveSelect;

	public BattleScreenController(Battle battle, Queue<BattleEvent> queue, DialogueBox dialogue, MoveSelectBox options,
			OptionBox optionBox) {
		this.battle = battle;
		this.queue = queue;
		this.dialogue = dialogue;
		this.moveSelect = options;
		this.optionBox = optionBox;
	}

	@Override
	public boolean keyDown(int keycode) {
		// Debug: uncomment to trace input
		// System.out.println("[BattleController] keyDown: " + keycode + ", state=" +
		// state);

		if (this.state == STATE.DEACTIVATED) {
			return false;
		}

		// Handle USE_NEXT_POKEMON state (Yes/No dialog)
		if (this.state == STATE.USE_NEXT_POKEMON && optionBox.isVisible()) {
			if (keycode == Keys.UP) {
				optionBox.moveUp();
				return true;
			} else if (keycode == Keys.DOWN) {
				optionBox.moveDown();
				return true;
			} else if (keycode == Keys.Z) {
				// Confirm selection
				if (optionBox.getIndex() == 0) { // YES
					for (int i = 0; i < battle.getPlayerTrainer().getTeamSize(); i++) {
						if (!battle.getPlayerTrainer().getPokemon(i).isFainted()) {
							battle.chooseNewPokemon(battle.getPlayerTrainer().getPokemon(i));
							optionBox.setVisible(false);
							this.state = STATE.DEACTIVATED;
							break;
						}
					}
				} else { // NO
					battle.attemptRun();
					optionBox.setVisible(false);
					this.state = STATE.DEACTIVATED;
				}
				return true;
			}
			return true; // Consume all input in this state
		}

		// Handle SELECT_ACTION state (Fight/Run menu)
		if (this.state == STATE.SELECT_ACTION && optionBox.isVisible()) {
			if (keycode == Keys.UP) {
				optionBox.moveUp();
				return true;
			} else if (keycode == Keys.DOWN) {
				optionBox.moveDown();
				return true;
			} else if (keycode == Keys.Z) {
				// Confirm selection
				if (optionBox.getIndex() == 0) { // FIGHT
					this.state = STATE.SELECT_MOVE;
					optionBox.setVisible(false);
					moveSelect.setVisible(true);
				} else { // RUN
					battle.attemptRun();
					this.state = STATE.DEACTIVATED;
					optionBox.setVisible(false);
				}
				return true;
			}
			return true; // Consume all input in this state
		}

		// Handle SELECT_MOVE state (Move selection 2x2 grid)
		if (this.state == STATE.SELECT_MOVE && moveSelect.isVisible()) {
			if (keycode == Keys.X) {
				// Cancel - go back to action menu
				this.state = STATE.SELECT_ACTION;
				moveSelect.setVisible(false);
				optionBox.setVisible(true);
				return true;
			} else if (keycode == Keys.Z) {
				// Confirm move selection
				int selection = moveSelect.getSelection();
				if (battle.getPlayerPokemon().getMove(selection) == null) {
					// No move in this slot - go back to action menu
					this.state = STATE.SELECT_ACTION;
					moveSelect.setVisible(false);
					optionBox.setVisible(true);
				} else {
					battle.progress(moveSelect.getSelection());
					endTurn();
				}
				return true;
			} else if (keycode == Keys.UP) {
				moveSelect.moveUp();
				return true;
			} else if (keycode == Keys.DOWN) {
				moveSelect.moveDown();
				return true;
			} else if (keycode == Keys.LEFT) {
				moveSelect.moveLeft();
				return true;
			} else if (keycode == Keys.RIGHT) {
				moveSelect.moveRight();
				return true;
			}
			return true; // Consume all input in this state
		}

		return false;
	}

	public STATE getState() {
		return state;
	}

	public void update(float delta) {
		if (isDisplayingNextDialogue() && dialogue.isFinished() && !optionBox.isVisible()) {
			optionBox.clearChoices();
			optionBox.addOption("YES");
			optionBox.addOption("NO");
			optionBox.setVisible(true);
		}
	}

	/**
	 * Displays the UI for a new turn (Fight/Run menu).
	 */
	public void restartTurn() {
		this.state = STATE.SELECT_ACTION;
		dialogue.setVisible(false);
		refreshMoves();

		optionBox.clearChoices();
		optionBox.addOption("FIGHT");
		optionBox.addOption("RUN");
		optionBox.setVisible(true);
	}

	private void refreshMoves() {
		for (int i = 0; i <= 3; i++) {
			String label = "------";
			MoveSpecification spec = battle.getPlayerPokemon().getMoveSpecification(i);
			if (spec != null) {
				label = spec.name();
			}
			moveSelect.setLabel(i, label.toUpperCase());
		}
	}

	/**
	 * Displays UI for selecting a new Pokemon after fainting.
	 */
	public void displayNextDialogue() {
		this.state = STATE.USE_NEXT_POKEMON;
		dialogue.setVisible(true);
		dialogue.animateText("Send out next pokemon?");
	}

	public boolean isDisplayingNextDialogue() {
		return this.state == STATE.USE_NEXT_POKEMON;
	}

	private void endTurn() {
		moveSelect.setVisible(false);
		this.state = STATE.DEACTIVATED;
	}
}