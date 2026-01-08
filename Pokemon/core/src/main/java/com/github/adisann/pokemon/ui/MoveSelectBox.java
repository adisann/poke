package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.github.adisann.pokemon.model.Type;

/**
 * Pokemon Emerald-style move selection box.
 * Left panel: 2x2 grid of move names with arrow selector
 * Right panel: PP and TYPE info for selected move
 * 
 * Layout:
 * ┌─────────────────────────────┬──────────────┐
 * │ ▶TACKLE GROWL │ PP 35/35 │
 * │ │ │
 * │ VINE WHIP ------ │ TYPE/NORMAL │
 * └─────────────────────────────┴──────────────┘
 */
public class MoveSelectBox extends Table {

	private int selectorIndex = 0;

	private Label[] moveLabels = new Label[4];
	private Image[] arrows = new Image[4];

	// Info panel labels
	private Label ppLabel;
	private Label ppValueLabel;
	private Label typeLabel;

	// Store move data
	private int[] currentPP = new int[4];
	private int[] maxPP = new int[4];
	private Type[] moveTypes = new Type[4];

	private Skin skin;

	public MoveSelectBox(Skin skin) {
		super(skin);
		this.skin = skin;
		this.setBackground("optionbox");

		// Main layout: Move grid (left) | Info panel (right)
		Table leftPanel = createMoveGrid();
		Table rightPanel = createInfoPanel();

		this.add(leftPanel).expand().fill().pad(8f);
		this.add(rightPanel).width(100f).fillY().pad(8f);

		setSelection(0);
	}

	private Table createMoveGrid() {
		Table grid = new Table();

		// Initialize move labels and arrows
		for (int i = 0; i < 4; i++) {
			moveLabels[i] = new Label("------", skin);
			arrows[i] = new Image(skin, "arrow");
			arrows[i].setScaling(Scaling.none);
			currentPP[i] = 0;
			maxPP[i] = 0;
			moveTypes[i] = Type.NORMAL;
		}

		// Row 1: Move 0 | Move 1
		Table cell0 = createMoveCell(0);
		Table cell1 = createMoveCell(1);
		grid.add(cell0).expand().fill().padRight(20f);
		grid.add(cell1).expand().fill().row();

		// Row 2: Move 2 | Move 3 (with top padding)
		Table cell2 = createMoveCell(2);
		Table cell3 = createMoveCell(3);
		grid.add(cell2).expand().fill().padRight(20f).padTop(8f);
		grid.add(cell3).expand().fill().padTop(8f);

		return grid;
	}

	private Table createMoveCell(int index) {
		Table cell = new Table();
		cell.add(arrows[index]).padRight(4f);
		cell.add(moveLabels[index]).align(Align.left).expandX().fillX();
		return cell;
	}

	private Table createInfoPanel() {
		Table panel = new Table(skin);
		panel.setBackground("optionbox");

		// PP row
		ppLabel = new Label("PP", skin);
		ppValueLabel = new Label("--/--", skin);

		Table ppRow = new Table();
		ppRow.add(ppLabel).padRight(8f);
		ppRow.add(ppValueLabel);

		// Type row
		typeLabel = new Label("TYPE/----", skin, "smallLabel");

		panel.add(ppRow).padBottom(8f).row();
		panel.add(typeLabel);

		return panel;
	}

	/**
	 * Set move name only (backwards compatible).
	 */
	public void setLabel(int index, String text) {
		moveLabels[index].setText(text.toUpperCase());
		if (text.equals("------") || text.equals("-")) {
			currentPP[index] = 0;
			maxPP[index] = 0;
			moveTypes[index] = Type.NORMAL;
		}
		updateInfoPanel();
	}

	/**
	 * Set move with PP display (Pokemon Emerald style).
	 */
	public void setMove(int index, String moveName, int currentPP, int maxPP) {
		setMove(index, moveName, currentPP, maxPP, Type.NORMAL);
	}

	/**
	 * Set move with PP and Type display (full Pokemon Emerald style).
	 */
	public void setMove(int index, String moveName, int currentPP, int maxPP, Type type) {
		moveLabels[index].setText(moveName.toUpperCase());
		this.currentPP[index] = currentPP;
		this.maxPP[index] = maxPP;
		this.moveTypes[index] = type;
		updateInfoPanel();
	}

	private void updateInfoPanel() {
		int idx = selectorIndex;
		String moveName = moveLabels[idx].getText().toString();

		if (moveName.equals("------") || moveName.equals("-") || maxPP[idx] == 0) {
			ppValueLabel.setText("--/--");
			ppValueLabel.setColor(Color.GRAY);
			typeLabel.setText("TYPE/----");
		} else {
			ppValueLabel.setText(currentPP[idx] + "/" + maxPP[idx]);

			// Color based on PP remaining
			float ratio = (float) currentPP[idx] / maxPP[idx];
			if (ratio <= 0.25f) {
				ppValueLabel.setColor(Color.RED);
			} else if (ratio <= 0.5f) {
				ppValueLabel.setColor(Color.ORANGE);
			} else {
				ppValueLabel.setColor(Color.WHITE);
			}

			// Type display
			typeLabel.setText("TYPE/" + moveTypes[idx].name());
		}
	}

	public int getSelection() {
		return selectorIndex;
	}

	public void moveUp() {
		if (selectorIndex == 2) {
			setSelection(0);
		} else if (selectorIndex == 3) {
			setSelection(1);
		}
	}

	public void moveDown() {
		if (selectorIndex == 0) {
			setSelection(2);
		} else if (selectorIndex == 1) {
			setSelection(3);
		}
	}

	public void moveLeft() {
		if (selectorIndex == 1) {
			setSelection(0);
		} else if (selectorIndex == 3) {
			setSelection(2);
		}
	}

	public void moveRight() {
		if (selectorIndex == 0) {
			setSelection(1);
		} else if (selectorIndex == 2) {
			setSelection(3);
		}
	}

	private void setSelection(int index) {
		selectorIndex = index;
		for (int i = 0; i < arrows.length; i++) {
			arrows[i].setVisible(i == index);
		}
		updateInfoPanel();
	}

	public void resetSelection() {
		setSelection(0);
	}
}