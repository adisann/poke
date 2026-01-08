package com.github.adisann.pokemon.worldloader;

import java.io.BufferedReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.TeleportTile;
import com.github.adisann.pokemon.model.Tile;
import com.github.adisann.pokemon.model.world.Door;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.model.world.WorldObject;
import com.github.adisann.pokemon.model.actor.NPCActor;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.battle.Trainer;
import com.github.adisann.pokemon.util.AnimationSet;

/**
 * Small loader to load World.class into AssetManager.
 */
public class WorldLoader extends AsynchronousAssetLoader<World, WorldLoader.WorldParameter> {

	private World world;

	private Animation<TextureRegion> flowerAnimation;
	private Animation<TextureRegion> doorOpen;
	private Animation<TextureRegion> doorClose;

	public WorldLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager asman, String filename, FileHandle file, WorldParameter parameter) {
		TextureAtlas atlas = asman.get("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);

		flowerAnimation = new Animation<TextureRegion>(0.8f, atlas.findRegions("flowers"), PlayMode.LOOP_PINGPONG);
		doorOpen = new Animation<TextureRegion>(0.8f / 4f, atlas.findRegions("woodenDoor"), PlayMode.NORMAL);
		doorClose = new Animation<TextureRegion>(0.5f / 4f, atlas.findRegions("woodenDoor"), PlayMode.REVERSED);

		BufferedReader reader = new BufferedReader(file.reader());
		int currentLine = 0;
		try {
			while (reader.ready()) {
				String line = reader.readLine();
				currentLine++;

				// header of file
				if (currentLine == 1) {
					String[] tokens = line.split("\\s+");
					world = new World(tokens[0], Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]),
							Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
					continue;
				}

				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}

				// functions
				String[] tokens = line.split("\\s+");
				switch (tokens[0]) {
					case "fillTerrain":
						fillTerrain(asman, tokens[1]);
						break;
					case "setTerrain":
						setTerrain(asman, tokens[1], tokens[2], tokens[3]);
						break;
					case "addFlowers":
						addFlowers(tokens[1], tokens[2]);
						break;
					case "addRug":
						addRug(asman, tokens[1], tokens[2]);
						break;
					case "addObj":
						addGameWorldObject(asman, tokens[1], tokens[2], tokens[3]);
						break;
					case "addTree":
						addGameWorldObject(asman, tokens[1], tokens[2], "BIG_TREE");
						break;
					case "addDoor":
						addDoor(tokens[1], tokens[2]);
						break;
					case "teleport":
						teleport(asman, tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7],
								tokens[8]);
						break;
					case "unwalkable":
						unwalkable(tokens[1], tokens[2]);
						break;
					case "addNPC":
						addNPC(asman, tokens[1], tokens[2], tokens[3], tokens[4]);
						break;
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading world file: " + filename);
			e.printStackTrace();
			Gdx.app.exit();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// Ignore close errors
			}
		}
	}

	private void fillTerrain(AssetManager asman, String terrain) {
		LTerrainDb terrainDb = asman.get("LTerrain.xml", LTerrainDb.class);
		LTerrain t = terrainDb.getLTerrain(terrain);

		for (int x = 0; x < world.getMap().getWidth(); x++) {
			for (int y = 0; y < world.getMap().getHeight(); y++) {
				world.getMap().setTile(new Tile(t), x, y);
			}
		}
	}

	private void setTerrain(AssetManager asman, String x, String y, String terrain) {
		LTerrainDb terrainDb = asman.get("LTerrain.xml", LTerrainDb.class);
		LTerrain t = terrainDb.getLTerrain(terrain);

		int ix = Integer.parseInt(x);
		int iy = Integer.parseInt(y);
		world.getMap().getTile(ix, iy).setTerrain(t);
	}

	private void addFlowers(String sx, String sy) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);

		GridPoint2[] gridArray = new GridPoint2[1];
		gridArray[0] = new GridPoint2(0, 0);
		WorldObject flowers = new WorldObject(x, y, true, flowerAnimation, 1f, 1f, gridArray);
		world.addObject(flowers);
	}

	private void addRug(AssetManager assetManager, String sx, String sy) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);

		TextureAtlas atlas = assetManager.get("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);
		TextureRegion rugRegion = atlas.findRegion("rug");
		GridPoint2[] gridArray = new GridPoint2[3 * 2];
		gridArray[0] = new GridPoint2(0, 0);
		gridArray[1] = new GridPoint2(0, 1);
		gridArray[2] = new GridPoint2(0, 2);
		gridArray[3] = new GridPoint2(1, 0);
		gridArray[4] = new GridPoint2(1, 1);
		gridArray[5] = new GridPoint2(1, 2);
		WorldObject rug = new WorldObject(x, y, true, rugRegion, 3f, 2f, gridArray);
		world.addObject(rug);
	}

	/**
	 * Adds a non-walkable game object to the World.
	 * 
	 * @param assetManager
	 * @param sx
	 * @param sy
	 * @param stype
	 */
	private void addGameWorldObject(AssetManager assetManager, String sx, String sy, String stype) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);

		LWorldObjectDb objDb = assetManager.get("LWorldObjects.xml", LWorldObjectDb.class);
		LWorldObject obj = objDb.getLWorldObject(stype);

		TextureAtlas atlas = assetManager.get("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);
		TextureRegion objRegion = atlas.findRegion(obj.getImageName());

		WorldObject worldObj = new WorldObject(x, y, false, objRegion, obj.getSizeX(), obj.getSizeY(), obj.getTiles());
		world.addObject(worldObj);
	}

	private void teleport(AssetManager asman, String sx, String sy, String sterrain, String stargetWorld,
			String stargetX, String stargetY, String stargetDir, String stransitionColor) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);

		int targetX = Integer.parseInt(stargetX);
		int targetY = Integer.parseInt(stargetY);

		LTerrainDb terrainDb = asman.get("LTerrain.xml", LTerrainDb.class);
		LTerrain t = terrainDb.getLTerrain(sterrain);

		DIRECTION targetDir = DIRECTION.valueOf(stargetDir);

		Color transitionColor;
		switch (stransitionColor) {
			case "WHITE":
				transitionColor = Color.WHITE;
				break;
			case "BLACK":
				transitionColor = Color.BLACK;
				break;
			default:
				transitionColor = Color.BLACK;
				break;
		}

		// Pass tile coordinates (x, y) so TeleportTile can calculate movement direction
		TeleportTile tile = new TeleportTile(t, x, y, stargetWorld, targetX, targetY, targetDir, transitionColor);

		// Add conditions based on destination (replaces hard-coded checks)
		if (stargetWorld.equals("oldale_town")) {
			tile.addCondition(new com.github.adisann.pokemon.model.portal.PartyNotFaintedCondition(
					"Your Pokemon is fainted. You cannot enter Oldale."));
		}

		// Add actions based on destination (replaces hard-coded actions)
		if (stargetWorld.equals("test_map_indoor")) {
			tile.addAction(new com.github.adisann.pokemon.model.portal.HealPartyAction());
		}

		world.getMap().setTile(tile, x, y);
	}

	private void unwalkable(String sx, String sy) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);
		world.getMap().getTile(x, y).setWalkable(false);
	}

	private void addDoor(String sx, String sy) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);
		Door door = new Door(x, y, doorOpen, doorClose);
		world.addObject(door);
	}

	/**
	 * Add an NPC actor to the world.
	 * Format: addNPC x y npcId facing
	 * 
	 * @param asman   Asset manager
	 * @param sx      X coordinate
	 * @param sy      Y coordinate
	 * @param npcId   NPC identifier (e.g., "may")
	 * @param sfacing Direction NPC faces (NORTH, SOUTH, EAST, WEST)
	 */
	private void addNPC(AssetManager asman, String sx, String sy, String npcId, String sfacing) {
		int x = Integer.parseInt(sx);
		int y = Integer.parseInt(sy);
		com.github.adisann.pokemon.model.DIRECTION facing = com.github.adisann.pokemon.model.DIRECTION.valueOf(sfacing);

		AnimationSet npcAnimations;

		// Create NPC-specific animations
		if (npcId.equals("may")) {
			// Load May's overworld sprite from AssetManager (pre-loaded as dependency)
			com.badlogic.gdx.graphics.Texture mayTexture = asman.get(
					"graphics/trainers/train_may_overworld.png", com.badlogic.gdx.graphics.Texture.class);
			TextureRegion mayRegion = new TextureRegion(mayTexture);

			// Create single-frame animations (NPC stands still)
			com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> staticAnim = new com.badlogic.gdx.graphics.g2d.Animation<>(
					1f, mayRegion);

			npcAnimations = new AnimationSet(
					staticAnim, staticAnim, staticAnim, staticAnim, // walk animations (all same for now)
					mayRegion, mayRegion, mayRegion, mayRegion); // stand sprites (all directions)
		} else {
			// Default: use Brendan sprites from atlas
			TextureAtlas atlas = asman.get("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);
			npcAnimations = new AnimationSet(
					new com.badlogic.gdx.graphics.g2d.Animation<>(0.4f / 2f, atlas.findRegions("brendan_walk_north"),
							com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_PINGPONG),
					new com.badlogic.gdx.graphics.g2d.Animation<>(0.4f / 2f, atlas.findRegions("brendan_walk_south"),
							com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_PINGPONG),
					new com.badlogic.gdx.graphics.g2d.Animation<>(0.4f / 2f, atlas.findRegions("brendan_walk_east"),
							com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_PINGPONG),
					new com.badlogic.gdx.graphics.g2d.Animation<>(0.4f / 2f, atlas.findRegions("brendan_walk_west"),
							com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_PINGPONG),
					atlas.findRegion("brendan_stand_north"),
					atlas.findRegion("brendan_stand_south"),
					atlas.findRegion("brendan_stand_east"),
					atlas.findRegion("brendan_stand_west"));
		}

		NPCActor npc = new NPCActor(world, x, y, npcAnimations);
		npc.setId(npcId);
		npc.refaceWithoutAnimation(facing);

		// Configure NPC based on ID
		if (npcId.equals("may")) {
			npc.setDisplayName("May");
			npc.setDialogueBeforeBattle("Hey there! I've been waiting for you. How about a Pokemon battle?");
			npc.setDialogueAfterBattle("That was a great battle! You're really strong!");

			// Create May's trainer with Torchic (using Charmander sprite as placeholder)
			// Pokemon is created without moves - moves will be loaded at battle start
			java.util.Map<com.github.adisann.pokemon.battle.STAT, Integer> torchicStats = new java.util.HashMap<>();
			torchicStats.put(com.github.adisann.pokemon.battle.STAT.HITPOINTS, 45);
			torchicStats.put(com.github.adisann.pokemon.battle.STAT.ATTACK, 60);
			torchicStats.put(com.github.adisann.pokemon.battle.STAT.DEFENCE, 40);
			torchicStats.put(com.github.adisann.pokemon.battle.STAT.SPECIAL_ATTACK, 70);
			torchicStats.put(com.github.adisann.pokemon.battle.STAT.SPECIAL_DEFENCE, 50);
			torchicStats.put(com.github.adisann.pokemon.battle.STAT.SPEED, 45);

			java.util.List<com.github.adisann.pokemon.model.Type> torchicTypes = new java.util.ArrayList<>();
			torchicTypes.add(com.github.adisann.pokemon.model.Type.FIRE);

			com.github.adisann.pokemon.model.PokemonSpecies charmanderSpec = new com.github.adisann.pokemon.model.PokemonSpecies(
					"Charmander", torchicStats, torchicTypes, 62, "graphics/pokemon/charmander.png");

			Pokemon charmander = new Pokemon(charmanderSpec, 5);
			Trainer mayTrainer = new Trainer(charmander);
			mayTrainer.setSpriteName("graphics/trainers/trainer_may.png");
			npc.setTrainer(mayTrainer);
		}

		world.addActor(npc);

		// Set NPC on tile to block walking through
		world.getMap().getTile(x, y).setActor(npc);
	}

	@Override
	public World loadSync(AssetManager arg0, String arg1, FileHandle arg2, WorldParameter arg3) {
		return world;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Array<AssetDescriptor> getDependencies(String filename, FileHandle file, WorldParameter parameter) {
		Array<AssetDescriptor> ad = new Array<AssetDescriptor>();
		ad.add(new AssetDescriptor("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class));
		ad.add(new AssetDescriptor("LWorldObjects.xml", LWorldObjectDb.class));
		ad.add(new AssetDescriptor("LTerrain.xml", LTerrainDb.class));
		// NPC sprite dependencies
		ad.add(new AssetDescriptor("graphics/trainers/train_may_overworld.png",
				com.badlogic.gdx.graphics.Texture.class));
		ad.add(new AssetDescriptor("graphics/trainers/trainer_may.png",
				com.badlogic.gdx.graphics.Texture.class));
		return ad;
	}

	static public class WorldParameter extends AssetLoaderParameters<World> {
	}
}