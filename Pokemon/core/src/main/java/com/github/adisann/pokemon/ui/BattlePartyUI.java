package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.adisann.pokemon.battle.Trainer;
import com.github.adisann.pokemon.model.Pokemon;

/**
 * Full-screen party UI for battle.
 * Shows all party Pokemon with HP, allows switching.
 */
public class BattlePartyUI extends Table {
    
    public interface PartyActionListener {
        void onPokemonSelected(Pokemon pokemon, int index);
        void onCancel();
    }
    
    private int selectedIndex = 0;
    private Trainer trainer;
    private Pokemon currentActive;
    private Skin skin;
    private PartyActionListener listener;
    private InputAdapter inputHandler;
    
    private Table contentTable;
    
    public BattlePartyUI(Skin skin, Trainer trainer, Pokemon currentActive) {
        super(skin);
        this.skin = skin;
        this.trainer = trainer;
        this.currentActive = currentActive;
        this.setFillParent(true);
        this.setBackground("optionbox");
        
        buildUI();
        createInputHandler();
    }
    
    private void buildUI() {
        this.clearChildren();
        
        // Title
        Label title = new Label("POKEMON", skin);
        this.add(title).pad(8f).row();
        
        contentTable = new Table(skin);
        contentTable.setBackground("optionbox");
        
        for (int i = 0; i < trainer.getTeamSize(); i++) {
            Pokemon pokemon = trainer.getPokemon(i);
            
            Table row = new Table();
            Image arrow = new Image(skin, "arrow");
            arrow.setScaling(Scaling.none);
            arrow.setVisible(i == selectedIndex);
            
            // Pokemon info
            String status = "";
            if (pokemon.isFainted()) {
                status = " (FNT)";
            } else if (pokemon == currentActive) {
                status = " (OUT)";
            }
            
            Label nameLabel = new Label(pokemon.getName() + status, skin);
            Label levelLabel = new Label("Lv" + pokemon.getLevel(), skin, "smallLabel");
            
            // HP display
            int currentHP = pokemon.getCurrentHitpoints();
            int maxHP = pokemon.getStat(com.github.adisann.pokemon.battle.STAT.HITPOINTS);
            Label hpLabel = new Label("HP: " + currentHP + "/" + maxHP, skin, "smallLabel");
            
            row.add(arrow).padRight(6f).width(16f);
            row.add(nameLabel).align(Align.left).expandX();
            row.add(levelLabel).padLeft(10f);
            row.add(hpLabel).padLeft(10f);
            
            contentTable.add(row).align(Align.left).pad(6f).expandX().fillX().row();
        }
        
        // Add CANCEL option
        Table cancelRow = new Table();
        Image cancelArrow = new Image(skin, "arrow");
        cancelArrow.setScaling(Scaling.none);
        cancelArrow.setVisible(selectedIndex == trainer.getTeamSize());
        Label cancelLabel = new Label("CANCEL", skin);
        cancelRow.add(cancelArrow).padRight(6f).width(16f);
        cancelRow.add(cancelLabel).align(Align.left);
        contentTable.add(cancelRow).align(Align.left).pad(6f).expandX().fillX();
        
        this.add(contentTable).expand().fill().pad(16f);
    }
    
    private void createInputHandler() {
        inputHandler = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (!isVisible()) return false;
                
                if (keycode == Keys.UP) {
                    moveUp();
                    return true;
                } else if (keycode == Keys.DOWN) {
                    moveDown();
                    return true;
                } else if (keycode == Keys.Z || keycode == Keys.ENTER) {
                    selectCurrent();
                    return true;
                } else if (keycode == Keys.X || keycode == Keys.ESCAPE) {
                    if (listener != null) listener.onCancel();
                    return true;
                }
                return false;
            }
        };
    }
    
    public void moveUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            buildUI();
        }
    }
    
    public void moveDown() {
        if (selectedIndex < trainer.getTeamSize()) {
            selectedIndex++;
            buildUI();
        }
    }
    
    private void selectCurrent() {
        if (listener == null) return;
        
        if (selectedIndex == trainer.getTeamSize()) {
            // CANCEL selected
            listener.onCancel();
        } else {
            Pokemon pokemon = trainer.getPokemon(selectedIndex);
            listener.onPokemonSelected(pokemon, selectedIndex);
        }
    }
    
    public void setListener(PartyActionListener listener) {
        this.listener = listener;
    }
    
    public InputAdapter getInputHandler() {
        return inputHandler;
    }
    
    public void refresh(Pokemon newActive) {
        this.currentActive = newActive;
        selectedIndex = 0;
        buildUI();
    }
    
    public Pokemon getSelectedPokemon() {
        if (selectedIndex < trainer.getTeamSize()) {
            return trainer.getPokemon(selectedIndex);
        }
        return null;
    }
}
