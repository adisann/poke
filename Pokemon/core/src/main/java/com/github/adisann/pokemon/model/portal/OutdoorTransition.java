package com.github.adisann.pokemon.model.portal;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.world.Door;
import com.github.adisann.pokemon.model.world.cutscene.ActorVisibilityEvent;
import com.github.adisann.pokemon.model.world.cutscene.ActorWalkEvent;
import com.github.adisann.pokemon.model.world.cutscene.ChangeWorldEvent;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;
import com.github.adisann.pokemon.model.world.cutscene.ScreenFadeEvent;
import com.github.adisann.pokemon.model.world.cutscene.ScreenFadeEvent.FadeType;
import com.github.adisann.pokemon.model.world.cutscene.WaitEvent;

/**
 * Transition strategy for outdoor map transitions (between towns/routes).
 * 
 * Creates a smooth transition where:
 * - Player walks VISIBLY to the edge of the map
 * - Screen fades to black (player hidden during fade)
 * - Map changes while screen is black
 * - Player appears STANDING in new location (not walking)
 * - Screen fades back in
 * 
 * Sequence:
 * 1. Player walks to map edge (VISIBLE)
 * 2. Brief pause
 * 3. Screen fades to black
 * 4. Player hidden (during fade)
 * 5. Map changes
 * 6. Player shown (standing)
 * 7. Screen fades from black
 */
public class OutdoorTransition implements TransitionStrategy {
    
    private static final float FADE_DURATION = 0.4f;
    private static final float PAUSE_AT_EDGE = 0.1f;
    
    private final List<PortalAction> actions;
    
    public OutdoorTransition() {
        this.actions = null;
    }
    
    public OutdoorTransition(List<PortalAction> actions) {
        this.actions = actions;
    }
    
    @Override
    public void queueTransitionEvents(Actor actor, CutscenePlayer cutscenes,
                                       DIRECTION movementDirection, DIRECTION targetFacing,
                                       String worldName, int x, int y, Color color,
                                       Door sourceDoor, Door targetDoor) {
        
        // --- PHASE 1: APPROACH (Player VISIBLE) ---
        
        // 1. Player walks to map edge (VISIBLE with correct animation)
        cutscenes.queueEvent(new ActorWalkEvent(actor, movementDirection));
        
        // 2. Brief pause at edge
        cutscenes.queueEvent(new WaitEvent(PAUSE_AT_EDGE));
        
        // --- PHASE 2: FADE OUT ---
        
        // 3. Start screen fade to black
        cutscenes.queueEvent(new ScreenFadeEvent(FadeType.FADE_OUT, Color.BLACK, FADE_DURATION));
        
        // 4. Hide player (during the fade, player disappears)
        cutscenes.queueEvent(new ActorVisibilityEvent(actor, true));
        
        // --- PHASE 3: MAP SWITCH (Screen is black) ---
        
        // 5. Change to new map
        cutscenes.queueEvent(new ChangeWorldEvent(worldName, x, y, targetFacing, color, actions));
        
        // --- PHASE 4: FADE IN ---
        
        // 6. Show player (BEFORE fade in - player already standing)
        cutscenes.queueEvent(new ActorVisibilityEvent(actor, false));
        
        // 7. Screen fades from black (reveals player standing in new location)
        cutscenes.queueEvent(new ScreenFadeEvent(FadeType.FADE_IN, Color.BLACK, FADE_DURATION));
        
        // --- PHASE 5: CONTROL RETURNED ---
        // No walk after - player is already in position, standing still
    }
}
