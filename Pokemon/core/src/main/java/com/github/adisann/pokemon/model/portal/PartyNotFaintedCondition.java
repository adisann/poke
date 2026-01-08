package com.github.adisann.pokemon.model.portal;

import com.github.adisann.pokemon.model.actor.PlayerActor;

/**
 * Condition that checks if the player's Pokemon party is not fainted.
 * 
 * This replaces the hard-coded check in TeleportTile that blocked
 * entry to "oldale_town" if the player's Pokemon was fainted.
 */
public class PartyNotFaintedCondition implements PortalCondition {
    
    private final String blockedMessage;
    
    /**
     * Create a condition with the default blocked message.
     */
    public PartyNotFaintedCondition() {
        this.blockedMessage = "Your Pokemon is fainted. You cannot travel here.";
    }
    
    /**
     * Create a condition with a custom blocked message.
     * 
     * @param customMessage The custom message to display when blocked
     */
    public PartyNotFaintedCondition(String customMessage) {
        this.blockedMessage = customMessage;
    }
    
    @Override
    public boolean canTravel(PlayerActor player) {
        // Check if the player's Pokemon is NOT fainted
        // Returns true if party is healthy, false if fainted
        return !player.getCutscenePlayer().isPlayerPokemonFainted();
    }
    
    @Override
    public String getBlockedMessage() {
        return blockedMessage;
    }
}
