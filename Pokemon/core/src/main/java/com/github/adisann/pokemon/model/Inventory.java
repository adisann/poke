package com.github.adisann.pokemon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Player's inventory/bag containing items.
 */
public class Inventory {
    
    private List<Item> items;
    private static final int MAX_ITEM_TYPES = 20;
    
    public Inventory() {
        items = new ArrayList<>();
    }
    
    /**
     * Add an item to inventory. Stacks if same item exists.
     */
    public boolean addItem(Item item) {
        // Check if item already exists
        for (Item existing : items) {
            if (existing.getName().equals(item.getName())) {
                existing.addQuantity(item.getQuantity());
                return true;
            }
        }
        
        // Add new item if space available
        if (items.size() < MAX_ITEM_TYPES) {
            items.add(item);
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove item from inventory. Returns true if successful.
     */
    public boolean removeItem(String itemName) {
        return items.removeIf(item -> item.getName().equals(itemName) && item.getQuantity() <= 0);
    }
    
    /**
     * Get item by name.
     */
    public Item getItem(String itemName) {
        for (Item item : items) {
            if (item.getName().equals(itemName)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Get item by index.
     */
    public Item getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }
    
    /**
     * Use an item on a Pokemon.
     */
    public boolean useItem(String itemName, Pokemon target) {
        Item item = getItem(itemName);
        if (item != null && item.getQuantity() > 0) {
            boolean used = item.use(target);
            if (used && item.getQuantity() <= 0) {
                items.remove(item);
            }
            return used;
        }
        return false;
    }
    
    /**
     * Use an item by index on a Pokemon.
     */
    public boolean useItem(int index, Pokemon target) {
        Item item = getItem(index);
        if (item != null && item.getQuantity() > 0) {
            boolean used = item.use(target);
            if (used && item.getQuantity() <= 0) {
                items.remove(item);
            }
            return used;
        }
        return false;
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * Initialize with starter items.
     */
    public void addStarterItems() {
        Potion potion = Potion.createPotion();
        potion.setQuantity(5);
        addItem(potion);
        
        Pokeball pokeball = Pokeball.createPokeball();
        pokeball.setQuantity(10);
        addItem(pokeball);
    }
}
