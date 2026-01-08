package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.adisann.pokemon.battle.Trainer;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.battle.STAT;

/**
 * Pokemon Emerald-style Party Screen.
 * Shows all Pokemon in the player's party with HP bars.
 */
public class PartyDisplay extends Table {
    
    private int selectedIndex = 0;
    private Trainer trainer;
    private Skin skin;
    
    private Table[] pokemonSlots = new Table[6];
    private Image[] arrows = new Image[6];
    private Label[] nameLabels = new Label[6];
    private Label[] levelLabels = new Label[6];
    private Label[] hpLabels = new Label[6];
    private HPBar[] hpBars = new HPBar[6];
    
    public PartyDisplay(Skin skin, Trainer trainer) {
        super(skin);
        this.skin = skin;
        this.trainer = trainer;
        this.setBackground("optionbox");
        
        buildUI();
    }
    
    private void buildUI() {
        this.clearChildren();
        
        Table content = new Table();
        
        for (int i = 0; i < 6; i++) {
            pokemonSlots[i] = new Table();
            arrows[i] = new Image(skin, "arrow");
            arrows[i].setScaling(Scaling.none);
            
            if (i < trainer.getTeamSize()) {
                Pokemon pokemon = trainer.getPokemon(i);
                
                nameLabels[i] = new Label(pokemon.getName(), skin);
                levelLabels[i] = new Label("Lv" + pokemon.getLevel(), skin, "smallLabel");
                
                int currentHP = pokemon.getCurrentHitpoints();
                int maxHP = pokemon.getStat(STAT.HITPOINTS);
                hpLabels[i] = new Label(currentHP + "/" + maxHP, skin);
                
                hpBars[i] = new HPBar(skin);
                hpBars[i].displayHPLeft((float) currentHP / maxHP);
                
                // Build slot layout
                Table info = new Table();
                info.add(nameLabels[i]).align(Align.left).row();
                info.add(levelLabels[i]).align(Align.left).row();
                
                Table hpRow = new Table();
                hpRow.add(hpBars[i]).padRight(4f);
                hpRow.add(hpLabels[i]);
                info.add(hpRow).align(Align.left);
                
                pokemonSlots[i].add(arrows[i]).padRight(6f);
                pokemonSlots[i].add(info).align(Align.left);
            } else {
                // Empty slot
                nameLabels[i] = new Label("------", skin);
                pokemonSlots[i].add(arrows[i]).padRight(6f);
                pokemonSlots[i].add(nameLabels[i]).align(Align.left);
            }
            
            content.add(pokemonSlots[i]).align(Align.left).pad(4f).row();
        }
        
        // Add CANCEL option
        Table cancelRow = new Table();
        Image cancelArrow = new Image(skin, "arrow");
        cancelArrow.setScaling(Scaling.none);
        cancelArrow.setVisible(false);
        Label cancelLabel = new Label("CANCEL", skin);
        cancelRow.add(cancelArrow).padRight(6f);
        cancelRow.add(cancelLabel).align(Align.left);
        content.add(cancelRow).align(Align.left).pad(4f);
        
        this.add(content).pad(8f);
        updateSelection();
    }
    
    public void moveUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            updateSelection();
        }
    }
    
    public void moveDown() {
        // Allow selecting up to team size + 1 (for CANCEL)
        if (selectedIndex < trainer.getTeamSize()) {
            selectedIndex++;
            updateSelection();
        }
    }
    
    private void updateSelection() {
        for (int i = 0; i < 6; i++) {
            arrows[i].setVisible(i == selectedIndex);
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public boolean isCancelSelected() {
        return selectedIndex == trainer.getTeamSize();
    }
    
    public Pokemon getSelectedPokemon() {
        if (selectedIndex < trainer.getTeamSize()) {
            return trainer.getPokemon(selectedIndex);
        }
        return null;
    }
    
    public void resetSelection() {
        selectedIndex = 0;
        updateSelection();
    }
    
    public void refresh() {
        buildUI();
    }
}
