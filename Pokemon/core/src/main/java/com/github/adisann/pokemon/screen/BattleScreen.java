package com.github.adisann.pokemon.screen;

import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
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
import com.github.adisann.pokemon.battle.STAT;
import com.github.adisann.pokemon.screen.renderer.BattleDebugRenderer;
import com.github.adisann.pokemon.screen.renderer.BattleRenderer;
import com.github.adisann.pokemon.screen.renderer.EventQueueRenderer;
import com.github.adisann.pokemon.ui.DetailedStatusBox;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.MoveSelectBox;
import com.github.adisann.pokemon.ui.OptionBox;
import com.github.adisann.pokemon.ui.ActionMenu;
import com.github.adisann.pokemon.ui.StatusBox;
import com.github.adisann.pokemon.ui.BattleBagUI;
import com.github.adisann.pokemon.ui.BattlePartyUI;
import com.github.adisann.pokemon.model.Inventory;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.View;

import aurelienribon.tweenengine.TweenManager;

/** */
@View(id = "battle", value = "")
public class BattleScreen implements AbstractScreen, BattleEventPlayer {

	/* Core */
	// @Inject -> Manual injection via init()
	private PokemonGameMain app;
	@Inject
	private AssetManager assetManager;
	@Inject
	private TweenManager tweenManager;
	@Inject
	private InterfaceService interfaceService;

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

	/**
	 * Set battle context for trainer battles (vs NPC trainer).
	 * 
	 * @param player    Player's trainer
	 * @param opponent  Opponent trainer (NPC)
	 * @param trainerId Unique ID for the trainer (for save system)
	 */
	public void setTrainerBattleContext(Trainer player, Trainer opponent, String trainerId) {
		this.pendingPlayer = player;
		this.pendingOpponentTrainer = opponent;
		this.opponentTrainerId = trainerId;
	}

	/**
	 * Callback interface for battle end events.
	 */
	public interface BattleEndCallback {
		void onBattleEnd(boolean playerWon);
	}

	/**
	 * Set a callback to be called when the battle ends.
	 * 
	 * @param callback Callback to call with battle result
	 */
	public void setBattleEndCallback(BattleEndCallback callback) {
		this.battleEndCallback = callback;
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
	private Trainer pendingOpponentTrainer; // For trainer battles
	private String opponentTrainerId; // For save system
	private BattleEndCallback battleEndCallback;

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

	private ActionMenu actionMenu;

	/* Sub-Screen Management */
	public enum SubScreen {
		BATTLE_FIELD, // Pokemon + Action Menu
		MOVE_SELECT, // Move grid
		BAG_SCREEN, // Full-screen bag
		PARTY_SCREEN // Full-screen party
	}

	private SubScreen currentSubScreen = SubScreen.BATTLE_FIELD;

	private BattleBagUI battleBagUI;
	private BattlePartyUI battlePartyUI;
	private Inventory playerInventory;

	/* DEBUG */
	private boolean uiDebug = false;
	private boolean battleDebug = true;

	@Override
	public void show() {
		skin = app.getSkin();
		gameViewport = new ScreenViewport();
		batch = new SpriteBatch();

		// Initialize battle based on context type
		if (pendingPlayer != null && pendingOpponentTrainer != null) {
			// Trainer battle
			battle = new Battle(pendingPlayer, pendingOpponentTrainer, app.getMoveDatabase());
			pendingPlayer = null;
			pendingOpponentTrainer = null;
		} else if (pendingPlayer != null && pendingOpponent != null) {
			// Wild battle
			battle = new Battle(pendingPlayer, pendingOpponent, app.getMoveDatabase());
			pendingPlayer = null;
			pendingOpponent = null;
		} else {
			// Debug fallback
			Trainer playerTrainer = new Trainer(
					Pokemon.generatePokemon("Bulba", "graphics/pokemon/bulbasaur.png", app.getMoveDatabase()));
			playerTrainer.addPokemon(
					Pokemon.generatePokemon("Golem", "graphics/pokemon/slowpoke.png", app.getMoveDatabase()));

			battle = new Battle(
					playerTrainer,
					Pokemon.generatePokemon("Grimer", "graphics/pokemon/slowpoke.png", app.getMoveDatabase()),
					app.getMoveDatabase());
		}
		battle.setEventPlayer(this);

		animationPrimary = BATTLE_PARTY.PLAYER;

		battleRenderer = new BattleRenderer(assetManager, app.getOverlayShader());
		battleDebugRenderer = new BattleDebugRenderer(battleRenderer);
		eventRenderer = new EventQueueRenderer(skin, queue);

		initUI();

		controller = new BattleScreenController(battle, queue, dialogueBox, moveSelectBox, optionBox, actionMenu);
		controller.setBattleScreen(this);

		battle.beginBattle();
		Gdx.input.setInputProcessor(controller);
	}

	@Override
	public void render(float delta) {
		update(delta);
		gameViewport.apply();
		batch.begin();

		// Handle opponent effects for catch animation
		if (currentEvent instanceof com.github.adisann.pokemon.battle.event.PokeballCatchEvent) {
			com.github.adisann.pokemon.battle.event.PokeballCatchEvent catchEvent = (com.github.adisann.pokemon.battle.event.PokeballCatchEvent) currentEvent;
			battleRenderer.setOpponentAlpha(catchEvent.getOpponentAlpha());
			battleRenderer.setOpponentScale(catchEvent.getOpponentScale());
		} else {
			battleRenderer.setOpponentAlpha(1f);
			battleRenderer.setOpponentScale(1f);
		}

		battleRenderer.render(batch, battleAnimation, animationPrimary);

		// Render pokeball catch animation
		if (currentEvent instanceof com.github.adisann.pokemon.battle.event.PokeballCatchEvent) {
			com.github.adisann.pokemon.battle.event.PokeballCatchEvent catchEvent = (com.github.adisann.pokemon.battle.event.PokeballCatchEvent) currentEvent;
			catchEvent.render(batch);
		}

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
					// Only restart turn when controller is DEACTIVATED
					if (controller.getState() == BattleScreenController.STATE.DEACTIVATED) {
						controller.restartTurn();
					}
				} else if (battle.getState() == STATE.WIN) {
					// Invoke callback for trainer battle persistence
					if (battleEndCallback != null) {
						battleEndCallback.onBattleEnd(true);
					}
					app.setScreen(app.getGameScreen());
				} else if (battle.getState() == STATE.LOSE) {
					// Invoke callback for trainer battle (player lost)
					if (battleEndCallback != null) {
						battleEndCallback.onBattleEnd(false);
					}
					// Do NOT auto-heal - HP stays 0, player must go to Mom's house
					// Teleport player to first town (littleroot_town spawn point)
					app.getGameScreen().teleportToFirstTown();
					app.setScreen(app.getGameScreen());
				} else if (battle.getState() == STATE.RAN) {
					app.setScreen(app.getGameScreen());
				} else if (battle.getState() == STATE.CAUGHT) {
					// Add caught Pokemon to player party
					Pokemon caught = battle.getCaughtPokemon();
					if (caught != null) {
						// Ensure moves are properly loaded
						caught.reloadMoves(app.getMoveDatabase());
						boolean added = battle.getPlayerTrainer().addPokemon(caught);
						if (added) {
							System.out.println("[Battle] " + caught.getName() + " added to party!");
						} else {
							System.out.println("[Battle] Party is full! " + caught.getName() + " not added.");
							// TODO: Send to PC
						}
					}
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

		/* STATUS BOXES - Pokemon Emerald Layout */
		/* Enemy status: top-left / Player status: bottom-right (ABOVE action bar) */
		statusBoxRoot = new Table();
		statusBoxRoot.setFillParent(true);
		uiStage.addActor(statusBoxRoot);

		playerStatus = new DetailedStatusBox(skin);
		playerStatus.setNameAndLevel(battle.getPlayerPokemon().getName(), battle.getPlayerPokemon().getLevel());
		playerStatus.setHPText(battle.getPlayerPokemon().getCurrentHitpoints(),
				battle.getPlayerPokemon().getStat(STAT.HITPOINTS));
		playerStatus.setEXPText(battle.getPlayerPokemon().getCurrentExp(),
				battle.getPlayerPokemon().getExpToNextLevel());

		opponentStatus = new StatusBox(skin);
		opponentStatus.setNameAndLevel(battle.getOpponentPokemon().getName(), battle.getOpponentPokemon().getLevel());

		// Top row: opponent status on left
		statusBoxRoot.add(opponentStatus).align(Align.topLeft).pad(8f);
		statusBoxRoot.add().expandX();
		statusBoxRoot.row();

		// Middle spacer - pushes player status up from bottom
		statusBoxRoot.add().expand();
		statusBoxRoot.add().expand();
		statusBoxRoot.row();

		// Player status row - positioned ABOVE action bar (with bottom padding for
		// action bar height)
		statusBoxRoot.add();
		statusBoxRoot.add(playerStatus).align(Align.right).padRight(8f).padBottom(60f);

		/* MOVE SELECTION BOX */
		moveSelectRoot = new Table();
		moveSelectRoot.setFillParent(true);
		uiStage.addActor(moveSelectRoot);

		moveSelectBox = new MoveSelectBox(skin);
		moveSelectBox.setVisible(false);

		moveSelectRoot.add(moveSelectBox).expand().align(Align.bottom);

		/* POKEMON EMERALD STYLE BOTTOM ACTION BAR */
		dialogueRoot = new Table();
		dialogueRoot.setFillParent(true);
		uiStage.addActor(dialogueRoot);

		// Dialogue box (left side - "What will [name] do?")
		dialogueBox = new DialogueBox(skin);
		dialogueBox.setVisible(false);

		// 4-option action menu (right side - FIGHT/BAG/POKéMON/RUN)
		actionMenu = new ActionMenu(skin);
		actionMenu.setVisible(false);

		// Option box for item/pokemon selection (REPLACES action menu when active)
		optionBox = new OptionBox(skin);
		optionBox.setVisible(false);

		// Bottom bar layout using Stack for swappable right-side content
		// Stack allows multiple actors, only visible one shows
		Table bottomBar = new Table();

		// Left side: Dialogue box
		bottomBar.add(dialogueBox).expandX().fillX().pad(4f);

		// Right side: Stack of menus (only one visible at a time)
		com.badlogic.gdx.scenes.scene2d.ui.Stack menuStack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
		menuStack.add(actionMenu);
		menuStack.add(optionBox);
		bottomBar.add(menuStack).pad(4f);

		dialogueRoot.add(bottomBar).expand().align(Align.bottom).fillX();
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
		// BattleRenderer now handles both PNG and GIF loading internally
		battleRenderer.setPokemonSprite(spriteName, party);
	}

	@Override
	public Pokemon getPlayerPokemon() {
		return battle.getPlayerPokemon();
	}

	// Sub-screen management methods

	public void setInventory(Inventory inventory) {
		this.playerInventory = inventory;
	}

	public void showBag() {
		if (battleBagUI == null && playerInventory != null) {
			battleBagUI = new BattleBagUI(this.skin, playerInventory);
			battleBagUI.setListener(new BattleBagUI.BagActionListener() {
				@Override
				public void onItemSelected(com.github.adisann.pokemon.model.Item item, int index) {
					handleBagItemSelected(item, index);
				}

				@Override
				public void onCancel() {
					hideBag();
				}
			});
			uiStage.addActor(battleBagUI);
		}

		if (battleBagUI != null) {
			battleBagUI.refresh();
			battleBagUI.setVisible(true);
			currentSubScreen = SubScreen.BAG_SCREEN;

			// Hide battle field UI
			dialogueRoot.setVisible(false);
			statusBoxRoot.setVisible(false);
			moveSelectRoot.setVisible(false);

			// Set input to bag
			Gdx.input.setInputProcessor(battleBagUI.getInputHandler());
		}
	}

	public void hideBag() {
		hideBagQuiet();
		// Restart turn (only for cancel - item use handles this differently)
		controller.restartTurn();
	}

	/**
	 * Hide bag without restarting turn - used when using an item.
	 */
	private void hideBagQuiet() {
		if (battleBagUI != null) {
			battleBagUI.setVisible(false);
		}
		currentSubScreen = SubScreen.BATTLE_FIELD;

		// Show battle field UI
		dialogueRoot.setVisible(true);
		statusBoxRoot.setVisible(true);
		moveSelectRoot.setVisible(true);

		// Restore input to controller
		Gdx.input.setInputProcessor(controller);
	}

	public void showParty() {
		if (battlePartyUI == null) {
			battlePartyUI = new BattlePartyUI(this.skin, battle.getPlayerTrainer(), battle.getPlayerPokemon());
			battlePartyUI.setListener(new BattlePartyUI.PartyActionListener() {
				@Override
				public void onPokemonSelected(Pokemon pokemon, int index) {
					handlePartyPokemonSelected(pokemon, index);
				}

				@Override
				public void onCancel() {
					hideParty();
				}
			});
			uiStage.addActor(battlePartyUI);
		}

		if (battlePartyUI != null) {
			battlePartyUI.refresh(battle.getPlayerPokemon());
			battlePartyUI.setVisible(true);
			currentSubScreen = SubScreen.PARTY_SCREEN;

			// Hide battle field UI
			dialogueRoot.setVisible(false);
			statusBoxRoot.setVisible(false);
			moveSelectRoot.setVisible(false);

			// Set input to party
			Gdx.input.setInputProcessor(battlePartyUI.getInputHandler());
		}
	}

	public void hideParty() {
		if (battlePartyUI != null) {
			battlePartyUI.setVisible(false);
		}
		currentSubScreen = SubScreen.BATTLE_FIELD;

		// Show battle field UI
		dialogueRoot.setVisible(true);
		statusBoxRoot.setVisible(true);
		moveSelectRoot.setVisible(true);

		// Restore input to controller
		Gdx.input.setInputProcessor(controller);
		controller.restartTurn();
	}

	private void handleBagItemSelected(com.github.adisann.pokemon.model.Item item, int index) {
		hideBagQuiet(); // Don't restart turn yet - events will play first

		if (item instanceof com.github.adisann.pokemon.model.Pokeball) {
			// Use Pokeball - attempt catch
			item.useQuantity(1);
			battle.attemptCatch(1.0f);
			// Controller deactivated so restartTurn is called after events
			controller.setState(BattleScreenController.STATE.DEACTIVATED);
		} else if (item instanceof com.github.adisann.pokemon.model.Potion) {
			// Use Potion - heal active Pokemon
			Pokemon player = battle.getPlayerPokemon();
			int maxHP = player.getStat(STAT.HITPOINTS);
			int currentHP = player.getCurrentHitpoints();
			if (currentHP < maxHP) {
				int healAmount = ((com.github.adisann.pokemon.model.Potion) item).getHealAmount();
				int newHP = Math.min(currentHP + healAmount, maxHP);
				player.setCurrentHitpoints(newHP);
				item.useQuantity(1);
				battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
						player.getName() + " was healed for " + (newHP - currentHP) + " HP!", 1.5f));
				battle.queueEvent(new com.github.adisann.pokemon.battle.event.HPAnimationEvent(
						BATTLE_PARTY.PLAYER, currentHP, newHP, maxHP, 0.5f));
				// Opponent gets a turn after using item
				battle.useItemTurn();
				// Controller deactivated so restartTurn is called after events
				controller.setState(BattleScreenController.STATE.DEACTIVATED);
			} else {
				battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
						player.getName() + " is already at full HP!", 1.5f));
			}
		}
	}

	private void handlePartyPokemonSelected(Pokemon pokemon, int index) {
		if (pokemon == battle.getPlayerPokemon()) {
			battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
					pokemon.getName() + " is already out!", 1.5f));
			hideParty();
		} else if (pokemon.isFainted()) {
			battle.queueEvent(new com.github.adisann.pokemon.battle.event.TextEvent(
					pokemon.getName() + " has no energy left!", 1.5f));
			// Stay in party screen
		} else {
			hideParty();
			battle.chooseNewPokemon(pokemon);
		}
	}

	public SubScreen getCurrentSubScreen() {
		return currentSubScreen;
	}
}