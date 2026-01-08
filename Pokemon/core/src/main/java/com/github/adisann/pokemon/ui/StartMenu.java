package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

/**
 * Pokemon Emerald-style Start Menu.
 * Appears when pressing Escape during gameplay.
 * 
 * Options:
 * - POKéMON (view party)
 * - BAG (view items)
 * - SAVE (save game)
 * - EXIT (close menu)
 */
public class StartMenu extends Table {
    
    public enum MenuOption {
        POKEMON(0, "POKeMON"),
        BAG(1, "BAG"),
        SAVE(2, "SAVE"),
        EXIT(3, "EXIT");
        
        public final int index;
        public final String label;
        
        MenuOption(int index, String label) {
            this.index = index;
            this.label = label;
        }
    }
    
    private int selectedIndex = 0;
    private final Label[] labels = new Label[4];
    private final Image[] arrows = new Image[4];
    private final MenuOption[] options = MenuOption.values();
    
    public StartMenu(Skin skin) {
        super(skin);
        this.setBackground("optionbox");
        
        Table content = new Table();
        
        for (int i = 0; i < options.length; i++) {
            arrows[i] = new Image(skin, "arrow");
            arrows[i].setScaling(Scaling.none);
            labels[i] = new Label(options[i].label, skin);
            
            Table row = new Table();
            row.add(arrows[i]).padRight(6f).align(Align.left);
            row.add(labels[i]).align(Align.left).expandX();
            
            content.add(row).align(Align.left).pad(4f).row();
        }
        
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
        if (selectedIndex < options.length - 1) {
            selectedIndex++;
            updateSelection();
        }
    }
    
    private void updateSelection() {
        for (int i = 0; i < options.length; i++) {
            arrows[i].setVisible(i == selectedIndex);
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public MenuOption getSelectedOption() {
        return options[selectedIndex];
    }
    
    public void resetSelection() {
        selectedIndex = 0;
        updateSelection();
    }
}
