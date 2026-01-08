package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

/**
 * Pokemon Emerald-style 2x2 action menu.
 * Layout:
 *   FIGHT    BAG
 *   POKéMON  RUN
 */
public class ActionMenu extends Table {
    
    public enum Action {
        FIGHT(0), BAG(1), POKEMON(2), RUN(3);
        
        public final int index;
        Action(int index) { this.index = index; }
    }
    
    private int selectedIndex = 0;
    private final Label[] labels = new Label[4];
    private final Image[] arrows = new Image[4];
    private final String[] options = {"FIGHT", "BAG", "POKéMON", "RUN"};
    
    public ActionMenu(Skin skin) {
        super(skin);
        this.setBackground("optionbox");
        
        Table grid = new Table();
        
        for (int i = 0; i < 4; i++) {
            arrows[i] = new Image(skin, "arrow");
            arrows[i].setScaling(Scaling.none);
            labels[i] = new Label(options[i], skin);
            
            Table cell = new Table();
            cell.add(arrows[i]).padRight(4f);
            cell.add(labels[i]).align(Align.left);
            
            grid.add(cell).pad(4f).align(Align.left).minWidth(70f);
            
            // Row break after 2 items
            if (i == 1) {
                grid.row();
            }
        }
        
        this.add(grid).pad(6f);
        updateSelection();
    }
    
    public void moveUp() {
        if (selectedIndex >= 2) {
            selectedIndex -= 2;
            updateSelection();
        }
    }
    
    public void moveDown() {
        if (selectedIndex < 2) {
            selectedIndex += 2;
            updateSelection();
        }
    }
    
    public void moveLeft() {
        if (selectedIndex == 1 || selectedIndex == 3) {
            selectedIndex--;
            updateSelection();
        }
    }
    
    public void moveRight() {
        if (selectedIndex == 0 || selectedIndex == 2) {
            selectedIndex++;
            updateSelection();
        }
    }
    
    private void updateSelection() {
        for (int i = 0; i < 4; i++) {
            arrows[i].setVisible(i == selectedIndex);
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public Action getSelectedAction() {
        return Action.values()[selectedIndex];
    }
    
    public void resetSelection() {
        selectedIndex = 0;
        updateSelection();
    }
}
