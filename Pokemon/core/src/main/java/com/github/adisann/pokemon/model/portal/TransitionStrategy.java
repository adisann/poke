package com.github.adisann.pokemon.model.portal;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.world.Door;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;

/**
 * Interface for different transition strategies when traveling through portals.
 * 
 * This follows the Strategy pattern, allowing different transition animations
 * to be used without modifying the TeleportTile class.
 * 
 * Examples of strategies:
 * - Door entry (open door, walk, hide, change map, show, walk)
 * - Door exit (walk, hide, change map, open door, show, walk, close door)
 * - Outdoor (walk, fade out, change map, fade in)
 * - Cave entrance (custom animation)
 */
public interface TransitionStrategy {
    
    /**
     * Queue the transition events for this strategy.
     * 
     * @param actor The actor traveling through the portal
     * @param cutscenes The cutscene player for queueing events
     * @param playerDirection The direction the player is currently facing
     * @param targetFacing The direction the player should face at destination
     * @param worldName The name of the destination world
     * @param x The x coordinate in the destination world
     * @param y The y coordinate in the destination world
     * @param color The transition color effect
     * @param sourceDoor The door at the source location (may be null)
     * @param targetDoor The door at the target location (may be null)
     */
    void queueTransitionEvents(Actor actor, CutscenePlayer cutscenes,
                                DIRECTION playerDirection, DIRECTION targetFacing,
                                String worldName, int x, int y, Color color,
                                Door sourceDoor, Door targetDoor);
}
