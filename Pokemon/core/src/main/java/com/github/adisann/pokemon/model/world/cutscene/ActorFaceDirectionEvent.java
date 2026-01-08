package com.github.adisann.pokemon.model.world.cutscene;

import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.Actor;

/**
 * Cutscene event that makes an actor face a specific direction without walking.
 * 
 * This is used during door transitions to ensure the player faces the correct
 * direction (e.g., facing the door before entering, facing away after exiting).
 */
public class ActorFaceDirectionEvent extends CutsceneEvent {
    
    private Actor actor;
    private DIRECTION direction;
    private boolean isFinished = false;
    
    /**
     * Create an event that changes the actor's facing direction.
     * 
     * @param actor The actor to reface
     * @param direction The direction to face
     */
    public ActorFaceDirectionEvent(Actor actor, DIRECTION direction) {
        this.actor = actor;
        this.direction = direction;
    }
    
    @Override
    public void begin(CutscenePlayer player) {
        super.begin(player);
        // Change facing direction without walking animation
        actor.refaceWithoutAnimation(direction);
        isFinished = true;
    }
    
    @Override
    public void update(float delta) {
        // Instant event, no update needed
    }
    
    @Override
    public boolean isFinished() {
        return isFinished;
    }
    
    @Override
    public void screenShow() {
        isFinished = true;
    }
}
