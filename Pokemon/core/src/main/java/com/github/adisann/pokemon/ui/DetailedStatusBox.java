package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * Pokemon Emerald-style detailed status box for player's Pokemon.
 * Shows:
 * - Name and level
 * - HP bar
 * - HP numbers (current/max) - uses default font which has "/" character
 * - EXP bar (visual only)
 */
public class DetailedStatusBox extends StatusBox {

    private Label hpCurrentLabel;
    private Label hpSlashLabel;
    private Label hpMaxLabel;
    private EXPBar expBar;

    public DetailedStatusBox(Skin skin) {
        super(skin);

        // HP numbers row - using default font style which has "/" character
        Table hpRow = new Table();
        
        // Use default label style which should have more characters
        hpCurrentLabel = new Label("0", skin);
        hpSlashLabel = new Label("/", skin);
        hpMaxLabel = new Label("0", skin);
        
        hpRow.add(hpCurrentLabel);
        hpRow.add(hpSlashLabel);
        hpRow.add(hpMaxLabel);
        
        uiContainer.row();
        uiContainer.add(hpRow).align(Align.right).padRight(4f);

        // EXP bar row
        Table expRow = new Table();
        Label expLabel = new Label("EXP", skin, "smallLabel");
        expBar = new EXPBar(skin);
        expRow.add(expLabel).padRight(4f);
        expRow.add(expBar).expandX().fillX();
        
        uiContainer.row();
        uiContainer.add(expRow).expandX().fillX().padTop(2f);
    }

    /**
     * Set HP display text.
     */
    public void setHPText(int hpLeft, int hpTotal) {
        hpCurrentLabel.setText(String.valueOf(hpLeft));
        hpMaxLabel.setText(String.valueOf(hpTotal));
    }

    /**
     * Set EXP bar progress.
     */
    public void setEXPText(int currentExp, int expToNext) {
        expBar.setEXP(currentExp, expToNext);
    }
    
    /**
     * Get the EXP bar for direct manipulation.
     */
    public EXPBar getEXPBar() {
        return expBar;
    }
}