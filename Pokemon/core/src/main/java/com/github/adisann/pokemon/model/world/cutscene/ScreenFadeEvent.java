package com.github.adisann.pokemon.model.world.cutscene;

import com.badlogic.gdx.graphics.Color;

/**
 * Cutscene event that fades the screen in or out.
 * 
 * This event works by setting a fade state that the renderer can check
 * and apply a color overlay. The event waits for the fade duration before
 * completing.
 */
public class ScreenFadeEvent extends CutsceneEvent {

	public enum FadeType {
		FADE_OUT,  // Fade to color (screen becomes covered)
		FADE_IN    // Fade from color to clear (screen becomes visible)
	}

	private FadeType fadeType;
	private Color fadeColor;
	private float duration;
	private float elapsed;
	private boolean isFinished = false;

	/**
	 * Create a screen fade event.
	 * 
	 * @param fadeType FADE_OUT to fade to color, FADE_IN to fade from color
	 * @param fadeColor The color to fade to/from (typically Color.BLACK or Color.WHITE)
	 * @param duration How long the fade takes in seconds
	 */
	public ScreenFadeEvent(FadeType fadeType, Color fadeColor, float duration) {
		this.fadeType = fadeType;
		this.fadeColor = fadeColor;
		this.duration = duration;
		this.elapsed = 0f;
	}

	/**
	 * Convenience constructor for black fade.
	 */
	public ScreenFadeEvent(FadeType fadeType, float duration) {
		this(fadeType, Color.BLACK, duration);
	}

	@Override
	public void begin(CutscenePlayer player) {
		super.begin(player);
		elapsed = 0f;
		
		// Notify the game to start fade
		if (player instanceof ScreenFadeHandler) {
			ScreenFadeHandler handler = (ScreenFadeHandler) player;
			float startAlpha = (fadeType == FadeType.FADE_OUT) ? 0f : 1f;
			float endAlpha = (fadeType == FadeType.FADE_OUT) ? 1f : 0f;
			handler.startScreenFade(fadeColor, startAlpha, endAlpha, duration);
		}
	}

	@Override
	public void update(float delta) {
		elapsed += delta;
		if (elapsed >= duration) {
			isFinished = true;
		}
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	@Override
	public void screenShow() {
		// If screen shows during fade, complete immediately
		isFinished = true;
	}

	public FadeType getFadeType() {
		return fadeType;
	}

	public Color getFadeColor() {
		return fadeColor;
	}

	public float getProgress() {
		return Math.min(1f, elapsed / duration);
	}

	/**
	 * Interface for objects that can handle screen fade effects.
	 * GameScreen should implement this.
	 */
	public interface ScreenFadeHandler {
		void startScreenFade(Color color, float startAlpha, float endAlpha, float duration);
	}
}
