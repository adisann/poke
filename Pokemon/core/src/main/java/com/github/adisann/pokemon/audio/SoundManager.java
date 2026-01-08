package com.github.adisann.pokemon.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Manages all game audio including music and sound effects.
 * 
 * PLACEHOLDER SETUP: Audio files are not included.
 * To add audio:
 * 1. Place audio files in: assets/audio/
 * 2. Update the path constants below
 * 3. Supported formats: .mp3, .ogg, .wav
 * 
 * Example file structure:
 *   assets/audio/music/battle.ogg
 *   assets/audio/sfx/hit.ogg
 *   assets/audio/sfx/select.ogg
 */
public class SoundManager {
    
    // ===== AUDIO FILE PATHS (UPDATE THESE) =====
    // Music
    private static final String BATTLE_MUSIC_PATH = "audio/music/battle.ogg";
    private static final String VICTORY_MUSIC_PATH = "audio/music/victory.ogg";
    
    // Sound Effects
    private static final String HIT_SFX_PATH = "audio/sfx/hit.ogg";
    private static final String SELECT_SFX_PATH = "audio/sfx/select.ogg";
    private static final String ATTACK_SFX_PATH = "audio/sfx/attack.ogg";
    private static final String POKEBALL_SFX_PATH = "audio/sfx/pokeball.ogg";
    
    // ===== AUDIO OBJECTS =====
    private Music battleMusic;
    private Music victoryMusic;
    private Sound hitSound;
    private Sound selectSound;
    private Sound attackSound;
    private Sound pokeballSound;
    
    // ===== VOLUME SETTINGS =====
    private float musicVolume = 0.5f;
    private float sfxVolume = 0.7f;
    private boolean audioEnabled = true;
    
    // Track if files exist (to avoid repeated error logs)
    private boolean battleMusicLoaded = false;
    private boolean sfxLoaded = false;
    
    /**
     * Initialize the sound manager.
     * Call this after LibGDX is initialized.
     */
    public void initialize() {
        loadMusic();
        loadSoundEffects();
    }
    
    private void loadMusic() {
        try {
            if (Gdx.files.internal(BATTLE_MUSIC_PATH).exists()) {
                battleMusic = Gdx.audio.newMusic(Gdx.files.internal(BATTLE_MUSIC_PATH));
                battleMusic.setLooping(true);
                battleMusicLoaded = true;
            } else {
                System.out.println("[SoundManager] Battle music not found: " + BATTLE_MUSIC_PATH);
            }
            
            if (Gdx.files.internal(VICTORY_MUSIC_PATH).exists()) {
                victoryMusic = Gdx.audio.newMusic(Gdx.files.internal(VICTORY_MUSIC_PATH));
                victoryMusic.setLooping(false);
            }
        } catch (Exception e) {
            System.out.println("[SoundManager] Could not load music: " + e.getMessage());
        }
    }
    
    private void loadSoundEffects() {
        try {
            if (Gdx.files.internal(HIT_SFX_PATH).exists()) {
                hitSound = Gdx.audio.newSound(Gdx.files.internal(HIT_SFX_PATH));
                sfxLoaded = true;
            }
            if (Gdx.files.internal(SELECT_SFX_PATH).exists()) {
                selectSound = Gdx.audio.newSound(Gdx.files.internal(SELECT_SFX_PATH));
            }
            if (Gdx.files.internal(ATTACK_SFX_PATH).exists()) {
                attackSound = Gdx.audio.newSound(Gdx.files.internal(ATTACK_SFX_PATH));
            }
            if (Gdx.files.internal(POKEBALL_SFX_PATH).exists()) {
                pokeballSound = Gdx.audio.newSound(Gdx.files.internal(POKEBALL_SFX_PATH));
            }
            
            if (!sfxLoaded) {
                System.out.println("[SoundManager] No SFX files found. Add .ogg files to assets/audio/sfx/");
            }
        } catch (Exception e) {
            System.out.println("[SoundManager] Could not load SFX: " + e.getMessage());
        }
    }
    
    // ===== MUSIC CONTROLS =====
    
    public void playBattleMusic() {
        if (!audioEnabled || battleMusic == null) return;
        battleMusic.setVolume(musicVolume);
        battleMusic.play();
    }
    
    public void stopBattleMusic() {
        if (battleMusic != null && battleMusic.isPlaying()) {
            battleMusic.stop();
        }
    }
    
    public void playVictoryMusic() {
        stopBattleMusic();
        if (!audioEnabled || victoryMusic == null) return;
        victoryMusic.setVolume(musicVolume);
        victoryMusic.play();
    }
    
    // ===== SOUND EFFECT CONTROLS =====
    
    public void playHitSound() {
        if (!audioEnabled || hitSound == null) return;
        hitSound.play(sfxVolume);
    }
    
    public void playSelectSound() {
        if (!audioEnabled || selectSound == null) return;
        selectSound.play(sfxVolume);
    }
    
    public void playAttackSound() {
        if (!audioEnabled || attackSound == null) return;
        attackSound.play(sfxVolume);
    }
    
    public void playPokeballSound() {
        if (!audioEnabled || pokeballSound == null) return;
        pokeballSound.play(sfxVolume);
    }
    
    // ===== SETTINGS =====
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (battleMusic != null) battleMusic.setVolume(musicVolume);
        if (victoryMusic != null) victoryMusic.setVolume(musicVolume);
    }
    
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
        if (!enabled) {
            stopBattleMusic();
        }
    }
    
    public boolean isAudioEnabled() {
        return audioEnabled;
    }
    
    // ===== CLEANUP =====
    
    public void dispose() {
        if (battleMusic != null) battleMusic.dispose();
        if (victoryMusic != null) victoryMusic.dispose();
        if (hitSound != null) hitSound.dispose();
        if (selectSound != null) selectSound.dispose();
        if (attackSound != null) attackSound.dispose();
        if (pokeballSound != null) pokeballSound.dispose();
    }
}
