package com.github.adisann.pokemon.model.portal;

import com.github.adisann.pokemon.model.actor.PlayerActor;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;

/**
 * Interface for actions that execute when a player enters a portal.
 * 
 * This follows the Strategy pattern, allowing different actions to be added
 * without modifying the ChangeWorldEvent class.
 * 
 * Examples of actions:
 * - Heal party
 * - Trigger dialogue
 * - Play sound effect
 * - Award item
 */
public interface PortalAction {
    
    /**
     * Execute the action when the player enters the portal.
     * 
     * @param player The player entering the portal
     * @param cutscenes The cutscene player for triggering events
     */
    void execute(PlayerActor player, CutscenePlayer cutscenes);
}
