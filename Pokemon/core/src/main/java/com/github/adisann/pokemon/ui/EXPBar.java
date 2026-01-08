package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Pokemon Emerald-style EXP bar.
 * Shows progression as a blue bar, no numbers.
 */
public class EXPBar extends Widget {
    
    private Skin skin;
    
    private float expAmount = 0f; // 0 to 1
    
    private Drawable background;
    private Drawable fill;
    
    // Bar dimensions
    private static final float BAR_WIDTH = 80f;
    private static final float BAR_HEIGHT = 4f;
    
    public EXPBar(Skin skin) {
        super();
        this.skin = skin;
        
        // Use existing drawables or fallback to simple colors
        background = skin.getDrawable("background_hpbar");
        fill = skin.getDrawable("green"); // EXP is typically blue, but use green for now
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        float fillWidth = expAmount * BAR_WIDTH;
        
        // Draw background
        background.draw(batch, getX(), getY(), BAR_WIDTH, BAR_HEIGHT);
        
        // Draw fill (EXP progress)
        if (fillWidth > 0) {
            fill.draw(batch, getX(), getY(), fillWidth, BAR_HEIGHT);
        }
    }
    
    @Override
    public float getMinWidth() {
        return BAR_WIDTH;
    }
    
    @Override
    public float getMinHeight() {
        return BAR_HEIGHT;
    }
    
    /**
     * Set EXP progress (0 to 1).
     */
    public void setProgress(float progress) {
        this.expAmount = MathUtils.clamp(progress, 0f, 1f);
    }
    
    /**
     * Set EXP from current and max values.
     */
    public void setEXP(int currentExp, int expToNext) {
        if (expToNext > 0) {
            this.expAmount = (float) currentExp / expToNext;
        } else {
            this.expAmount = 0f;
        }
        this.expAmount = MathUtils.clamp(expAmount, 0f, 1f);
    }
}
