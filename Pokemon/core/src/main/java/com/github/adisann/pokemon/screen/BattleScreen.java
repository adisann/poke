package com.github.adisann.pokemon.screen;

import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.adisann.pokemon.PokemonGameMain;
import com.github.adisann.pokemon.Settings;
import com.github.adisann.pokemon.battle.BATTLE_PARTY;
import com.github.adisann.pokemon.battle.Battle;
import com.github.adisann.pokemon.battle.Battle.STATE;
import com.github.adisann.pokemon.battle.Trainer;
import com.github.adisann.pokemon.battle.animation.BattleAnimation;
import com.github.adisann.pokemon.battle.event.BattleEvent;
import com.github.adisann.pokemon.battle.event.BattleEventPlayer;
import com.github.adisann.pokemon.controller.BattleScreenController;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.screen.renderer.BattleDebugRenderer;
import com.github.adisann.pokemon.screen.renderer.BattleRenderer;
import com.github.adisann.pokemon.screen.renderer.EventQueueRenderer;
import com.github.adisann.pokemon.ui.DetailedStatusBox;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.MoveSelectBox;
import com.github.adisann.pokemon.ui.OptionBox;
import com.github.adisann.pokemon.ui.StatusBox;

import aurelienribon.tweenengine.TweenManager;

/**
 * Battle screen for Pokemon battles.
 * Uses standard LibGDX Screen interface with manual dependency injection.
 */
public class BattleScreen implements AbstractScreen, BattleEventPlayer {

	/* Core - Manual injection via init() */
	private PokemonGameMain app;
	private AssetManager assetManager;
	private TweenManager tweenManager;

	private Skin skin;

	public void init(PokemonGameMain game) {
		this.app = game;
		this.assetManager = game.getAssetManager();
		this.tweenManager = game.getTweenManager();
	}

	public void setBattleContext(Trainer player, Pokemon opponent) {
		this.pendingPlayer = player;
		this.pendingOpponent = opponent;
	}

	/* Controller */
	private BattleScreenController controller;

	/* Event system */
	private BattleEvent currentEvent;
	private Queue<BattleEvent> queue = new ArrayDeque<BattleEvent>();

	/* Model */
	private Battle battle;

	private BATTLE_PARTY animationPrimary;
	private BattleAnimation battleAnimation = null;

	private Trainer pendingPlayer;
	private Pokemon pendingOpponent;

	/* View */
	// private BitmapFont text = new BitmapFont(); // Removed: not used in new
	// architecture

	private Viewport gameViewport;

	private SpriteBatch batch;
	private BattleRenderer battleRenderer;
	private BattleDebugRenderer battleDebugRenderer;
	private EventQueueRenderer eventRenderer;

	/* UI */
	private Stage uiStage;
	private Table dialogueRoot;
	private DialogueBox dialogueBox;
	private OptionBox optionBox;

	private Table moveSelectRoot;
	private MoveSelectBox moveSelectBox;

	private Table statusBoxRoot;
	private DetailedStatusBox playerStatus;
	private StatusBox opponentStatus;

	/* DEBUG */
	private boolean uiDebug = false;
	private boolean battleDebug = true;

	@Override
	public void show() {
		skin = app.getSkin();
		gameViewport = new ScreenViewport();
		batch = new SpriteBatch();

		if (pendingPlayer != null && pendingOpponent != null) {
			battle = new Battle(pendingPlayer, pendingOpponent);
			pendingPlayer = null;
			pendingOpponent = null;
		} else {
			Trainer playerTrainer = new Trainer(
					Pokemon.generatePokemon("Bulba", "graphics/pokemon/bulbasaur.png", app.getMoveDatabase()));
			playerTrainer.addPokemon(
					Pokemon.generatePokemon("Golem", "graphics/pokemon/slowpoke.png", app.getMoveDatabase()));

			battle = new Battle(
					playerTrainer,
					Pokemon.generatePokemon("Grimer", "graphics/pokemon/slowpoke.png", app.getMoveDatabase()));
		}
		battle.setEventPlayer(this);

		animationPrimary = BATTLE_PARTY.PLAYER;

		battleRenderer = new BattleRenderer(assetManager, app.getOverlayShader());
		battleDebugRenderer = new BattleDebugRenderer(battleRenderer);
		eventRenderer = new EventQueueRenderer(skin, queue);

		initUI();

		controller = new BattleScreenController(battle, queue, dialogueBox, moveSelectBox, optionBox);

		battle.beginBattle();
		Gdx.input.setInputProcessor(controller);
	}

	@Override
	public void render(float delta) {
		update(delta);
		gameViewport.apply();
		batch.begin();
		battleRenderer.render(batch, battleAnimation, animationPrimary);
		if (currentEvent != null && battleDebug) {
			eventRenderer.render(batch, currentEvent);
		}
		batch.end();

		if (battleDebug) {
			battleDebugRenderer.render();
		}

		uiStage.draw();
	}

	@Override
	public void resize(int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		uiStage.getViewport().update(
				(int) (Gdx.graphics.getWidth() / Settings.SCALE_UI),
				(int) (Gdx.graphics.getHeight() / Settings.SCALE_UI),
				true);
		gameViewport.update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void update(float delta) {
		if (Gdx.input.isKeyJustPressed(Keys.F9)) {
			uiDebug = !uiDebug;
			uiStage.setDebugAll(uiDebug);
		}
		if (Gdx.input.isKeyJustPressed(Keys.F10)) {
			battleDebug = !battleDebug;
		}

		while (currentEvent == null || currentEvent.finished()) { // no active event
			if (queue.peek() == null) { // no event queued up
				currentEvent = null;

				if (battle.getState() == STATE.SELECT_NEW_POKEMON) {
					if (controller.getState() != BattleScreenController.STATE.USE_NEXT_POKEMON) {
						controller.displayNextDialogue();
					}
				} else if (battle.getState() == STATE.READY_TO_PROGRESS) {
					// Only restart turn if not already selecting
					if (controller.getState() != BattleScreenController.STATE.SELECT_ACTION
							&& controller.getState() != BattleScreenController.STATE.SELECT_MOVE) {
						controller.restartTurn();
					}
				} else if (battle.getState() == STATE.WIN) {
					app.setScreen(app.getGameScreen());
				} else if (battle.getState() == STATE.LOSE) {
					// Do NOT auto-heal - HP stays 0, player must go to Mom's house
					// Teleport player to first town (littleroot_town spawn point)
					app.getGameScreen().teleportToFirstTown();
					app.setScreen(app.getGameScreen());
				} else if (battle.getState() == STATE.RAN) {
					app.setScreen(app.getGameScreen());
				}
				break;
			} else { // event queued up
				currentEvent = queue.poll();
				currentEvent.begin(this);
			}
		}

		if (currentEvent != null) {
			currentEvent.update(delta);
		}

		controller.update(delta);
		uiStage.act(); // update ui
	}

	private void initUI() {
		/* ROOT UI STAGE */
		uiStage = new Stage(new ScreenViewport());
		uiStage.getViewport().update(
				(int) (Gdx.graphics.getWidth() / Settings.SCALE_UI),
				(int) (Gdx.graphics.getHeight() / Settings.SCALE_UI),
				true);
		uiStage.setDebugAll(false);

		/* STATUS BOXES */
		statusBoxRoot = new Table();
		statusBoxRoot.setFillParent(true);
		uiStage.addActor(statusBoxRoot);

		playerStatus = new DetailedStatusBox(skin);
		playerStatus.setNameAndLevel(battle.getPlayerPokemon().getName(), battle.getPlayerPokemon().getLevel());
		playerStatus.setEXPText(battle.getPlayerPokemon().getCurrentExp(),
				battle.getPlayerPokemon().getExpToNextLevel());

		opponentStatus = new StatusBox(skin);
		opponentStatus.setNameAndLevel(battle.getOpponentPokemon().getName(), battle.getOpponentPokemon().getLevel());

		statusBoxRoot.add(playerStatus).expand().align(Align.left);
		statusBoxRoot.add(opponentStatus).expand().align(Align.right);

		/* MOVE SELECTION BOX */
		moveSelectRoot = new Table();
		moveSelectRoot.setFillParent(true);
		uiStage.addActor(moveSelectRoot);

		moveSelectBox = new MoveSelectBox(skin);
		moveSelectBox.setVisible(false);

		moveSelectRoot.add(moveSelectBox).expand().align(Align.bottom);

		/* OPTION BOX */
		dialogueRoot = new Table();
		dialogueRoot.setFillParent(true);
		uiStage.addActor(dialogueRoot);

		optionBox = new OptionBox(skin);
		optionBox.setVisible(false);

		/* DIALOGUE BOX */
		dialogueBox = new DialogueBox(skin);
		dialogueBox.setVisible(false);

		Table dialogTable = new Table();
		dialogTable.add(optionBox).expand().align(Align.right).space(8f).row();
		dialogTable.add(dialogueBox).expand().align(Align.bottom).space(8f);

		dialogueRoot.add(dialogTable).expand().align(Align.bottom);
	}

	public StatusBox getStatus(BATTLE_PARTY hpbar) {
		if (hpbar == BATTLE_PARTY.PLAYER) {
			return playerStatus;
		} else if (hpbar == BATTLE_PARTY.OPPONENT) {
			return opponentStatus;
		} else {
			return null;
		}
	}

	@Override
	public void queueEvent(BattleEvent event) {
		queue.add(event);
	}

	@Override
	public DialogueBox getDialogueBox() {
		return dialogueBox;
	}

	@Override
	public BattleAnimation getBattleAnimation() {
		return battleAnimation;
	}

	@Override
	public TweenManager getTweenManager() {
		return tweenManager;
	}

	@Override
	public void playBattleAnimation(BattleAnimation animation, BATTLE_PARTY party) {
		this.animationPrimary = party;
		this.battleAnimation = animation;
		animation.initialize(assetManager, tweenManager);
	}

	@Override
	public StatusBox getStatusBox(BATTLE_PARTY party) {
		if (party == BATTLE_PARTY.PLAYER) {
			return playerStatus;
		} else if (party == BATTLE_PARTY.OPPONENT) {
			return opponentStatus;
		} else {
			return null;
		}
	}

	@Override
	public void setPokemonSprite(String spriteName, BATTLE_PARTY party) {
		if (spriteName == null)
			return;
		if (!assetManager.isLoaded(spriteName)) {
			assetManager.load(spriteName, Texture.class);
			assetManager.finishLoading();
		}
		Texture texture = assetManager.get(spriteName, Texture.class);
		battleRenderer.setPokemonSprite(texture, party);
	}

	@Override
	public Pokemon getPlayerPokemon() {
		return battle.getPlayerPokemon();
	}
}