package com.github.adisann.pokemon.model.portal;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.world.Door;
import com.github.adisann.pokemon.model.world.cutscene.ActorFaceDirectionEvent;
import com.github.adisann.pokemon.model.world.cutscene.ActorVisibilityEvent;
import com.github.adisann.pokemon.model.world.cutscene.ActorWalkEvent;
import com.github.adisann.pokemon.model.world.cutscene.ChangeWorldEvent;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;
import com.github.adisann.pokemon.model.world.cutscene.DoorEvent;
import com.github.adisann.pokemon.model.world.cutscene.ScreenFadeEvent;
import com.github.adisann.pokemon.model.world.cutscene.ScreenFadeEvent.FadeType;
import com.github.adisann.pokemon.model.world.cutscene.WaitEvent;

/**
 * Transition strategy for exiting a building through a door.
 * 
 * EXITING A HOUSE (INSIDE → OUTSIDE):
 * 1. Player walks to the exit
 * 2. Player steps on the exit tile
 * 3. Player faces down (toward exit)
 * 4. Screen fades to black
 * 5. Player sprite disappears
 * 6. Inside house map is removed
 * 7. Outside map loads
 * 8. Player appears outside (one tile in front of the door)
 * 9. Player faces down
 * 10. Screen fades in
 * 11. Player can move again
 */
public class DoorExitTransition implements TransitionStrategy {
    
    private static final float FADE_DURATION = 0.3f;
    private static final float PAUSE_AT_EXIT = 0.1f;
    
    private final List<PortalAction> actions;
    
    public DoorExitTransition() {
        this.actions = null;
    }
    
    public DoorExitTransition(List<PortalAction> actions) {
        this.actions = actions;
    }
    
    @Override
    public void queueTransitionEvents(Actor actor, CutscenePlayer cutscenes,
                                       DIRECTION movementDirection, DIRECTION targetFacing,
                                       String worldName, int x, int y, Color color,
                                       Door sourceDoor, Door targetDoor) {
        
        // Calculate spawn position: one tile offset from door in targetFacing direction
        int spawnX = x + getOffsetX(targetFacing);
        int spawnY = y + getOffsetY(targetFacing);
        
        // === PHASE 1: APPROACH (Visible) ===
        
        // 1-2. Player walks onto exit tile
        cutscenes.queueEvent(new ActorWalkEvent(actor, movementDirection));
        
        // 3. Player faces DOWN (toward exit)
        cutscenes.queueEvent(new ActorFaceDirectionEvent(actor, DIRECTION.SOUTH));
        cutscenes.queueEvent(new WaitEvent(PAUSE_AT_EXIT));
        
        // === PHASE 2: FADE OUT ===
        
        // 4. Screen fades to black
        cutscenes.queueEvent(new ScreenFadeEvent(FadeType.FADE_OUT, Color.BLACK, FADE_DURATION));
        
        // 5. Player sprite disappears
        cutscenes.queueEvent(new ActorVisibilityEvent(actor, true));
        
        // === PHASE 3: MAP CHANGE (Screen is black) ===
        
        // 6-7. Inside map removed, outside map loads
        // 8-9. Player appears one tile in front of door, facing DOWN
        cutscenes.queueEvent(new ChangeWorldEvent(worldName, spawnX, spawnY, DIRECTION.SOUTH, color, actions));
        
        // Open door behind player (visible when screen fades in)
        if (targetDoor != null) {
            cutscenes.queueEvent(new DoorEvent(targetDoor, true));
        }
        
        // === PHASE 4: FADE IN ===
        
        // Player appears (already facing down, already in position)
        cutscenes.queueEvent(new ActorVisibilityEvent(actor, false));
        
        // 10. Screen fades in
        cutscenes.queueEvent(new ScreenFadeEvent(FadeType.FADE_IN, Color.BLACK, FADE_DURATION));
        
        // 11. Player can move again (no walk - already in position)
        
        // Close door behind player after fade completes
        if (targetDoor != null) {
            cutscenes.queueEvent(new WaitEvent(0.2f));
            cutscenes.queueEvent(new DoorEvent(targetDoor, false));
        }
    }
    
    /**
     * Get X offset for spawn position based on facing direction.
     */
    private int getOffsetX(DIRECTION facing) {
        switch (facing) {
            case EAST: return 1;
            case WEST: return -1;
            default: return 0;
        }
    }
    
    /**
     * Get Y offset for spawn position based on facing direction.
     */
    private int getOffsetY(DIRECTION facing) {
        switch (facing) {
            case NORTH: return 1;
            case SOUTH: return -1;
            default: return 0;
        }
    }
}
