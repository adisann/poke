package com.github.adisann.pokemon.screen;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.adisann.pokemon.PokemonGameMain;
import com.github.adisann.pokemon.controller.ActorMovementController;
import com.github.adisann.pokemon.controller.DialogueController;
import com.github.adisann.pokemon.controller.InteractionController;
import com.github.adisann.pokemon.controller.OptionBoxController;
import com.github.adisann.pokemon.dialogue.Dialogue;
import com.github.adisann.pokemon.dialogue.LinearDialogueNode;
import com.github.adisann.pokemon.model.Camera;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.PlayerActor;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.model.world.cutscene.CutsceneEvent;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;
import com.github.adisann.pokemon.screen.renderer.EventQueueRenderer;
import com.github.adisann.pokemon.screen.renderer.TileInfoRenderer;
import com.github.adisann.pokemon.screen.renderer.WorldRenderer;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.OptionBox;
import com.github.adisann.pokemon.util.Action;
import com.github.adisann.pokemon.util.Action;
import com.github.adisann.pokemon.util.AnimationSet;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.battle.Trainer;
import com.github.adisann.pokemon.battle.EncounterManager;
import com.github.adisann.pokemon.save.SaveManager;
import com.github.adisann.pokemon.save.GameSaveData;
import com.github.adisann.pokemon.model.actor.Actor.MOVEMENT_STATE;
import com.github.adisann.pokemon.model.actor.Actor.MOVEMENT_MODE;

/**
 * Main game screen using standard LibGDX Screen interface.
 * */
public class GameScreen implements AbstractScreen, CutscenePlayer {

	private PokemonGameMain game;
	private AssetManager assetManager;
	private Skin skin;

	private InputMultiplexer multiplexer;
	private DialogueController dialogueController;
	private ActorMovementController playerController;
	private InteractionController interactionController;
	private OptionBoxController debugController;

	private EncounterManager encounterManager;
	private Trainer playerTrainer;
	private MOVEMENT_STATE lastPlayerState;

	private HashMap<String, World> worlds = new HashMap<String, World>();
	private World world;
	private PlayerActor player;
	private Camera camera;
	private Dialogue dialogue;

	/* cutscenes */
	private Queue<CutsceneEvent> eventQueue = new ArrayDeque<CutsceneEvent>();
	private CutsceneEvent currentEvent;

	private SpriteBatch batch;
	private Viewport gameViewport;

	private WorldRenderer worldRenderer;
	private EventQueueRenderer queueRenderer;
	private TileInfoRenderer tileInfoRenderer;
	private boolean renderTileInfo = false;

	private int uiScale = 2;

	private Stage uiStage;
	private Table dialogRoot;
	private Table menuRoot;
	private DialogueBox dialogueBox;
	private OptionBox optionsBox;
	private OptionBox debugBox;

	public GameScreen() {
	}

	/**
	 * Initialize the screen with the game reference.
	 */
	public void init(PokemonGameMain game) {
		this.game = game;
		this.assetManager = game.getAssetManager();
		this.skin = game.getSkin();
		this.encounterManager = new EncounterManager(game.getMoveDatabase());
	}

	@Override
	public void show() {
		gameViewport = new ScreenViewport();
		batch = new SpriteBatch();

		TextureAtlas atlas = assetManager.get("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);

		AnimationSet animations = new AnimationSet(
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_walk_north"),
						PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_walk_south"),
						PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_walk_east"), PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_walk_west"), PlayMode.LOOP_PINGPONG),
				atlas.findRegion("brendan_stand_north"),
				atlas.findRegion("brendan_stand_south"),
				atlas.findRegion("brendan_stand_east"),
				atlas.findRegion("brendan_stand_west"));
		animations.addBiking(
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_bike_north"),
						PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_bike_south"),
						PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_bike_east"), PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.4f / 2f, atlas.findRegions("brendan_bike_west"),
						PlayMode.LOOP_PINGPONG));
		animations.addRunning(
				new Animation<TextureRegion>(0.25f / 2f, atlas.findRegions("brendan_run_north"),
						PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.25f / 2f, atlas.findRegions("brendan_run_south"),
						PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.25f / 2f, atlas.findRegions("brendan_run_east"), PlayMode.LOOP_PINGPONG),
				new Animation<TextureRegion>(0.25f / 2f, atlas.findRegions("brendan_run_west"),
						PlayMode.LOOP_PINGPONG));

		Array<World> loadedWorlds = assetManager.getAll(World.class, new Array<World>());
		System.out.println("Loaded " + loadedWorlds.size + " worlds:");
		for (World w : loadedWorlds) {
			System.out.println("  - " + w.getName());
			worlds.put(w.getName(), w);
		}
		if (player == null) {
			world = worlds.get("littleroot_town");
			camera = new Camera();
			player = new PlayerActor(world, world.getSafeX(), world.getSafeY(), animations, this);
			world.addActor(player);
		} else {
			if (!world.getActors().contains(player)) {
				world.addActor(player);
			}
		}

		if (playerController == null) {
			// First time init
			// Load Game Logic or New Game
			if (game.getSaveManager().hasSaveGame(0)) {
				try {
					GameSaveData data = game.getSaveManager().loadGame(0);
					if (data != null && worlds.containsKey(data.worldName)) {
						world.removeActor(player); // remove from initial world
						world = worlds.get(data.worldName);
						player = new PlayerActor(world, data.playerX, data.playerY, animations, this);
						player.refaceWithoutAnimation(DIRECTION.valueOf(data.playerFacing));
						world.addActor(player);

						// Reconstruct Trainer
						if (data.team != null && !data.team.isEmpty()) {
							playerTrainer = new Trainer(data.team.get(0));
							for (int i = 1; i < data.team.size(); i++) {
								playerTrainer.addPokemon(data.team.get(i));
							}
						} else {
							// Fallback starter
							playerTrainer = new Trainer(
									Pokemon.generatePokemon("Bulba", "graphics/pokemon/bulbasaur.png",
											game.getMoveDatabase()));
						}
						System.out.println("Loaded Save Game from Slot 0");
					}
				} catch (Exception e) {
					System.err.println("Failed to load save: " + e.getMessage());
					e.printStackTrace();
					// Fallback
					playerTrainer = new Trainer(
							Pokemon.generatePokemon("Bulba", "graphics/pokemon/bulbasaur.png", game.getMoveDatabase()));
				}
			} else {
				// New Game
				playerTrainer = new Trainer(
						Pokemon.generatePokemon("Bulba", "graphics/pokemon/bulbasaur.png", game.getMoveDatabase()));
			}

			lastPlayerState = player.getMovementState();

			initUI();

			multiplexer = new InputMultiplexer();

			playerController = new ActorMovementController(player);
			dialogueController = new DialogueController(dialogueBox, optionsBox);
			interactionController = new InteractionController(player, dialogueController);
			debugController = new OptionBoxController(debugBox);
			debugController.addAction(new Action() {
				@Override
				public void action() {
					renderTileInfo = !renderTileInfo;
				}
			}, "Toggle show coords");

			multiplexer.addProcessor(0, debugController);
			multiplexer.addProcessor(1, dialogueController);
			multiplexer.addProcessor(2, playerController);
			multiplexer.addProcessor(3, interactionController);

			worldRenderer = new WorldRenderer(assetManager, world);
			queueRenderer = new EventQueueRenderer(skin, eventQueue);
			tileInfoRenderer = new TileInfoRenderer(world, camera);
		}

		Gdx.input.setInputProcessor(multiplexer);

		// Clear any held key states to prevent automatic walking after battle
		if (playerController != null) {
			playerController.clearAllInput();
		}

		System.out.println("GameScreen.show() completed - game should be visible now");

	}

	@Override
	public void render(float delta) {
		// Clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		update(delta);
		gameViewport.apply();
		batch.begin();
		worldRenderer.render(batch, camera);
		queueRenderer.render(batch, currentEvent);
		if (renderTileInfo) {
			tileInfoRenderer.render(batch, Gdx.input.getX(), Gdx.input.getY());
		}

		// Debug player pos
		// System.out.println("Player: " + player.getWorldX() + "," + player.getWorldY()
		// + " Vis: " + player.isVisible());

		batch.end();

		uiStage.draw();
	}

	@Override
	public void resize(int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		uiStage.getViewport().update(width / uiScale, height / uiScale, true);
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
		while (currentEvent == null || currentEvent.isFinished()) {
			// Check inputs for Save/Load
			if (Gdx.input.isKeyJustPressed(Keys.F5)) {
				GameSaveData data = new GameSaveData(world.getName(), player.getX(), player.getY(),
						player.getFacing().name());
				data.team = playerTrainer.getTeam();
				game.getSaveManager().saveGame(0, data);
				// TODO: Show 'Game Saved' message
			}
			if (Gdx.input.isKeyJustPressed(Keys.F6)) {
				// Quick Reload
				// For now, simpler to just logging
				System.out
						.println("Quick Load triggered - restart game to load properly or implement full state reset");
			}

			if (eventQueue.peek() == null) {
				currentEvent = null;
				break;
			} else {
				currentEvent = eventQueue.poll();
				currentEvent.begin(this);
			}
		}

		if (currentEvent != null) {
			currentEvent.update(delta);
		}

		if (currentEvent == null) {
			playerController.update(delta);

			// Detect Step Finish
			if (lastPlayerState == MOVEMENT_STATE.MOVING && player.getMovementState() == MOVEMENT_STATE.STILL) {
				// Step just finished
				Pokemon wild = encounterManager.checkEncounter(world, player.getX(), player.getY());
				if (wild != null) {
					startBattle(wild);
				}
			}
			lastPlayerState = player.getMovementState();
		}

		dialogueController.update(delta);

		if (!dialogueBox.isVisible()) {
			camera.update(player.getWorldX() + 0.5f, player.getWorldY() + 0.5f);
			world.update(delta);
		}

		uiStage.act(delta);
	}

	private void initUI() {
		uiStage = new Stage(new ScreenViewport());
		uiStage.getViewport().update(Gdx.graphics.getWidth() / uiScale, Gdx.graphics.getHeight() / uiScale, true);

		dialogRoot = new Table();
		dialogRoot.setFillParent(true);
		uiStage.addActor(dialogRoot);

		dialogueBox = new DialogueBox(skin);
		dialogueBox.setVisible(false);

		optionsBox = new OptionBox(skin);
		optionsBox.setVisible(false);

		Table dialogTable = new Table();
		dialogTable.add(optionsBox).expand().align(Align.right).space(8f).row();
		dialogTable.add(dialogueBox).expand().align(Align.bottom).space(8f);

		dialogRoot.add(dialogTable).expand().align(Align.bottom);

		menuRoot = new Table();
		menuRoot.setFillParent(true);
		uiStage.addActor(menuRoot);

		debugBox = new OptionBox(skin);
		debugBox.addOption("Toggle show coords");
		debugBox.setVisible(false);

		menuRoot.add(debugBox).expand().align(Align.topLeft);
	}

	public PlayerActor getPlayer() {
		return player;
	}

	public void changeWorld(World newWorld, int x, int y, DIRECTION facing) {
		System.out.println("Changing to world: " + newWorld.getName() + " at " + x + "," + y);
		player.changeWorld(newWorld, x, y);
		this.world = newWorld;
		player.refaceWithoutAnimation(facing);
		this.worldRenderer = new WorldRenderer(assetManager, world);
		this.tileInfoRenderer = new TileInfoRenderer(world, camera);
		// Reset camera to player position
		camera.update(player.getWorldX() + 0.5f, player.getWorldY() + 0.5f);
	}

	@Override
	public void changeLocation(World newWorld, int x, int y, DIRECTION facing, Color color) {
		System.out.println("changeLocation called for: " + (newWorld != null ? newWorld.getName() : "null"));
		if (newWorld != null) {
			changeWorld(newWorld, x, y, facing);
		} else {
			System.out.println("ERROR: newWorld is null!");
		}
	}

	@Override
	public World getWorld(String worldName) {
		return worlds.get(worldName);
	}

	@Override
	public void queueEvent(CutsceneEvent event) {
		eventQueue.add(event);
	}

	@Override
	public void healPlayerTeam() {
		if (playerTrainer != null) {
			playerTrainer.healAll();
			// Show healing message via dialogue
			dialogue = new Dialogue();
			dialogue.addNode(new LinearDialogueNode("Your Pokemon were fully healed!", 0));
			dialogueController.startDialogue(dialogue);
		}
	}

	/**
	 * Teleports the player to the first town (littleroot_town) spawn point.
	 * Called after losing a battle.
	 */
	public void teleportToFirstTown() {
		World firstTown = worlds.get("littleroot_town");
		if (firstTown != null && player != null) {
			// Use changeWorld to properly relocate player
			changeWorld(firstTown, firstTown.getSafeX(), firstTown.getSafeY(), DIRECTION.SOUTH);
		}
	}

	/**
	 * Checks if player's first Pokemon has fainted (HP = 0).
	 */
	public boolean isPlayerPokemonFainted() {
		if (playerTrainer != null && playerTrainer.getTeamSize() > 0) {
			return playerTrainer.getPokemon(0).isFainted();
		}
		return false;
	}

	private void startBattle(Pokemon wild) {
		BattleScreen battleScreen = new BattleScreen();
		battleScreen.init(game);
		battleScreen.setBattleContext(playerTrainer, wild);
		game.setScreen(battleScreen);
	}

	@Override
	public void showDialogue(String text) {
		if (dialogueBox != null && text != null) {
			dialogueBox.setVisible(true);
			dialogueBox.animateText(text);
		}
	}

	@Override
	public void hideDialogue() {
		if (dialogueBox != null) {
			dialogueBox.setVisible(false);
		}
	}
}