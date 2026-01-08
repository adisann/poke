package com.github.adisann.pokemon.battle.event;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.github.adisann.pokemon.battle.moves.Move;
import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.adisann.pokemon.battle.moves.MoveSpecification;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.ui.DialogueBox;

/**
 * Battle event for learning a new move when Pokemon already has 4 moves.
 * Shows current moves and asks player which to forget.
 */
public class LearnMoveEvent extends BattleEvent {

    private enum State {
        ASKING_REPLACE, // "Forget a move to learn [new move]?"
        SELECTING_MOVE, // Player selecting which move to forget (0-3) or cancel (4)
        CONFIRMING, // "1, 2, and... Poof!"
        LEARNED, // "[Pokemon] learned [move]!"
        CANCELLED // "[Pokemon] did not learn [move]."
    }

    private State state = State.ASKING_REPLACE;
    private boolean finished = false;

    private Pokemon pokemon;
    private String newMoveName;
    private MoveDatabase moveDatabase;
    private DialogueBox dialogue;

    private int selection = 0; // 0-3 = moves, 4 = don't learn
    private float inputCooldown = 0f;

    public LearnMoveEvent(Pokemon pokemon, String newMoveName, MoveDatabase moveDatabase) {
        this.pokemon = pokemon;
        this.newMoveName = newMoveName;
        this.moveDatabase = moveDatabase;
    }

    @Override
    public void begin(BattleEventPlayer player) {
        super.begin(player);
        dialogue = player.getDialogueBox();
        dialogue.setVisible(true);
        showMoveSelection();
    }

    private void showMoveSelection() {
        state = State.SELECTING_MOVE;
        updateDialogue();
    }

    private void updateDialogue() {
        StringBuilder sb = new StringBuilder();
        sb.append("Which move should be forgotten?\n");

        for (int i = 0; i < 4; i++) {
            MoveSpecification spec = pokemon.getMoveSpecification(i);
            String moveName = (spec != null) ? spec.name() : "------";
            String marker = (i == selection) ? "> " : "  ";
            sb.append(marker).append((i + 1)).append(". ").append(moveName);
            if (i < 3)
                sb.append("\n");
        }
        sb.append("\n");
        String cancelMarker = (selection == 4) ? "> " : "  ";
        sb.append(cancelMarker).append("5. Don't learn ").append(newMoveName);

        dialogue.animateText(sb.toString());
    }

    @Override
    public void update(float delta) {
        inputCooldown -= delta;
        if (inputCooldown > 0)
            return;

        switch (state) {
            case SELECTING_MOVE:
                handleMoveSelection();
                break;
            case CONFIRMING:
            case LEARNED:
            case CANCELLED:
                // Wait for input to proceed
                if (Gdx.input.isKeyJustPressed(Keys.X) || Gdx.input.isKeyJustPressed(Keys.Z)) {
                    if (state == State.CONFIRMING) {
                        state = State.LEARNED;
                        dialogue.animateText(pokemon.getName() + " learned " + newMoveName + "!");
                        inputCooldown = 0.3f;
                    } else {
                        finished = true;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleMoveSelection() {
        // Navigation
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selection = (selection + 4) % 5; // Wrap around (5 options: 0-3 moves + cancel)
            updateDialogue();
            inputCooldown = 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selection = (selection + 1) % 5;
            updateDialogue();
            inputCooldown = 0.1f;
        }

        // Selection
        if (Gdx.input.isKeyJustPressed(Keys.X) || Gdx.input.isKeyJustPressed(Keys.Z)) {
            if (selection == 4) {
                // Cancel - don't learn
                state = State.CANCELLED;
                dialogue.animateText(pokemon.getName() + " did not learn " + newMoveName + ".");
                inputCooldown = 0.3f;
            } else {
                // Forget selected move and learn new one
                MoveSpecification oldMove = pokemon.getMoveSpecification(selection);
                String oldMoveName = (oldMove != null) ? oldMove.name() : "a move";

                // Actually replace the move
                Move newMove = moveDatabase.getMove(newMoveName);
                if (newMove != null) {
                    pokemon.setMove(selection, newMove);
                }

                state = State.CONFIRMING;
                dialogue.animateText("1, 2, and... Poof!\n" + pokemon.getName() + " forgot " + oldMoveName + ".");
                inputCooldown = 0.3f;
            }
        }

        // Back button - cancel
        if (Gdx.input.isKeyJustPressed(Keys.C)) {
            state = State.CANCELLED;
            dialogue.animateText(pokemon.getName() + " did not learn " + newMoveName + ".");
            inputCooldown = 0.3f;
        }
    }

    @Override
    public boolean finished() {
        return finished;
    }
}
