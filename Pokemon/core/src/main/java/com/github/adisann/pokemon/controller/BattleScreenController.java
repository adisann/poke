package com.github.adisann.pokemon.controller;

import java.util.Queue;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.github.adisann.pokemon.battle.Battle;
import com.github.adisann.pokemon.battle.event.BattleEvent;
import com.github.adisann.pokemon.battle.moves.MoveSpecification;
import com.github.adisann.pokemon.ui.ActionMenu;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.MoveSelectBox;
import com.github.adisann.pokemon.ui.OptionBox;
import com.github.adisann.pokemon.screen.BattleScreen;

/**
 * Pokemon Emerald-style battle controller.
 * Handles 4-option action menu (FIGHT/BAG/POKéMON/RUN).
 */
public class BattleScreenController extends InputAdapter {

    public enum STATE {
        USE_NEXT_POKEMON,
        SELECT_ACTION,
        SELECT_MOVE,
        SELECT_BAG_ITEM,
        SELECT_POKEMON,
        DEACTIVATED,
    }

    private STATE state = STATE.DEACTIVATED;

    private Queue<BattleEvent> queue;
    private Battle battle;
    private BattleScreen battleScreen;

    private DialogueBox dialogue;
    private OptionBox optionBox;
    private MoveSelectBox moveSelect;
    private ActionMenu actionMenu;

    public BattleScreenController(Battle battle, Queue<BattleEvent> queue, DialogueBox dialogue,
            MoveSelectBox moveSelect, OptionBox optionBox, ActionMenu actionMenu) {
        this.battle = battle;
        this.queue = queue;
        this.dialogue = dialogue;
        this.moveSelect = moveSelect;
        this.optionBox = optionBox;
        this.actionMenu = actionMenu;
    }

    public void setBattleScreen(BattleScreen battleScreen) {
        this.battleScreen = battleScreen;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (this.state == STATE.DEACTIVATED) {
            return false;
        }

        // Handle USE_NEXT_POKEMON state (Yes/No dialog)
        if (this.state == STATE.USE_NEXT_POKEMON) {
            return handleNextPokemonInput(keycode);
        }

        // Handle SELECT_BAG_ITEM state - MUST come before SELECT_ACTION
        if (this.state == STATE.SELECT_BAG_ITEM) {
            return handleBagItemInput(keycode);
        }

        // Handle SELECT_POKEMON state - MUST come before SELECT_ACTION
        if (this.state == STATE.SELECT_POKEMON) {
            return handlePokemonSelectInput(keycode);
        }

        // Handle SELECT_MOVE state
        if (this.state == STATE.SELECT_MOVE) {
            return handleMoveSelectInput(keycode);
        }

        // Handle SELECT_ACTION state (FIGHT/BAG/POKéMON/RUN)
        if (this.state == STATE.SELECT_ACTION) {
            return handleActionMenuInput(keycode);
        }

        return false;
    }

    private boolean handleNextPokemonInput(int keycode) {
        if (keycode == Keys.UP) {
            optionBox.moveUp();
        } else if (keycode == Keys.DOWN) {
            optionBox.moveDown();
        } else if (keycode == Keys.Z) {
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
        }
        return true;
    }

    private boolean handleActionMenuInput(int keycode) {
        if (keycode == Keys.UP) {
            actionMenu.moveUp();
        } else if (keycode == Keys.DOWN) {
            actionMenu.moveDown();
        } else if (keycode == Keys.LEFT) {
            actionMenu.moveLeft();
        } else if (keycode == Keys.RIGHT) {
            actionMenu.moveRight();
        } else if (keycode == Keys.Z) {
            ActionMenu.Action selected = actionMenu.getSelectedAction();
            switch (selected) {
                case FIGHT:
                    this.state = STATE.SELECT_MOVE;
                    actionMenu.setVisible(false);
                    dialogue.setVisible(false);
                    moveSelect.setVisible(true);
                    break;
                case BAG:
                    // Delegate to BattleScreen's full-screen bag UI
                    if (battleScreen != null) {
                        battleScreen.showBag();
                    }
                    break;
                case POKEMON:
                    // Delegate to BattleScreen's full-screen party UI
                    if (battleScreen != null) {
                        battleScreen.showParty();
                    }
                    break;
                case RUN:
                    battle.attemptRun();
                    this.state = STATE.DEACTIVATED;
                    actionMenu.setVisible(false);
                    dialogue.setVisible(false);
                    break;
            }
        }
        return true;
    }

    private boolean handleMoveSelectInput(int keycode) {
        if (keycode == Keys.X) {
            // Cancel - go back to action menu
            this.state = STATE.SELECT_ACTION;
            moveSelect.setVisible(false);
            dialogue.setVisible(true);
            actionMenu.setVisible(true);
        } else if (keycode == Keys.Z) {
            int selection = moveSelect.getSelection();
            if (battle.getPlayerPokemon().getMove(selection) == null) {
                // No move - go back
                this.state = STATE.SELECT_ACTION;
                moveSelect.setVisible(false);
                dialogue.setVisible(true);
                actionMenu.setVisible(true);
            } else {
                battle.progress(moveSelect.getSelection());
                endTurn();
            }
        } else if (keycode == Keys.UP) {
            moveSelect.moveUp();
        } else if (keycode == Keys.DOWN) {
            moveSelect.moveDown();
        } else if (keycode == Keys.LEFT) {
            moveSelect.moveLeft();
        } else if (keycode == Keys.RIGHT) {
            moveSelect.moveRight();
        }
        return true;
    }

    private boolean handleBagItemInput(int keycode) {
        if (keycode == Keys.UP) {
            optionBox.moveUp();
        } else if (keycode == Keys.DOWN) {
            optionBox.moveDown();
        } else if (keycode == Keys.X) {
            // Cancel - go back to action menu
            this.state = STATE.SELECT_ACTION;
            optionBox.setVisible(false);
            dialogue.setVisible(true);
            actionMenu.setVisible(true);
        } else if (keycode == Keys.Z) {
            int selection = optionBox.getIndex();
            optionBox.setVisible(false);

            if (selection == 0) {
                // POKE BALL - attempt catch
                battle.attemptCatch(1.0f);
                this.state = STATE.DEACTIVATED;
                dialogue.setVisible(false);
            } else if (selection == 1) {
                // POTION - heal player's Pokemon
                // For simplicity, auto-heal the active Pokemon
                com.github.adisann.pokemon.model.Pokemon player = battle.getPlayerPokemon();
                int maxHP = player.getStat(com.github.adisann.pokemon.battle.STAT.HITPOINTS);
                int currentHP = player.getCurrentHitpoints();
                if (currentHP < maxHP) {
                    int newHP = Math.min(currentHP + 20, maxHP);
                    player.setCurrentHitpoints(newHP);
                    battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
                            player.getName() + " was healed for " + (newHP - currentHP) + " HP!", 1.5f));
                    battle.queueEvent(new com.github.adisann.pokemon.battle.event.HPAnimationEvent(
                            com.github.adisann.pokemon.battle.BATTLE_PARTY.PLAYER,
                            currentHP, newHP, maxHP, 0.5f));
                    // Opponent gets a turn after using item (like Emerald)
                    battle.useItemTurn();
                } else {
                    battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
                            player.getName() + " is already at full HP!", 1.5f));
                }
                this.state = STATE.DEACTIVATED;
                dialogue.setVisible(false);
            } else {
                // CANCEL - go back
                this.state = STATE.SELECT_ACTION;
                dialogue.setVisible(true);
                actionMenu.setVisible(true);
            }
        }
        return true;
    }

    private boolean handlePokemonSelectInput(int keycode) {
        if (keycode == Keys.UP) {
            optionBox.moveUp();
        } else if (keycode == Keys.DOWN) {
            optionBox.moveDown();
        } else if (keycode == Keys.X) {
            // Cancel - go back to action menu
            this.state = STATE.SELECT_ACTION;
            optionBox.setVisible(false);
            dialogue.setVisible(true);
            actionMenu.setVisible(true);
        } else if (keycode == Keys.Z) {
            int selection = optionBox.getIndex();
            int teamSize = battle.getPlayerTrainer().getTeamSize();

            if (selection == teamSize) {
                // CANCEL selected
                this.state = STATE.SELECT_ACTION;
                optionBox.setVisible(false);
                dialogue.setVisible(true);
                actionMenu.setVisible(true);
            } else {
                com.github.adisann.pokemon.model.Pokemon selected = battle.getPlayerTrainer().getPokemon(selection);

                // Check if trying to switch to current Pokemon
                if (selected == battle.getPlayerPokemon()) {
                    battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
                            selected.getName() + " is already out!", 1.5f));
                    this.state = STATE.DEACTIVATED;
                    optionBox.setVisible(false);
                    dialogue.setVisible(false);
                } else if (selected.isFainted()) {
                    battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
                            selected.getName() + " has no energy left!", 1.5f));
                    // Stay in select state
                } else {
                    // Switch Pokemon - costs a turn (opponent attacks)
                    optionBox.setVisible(false);
                    dialogue.setVisible(false);
                    battle.chooseNewPokemon(selected);
                    this.state = STATE.DEACTIVATED;
                }
            }
        }
        return true;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
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
     * Shows the action menu for a new turn.
     */
    public void restartTurn() {
        this.state = STATE.SELECT_ACTION;
        refreshMoves();

        // Show "What will [name] do?" and action menu
        dialogue.setVisible(true);
        dialogue.animateText("What will " + battle.getPlayerPokemon().getName() + " do?");
        actionMenu.setVisible(true);
        actionMenu.resetSelection();
    }

    private void refreshMoves() {
        for (int i = 0; i <= 3; i++) {
            MoveSpecification spec = battle.getPlayerPokemon().getMoveSpecification(i);
            if (spec != null) {
                // Use setMove to display PP and Type (Emerald style)
                int currentPP = spec.pp(); // Using max PP for now (no tracking implemented)
                int maxPP = spec.pp();
                moveSelect.setMove(i, spec.name(), currentPP, maxPP, spec.type());
            } else {
                moveSelect.setLabel(i, "------");
            }
        }
        moveSelect.resetSelection();
    }

    public void displayNextDialogue() {
        this.state = STATE.USE_NEXT_POKEMON;
        dialogue.setVisible(true);
        dialogue.animateText("Send out next pokemon?");
        actionMenu.setVisible(false);
    }

    public boolean isDisplayingNextDialogue() {
        return this.state == STATE.USE_NEXT_POKEMON;
    }

    private void endTurn() {
        moveSelect.setVisible(false);
        this.state = STATE.DEACTIVATED;
    }
}