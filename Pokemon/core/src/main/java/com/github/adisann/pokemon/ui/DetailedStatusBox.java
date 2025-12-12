package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

/**
 * A more detailed {@link StatusBox}.
 * Used to display info for the players pokemon.
 * Shows HP text and EXP bar.
 * */
public class DetailedStatusBox extends StatusBox {

	private Label hpText;
	private Label expText;

	public DetailedStatusBox(Skin skin) {
		super(skin);

		hpText = new Label("0 / 0", skin, "smallLabel");
		uiContainer.row();
		uiContainer.add(hpText).expand().right();

		expText = new Label("EXP: 0 / 0", skin, "smallLabel");
		uiContainer.row();
		uiContainer.add(expText).expand().right();
	}

	public void setHPText(int hpLeft, int hpTotal) {
		hpText.setText(hpLeft + " / " + hpTotal);
	}

	/**
	 * Sets the EXP display text.
	 * 
	 * @param currentExp Current EXP points
	 * @param expToNext  EXP needed for next level
	 */
	public void setEXPText(int currentExp, int expToNext) {
		expText.setText("EXP: " + currentExp + " / " + expToNext);
	}
}