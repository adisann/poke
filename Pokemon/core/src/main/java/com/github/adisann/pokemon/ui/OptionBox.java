package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * UI Element that is a box with alist inside it. Each line can be selected with
 * an arrow.
 * */
public class OptionBox extends Table {

	private int selectorIndex = 0;

	private List<Image> arrows = new ArrayList<Image>();
	private List<Label> options = new ArrayList<Label>();

	private Table uiContainer;

	public OptionBox(Skin skin) {
		super(skin);
		this.setBackground("optionbox");
		uiContainer = new Table();
		this.add(uiContainer).pad(5f);
	}

	public void addOption(String option) {
		Label optionLabel = new Label(option, this.getSkin());
		options.add(optionLabel);
		Image selectorLabel = new Image(this.getSkin(), "arrow");
		selectorLabel.setScaling(Scaling.none);
		arrows.add(selectorLabel);

		// Show arrow only for the currently selected index
		int thisIndex = arrows.size() - 1;
		selectorLabel.setVisible(thisIndex == selectorIndex);

		uiContainer.add(selectorLabel).expand().align(Align.left).space(5f);
		uiContainer.add(optionLabel)
				.expand()
				.align(Align.left)
				.space(8f);
		uiContainer.row();
	}

	public void moveUp() {
		System.out.println("[OptionBox] moveUp: index before=" + selectorIndex);
		selectorIndex--;
		if (selectorIndex < 0) {
			selectorIndex = 0;
		}
		System.out.println("[OptionBox] moveUp: index after=" + selectorIndex);
		updateArrowVisibility();
	}

	public void moveDown() {
		System.out.println("[OptionBox] moveDown: index before=" + selectorIndex);
		selectorIndex++;
		if (selectorIndex >= arrows.size()) {
			selectorIndex = arrows.size() - 1;
		}
		System.out.println("[OptionBox] moveDown: index after=" + selectorIndex);
		updateArrowVisibility();
	}

	private void updateArrowVisibility() {
		for (int i = 0; i < arrows.size(); i++) {
			boolean visible = (i == selectorIndex);
			arrows.get(i).setVisible(visible);
			System.out.println("[OptionBox] arrow[" + i + "].visible=" + visible);
		}
	}

	public void clearChoices() {
		uiContainer.clearChildren();
		options.clear();
		arrows.clear();
		selectorIndex = 0;
	}

	public int getIndex() {
		return selectorIndex;
	}

	/**
	 * @return Number of options added to this OptionBox
	 */
	public int getAmount() {
		return options.size();
	}
}