package com.github.adisann.pokemon.controller;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.github.adisann.pokemon.dialogue.ChoiceDialogueNode;
import com.github.adisann.pokemon.dialogue.Dialogue;
import com.github.adisann.pokemon.dialogue.DialogueNode;
import com.github.adisann.pokemon.dialogue.DialogueTraverser;
import com.github.adisann.pokemon.dialogue.LinearDialogueNode;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.OptionBox;

/**
 * Controller for the game's dialogue system.
 */
public class DialogueController extends InputAdapter {

	private DialogueTraverser traverser;
	private DialogueBox dialogueBox;
	private OptionBox optionBox;
	private Runnable onDialogueEndCallback;
	private int lastNodeId = -1; // Track which node dialogue ended on

	public DialogueController(DialogueBox box, OptionBox optionBox) {
		this.dialogueBox = box;
		this.optionBox = optionBox;
	}

	/**
	 * Set a callback to be called when dialogue ends.
	 * Callback is cleared after being called.
	 */
	public void setOnDialogueEndCallback(Runnable callback) {
		this.onDialogueEndCallback = callback;
	}

	/**
	 * Get the last node ID that the dialogue ended on.
	 * Useful for checking which choice was made.
	 */
	public int getLastNodeId() {
		return lastNodeId;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (dialogueBox.isVisible()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (optionBox.isVisible()) {
			if (keycode == Keys.UP) {
				optionBox.moveUp();
				return true;
			} else if (keycode == Keys.DOWN) {
				optionBox.moveDown();
				return true;
			}
		}
		if (dialogueBox.isVisible() && !dialogueBox.isFinished()) {
			return false;
		}
		if (traverser != null && keycode == Keys.X) { // continue through tree
			DialogueNode thisNode = traverser.getNode();

			if (thisNode instanceof LinearDialogueNode) {
				LinearDialogueNode node = (LinearDialogueNode) thisNode;
				if (node.getPointers().isEmpty()) { // dead end, since no pointers
					lastNodeId = node.getID(); // Track which node we ended on
					traverser = null; // end dialogue
					dialogueBox.setVisible(false);

					// Invoke callback if set
					if (onDialogueEndCallback != null) {
						Runnable callback = onDialogueEndCallback;
						onDialogueEndCallback = null; // Clear to prevent re-trigger
						callback.run();
					}
				} else {
					progress(0); // progress through first pointer
				}
			}
			if (thisNode instanceof ChoiceDialogueNode) {
				ChoiceDialogueNode node = (ChoiceDialogueNode) thisNode;
				progress(optionBox.getIndex());
			}

			return true;
		}
		if (dialogueBox.isVisible()) {
			return true;
		}
		return false;
	}

	public void update(float delta) {
		if (dialogueBox.isFinished() && traverser != null) {
			DialogueNode nextNode = traverser.getNode();
			if (nextNode instanceof ChoiceDialogueNode) {
				optionBox.setVisible(true);
			}
		}
	}

	public void startDialogue(Dialogue dialogue) {
		traverser = new DialogueTraverser(dialogue);
		dialogueBox.setVisible(true);

		DialogueNode nextNode = traverser.getNode();
		if (nextNode instanceof LinearDialogueNode) {
			LinearDialogueNode node = (LinearDialogueNode) nextNode;
			dialogueBox.animateText(node.getText());
		}
		if (nextNode instanceof ChoiceDialogueNode) {
			ChoiceDialogueNode node = (ChoiceDialogueNode) nextNode;
			dialogueBox.animateText(node.getText());
			optionBox.clearChoices();
			for (String s : node.getLabels()) {
				optionBox.addOption(s);
			}
		}
	}

	private void progress(int index) {
		optionBox.setVisible(false);
		DialogueNode nextNode = traverser.getNextNode(index);

		if (nextNode instanceof LinearDialogueNode) {
			LinearDialogueNode node = (LinearDialogueNode) nextNode;
			dialogueBox.animateText(node.getText());
		}
		if (nextNode instanceof ChoiceDialogueNode) {
			ChoiceDialogueNode node = (ChoiceDialogueNode) nextNode;
			dialogueBox.animateText(node.getText());
			optionBox.clearChoices();
			for (String s : node.getLabels()) {
				optionBox.addOption(s);
			}
		}
	}

	public boolean isDialogueShowing() {
		return dialogueBox.isVisible();
	}
}