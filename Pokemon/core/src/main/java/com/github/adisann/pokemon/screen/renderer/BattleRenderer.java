package com.github.adisann.pokemon.screen.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.github.adisann.pokemon.Settings;
import com.github.adisann.pokemon.battle.BATTLE_PARTY;
import com.github.adisann.pokemon.battle.animation.BattleAnimation;
import com.github.adisann.pokemon.battle.animation.BattleSprite;

/**
 * Pokemon Emerald-style Battle Renderer.
 * 
 * Layout:
 * - Enemy Pokemon: top-right, smaller (perspective)
 * - Player Pokemon: bottom-left, larger
 * - Diagonal battle field appearance
 */
public class BattleRenderer {
	
	// Screen layout constants (as ratios of screen size)
	private static final float PLAYER_X_RATIO = 0.22f;   // Player on left side
	private static final float PLAYER_Y_RATIO = 0.35f;   // Player lower
	private static final float OPPONENT_X_RATIO = 0.72f; // Opponent on right side
	private static final float OPPONENT_Y_RATIO = 0.65f; // Opponent higher (raised to avoid UI overlap)
	
	// Scale factors for perspective effect (adjusted for 96x96 sprites)
	private static final float PLAYER_SCALE = 1.8f;      // Player Pokemon larger (closer)
	private static final float OPPONENT_SCALE = 1.4f;    // Opponent Pokemon smaller (farther)
	
	// Platform ellipse sizes
	private static final float PLAYER_PLATFORM_WIDTH = 120f;
	private static final float PLAYER_PLATFORM_HEIGHT = 24f;
	private static final float OPPONENT_PLATFORM_WIDTH = 90f;
	private static final float OPPONENT_PLATFORM_HEIGHT = 18f;
	
	// Sprite Y offset to account for Pokemon being centered in 96x96 image
	// Negative value pushes sprite DOWN so visible Pokemon sits on platform center
	private static final float PLAYER_SPRITE_Y_OFFSET = -60f;   // Lower on platform
	private static final float OPPONENT_SPRITE_Y_OFFSET = -50f; // Lower on platform
	
	private int squareSize = 100;
	
	private float playerSquareMiddleX = 0;
	private float playerSquareMiddleY = 0;
	private int opponentSquareMiddleX = 0;
	private int opponentSquareMiddleY = 0;
	
	private AssetManager assetManager;
	private ShaderProgram maskShader;
	private ShaderProgram defaultShader;
	
	private TextureRegion background;
	private TextureRegion platform;
	
	private Texture playerPokemonTexture;
	private Texture opponentPokemonTexture;
	
	private int playerSpriteWidth = 96;
	private int playerSpriteHeight = 96;
	private int opponentSpriteWidth = 96;
	private int opponentSpriteHeight = 96;
	
	private Texture pokemonTexture;
	
	// Cached positions for status box positioning
	private float lastPlayerX, lastPlayerY;
	private float lastOpponentX, lastOpponentY;
	
	// Alpha and scale for catch animation
	private float opponentAlpha = 1f;
	private float opponentScale = 1f;
	
	public void setOpponentAlpha(float alpha) {
		this.opponentAlpha = alpha;
	}
	
	public void setOpponentScale(float scale) {
		this.opponentScale = scale;
	}
	
	public BattleRenderer(AssetManager assetManager, ShaderProgram maskShader) {
		this.assetManager = assetManager;
		this.maskShader = maskShader;
		TextureAtlas atlas = assetManager.get("graphics_packed/battle/battlepack.atlas", TextureAtlas.class);
		background = atlas.findRegion("background");
		platform = atlas.findRegion("platform");
		pokemonTexture = assetManager.get("graphics/pokemon/bulbasaur.png", Texture.class);
	}
	
	/**
	 * Renders the battle scene in Pokemon Emerald style.
	 */
	public void render(SpriteBatch batch, BattleAnimation animation, BATTLE_PARTY primarilyAnimated) {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		// Calculate Pokemon Emerald-style positions
		// Player: bottom-left
		float playerBaseX = screenWidth * PLAYER_X_RATIO;
		float playerBaseY = screenHeight * PLAYER_Y_RATIO;
		
		// Opponent: top-right
		float opponentBaseX = screenWidth * OPPONENT_X_RATIO;
		float opponentBaseY = screenHeight * OPPONENT_Y_RATIO;
		
		// Store for external access (status boxes, etc)
		playerSquareMiddleX = playerBaseX;
		playerSquareMiddleY = playerBaseY;
		opponentSquareMiddleX = (int) opponentBaseX;
		opponentSquareMiddleY = (int) opponentBaseY;
		
		float playerAlpha = 1f;
		// Note: opponentAlpha uses class field (set by setOpponentAlpha)
		float playerWidthMod = 1f;
		float playerHeightMod = 1f;
		float opponentWidthMod = 1f;
		float opponentHeightMod = 1f;
		float playerOffsetX = 0f;
		float playerOffsetY = 0f;
		float opponentOffsetX = 0f;
		float opponentOffsetY = 0f;
		
		// Apply battle animation modifiers
		if (animation != null) {
			if (primarilyAnimated == BATTLE_PARTY.PLAYER) {
				playerWidthMod = animation.getPrimaryWidth();
				playerHeightMod = animation.getPrimaryHeight();
				playerAlpha = animation.getPrimaryAlpha();
				opponentAlpha = animation.getSecondaryAlpha();
				playerOffsetX = animation.getPrimaryOffsetX() * squareSize;
				playerOffsetY = animation.getPrimaryOffsetY() * squareSize;
				opponentOffsetX = -animation.getSecondaryOffsetX() * squareSize;
				opponentOffsetY = -animation.getSecondaryOffsetY() * squareSize;
				opponentWidthMod = animation.getSecondaryWidth();
				opponentHeightMod = animation.getSecondaryHeight();
			} else if (primarilyAnimated == BATTLE_PARTY.OPPONENT) {
				opponentWidthMod = animation.getPrimaryWidth();
				opponentHeightMod = animation.getPrimaryHeight();
				playerAlpha = animation.getSecondaryAlpha();
				opponentAlpha = animation.getPrimaryAlpha();
				opponentOffsetX = -animation.getPrimaryOffsetX() * squareSize;
				opponentOffsetY = animation.getPrimaryOffsetY() * squareSize;
				playerOffsetX = animation.getSecondaryOffsetX() * squareSize;
				playerOffsetY = animation.getSecondaryOffsetY() * squareSize;
				playerWidthMod = animation.getSecondaryWidth();
				playerHeightMod = animation.getSecondaryHeight();
			}
		}
		
		// ===== RENDER BACKGROUND =====
		// Draw gradient-style battle background (Emerald uses blue gradient)
		batch.setColor(0.4f, 0.6f, 0.9f, 1f); // Light blue
		batch.draw(background, 0, screenHeight * 0.4f, screenWidth, screenHeight * 0.6f);
		batch.setColor(0.3f, 0.8f, 0.4f, 1f); // Green for ground
		batch.draw(background, 0, 0, screenWidth, screenHeight * 0.45f);
		batch.setColor(1f, 1f, 1f, 1f);
		
		// ===== RENDER PLATFORMS =====
		// Opponent platform (smaller, higher)
		float oppPlatformX = opponentBaseX - OPPONENT_PLATFORM_WIDTH * Settings.SCALE / 2;
		float oppPlatformY = opponentBaseY - OPPONENT_PLATFORM_HEIGHT * Settings.SCALE / 2;
		batch.setColor(0.2f, 0.5f, 0.3f, 1f); // Dark green platform
		batch.draw(platform, oppPlatformX, oppPlatformY,
				OPPONENT_PLATFORM_WIDTH * Settings.SCALE,
				OPPONENT_PLATFORM_HEIGHT * Settings.SCALE);
		
		// Player platform (larger, lower)
		float playerPlatformX = playerBaseX - PLAYER_PLATFORM_WIDTH * Settings.SCALE / 2;
		float playerPlatformY = playerBaseY - PLAYER_PLATFORM_HEIGHT * Settings.SCALE / 2;
		batch.setColor(0.3f, 0.6f, 0.35f, 1f); // Lighter green platform
		batch.draw(platform, playerPlatformX, playerPlatformY,
				PLAYER_PLATFORM_WIDTH * Settings.SCALE,
				PLAYER_PLATFORM_HEIGHT * Settings.SCALE);
		batch.setColor(1f, 1f, 1f, 1f);
		
		// ===== RENDER OPPONENT POKEMON (back layer, farther) =====
		if (opponentPokemonTexture != null && opponentAlpha > 0) {
			float baseWidth = opponentSpriteWidth * OPPONENT_SCALE * opponentWidthMod;
			float baseHeight = opponentSpriteHeight * OPPONENT_SCALE * opponentHeightMod;
			
			// Apply capture scale effect
			float oppWidth = baseWidth * opponentScale;
			float oppHeight = baseHeight * opponentScale;
			
			// Center on original position when scaling
			float oppBaseX = opponentBaseX + opponentOffsetX;
			float oppBaseY = oppPlatformY + OPPONENT_PLATFORM_HEIGHT * Settings.SCALE * 0.5f + OPPONENT_SPRITE_Y_OFFSET + opponentOffsetY;
			float oppX = oppBaseX - oppWidth / 2;
			float oppY = oppBaseY - oppHeight / 2 + baseHeight / 2;  // Anchor to bottom
			
			lastOpponentX = oppX;
			lastOpponentY = oppY;
			
			batch.setColor(1f, 1f, 1f, opponentAlpha);
			batch.draw(opponentPokemonTexture, oppX, oppY, oppWidth, oppHeight);
		}
		
		// ===== RENDER PLAYER POKEMON (front layer, closer) =====
		// Uses backside sprite - Pokemon faces away from camera toward opponent
		if (playerPokemonTexture != null) {
			float playerWidth = playerSpriteWidth * PLAYER_SCALE * playerWidthMod;
			float playerHeight = playerSpriteHeight * PLAYER_SCALE * playerHeightMod;
			float playerX = playerBaseX - playerWidth / 2 + playerOffsetX;
			// Position Pokemon ON the platform (with offset for centered sprite)
			float playerY = playerPlatformY + PLAYER_PLATFORM_HEIGHT * Settings.SCALE * 0.5f + PLAYER_SPRITE_Y_OFFSET + playerOffsetY;
			
			lastPlayerX = playerX;
			lastPlayerY = playerY;
			
			batch.setColor(1f, 1f, 1f, playerAlpha);
			// No flip needed - backside sprite already faces the right direction
			batch.draw(playerPokemonTexture, playerX, playerY, playerWidth, playerHeight);
		}
		
		batch.setColor(1f, 1f, 1f, 1f);
		
		// ===== RENDER BATTLE ANIMATION SPRITES =====
		if (animation != null && !animation.isFinished()) {
			for (BattleSprite sprite : animation.getSprites()) {
				batch.setColor(1f, 1f, 1f, sprite.getAlpha());
				float spriteX, spriteY;
				if (primarilyAnimated == BATTLE_PARTY.PLAYER) {
					spriteX = playerBaseX + sprite.getX() * squareSize - sprite.getWidth() * sprite.getRegion().getRegionWidth() / 2;
					spriteY = playerBaseY + sprite.getY() * squareSize - sprite.getHeight() * sprite.getRegion().getRegionHeight() / 2;
				} else {
					spriteX = opponentBaseX - sprite.getX() * squareSize - sprite.getWidth() * sprite.getRegion().getRegionWidth() / 2;
					spriteY = opponentBaseY + sprite.getY() * squareSize - sprite.getHeight() * sprite.getRegion().getRegionHeight() / 2;
				}
				
				float spriteDrawWidth = Settings.SCALE * sprite.getWidth() * sprite.getRegion().getRegionWidth();
				float spriteDrawHeight = Settings.SCALE * sprite.getHeight() * sprite.getRegion().getRegionHeight();
				
				batch.draw(
						sprite.getRegion(),
						spriteX - spriteDrawWidth / 2,
						spriteY - spriteDrawHeight / 2,
						spriteDrawWidth / 2,
						spriteDrawHeight / 2,
						spriteDrawWidth,
						spriteDrawHeight,
						1f, 1f,
						sprite.getRotation());
				batch.setColor(1f, 1f, 1f, 1f);
			}
		}
	}
	
	/**
	 * Set Pokemon sprite from path (supports PNG).
	 */
	public void setPokemonSprite(String spritePath, BATTLE_PARTY party) {
		if (spritePath == null) return;
		
		// Load as static PNG texture
		if (!assetManager.isLoaded(spritePath)) {
			assetManager.load(spritePath, Texture.class);
			assetManager.finishLoading();
		}
		Texture texture = assetManager.get(spritePath, Texture.class);
		setPokemonSprite(texture, party);
	}
	
	/**
	 * Set Pokemon sprite directly from texture.
	 */
	public void setPokemonSprite(Texture texture, BATTLE_PARTY party) {
		if (party == BATTLE_PARTY.PLAYER) {
			playerPokemonTexture = texture;
			if (texture != null) {
				playerSpriteWidth = texture.getWidth();
				playerSpriteHeight = texture.getHeight();
			}
		} else if (party == BATTLE_PARTY.OPPONENT) {
			opponentPokemonTexture = texture;
			if (texture != null) {
				opponentSpriteWidth = texture.getWidth();
				opponentSpriteHeight = texture.getHeight();
			}
		}
	}
	
	public void dispose() {
		// No GIF animations to dispose anymore
	}
	
	public int squareSize() {
		return squareSize;
	}
	
	public float playerSquareMiddleX() {
		return playerSquareMiddleX;
	}
	
	public float playerSquareMiddleY() {
		return playerSquareMiddleY;
	}
	
	public int opponentSquareMiddleX() {
		return opponentSquareMiddleX;
	}
	
	public int opponentSquareMiddleY() {
		return opponentSquareMiddleY;
	}
}