package com.github.adisann.pokemon.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.adisann.pokemon.PokemonGame;
import com.github.adisann.pokemon.screen.transition.Transition;
import com.github.adisann.pokemon.util.Action;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;

/**
 * Used for transitions between screens.
 * */
@Component
public class TransitionScreen implements AbstractScreen {

	@Inject
	private PokemonGame app;
	@Inject
	private InterfaceService interfaceService;

	private AbstractScreen from;
	private AbstractScreen to;

	private Transition outTransition;
	private Transition inTransition;

	private Action action;

	private SpriteBatch batch;
	private Viewport viewport;

	private TRANSITION_STATE state;

	private enum TRANSITION_STATE {
		OUT,
		IN,
		;
	}

	public TransitionScreen() {
		batch = new SpriteBatch();
		viewport = new ScreenViewport();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void update(float delta) {
		if (state == TRANSITION_STATE.OUT) {
			outTransition.update(delta);
			if (outTransition.isFinished()) {
				action.action();
				state = TRANSITION_STATE.IN;
				return;
			}
		} else if (state == TRANSITION_STATE.IN) {
			inTransition.update(delta);
			if (inTransition.isFinished()) {
				// Transition completed, show the destination screen
				interfaceService.show(to.getClass());
			}
		}
	}

	@Override
	public void render(float delta) {
		if (state == TRANSITION_STATE.OUT) {
			from.render(delta);

			viewport.apply();
			outTransition.render(delta, batch);
		} else if (state == TRANSITION_STATE.IN) {
			to.render(delta);

			viewport.apply();
			inTransition.render(delta, batch);
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		if (to != null) {
			to.resize(width, height);
		}
		if (from != null) {
			from.resize(width, height);
		}
	}

	@Override
	public void resume() {

	}

	@Override
	public void show() {

	}

	public void startTransition(AbstractScreen from, AbstractScreen to, Transition out, Transition in, Action action) {
		this.from = from;
		this.to = to;
		this.outTransition = out;
		this.inTransition = in;
		this.action = action;
		this.state = TRANSITION_STATE.OUT;
	}
}