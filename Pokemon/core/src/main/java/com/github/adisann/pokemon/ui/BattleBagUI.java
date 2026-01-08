package com.github.adisann.pokemon.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
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
import com.github.adisann.pokemon.model.Pokeball;
import com.github.adisann.pokemon.util.SpriteManager;

/**
 * Full-screen bag UI for battle.
 * Shows all items with icons, handles selection and use.
 */
public class BattleBagUI extends Table {
    
    public interface BagActionListener {
        void onItemSelected(Item item, int index);
        void onCancel();
    }
    
    private int selectedIndex = 0;
    private Inventory inventory;
    private Skin skin;
    private BagActionListener listener;
    private InputAdapter inputHandler;
    
    private Table contentTable;
    
    public BattleBagUI(Skin skin, Inventory inventory) {
        super(skin);
        this.skin = skin;
        this.inventory = inventory;
        this.setFillParent(true);
        this.setBackground("optionbox");
        
        buildUI();
        createInputHandler();
    }
    
    private void buildUI() {
        this.clearChildren();
        
        // Title
        Label title = new Label("BAG", skin);
        this.add(title).pad(8f).row();
        
        contentTable = new Table(skin);
        contentTable.setBackground("optionbox");
        
        if (inventory.isEmpty()) {
            Label emptyLabel = new Label("No items", skin);
            contentTable.add(emptyLabel).pad(16f);
        } else {
            SpriteManager sprites = SpriteManager.getInstance();
            
            for (int i = 0; i < inventory.getItemCount(); i++) {
                Item item = inventory.getItem(i);
                
                Table row = new Table();
                Image arrow = new Image(skin, "arrow");
                arrow.setScaling(Scaling.none);
                arrow.setVisible(i == selectedIndex);
                
                // Get item icon
                TextureRegion iconRegion = sprites.getItemByName(item.getName());
                Image itemIcon = null;
                if (iconRegion != null) {
                    itemIcon = new Image(new TextureRegionDrawable(iconRegion));
                    itemIcon.setScaling(Scaling.fit);
                }
                
                Label nameLabel = new Label(item.getName(), skin);
                Label quantityLabel = new Label("x" + item.getQuantity(), skin, "smallLabel");
                
                row.add(arrow).padRight(6f).width(16f);
                if (itemIcon != null) {
                    row.add(itemIcon).size(24, 24).padRight(6f);
                }
                row.add(nameLabel).align(Align.left).expandX();
                row.add(quantityLabel).padLeft(16f);
                
                contentTable.add(row).align(Align.left).pad(6f).expandX().fillX().row();
            }
        }
        
        // Add CANCEL option
        Table cancelRow = new Table();
        Image cancelArrow = new Image(skin, "arrow");
        cancelArrow.setScaling(Scaling.none);
        cancelArrow.setVisible(selectedIndex == inventory.getItemCount());
        Label cancelLabel = new Label("CANCEL", skin);
        cancelRow.add(cancelArrow).padRight(6f).width(16f);
        cancelRow.add(cancelLabel).align(Align.left);
        contentTable.add(cancelRow).align(Align.left).pad(6f).expandX().fillX();
        
        this.add(contentTable).expand().fill().pad(16f);
    }
    
    private void createInputHandler() {
        inputHandler = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (!isVisible()) return false;
                
                if (keycode == Keys.UP) {
                    moveUp();
                    return true;
                } else if (keycode == Keys.DOWN) {
                    moveDown();
                    return true;
                } else if (keycode == Keys.Z || keycode == Keys.ENTER) {
                    selectCurrent();
                    return true;
                } else if (keycode == Keys.X || keycode == Keys.ESCAPE) {
                    if (listener != null) listener.onCancel();
                    return true;
                }
                return false;
            }
        };
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
    
    private void selectCurrent() {
        if (listener == null) return;
        
        if (selectedIndex == inventory.getItemCount()) {
            // CANCEL selected
            listener.onCancel();
        } else {
            Item item = inventory.getItem(selectedIndex);
            listener.onItemSelected(item, selectedIndex);
        }
    }
    
    public void setListener(BagActionListener listener) {
        this.listener = listener;
    }
    
    public InputAdapter getInputHandler() {
        return inputHandler;
    }
    
    public void refresh() {
        selectedIndex = 0;
        buildUI();
    }
    
    public Item getSelectedItem() {
        if (selectedIndex < inventory.getItemCount()) {
            return inventory.getItem(selectedIndex);
        }
        return null;
    }
    
    public boolean isPokeball(Item item) {
        return item instanceof Pokeball;
    }
}
