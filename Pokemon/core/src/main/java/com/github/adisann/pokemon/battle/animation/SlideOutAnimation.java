package com.github.adisann.pokemon.battle.animation;

import com.badlogic.gdx.assets.AssetManager;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * Slide-out animation.
 * The Pokemon slides OUT of the screen.
 * 
 * For opponent: slides to right (0 to positive X offset)
 */
public class SlideOutAnimation extends BattleAnimation {

    private boolean toRight;

    /**
     * Create a slide-out animation.
     * 
     * @param toRight If true, slides out to right (for opponent)
     */
    public SlideOutAnimation(boolean toRight) {
        super(0.6f); // 0.6 second animation
        this.toRight = toRight;
    }

    @Override
    public void initialize(AssetManager assetManager, TweenManager tweenManager) {
        super.initialize(assetManager, tweenManager);

        // Start at center (0)
        this.setPrimaryX(0f);
        this.setPrimaryAlpha(1f);

        // Slide to off-screen
        float endOffset = toRight ? 1.5f : -1.5f;

        Tween.to(this, BattleAnimationAccessor.PRIMARY_OFFSET_X, 0.6f)
                .target(endOffset)
                .start(tweenManager);
    }
}
