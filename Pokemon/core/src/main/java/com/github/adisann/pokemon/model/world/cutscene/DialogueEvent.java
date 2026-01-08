package com.github.adisann.pokemon.model.world.cutscene;

import com.github.adisann.pokemon.dialogue.Dialogue;
import com.github.adisann.pokemon.dialogue.LinearDialogueNode;

/**
 * A CutsceneEvent that displays a dialogue message for a set duration.
 * Blocks player input while shown.
 */
public class DialogueEvent extends CutsceneEvent {

    private Dialogue dialogue;
    private String text;
    private boolean finished = false;
    private float timer = 0f;
    private float duration = 2f; // 2 seconds default

    public DialogueEvent(Dialogue dialogue) {
        this.dialogue = dialogue;
        // Extract text from first node if available
        if (dialogue.getNode(0) instanceof LinearDialogueNode) {
            this.text = ((LinearDialogueNode) dialogue.getNode(0)).getText();
        }
    }

    /**
     * Convenience constructor for a simple one-line message.
     */
    public DialogueEvent(String message) {
        this.dialogue = new Dialogue();
        this.dialogue.addNode(new LinearDialogueNode(message, 0));
        this.text = message;
    }

    /**
     * Constructor with custom duration.
     */
    public DialogueEvent(String message, float duration) {
        this(message);
        this.duration = duration;
    }

    @Override
    public void begin(CutscenePlayer player) {
        super.begin(player);
        // Show the dialogue using the cutscene player's dialogue system
        player.showDialogue(text);
    }

    @Override
    public void update(float delta) {
        timer += delta;
        if (timer >= duration) {
            finished = true;
            getPlayer().hideDialogue();
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void screenShow() {
        finished = true;
    }

    public Dialogue getDialogue() {
        return dialogue;
    }

    public String getText() {
        return text;
    }
}
