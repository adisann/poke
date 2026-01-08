package com.github.adisann.pokemon.model;

/**
 * Pokeball item for catching wild Pokemon.
 */
public class Pokeball extends Item {
    
    private float catchRateModifier;
    private String spritePath;
    
    public Pokeball() {
        super("Poke Ball", "A device for catching wild Pokemon");
        this.catchRateModifier = 1.0f;
        this.spritePath = "graphics/items/pokeball.png";
    }
    
    public Pokeball(String name, String description, float catchRateModifier) {
        super(name, description);
        this.catchRateModifier = catchRateModifier;
        this.spritePath = "graphics/items/pokeball.png";
    }
    
    /**
     * Pokeballs don't use the standard use() method.
     * They are used through the battle system's catch mechanic.
     */
    @Override
    public boolean use(Pokemon target) {
        // Pokeballs are used in battle, not from menu
        return false;
    }
    
    /**
     * Attempt to catch a wild Pokemon.
     * @param wildPokemon The Pokemon to catch
     * @return true if catch was successful
     */
    public boolean attemptCatch(Pokemon wildPokemon) {
        if (wildPokemon == null) {
            return false;
        }
        
        // Calculate catch rate based on HP and Pokemon's base catch rate
        // Lower HP = higher catch chance
        float hpRatio = (float) wildPokemon.getCurrentHitpoints() / 
                        wildPokemon.getStat(com.github.adisann.pokemon.battle.STAT.HITPOINTS);
        
        // Base catch rate (simplified formula)
        // Full HP = 30% chance, 1 HP = 90% chance (with normal Pokeball)
        float baseCatchRate = 0.3f + (1.0f - hpRatio) * 0.6f;
        float finalCatchRate = baseCatchRate * catchRateModifier;
        
        // Random roll
        float roll = (float) Math.random();
        boolean caught = roll < finalCatchRate;
        
        System.out.println("[Pokeball] Catch attempt - HP ratio: " + hpRatio + 
                ", catch rate: " + finalCatchRate + ", roll: " + roll + 
                ", caught: " + caught);
        
        if (caught) {
            useQuantity(1);
        }
        
        return caught;
    }
    
    public float getCatchRateModifier() {
        return catchRateModifier;
    }
    
    public String getSpritePath() {
        return spritePath;
    }
    
    // Factory methods for different Pokeball types
    public static Pokeball createPokeball() {
        return new Pokeball("Poke Ball", "A device for catching wild Pokemon", 1.0f);
    }
    
    public static Pokeball createGreatBall() {
        return new Pokeball("Great Ball", "A good Ball with a higher catch rate", 1.5f);
    }
    
    public static Pokeball createUltraBall() {
        return new Pokeball("Ultra Ball", "A high-performance Ball", 2.0f);
    }
    
    public static Pokeball createMasterBall() {
        return new Pokeball("Master Ball", "The best Ball that catches any Pokemon", 255.0f);
    }
}
