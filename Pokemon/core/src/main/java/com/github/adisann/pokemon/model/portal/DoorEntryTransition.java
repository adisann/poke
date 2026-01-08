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
 * Transition strategy for entering a building through a door.
 * 
 * ENTERING A HOUSE (OUTSIDE → INSIDE):
 * 1. Player walks to the door
 * 2. Player steps on the door tile
 * 3. Player stops moving
 * 4. Player faces up (toward door)
 * 5. Screen fades to black
 * 6. Player sprite disappears
 * 7. Outside map is removed
 * 8. Inside house map loads
 * 9. Player appears inside (one tile from the door, offset by facing direction)
 * 10. Player faces down
 * 11. Screen fades in
 * 12. Player can move again
 */
public class DoorEntryTransition implements TransitionStrategy {
    
    private static final float FADE_DURATION = 0.3f;
    private static final float PAUSE_AT_DOOR = 0.1f;
    
    private final List<PortalAction> actions;
    
    public DoorEntryTransition() {
        this.actions = null;
    }
    
    public DoorEntryTransition(List<PortalAction> actions) {
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
        
        // 1-2. Open door and player walks onto door tile
        if (sourceDoor != null) {
            cutscenes.queueEvent(new DoorEvent(sourceDoor, true));
        }
        cutscenes.queueEvent(new ActorWalkEvent(actor, movementDirection));
        
        // 3-4. Player stops and faces UP (toward the door)
        cutscenes.queueEvent(new ActorFaceDirectionEvent(actor, DIRECTION.NORTH));
        cutscenes.queueEvent(new WaitEvent(PAUSE_AT_DOOR));
        
        // === PHASE 2: FADE OUT ===
        
        // 5. Screen fades to black
        cutscenes.queueEvent(new ScreenFadeEvent(FadeType.FADE_OUT, Color.BLACK, FADE_DURATION));
        
        // 6. Player sprite disappears
        cutscenes.queueEvent(new ActorVisibilityEvent(actor, true));
        
        // === PHASE 3: MAP CHANGE (Screen is black) ===
        
        // 7. Close door (not visible)
        if (sourceDoor != null) {
            cutscenes.queueEvent(new DoorEvent(sourceDoor, false));
        }
        
        // 8-9. Inside map loads, player placed one tile from door, facing DOWN
        cutscenes.queueEvent(new ChangeWorldEvent(worldName, spawnX, spawnY, DIRECTION.SOUTH, color, actions));
        
        // === PHASE 4: FADE IN ===
        
        // 10-11. Player appears (already facing down), screen fades in
        cutscenes.queueEvent(new ActorVisibilityEvent(actor, false));
        cutscenes.queueEvent(new ScreenFadeEvent(FadeType.FADE_IN, Color.BLACK, FADE_DURATION));
        
        // 12. Player can move again (no walk animation needed - already in position)
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
