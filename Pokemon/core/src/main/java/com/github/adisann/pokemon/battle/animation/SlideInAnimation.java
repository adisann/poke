package com.github.adisann.pokemon.battle.animation;

import com.badlogic.gdx.assets.AssetManager;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * Pokemon Emerald-style slide-in animation.
 * The Pokemon slides in from outside the screen.
 * 
 * For opponent: slides from right (positive X offset to 0)
 * For player: would slide from left if used
 */
public class SlideInAnimation extends BattleAnimation {

	private boolean fromRight;

	/**
	 * Create a slide-in animation.
	 * @param fromRight If true, slides in from right (for opponent), else from left
	 */
	public SlideInAnimation(boolean fromRight) {
		super(0.6f); // 0.6 second animation
		this.fromRight = fromRight;
	}
	
	@Override
	public void initialize(AssetManager assetManager, TweenManager tweenManager) {
		super.initialize(assetManager, tweenManager);
		
		// Start off-screen
		float startOffset = fromRight ? 1.5f : -1.5f;
		this.setPrimaryX(startOffset);
		this.setPrimaryAlpha(1f); // Fully visible while sliding
		
		// Slide to center position
		Tween.to(this, BattleAnimationAccessor.PRIMARY_OFFSET_X, 0.6f)
			.target(0f)
			.start(tweenManager);
	}
}
