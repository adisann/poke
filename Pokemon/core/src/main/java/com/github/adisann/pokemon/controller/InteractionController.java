package com.github.adisann.pokemon.controller;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.github.adisann.pokemon.dialogue.Dialogue;
import com.github.adisann.pokemon.dialogue.LinearDialogueNode;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.Tile;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.actor.NPCActor;
import com.github.adisann.pokemon.screen.GameScreen;
import com.github.adisann.pokemon.save.GameSaveData;

/**
 * Controller that interacts with what is in front of the player Actor.
 */
public class InteractionController extends InputAdapter {

	private Actor a;
	private DialogueController dialogueController;
	private GameScreen gameScreen; // For trainer battles

	public InteractionController(Actor a, DialogueController dialogueController) {
		this.a = a;
		this.dialogueController = dialogueController;
	}

	/**
	 * Set GameScreen reference for trainer battle support.
	 */
	public void setGameScreen(GameScreen gameScreen) {
		this.gameScreen = gameScreen;
	}

	@Override
	public boolean keyUp(int keycode) {
		// Issue 1: Block X key if dialogue is showing
		if (dialogueController.isDialogueShowing()) {
			return false; // Let DialogueController handle it
		}

		if (keycode == Keys.X) {
			Tile target = a.getWorld().getMap().getTile(a.getX() + a.getFacing().getDX(),
					a.getY() + a.getFacing().getDY());
			if (target.getActor() != null) {
				Actor targetActor = target.getActor();

				// Special handling for NPC actors (trainers, etc.)
				if (targetActor instanceof NPCActor) {
					NPCActor npc = (NPCActor) targetActor;

					// Face the player
					npc.refaceWithoutAnimation(DIRECTION.getOpposite(a.getFacing()));

					// Issue 2: Check defeated state (using session memory + save data)
					boolean isDefeated = false;
					if (npc.isTrainer() && gameScreen != null) {
						isDefeated = gameScreen.isTrainerDefeated(npc.getId());
					} else {
						// Fallback for non-trainer NPCs
						GameSaveData saveData = gameScreen != null ? gameScreen.getSaveData() : null;
						isDefeated = saveData != null && saveData.isDefeated(npc.getId());
					}

					// Check if this is a trainer that hasn't been defeated
					if (npc.isTrainer() && gameScreen != null && !isDefeated) {
						// Issue 3: Check if player's Pokemon is fainted
						com.github.adisann.pokemon.battle.Trainer playerTrainer = gameScreen.getPlayerTrainer();
						if (playerTrainer != null && playerTrainer.getPokemon(0).isFainted()) {
							// Show "heal your Pokemon" message
							Dialogue healDialogue = new Dialogue();
							LinearDialogueNode node = new LinearDialogueNode("Please heal your Pokemon first!", 0);
							healDialogue.addNode(node);
							dialogueController.startDialogue(healDialogue);
							return true;
						}

						// Show dialogue first (Before Battle)
						Dialogue dialogue = new Dialogue();
						String text = npc.getDialogueBeforeBattle();
						if (text == null)
							text = "Let's battle!";
						dialogue.addNode(new LinearDialogueNode(text, 0));
						dialogueController.startDialogue(dialogue);

						// Set callback to start battle after dialogue dismissed
						final String trainerId = npc.getId();
						final com.github.adisann.pokemon.battle.Trainer trainer = npc.getTrainer();
						dialogueController.setOnDialogueEndCallback(() -> {
							gameScreen.startTrainerBattle(trainerId, trainer);
						});
					} else {
						// Regular NPC dialogue (defeated trainer or non-trainer)
						Dialogue dialogue;
						if (isDefeated) {
							// Manually construct defeated dialogue
							dialogue = new Dialogue();
							String text = npc.getDialogueAfterBattle();
							if (text == null)
								text = "...";
							dialogue.addNode(new LinearDialogueNode(text, 0));
						} else if (npc.isHealer()) {
							// Healer NPC - show Yes/No healing choice
							GameSaveData saveData = gameScreen != null ? gameScreen.getSaveData() : null;
							dialogue = npc.getInteractionDialogue(saveData);

							// Set callback to heal party ONLY if player chose "Yes" (ends on node 1)
							if (gameScreen != null) {
								dialogueController.setOnDialogueEndCallback(() -> {
									// Check which choice was made: node 1 = Yes, node 2 = No
									if (dialogueController.getLastNodeId() == 1) {
										gameScreen.healPlayerTeam();
										System.out.println("[InteractionController] Healed player's team via Mom NPC.");
									}
								});
							}
						} else {
							// Use standard method for non-trainers
							GameSaveData saveData = gameScreen != null ? gameScreen.getSaveData() : null;
							dialogue = npc.getInteractionDialogue(saveData);
						}

						if (dialogue != null) {
							dialogueController.startDialogue(dialogue);
						}
					}
					return true;
				}

				// Regular Actor dialogue
				if (targetActor.getDialogue() != null) {
					if (targetActor.refaceWithoutAnimation(DIRECTION.getOpposite(a.getFacing()))) {
						Dialogue dialogue = targetActor.getDialogue();
						dialogueController.startDialogue(dialogue);
					}
				}
			}
			return false;
		}
		return false;
	}

}