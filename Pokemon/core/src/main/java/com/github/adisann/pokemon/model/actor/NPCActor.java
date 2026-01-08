package com.github.adisann.pokemon.model.actor;

import com.github.adisann.pokemon.battle.Trainer;
import com.github.adisann.pokemon.dialogue.Dialogue;
import com.github.adisann.pokemon.dialogue.LinearDialogueNode;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.save.GameSaveData;
import com.github.adisann.pokemon.util.AnimationSet;

/**
 * An NPC actor that can be interacted with for dialogue and trainer battles.
 */
public class NPCActor extends Actor {

    private String id; // Unique identifier for save system (e.g., "may")
    private String displayName; // Display name (e.g., "May")
    private Trainer trainer; // Trainer data for battles

    private String dialogueBeforeBattle;
    private String dialogueAfterBattle;

    /**
     * Create an NPC actor.
     * 
     * @param world      The world this NPC belongs to
     * @param x          X coordinate
     * @param y          Y coordinate
     * @param animations Animation set for the NPC
     */
    public NPCActor(World world, int x, int y, AnimationSet animations) {
        super(world, x, y, animations);
    }

    /**
     * Set the NPC's unique identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the NPC's unique identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the NPC's display name.
     */
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    /**
     * Get the NPC's display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the trainer data for this NPC (makes them a trainer NPC).
     */
    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    /**
     * Get the trainer data for this NPC.
     */
    public Trainer getTrainer() {
        return trainer;
    }

    /**
     * Check if this NPC is a trainer (can battle).
     */
    public boolean isTrainer() {
        return trainer != null;
    }

    /**
     * Set dialogue shown before battle.
     */
    public void setDialogueBeforeBattle(String dialogue) {
        this.dialogueBeforeBattle = dialogue;
    }

    /**
     * Set dialogue shown after being defeated.
     */
    public void setDialogueAfterBattle(String dialogue) {
        this.dialogueAfterBattle = dialogue;
    }

    public String getDialogueBeforeBattle() {
        return dialogueBeforeBattle;
    }

    public String getDialogueAfterBattle() {
        return dialogueAfterBattle;
    }

    /**
     * Get appropriate dialogue based on defeated status.
     * 
     * @param saveData Save data to check defeated status
     * @return Dialogue object with appropriate message
     */
    public Dialogue getInteractionDialogue(GameSaveData saveData) {
        Dialogue d = new Dialogue();

        if (saveData != null && saveData.isDefeated(this.id)) {
            // Already defeated - show after battle dialogue
            d.addNode(new LinearDialogueNode(dialogueAfterBattle != null ? dialogueAfterBattle : "...", 0));
        } else if (isTrainer()) {
            // Not defeated yet - show battle dialogue
            d.addNode(new LinearDialogueNode(dialogueBeforeBattle != null ? dialogueBeforeBattle : "Let's battle!", 0));
        } else {
            // Not a trainer - just show normal dialogue
            Dialogue existingDialogue = getDialogue();
            if (existingDialogue != null) {
                return existingDialogue;
            }
            d.addNode(new LinearDialogueNode("...", 0));
        }

        return d;
    }

    /**
     * Check if this trainer has been defeated.
     * 
     * @param saveData Save data to check
     * @return true if defeated
     */
    public boolean isDefeated(GameSaveData saveData) {
        return saveData != null && saveData.isDefeated(this.id);
    }
}
