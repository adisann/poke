package com.github.adisann.pokemon.model;

import com.github.adisann.pokemon.battle.STAT;

/**
 * Healing item that restores HP.
 */
public class Potion extends Item {
    
    private int healAmount;
    
    public Potion() {
        super("Potion", "Restores 20 HP");
        this.healAmount = 20;
    }
    
    public Potion(String name, String description, int healAmount) {
        super(name, description);
        this.healAmount = healAmount;
    }
    
    @Override
    public boolean use(Pokemon target) {
        if (target == null || target.isFainted()) {
            return false;
        }
        
        int maxHP = target.getStat(STAT.HITPOINTS);
        int currentHP = target.getCurrentHitpoints();
        
        if (currentHP >= maxHP) {
            // Already at full HP
            return false;
        }
        
        int newHP = Math.min(currentHP + healAmount, maxHP);
        target.setCurrentHitpoints(newHP);
        
        System.out.println("[Item] Used " + name + " on " + target.getName() + 
                " - healed " + (newHP - currentHP) + " HP");
        
        return useQuantity(1);
    }
    
    public int getHealAmount() {
        return healAmount;
    }
    
    // Factory methods for different potion types
    public static Potion createPotion() {
        return new Potion("Potion", "Restores 20 HP", 20);
    }
    
    public static Potion createSuperPotion() {
        return new Potion("Super Potion", "Restores 50 HP", 50);
    }
    
    public static Potion createHyperPotion() {
        return new Potion("Hyper Potion", "Restores 200 HP", 200);
    }
    
    public static Potion createMaxPotion() {
        return new Potion("Max Potion", "Fully restores HP", 9999);
    }
}
