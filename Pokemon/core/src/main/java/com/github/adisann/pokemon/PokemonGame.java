package com.github.adisann.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.adisann.pokemon.battle.animation.AnimatedBattleSprite;
import com.github.adisann.pokemon.battle.animation.BattleAnimation;
import com.github.adisann.pokemon.battle.animation.BattleAnimationAccessor;
import com.github.adisann.pokemon.battle.animation.BattleSprite;
import com.github.adisann.pokemon.battle.animation.BattleSpriteAccessor;
import com.github.adisann.pokemon.battle.moves.MoveDatabase;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.screen.AbstractScreen;
import com.github.adisann.pokemon.screen.TransitionScreen;
import com.github.adisann.pokemon.screen.transition.BattleBlinkTransition;
import com.github.adisann.pokemon.screen.transition.BattleBlinkTransitionAccessor;
import com.github.adisann.pokemon.screen.transition.Transition;
import com.github.adisann.pokemon.util.Action;
import com.github.adisann.pokemon.util.SkinGenerator;
import com.github.adisann.pokemon.worldloader.DialogueDb;
import com.github.adisann.pokemon.worldloader.DialogueLoader;
import com.github.adisann.pokemon.worldloader.LTerrainDb;
import com.github.adisann.pokemon.worldloader.LTerrainLoader;
import com.github.adisann.pokemon.worldloader.LWorldObjectDb;
import com.github.adisann.pokemon.worldloader.LWorldObjectLoader;
import com.github.adisann.pokemon.worldloader.WorldLoader;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

@Component
public class PokemonGame {

	@Inject
	private TransitionScreen transitionScreen;
	@Inject
	private AssetManager assetManager;
	@Inject
	private TweenManager tweenManager;
	@Inject
	private AppConfig appConfig;
	@Inject
	private MoveDatabase moveDatabase;

	private String version;
	private Skin skin;

	@Initiate(priority = com.github.czyzby.autumn.mvc.config.AutumnActionPriority.LOW_PRIORITY)
	public void init() {
		version = Gdx.files.internal("version.txt").readString().trim();
		System.out.println("Pokémon - Kelompok 5, version " + version);
		Gdx.app.getGraphics().setTitle("Pokémon - Kelompok 5, version " + version);

		Tween.registerAccessor(BattleAnimation.class, new BattleAnimationAccessor());
		Tween.registerAccessor(BattleSprite.class, new BattleSpriteAccessor());
		Tween.registerAccessor(AnimatedBattleSprite.class, new BattleSpriteAccessor());
		Tween.registerAccessor(BattleBlinkTransition.class, new BattleBlinkTransitionAccessor());

		assetManager.setLoader(LWorldObjectDb.class, new LWorldObjectLoader(new InternalFileHandleResolver()));
		assetManager.setLoader(LTerrainDb.class, new LTerrainLoader(new InternalFileHandleResolver()));
		assetManager.setLoader(DialogueDb.class, new DialogueLoader(new InternalFileHandleResolver()));
		assetManager.setLoader(World.class, new WorldLoader(new InternalFileHandleResolver()));

		assetManager.load("LTerrain.xml", LTerrainDb.class);
		assetManager.load("LWorldObjects.xml", LWorldObjectDb.class);
		assetManager.load("Dialogues.xml", DialogueDb.class);

		assetManager.load("graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);
		assetManager.load("graphics_packed/ui/uipack.atlas", TextureAtlas.class);
		assetManager.load("graphics_packed/battle/battlepack.atlas", TextureAtlas.class);
		assetManager.load("graphics/pokemon/bulbasaur.png", Texture.class);
		assetManager.load("graphics/pokemon/slowpoke.png", Texture.class);

		for (int i = 0; i < 32; i++) {
			assetManager.load("graphics/statuseffect/attack_" + i + ".png", Texture.class);
		}
		assetManager.load("graphics/statuseffect/white.png", Texture.class);

		for (int i = 0; i < 13; i++) {
			assetManager.load("graphics/transitions/transition_" + i + ".png", Texture.class);
		}
		assetManager.load("font/small_letters_font.fnt", BitmapFont.class);

		String assetFile = Gdx.files.internal("assets.txt").readString();
		String[] assetFiles = assetFile.split("\\r?\\n");
		for (String file : assetFiles) {
			// Use forward slash for cross-platform compatibility
			if (file.startsWith("worlds/") || file.startsWith("worlds\\")) {
				System.out.println("Loading world " + file);
				// Normalize path separators
				String normalizedPath = file.replace("\\", "/");
				assetManager.load(normalizedPath, World.class);
			}
		}

		assetManager.finishLoading();
		System.out.println("Assets loaded: " + assetManager.getLoadedAssets());
		System.out.println("Tilepack loaded: " + assetManager.isLoaded("graphics_packed/tiles/tilepack.atlas"));

		// Generate skin AFTER assets are loaded
		skin = SkinGenerator.generateSkin(assetManager);
		System.out.println("Skin generated successfully");
	}

	public void startTransition(AbstractScreen from, AbstractScreen to, Transition out, Transition in, Action action) {
		transitionScreen.startTransition(from, to, out, in, action);
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public TweenManager getTweenManager() {
		return tweenManager;
	}

	public ShaderProgram getOverlayShader() {
		return appConfig.getOverlayShader();
	}

	public ShaderProgram getTransitionShader() {
		return appConfig.getTransitionShader();
	}

	public MoveDatabase getMoveDatabase() {
		return moveDatabase;
	}

	public String getVersion() {
		return version;
	}

	public Skin getSkin() {
		return skin;
	}
}
