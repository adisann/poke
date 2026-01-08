package com.github.adisann.pokemon.model;

/**
 * Base class for all items in the game.
 */
public abstract class Item {
    
    protected String name;
    protected String description;
    protected int quantity;
    
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.quantity = 1;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
    
    public boolean useQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Use the item. Returns true if item was successfully used.
     */
    public abstract boolean use(Pokemon target);
}
