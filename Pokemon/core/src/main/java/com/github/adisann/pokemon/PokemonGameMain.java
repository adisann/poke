package com.github.adisann.pokemon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.github.adisann.pokemon.screen.GameScreen;
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
import com.github.adisann.pokemon.save.SaveManager;
import com.github.adisann.pokemon.worldloader.WorldLoader;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * Main game class - uses standard LibGDX Game/Screen system instead of Autumn
 * MVC views.
 */
public class PokemonGameMain extends Game {

    private AssetManager assetManager;
    private TweenManager tweenManager;
    private MoveDatabase moveDatabase;
    private SaveManager saveManager;
    private ShaderProgram overlayShader;
    private ShaderProgram transitionShader;
    private Skin skin;
    private String version;

    private GameScreen gameScreen;

    @Override
    public void create() {
        // Initialize managers
        assetManager = new AssetManager();
        tweenManager = new TweenManager();
        moveDatabase = new MoveDatabase();
        saveManager = new SaveManager();

        // Read version
        version = Gdx.files.internal("version.txt").readString().trim();
        System.out.println("Pokémon - Kelompok 5, version " + version);
        Gdx.graphics.setTitle("Pokémon - Kelompok 5, version " + version);

        // Initialize shaders
        initShaders();

        // Register tween accessors
        Tween.registerAccessor(BattleAnimation.class, new BattleAnimationAccessor());
        Tween.registerAccessor(BattleSprite.class, new BattleSpriteAccessor());
        Tween.registerAccessor(AnimatedBattleSprite.class, new BattleSpriteAccessor());
        Tween.registerAccessor(BattleBlinkTransition.class, new BattleBlinkTransitionAccessor());

        // Set up asset loaders
        assetManager.setLoader(LWorldObjectDb.class, new LWorldObjectLoader(new InternalFileHandleResolver()));
        assetManager.setLoader(LTerrainDb.class, new LTerrainLoader(new InternalFileHandleResolver()));
        assetManager.setLoader(DialogueDb.class, new DialogueLoader(new InternalFileHandleResolver()));
        assetManager.setLoader(World.class, new WorldLoader(new InternalFileHandleResolver()));

        // Load assets
        loadAssets();
        assetManager.finishLoading();
        System.out.println("Assets loaded: " + assetManager.getLoadedAssets());

        // Generate skin
        skin = SkinGenerator.generateSkin(assetManager);
        System.out.println("Skin generated successfully");

        // Create and show game screen
        gameScreen = new GameScreen();
        gameScreen.init(this);
        setScreen(gameScreen);
    }

    private void initShaders() {
        ShaderProgram.pedantic = false;

        overlayShader = new ShaderProgram(
                Gdx.files.internal("shaders/overlay/vertexshader.txt"),
                Gdx.files.internal("shaders/overlay/fragmentshader.txt"));
        if (!overlayShader.isCompiled()) {
            System.out.println("Overlay shader compilation failed: " + overlayShader.getLog());
            Gdx.app.exit();
        }

        transitionShader = new ShaderProgram(
                Gdx.files.internal("shaders/transition/vertexshader.txt"),
                Gdx.files.internal("shaders/transition/fragmentshader.txt"));
        if (!transitionShader.isCompiled()) {
            System.out.println("Transition shader compilation failed: " + transitionShader.getLog());
            Gdx.app.exit();
        }
        System.out.println("Shaders initialized");
    }

    private void loadAssets() {
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
            if (file.startsWith("worlds/") || file.startsWith("worlds\\")) {
                System.out.println("Loading world " + file);
                String normalizedPath = file.replace("\\", "/");
                assetManager.load(normalizedPath, World.class);
            }
        }
    }

    @Override
    public void render() {
        super.render();
        tweenManager.update(Gdx.graphics.getDeltaTime());
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public TweenManager getTweenManager() {
        return tweenManager;
    }

    public ShaderProgram getOverlayShader() {
        return overlayShader;
    }

    public ShaderProgram getTransitionShader() {
        return transitionShader;
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

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }
}
