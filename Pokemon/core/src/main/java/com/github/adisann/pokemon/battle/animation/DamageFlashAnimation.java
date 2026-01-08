package com.github.adisann.pokemon.battle.animation;

import com.badlogic.gdx.assets.AssetManager;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * Pokemon Emerald-style damage flash animation.
 * The damaged Pokemon flashes (blinks) 3 times quickly.
 * Used when a Pokemon takes damage from an attack.
 */
public class DamageFlashAnimation extends BattleAnimation {
	
	private static final int FLASH_COUNT = 3;
	private static final float FLASH_DURATION = 0.3f;

	public DamageFlashAnimation() {
		super(FLASH_DURATION);
	}
	
	@Override
	public void initialize(AssetManager assetManager, TweenManager tweenManager) {
		super.initialize(assetManager, tweenManager);
		
		float timePerFlash = FLASH_DURATION / (FLASH_COUNT * 2);
		
		// Flash on/off FLASH_COUNT times
		for (int i = 0; i < FLASH_COUNT * 2; i++) {
			float targetAlpha = (i % 2 == 0) ? 0.2f : 1f; // Flash to low alpha, then back
			Tween.to(this, BattleAnimationAccessor.PRIMARY_ALPHA, 0f)
				.target(targetAlpha)
				.delay(i * timePerFlash)
				.start(tweenManager);
		}
	}
}
