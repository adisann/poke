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
import com.github.adisann.pokemon.ui.StartMenu;
import com.github.adisann.pokemon.ui.PartyDisplay;
import com.github.adisann.pokemon.ui.BagDisplay;
import com.github.adisann.pokemon.model.Inventory;
import com.github.adisann.pokemon.model.Item;
import com.github.adisann.pokemon.model.world.cutscene.ScreenFadeEvent;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
 */
public class GameScreen implements AbstractScreen, CutscenePlayer, ScreenFadeEvent.ScreenFadeHandler {

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

	/* Start Menu */
	private StartMenu startMenu;
	private Table startMenuRoot;
	private boolean startMenuOpen = false;

	/* Party Display */
	private PartyDisplay partyDisplay;
	private Table partyDisplayRoot;
	private boolean partyDisplayOpen = false;

	/* Bag Display */
	private BagDisplay bagDisplay;
	private Table bagDisplayRoot;
	private boolean bagDisplayOpen = false;
	private Inventory playerInventory;

	/* Message timer for auto-dismiss */
	private float saveMessageTimer = 0f;

	/* Screen fade state - Pokemon GBA style */
	private boolean isFading = false;
	private float fadeProgress = 0f; // 0.0 = no fade, 1.0 = fully black
	private float fadeStartProgress = 0f;
	private float fadeEndProgress = 1f;
	private float fadeDuration = 0.5f;
	private float fadeElapsed = 0f;
	private static final float FADE_STEPS = 8f; // Number of color steps (GBA style)
	private ShaderProgram fadeShader;
	private ShaderProgram defaultShader;

	public GameScreen() {
	}

	// Track defeated trainers in current session (fallback if save fails)
	private java.util.Set<String> sessionDefeatedTrainers = new java.util.HashSet<>();

	/**
	 * Check if a trainer is defeated (checks both session and save data).
	 */
	public boolean isTrainerDefeated(String trainerId) {
		if (sessionDefeatedTrainers.contains(trainerId)) {
			return true;
		}
		GameSaveData data = getSaveData();
		boolean result = data != null && data.isDefeated(trainerId);
		return result;
	}

	/**
	 * Mark a trainer as defeated in session memory.
	 */
	public void markTrainerDefeated(String trainerId) {
		sessionDefeatedTrainers.add(trainerId);
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

		// Initialize fade shader for Pokemon-style screen transitions
		fadeShader = game.getFadeShader();
		defaultShader = batch.getShader();

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
							// Reload moves for each Pokemon (moves are stored as names in save)
							for (Pokemon p : data.team) {
								p.reloadMoves(game.getMoveDatabase());
							}
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

			// Initialize inventory with starter items
			playerInventory = new Inventory();
			playerInventory.addStarterItems();

			lastPlayerState = player.getMovementState();

			initUI();

			multiplexer = new InputMultiplexer();

			playerController = new ActorMovementController(player);
			dialogueController = new DialogueController(dialogueBox, optionsBox);
			interactionController = new InteractionController(player, dialogueController);
			interactionController.setGameScreen(this); // For trainer battle support
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

		// Apply fade shader when fadeProgress > 0 (keeps screen black between FADE_OUT
		// and FADE_IN)
		// This prevents flickering when transitioning between fade events
		if (fadeProgress > 0f && fadeShader != null) {
			batch.setShader(fadeShader);
			fadeShader.bind();
			fadeShader.setUniformf("u_fadeProgress", fadeProgress);
			fadeShader.setUniformf("u_fadeSteps", FADE_STEPS);
			fadeShader.setUniformf("u_fadeColor", 0f, 0f, 0f); // Black
		} else {
			batch.setShader(defaultShader);
		}

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

		// Reset shader after rendering
		if (fadeProgress > 0f) {
			batch.setShader(defaultShader);
		}

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
			// Start Menu handling (Escape key)
			if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
				// Close any open display first
				if (partyDisplayOpen) {
					closePartyDisplay();
					return;
				}
				if (bagDisplayOpen) {
					closeBagDisplay();
					return;
				}
				// Then handle start menu
				if (startMenuOpen) {
					closeStartMenu();
				} else if (!dialogueBox.isVisible()) {
					openStartMenu();
				}
				return; // Don't process other inputs this frame
			}

			// Start Menu navigation when open
			if (startMenuOpen) {
				if (Gdx.input.isKeyJustPressed(Keys.UP)) {
					startMenu.moveUp();
				} else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
					startMenu.moveDown();
				} else if (Gdx.input.isKeyJustPressed(Keys.Z) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					handleStartMenuSelection();
				} else if (Gdx.input.isKeyJustPressed(Keys.X)) {
					closeStartMenu();
				}
				return; // Don't process other game inputs when menu is open
			}

			// Party Display navigation when open
			if (partyDisplayOpen && partyDisplay != null) {
				if (Gdx.input.isKeyJustPressed(Keys.UP)) {
					partyDisplay.moveUp();
				} else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
					partyDisplay.moveDown();
				} else if (Gdx.input.isKeyJustPressed(Keys.Z) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					if (partyDisplay.isCancelSelected()) {
						closePartyDisplay();
					} else {
						// Selected a Pokemon - for now just close
						System.out.println("[Party] Selected: " + partyDisplay.getSelectedPokemon().getName());
						closePartyDisplay();
					}
				} else if (Gdx.input.isKeyJustPressed(Keys.X) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
					closePartyDisplay();
				}
				return; // Don't process other game inputs when party display is open
			}

			// Bag Display navigation when open
			if (bagDisplayOpen && bagDisplay != null) {
				if (Gdx.input.isKeyJustPressed(Keys.UP)) {
					bagDisplay.moveUp();
				} else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
					bagDisplay.moveDown();
				} else if (Gdx.input.isKeyJustPressed(Keys.Z) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					if (bagDisplay.isCancelSelected()) {
						closeBagDisplay();
					} else {
						// Selected an item - use on first Pokemon
						Item selectedItem = bagDisplay.getSelectedItem();
						if (selectedItem != null && playerTrainer.getTeamSize() > 0) {
							// Check if it's a Pokeball (can't use outside battle)
							if (selectedItem instanceof com.github.adisann.pokemon.model.Pokeball) {
								closeBagDisplay();
								dialogueBox.animateText("Can't use that here!");
								dialogueBox.setVisible(true);
								saveMessageTimer = 2.0f;
							} else {
								boolean used = playerInventory.useItem(bagDisplay.getSelectedIndex(),
										playerTrainer.getPokemon(0));
								if (used) {
									closeBagDisplay();
									dialogueBox.animateText(selectedItem.getName() + " used on " +
											playerTrainer.getPokemon(0).getName() + "!");
									dialogueBox.setVisible(true);
									saveMessageTimer = 2.0f;
								} else {
									closeBagDisplay();
									dialogueBox.animateText("It won't have any effect.");
									dialogueBox.setVisible(true);
									saveMessageTimer = 2.0f;
								}
							}
						}
					}
				} else if (Gdx.input.isKeyJustPressed(Keys.X) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
					closeBagDisplay();
				}
				return; // Don't process other game inputs when bag is open
			}

			// Check inputs for Save/Load (quick save with F5)
			if (Gdx.input.isKeyJustPressed(Keys.F5)) {
				// Load existing save to preserve defeatedTrainers
				GameSaveData data = game.getSaveManager().loadGame(0);
				if (data == null) {
					data = new GameSaveData(world.getName(), player.getX(), player.getY(),
							player.getFacing().name());
				} else {
					data.worldName = world.getName();
					data.playerX = player.getX();
					data.playerY = player.getY();
					data.playerFacing = player.getFacing().name();
				}
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

		// Auto-dismiss save message after timer expires
		if (saveMessageTimer > 0) {
			saveMessageTimer -= delta;
			if (saveMessageTimer <= 0) {
				dialogueBox.setVisible(false);
				saveMessageTimer = 0;
			}
		}

		if (!dialogueBox.isVisible()) {
			camera.update(player.getWorldX() + 0.5f, player.getWorldY() + 0.5f);
			world.update(delta);
		}

		uiStage.act(delta);

		// Update fade progress (Pokemon GBA style)
		if (isFading) {
			fadeElapsed += delta;
			float t = Math.min(1f, fadeElapsed / fadeDuration);
			fadeProgress = fadeStartProgress + (fadeEndProgress - fadeStartProgress) * t;

			if (fadeElapsed >= fadeDuration) {
				fadeProgress = fadeEndProgress;
				isFading = false;
			}
		}
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

		/* START MENU - Pokemon Emerald style (top-right) */
		startMenuRoot = new Table();
		startMenuRoot.setFillParent(true);
		uiStage.addActor(startMenuRoot);

		startMenu = new StartMenu(skin);
		startMenu.setVisible(false);
		startMenuRoot.add(startMenu).expand().align(Align.topRight).pad(10f);

		/* PARTY DISPLAY - Pokemon party screen */
		partyDisplayRoot = new Table();
		partyDisplayRoot.setFillParent(true);
		uiStage.addActor(partyDisplayRoot);

		// PartyDisplay is created when opened (needs trainer reference)

		/* BAG DISPLAY - Item inventory screen */
		bagDisplayRoot = new Table();
		bagDisplayRoot.setFillParent(true);
		uiStage.addActor(bagDisplayRoot);

		// BagDisplay is created when opened (needs inventory reference)
	}

	public PlayerActor getPlayer() {
		return player;
	}

	@Override
	public PlayerActor getPlayerActor() {
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
		battleScreen.setInventory(playerInventory);
		game.setScreen(battleScreen);
	}

	/**
	 * Start a trainer battle with callback for persistence.
	 * 
	 * @param trainerId       Unique ID for the trainer (for save system)
	 * @param opponentTrainer The opponent trainer
	 */
	public void startTrainerBattle(String trainerId, Trainer opponentTrainer) {
		// Ensure opponent Pokemon have moves loaded
		com.github.adisann.pokemon.battle.moves.MoveDatabase moveDb = game.getMoveDatabase();
		for (int i = 0; i < opponentTrainer.getTeamSize(); i++) {
			com.github.adisann.pokemon.model.Pokemon pkmn = opponentTrainer.getPokemon(i);
			// If Pokemon has no moves, give it default moves
			if (pkmn.getMove(0) == null) {
				pkmn.setMove(0, moveDb.getMove("Tackle"));
				pkmn.setMove(1, moveDb.getMove("Scratch"));
			}
		}

		BattleScreen battleScreen = new BattleScreen();
		battleScreen.init(game);
		battleScreen.setTrainerBattleContext(playerTrainer, opponentTrainer, trainerId);
		battleScreen.setInventory(playerInventory);
		battleScreen.setBattleEndCallback((playerWon) -> {
			if (playerWon) {
				// Mark trainer as defeated in session memory (fallback)
				markTrainerDefeated(trainerId);

				// Load existing save data to preserve previous state (including previously
				// defeated trainers)
				GameSaveData data = game.getSaveManager().loadGame(0);
				if (data == null) {
					// No existing save - create new one
					data = new GameSaveData(world.getName(), player.getX(), player.getY(),
							player.getFacing().name());
				} else {
					// Update position in existing save
					data.worldName = world.getName();
					data.playerX = player.getX();
					data.playerY = player.getY();
					data.playerFacing = player.getFacing().name();
				}

				// Update team and mark trainer as defeated
				data.team = playerTrainer.getTeam();
				data.markDefeated(trainerId);
				game.getSaveManager().saveGame(0, data);
				System.out.println("[Trainer Battle] " + trainerId + " marked as defeated! Total defeated: "
						+ data.defeatedTrainers.size());
			}
		});
		game.setScreen(battleScreen);
	}

	/**
	 * Get the player's trainer for NPC interaction.
	 */
	public Trainer getPlayerTrainer() {
		return playerTrainer;
	}

	/**
	 * Get the game's move database.
	 */
	public com.github.adisann.pokemon.battle.moves.MoveDatabase getMoveDatabase() {
		return game.getMoveDatabase();
	}

	/**
	 * Get the current save data for NPC state checking.
	 * Returns null if no save exists or if loading fails.
	 */
	public com.github.adisann.pokemon.save.GameSaveData getSaveData() {
		try {
			return game.getSaveManager().quickLoad();
		} catch (Exception e) {
			// Save file corrupted or incompatible, treat as no save
			System.out.println("[GameScreen] Warning: Could not load save data: " + e.getMessage());
			return null;
		}
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

	@Override
	public void startScreenFade(Color color, float startAlpha, float endAlpha, float duration) {
		System.out
				.println("Starting Pokemon-style fade: " + startAlpha + " -> " + endAlpha + " over " + duration + "s");
		// For shader-based fade, alpha values are treated as fade progress
		// 0 = no fade (normal colors), 1 = full fade (all black)
		this.fadeStartProgress = startAlpha;
		this.fadeEndProgress = endAlpha;
		this.fadeProgress = startAlpha;
		this.fadeDuration = duration;
		this.fadeElapsed = 0f;
		this.isFading = true;
	}

	/* ===== START MENU METHODS ===== */

	private void openStartMenu() {
		startMenuOpen = true;
		startMenu.setVisible(true);
		startMenu.resetSelection();
		// Disable player movement while menu is open
		if (playerController != null) {
			playerController.clearAllInput();
		}
	}

	private void closeStartMenu() {
		startMenuOpen = false;
		startMenu.setVisible(false);
	}

	private void handleStartMenuSelection() {
		StartMenu.MenuOption selected = startMenu.getSelectedOption();
		switch (selected) {
			case POKEMON:
				closeStartMenu();
				openPartyDisplay();
				break;
			case BAG:
				closeStartMenu();
				openBagDisplay();
				break;
			case SAVE:
				// Save game - load existing to preserve defeatedTrainers
				GameSaveData data = game.getSaveManager().loadGame(0);
				if (data == null) {
					data = new GameSaveData(world.getName(), player.getX(), player.getY(),
							player.getFacing().name());
				} else {
					data.worldName = world.getName();
					data.playerX = player.getX();
					data.playerY = player.getY();
					data.playerFacing = player.getFacing().name();
				}
				data.team = playerTrainer.getTeam();
				game.getSaveManager().saveGame(0, data);
				closeStartMenu();
				// Show confirmation message
				dialogueBox.animateText("Game Saved!");
				dialogueBox.setVisible(true);
				saveMessageTimer = 2.0f; // Show for 2 seconds
				break;
			case EXIT:
				closeStartMenu();
				break;
		}
	}

	/* ===== PARTY DISPLAY METHODS ===== */

	private void openPartyDisplay() {
		partyDisplayOpen = true;
		partyDisplayRoot.clearChildren();
		partyDisplay = new PartyDisplay(skin, playerTrainer);
		partyDisplayRoot.add(partyDisplay).expand().center();
		partyDisplay.setVisible(true);
	}

	private void closePartyDisplay() {
		partyDisplayOpen = false;
		if (partyDisplay != null) {
			partyDisplay.setVisible(false);
		}
	}

	/* ===== BAG DISPLAY METHODS ===== */

	private void openBagDisplay() {
		bagDisplayOpen = true;
		bagDisplayRoot.clearChildren();
		bagDisplay = new BagDisplay(skin, playerInventory);
		bagDisplayRoot.add(bagDisplay).expand().center();
		bagDisplay.setVisible(true);
	}

	private void closeBagDisplay() {
		bagDisplayOpen = false;
		if (bagDisplay != null) {
			bagDisplay.setVisible(false);
		}
	}
}