package com.github.adisann.pokemon.model.portal;

import com.github.adisann.pokemon.model.actor.PlayerActor;

/**
 * Interface for conditions that determine if a player can travel through a portal.
 * 
 * This follows the Strategy pattern, allowing different conditions to be added
 * without modifying the TeleportTile class.
 * 
 * Examples of conditions:
 * - Party not fainted
 * - Has specific badge
 * - Has completed certain quest
 * - Time of day restrictions
 */
public interface PortalCondition {
    
    /**
     * Check if the player meets the condition to travel through the portal.
     * 
     * @param player The player attempting to travel
     * @return true if the player can travel, false otherwise
     */
    boolean canTravel(PlayerActor player);
    
    /**
     * Get the message to display when the player cannot travel.
     * 
     * @return The message explaining why travel is blocked
     */
    String getBlockedMessage();
}
