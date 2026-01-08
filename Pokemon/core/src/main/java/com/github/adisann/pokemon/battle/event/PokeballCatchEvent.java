package com.github.adisann.pokemon.battle.event;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.adisann.pokemon.util.SpriteManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Pokeball catch animation - Emerald-style timing.
 * THROW → HIT → LAND_PAUSE → (WIGGLE → PAUSE) x N → RESULT
 */
public class PokeballCatchEvent extends BattleEvent {
    
    public enum Phase {
        THROW,       // Ball arcs to opponent
        HIT,         // Ball opens, Pokemon shrinks in
        LAND_PAUSE,  // Pause after landing
        WIGGLE,      // Ball wobbles
        WIGGLE_PAUSE,// Pause between wiggles
        RESULT       // Success or break
    }
    
    // Emerald-accurate timing
    private static final float THROW_DURATION = 0.5f;
    private static final float HIT_DURATION = 0.35f;
    private static final float LAND_PAUSE_DURATION = 0.4f;
    private static final float WIGGLE_DURATION = 0.45f;
    private static final float WIGGLE_PAUSE_DURATION = 0.3f;
    private static final float RESULT_DURATION = 0.5f;
    
    private static final int POKEBALL_COL = 3;
    
    private Phase currentPhase = Phase.THROW;
    private float phaseTimer = 0f;
    private boolean catchSuccess;
    private int shakeCount;
    private int currentShake = 0;
    
    private Vector2 startPos;
    private Vector2 targetPos;
    private Vector2 ballPos;
    
    private float ballRotation = 0f;
    
    private List<Particle> particles = new ArrayList<>();
    
    private TextureRegion pokeballClosed;
    private TextureRegion pokeballOpen;
    
    private float screenWidth;
    private float screenHeight;
    
    private float opponentAlpha = 1f;
    private float opponentScale = 1f;
    private float opponentWhiteness = 0f;
    private boolean useOpenBall = false;
    
    public PokeballCatchEvent(boolean catchSuccess, int shakeCount) {
        this.catchSuccess = catchSuccess;
        this.shakeCount = Math.max(0, Math.min(3, shakeCount));
    }
    
    @Override
    public void begin(BattleEventPlayer player) {
        super.begin(player);
        
        pokeballClosed = SpriteManager.getInstance().getPokeballIcon(0, POKEBALL_COL);
        pokeballOpen = SpriteManager.getInstance().getPokeballIcon(1, POKEBALL_COL);
        
        System.out.println("[PokeballCatchEvent] Sprites: closed=" + (pokeballClosed != null) + ", open=" + (pokeballOpen != null));
        
        screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
        screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
        
        startPos = new Vector2(screenWidth * 0.25f, screenHeight * 0.35f);
        targetPos = new Vector2(screenWidth * 0.72f, screenHeight * 0.65f);
        
        ballPos = new Vector2(startPos);
        currentPhase = Phase.THROW;
        phaseTimer = 0f;
    }
    
    @Override
    public void update(float delta) {
        phaseTimer += delta;
        
        switch (currentPhase) {
            case THROW: updateThrow(delta); break;
            case HIT: updateHit(delta); break;
            case LAND_PAUSE: updateLandPause(delta); break;
            case WIGGLE: updateWiggle(delta); break;
            case WIGGLE_PAUSE: updateWigglePause(delta); break;
            case RESULT: updateResult(delta); break;
        }
        
        // Update particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) particles.remove(i);
        }
    }
    
    private void updateThrow(float delta) {
        float progress = Math.min(phaseTimer / THROW_DURATION, 1f);
        
        float x = Interpolation.linear.apply(startPos.x, targetPos.x, progress);
        float baseY = Interpolation.linear.apply(startPos.y, targetPos.y, progress);
        float arcHeight = 80f;
        float arc = 4f * arcHeight * progress * (1f - progress);
        
        ballPos.set(x, baseY + arc);
        ballRotation += delta * 720f;
        
        if (progress >= 1f) {
            currentPhase = Phase.HIT;
            phaseTimer = 0f;
            useOpenBall = true;
            ballPos.set(targetPos);
            spawnCaptureParticles();
        }
    }
    
    private void updateHit(float delta) {
        float progress = Math.min(phaseTimer / HIT_DURATION, 1f);
        
        if (progress < 0.3f) {
            opponentWhiteness = 1f;
            opponentAlpha = 1f;
            opponentScale = 1f;
        } else {
            float shrinkProgress = (progress - 0.3f) / 0.7f;
            opponentWhiteness = 1f - shrinkProgress * 0.5f;
            opponentScale = 1f - shrinkProgress * 0.9f;
            opponentAlpha = 1f - shrinkProgress;
        }
        
        ballPos.set(targetPos);
        ballRotation = 0f;
        
        if (progress >= 1f) {
            currentPhase = Phase.LAND_PAUSE;
            phaseTimer = 0f;
            opponentAlpha = 0f;
            opponentScale = 0f;
            useOpenBall = false;  // Ball closes
            currentShake = 0;
        }
    }
    
    private void updateLandPause(float delta) {
        // Ball sits still, closed
        ballPos.set(targetPos);
        ballRotation = 0f;
        
        if (phaseTimer >= LAND_PAUSE_DURATION) {
            if (shakeCount > 0) {
                currentPhase = Phase.WIGGLE;
            } else {
                currentPhase = Phase.RESULT;
                if (!catchSuccess) useOpenBall = true;
            }
            phaseTimer = 0f;
        }
    }
    
    private void updateWiggle(float delta) {
        float progress = Math.min(phaseTimer / WIGGLE_DURATION, 1f);
        
        // Wobble animation: tilt left then right
        ballRotation = MathUtils.sin(progress * MathUtils.PI * 2) * 20f;
        
        if (progress >= 1f) {
            currentShake++;
            ballRotation = 0f;
            phaseTimer = 0f;
            
            if (currentShake >= shakeCount) {
                currentPhase = Phase.RESULT;
                if (catchSuccess) {
                    spawnSuccessParticles();
                } else {
                    useOpenBall = true;
                }
            } else {
                currentPhase = Phase.WIGGLE_PAUSE;
            }
        }
    }
    
    private void updateWigglePause(float delta) {
        ballRotation = 0f;
        
        if (phaseTimer >= WIGGLE_PAUSE_DURATION) {
            currentPhase = Phase.WIGGLE;
            phaseTimer = 0f;
        }
    }
    
    private void updateResult(float delta) {
        if (catchSuccess) {
            // Keep opponent hidden on success
            opponentAlpha = 0f;
            opponentScale = 0f;
        } else {
            float progress = Math.min(phaseTimer / RESULT_DURATION, 1f);
            
            useOpenBall = progress < 0.3f;
            opponentScale = progress;
            opponentAlpha = progress;
            opponentWhiteness = Math.max(0f, 1f - progress * 2f);
        }
    }
    
    private void spawnCaptureParticles() {
        for (int i = 0; i < 12; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(80f, 180f);
            particles.add(new Particle(targetPos.x, targetPos.y,
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                0.4f, ParticleType.CAPTURE));
        }
    }
    
    private void spawnSuccessParticles() {
        // Spawn star particles around the ball
        for (int i = 0; i < 12; i++) {
            float angle = i * 30f + MathUtils.random(-10f, 10f);
            float speed = MathUtils.random(50f, 120f);
            particles.add(new Particle(targetPos.x, targetPos.y,
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                0.8f, ParticleType.SUCCESS));
        }
    }
    
    @Override
    public boolean finished() {
        return currentPhase == Phase.RESULT && phaseTimer >= RESULT_DURATION;
    }
    
    public void render(SpriteBatch batch) {
        TextureRegion ballSprite = useOpenBall ? pokeballOpen : pokeballClosed;
        
        if (ballSprite != null) {
            float size = 48f;
            
            // On success, show darkened ball; on failure result, hide ball after opening
            if (currentPhase == Phase.RESULT) {
                if (catchSuccess) {
                    // Darken ball on success
                    batch.setColor(0.7f, 0.7f, 0.7f, 1f);
                    batch.draw(ballSprite, ballPos.x - size/2, ballPos.y - size/2,
                        size/2, size/2, size, size, 1f, 1f, ballRotation);
                } else if (phaseTimer < 0.4f) {
                    // Show ball briefly during break-out
                    batch.setColor(1f, 1f, 1f, 1f);
                    batch.draw(ballSprite, ballPos.x - size/2, ballPos.y - size/2,
                        size/2, size/2, size, size, 1f, 1f, ballRotation);
                }
            } else {
                // Normal rendering during other phases
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(ballSprite, ballPos.x - size/2, ballPos.y - size/2,
                    size/2, size/2, size, size, 1f, 1f, ballRotation);
            }
        }
        
        // Draw particles
        for (Particle p : particles) {
            p.render(batch);
        }
        
        batch.setColor(1f, 1f, 1f, 1f);
    }
    
    public float getOpponentAlpha() { return opponentAlpha; }
    public float getOpponentScale() { return opponentScale; }
    public float getOpponentWhiteness() { return opponentWhiteness; }
    public Vector2 getBallPosition() { return ballPos; }
    public Phase getCurrentPhase() { return currentPhase; }
    public boolean isCatchSuccess() { return catchSuccess; }
    
    private enum ParticleType { CAPTURE, SUCCESS }
    
    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        ParticleType type;
        
        Particle(float x, float y, float vx, float vy, float life, ParticleType type) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life; this.type = type;
        }
        
        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            vy -= 80f * delta;
            life -= delta;
        }
        
        boolean isDead() { return life <= 0; }
        
        void render(SpriteBatch batch) {
            float alpha = life / maxLife;
            if (type == ParticleType.SUCCESS) {
                batch.setColor(1f, 1f, 0f, alpha);
            } else {
                batch.setColor(1f, 1f, 1f, alpha);
            }
        }
    }
}
