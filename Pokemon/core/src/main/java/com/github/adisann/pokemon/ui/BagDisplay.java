package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.adisann.pokemon.model.Inventory;
import com.github.adisann.pokemon.model.Item;
import com.github.adisann.pokemon.util.SpriteManager;

/**
 * Pokemon Emerald-style Bag Display.
 * Shows all items in the player's inventory with icons.
 */
public class BagDisplay extends Table {
    
    private int selectedIndex = 0;
    private Inventory inventory;
    private Skin skin;
    
    private Table contentTable;
    
    public BagDisplay(Skin skin, Inventory inventory) {
        super(skin);
        this.skin = skin;
        this.inventory = inventory;
        this.setBackground("optionbox");
        
        buildUI();
    }
    
    private void buildUI() {
        this.clearChildren();
        
        // Title
        Label title = new Label("BAG", skin);
        this.add(title).pad(4f).row();
        
        contentTable = new Table();
        
        if (inventory.isEmpty()) {
            Label emptyLabel = new Label("No items", skin, "smallLabel");
            contentTable.add(emptyLabel).pad(8f);
        } else {
            SpriteManager sprites = SpriteManager.getInstance();
            
            for (int i = 0; i < inventory.getItemCount(); i++) {
                Item item = inventory.getItem(i);
                
                Table row = new Table();
                Image arrow = new Image(skin, "arrow");
                arrow.setScaling(Scaling.none);
                arrow.setVisible(i == selectedIndex);
                
                // Get item icon from sprite sheet
                TextureRegion iconRegion = sprites.getItemByName(item.getName());
                Image itemIcon = null;
                if (iconRegion != null) {
                    itemIcon = new Image(new TextureRegionDrawable(iconRegion));
                    itemIcon.setScaling(Scaling.fit);
                }
                
                Label nameLabel = new Label(item.getName(), skin);
                Label quantityLabel = new Label("x" + item.getQuantity(), skin, "smallLabel");
                
                row.add(arrow).padRight(4f);
                if (itemIcon != null) {
                    row.add(itemIcon).size(20, 20).padRight(4f);
                }
                row.add(nameLabel).align(Align.left).expandX();
                row.add(quantityLabel).padLeft(10f);
                
                contentTable.add(row).align(Align.left).pad(4f).expandX().fillX().row();
            }
        }
        
        // Add CANCEL option
        Table cancelRow = new Table();
        Image cancelArrow = new Image(skin, "arrow");
        cancelArrow.setScaling(Scaling.none);
        cancelArrow.setVisible(selectedIndex == inventory.getItemCount());
        Label cancelLabel = new Label("CANCEL", skin);
        cancelRow.add(cancelArrow).padRight(6f);
        cancelRow.add(cancelLabel).align(Align.left);
        contentTable.add(cancelRow).align(Align.left).pad(4f);
        
        this.add(contentTable).pad(8f);
    }
    
    public void moveUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            buildUI();
        }
    }
    
    public void moveDown() {
        if (selectedIndex < inventory.getItemCount()) {
            selectedIndex++;
            buildUI();
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public boolean isCancelSelected() {
        return selectedIndex == inventory.getItemCount();
    }
    
    public Item getSelectedItem() {
        if (selectedIndex < inventory.getItemCount()) {
            return inventory.getItem(selectedIndex);
        }
        return null;
    }
    
    public void resetSelection() {
        selectedIndex = 0;
        buildUI();
    }
    
    public void refresh() {
        buildUI();
    }
}
