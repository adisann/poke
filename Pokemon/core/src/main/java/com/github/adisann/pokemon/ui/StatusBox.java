package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * Displays generel stats about a Pokemon during a {@link Battle}.
 * */
public class StatusBox extends Table {

	private Label text;
	private HPBar hpbar;

	protected Table uiContainer;

	public StatusBox(Skin skin) {
		super(skin);
		this.setBackground("battleinfobox");
		uiContainer = new Table();
		this.add(uiContainer).pad(0f).expand().fill();

		text = new Label("namenull", skin, "smallLabel");
		uiContainer.add(text).align(Align.left).padTop(0f).row();

		hpbar = new HPBar(skin);
		uiContainer.add(hpbar).spaceTop(0f).expand().fill();
	}

	public void setText(String newText) {
		text.setText(newText);
	}

	/**
	 * Sets the display text with name and level.
	 */
	public void setNameAndLevel(String name, int level) {
		text.setText(name + "  Lv" + level);
	}

	public HPBar getHPBar() {
		return hpbar;
	}

}