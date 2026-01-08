package com.github.adisann.pokemon.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Manages Pokemon Emerald sprite sheets.
 * Provides easy access to individual sprites from sprite sheets.
 */
public class SpriteManager {
    
    private static SpriteManager instance;
    
    // Sprite sheets
    private Texture itemIconsSheet;
    private Texture pokeballIconsSheet;
    private Texture pokemonMenuSheet;
    private Texture battleBackgroundsSheet;
    
    // Split regions
    private TextureRegion[][] itemRegions;
    private TextureRegion[][] pokeballRegions;
    
    // Grid sizes
    private static final int ITEM_SIZE = 24;
    private static final int POKEBALL_SIZE = 16;
    
    private SpriteManager() {
        loadSprites();
    }
    
    public static SpriteManager getInstance() {
        if (instance == null) {
            instance = new SpriteManager();
        }
        return instance;
    }
    
    private void loadSprites() {
        // Load item icons (24x24 grid)
        if (Gdx.files.internal("graphics/ui/item_icons.png").exists()) {
            itemIconsSheet = new Texture(Gdx.files.internal("graphics/ui/item_icons.png"));
            itemIconsSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            itemRegions = TextureRegion.split(itemIconsSheet, ITEM_SIZE, ITEM_SIZE);
        }
        
        // Load pokeball icons (16x16 grid)
        if (Gdx.files.internal("graphics/ui/pokeball_icons.png").exists()) {
            pokeballIconsSheet = new Texture(Gdx.files.internal("graphics/ui/pokeball_icons.png"));
            pokeballIconsSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            pokeballRegions = TextureRegion.split(pokeballIconsSheet, POKEBALL_SIZE, POKEBALL_SIZE);
        }
        
        // Load menu UI sheet (variable sizes, access via getMenuRegion)
        if (Gdx.files.internal("graphics/ui/pokemon_menu.png").exists()) {
            pokemonMenuSheet = new Texture(Gdx.files.internal("graphics/ui/pokemon_menu.png"));
            pokemonMenuSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        
        // Load battle backgrounds
        if (Gdx.files.internal("graphics/ui/battle_backgrounds.png").exists()) {
            battleBackgroundsSheet = new Texture(Gdx.files.internal("graphics/ui/battle_backgrounds.png"));
            battleBackgroundsSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }
    
    /**
     * Get item icon by row and column (0-indexed).
     */
    public TextureRegion getItemIcon(int row, int col) {
        if (itemRegions != null && row < itemRegions.length && col < itemRegions[row].length) {
            return itemRegions[row][col];
        }
        return null;
    }
    
    /**
     * Get pokeball icon by row and column (0-indexed).
     */
    public TextureRegion getPokeballIcon(int row, int col) {
        if (pokeballRegions != null && row < pokeballRegions.length && col < pokeballRegions[row].length) {
            return pokeballRegions[row][col];
        }
        return null;
    }
    
    /**
     * Get common item icons by name.
     * Sprite sheet starts at (8, 24) with 24x24 grid.
     */
    public TextureRegion getItemByName(String name) {
        if (itemIconsSheet == null) return null;
        
        // Exact positions from sprite_reference.md
        // Grid starts at X=8, Y=24
        // Each item is 24x24
        int xOffset = 8;
        int yOffset = 24;
        int size = ITEM_SIZE;
        
        switch (name.toLowerCase()) {
            // Row 0 - Pokeballs
            case "poke ball":
            case "pokeball":
                return new TextureRegion(itemIconsSheet, xOffset + 3 * size, yOffset, size, size);
            case "great ball":
                return new TextureRegion(itemIconsSheet, xOffset + 2 * size, yOffset, size, size);
            case "ultra ball":
                return new TextureRegion(itemIconsSheet, xOffset + 1 * size, yOffset, size, size);
            case "master ball":
                return new TextureRegion(itemIconsSheet, xOffset, yOffset, size, size);
            
            // Row 0 - Medicine (continues after Pokeballs)
            case "potion":
                return new TextureRegion(itemIconsSheet, xOffset + 12 * size, yOffset, size, size); // col 12, row 0
            case "antidote":
                return new TextureRegion(itemIconsSheet, xOffset + 13 * size, yOffset, size, size); // col 13, row 0
            case "burn heal":
                return new TextureRegion(itemIconsSheet, xOffset + 14 * size, yOffset, size, size); // col 14, row 0
            case "ice heal":
                return new TextureRegion(itemIconsSheet, xOffset + 15 * size, yOffset, size, size); // col 15, row 0
            case "full restore":
                return new TextureRegion(itemIconsSheet, xOffset + 6 * size, yOffset + size, size, size); // col 6, row 1
            case "revive":
                return new TextureRegion(itemIconsSheet, xOffset + 11 * size, yOffset + size, size, size); // col 11, row 1
            
            // Row 3 - Battle items
            case "rare candy":
                return new TextureRegion(itemIconsSheet, xOffset + 6 * size, yOffset + 3 * size, size, size); // col 6, row 3
                
            default:
                // Return Master Ball as fallback
                return new TextureRegion(itemIconsSheet, xOffset, yOffset, size, size);
        }
    }
    
    /**
     * Get a custom region from the menu sheet.
     */
    public TextureRegion getMenuRegion(int x, int y, int width, int height) {
        if (pokemonMenuSheet != null) {
            return new TextureRegion(pokemonMenuSheet, x, y, width, height);
        }
        return null;
    }
    
    /**
     * Get battle background by type (0=grass, 1=water, 2=sand, 3=cave).
     */
    public TextureRegion getBattleBackground(int type) {
        if (battleBackgroundsSheet == null) return null;
        
        int bgWidth = battleBackgroundsSheet.getWidth() / 2;
        int bgHeight = battleBackgroundsSheet.getHeight() / 3;
        
        int col = type % 2;
        int row = type / 2;
        
        return new TextureRegion(battleBackgroundsSheet, col * bgWidth, row * bgHeight, bgWidth, bgHeight);
    }
    
    public void dispose() {
        if (itemIconsSheet != null) itemIconsSheet.dispose();
        if (pokeballIconsSheet != null) pokeballIconsSheet.dispose();
        if (pokemonMenuSheet != null) pokemonMenuSheet.dispose();
        if (battleBackgroundsSheet != null) battleBackgroundsSheet.dispose();
    }
}
