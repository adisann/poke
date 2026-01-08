package com.github.adisann.pokemon.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.actor.PlayerActor;
import com.github.adisann.pokemon.model.portal.DoorEntryTransition;
import com.github.adisann.pokemon.model.portal.DoorExitTransition;
import com.github.adisann.pokemon.model.portal.OutdoorTransition;
import com.github.adisann.pokemon.model.portal.PortalAction;
import com.github.adisann.pokemon.model.portal.PortalCondition;
import com.github.adisann.pokemon.model.portal.TransitionStrategy;
import com.github.adisann.pokemon.model.world.Door;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.model.world.WorldObject;
import com.github.adisann.pokemon.model.world.cutscene.ActorWalkEvent;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;
import com.github.adisann.pokemon.model.world.cutscene.DialogueEvent;
import com.github.adisann.pokemon.worldloader.LTerrain;

/**
 * A tile that teleports the player to another world/map when stepped on.
 * 
 * This class uses the Strategy pattern for different transition types
 * and supports configurable conditions and actions.
 */
public class TeleportTile extends Tile {

	/* this tile's position on the map */
	private int tileX, tileY;

	/* destination */
	private String worldName;
	private int destX, destY;
	private DIRECTION destFacing;

	/* transition color */
	private Color color;

	/* extensible conditions and actions */
	private List<PortalCondition> conditions = new ArrayList<>();
	private List<PortalAction> actions = new ArrayList<>();

	/**
	 * Create a teleport tile with destination information.
	 * 
	 * @param terrain The terrain type of this tile
	 * @param tileX The x position of this tile on the map
	 * @param tileY The y position of this tile on the map
	 * @param worldName The destination world name
	 * @param destX The destination x coordinate
	 * @param destY The destination y coordinate
	 * @param destFacing The direction to face at destination
	 * @param color The transition color effect
	 */
	public TeleportTile(LTerrain terrain, int tileX, int tileY, String worldName, 
	                    int destX, int destY, DIRECTION destFacing, Color color) {
		super(terrain);
		this.tileX = tileX;
		this.tileY = tileY;
		this.worldName = worldName;
		this.destX = destX;
		this.destY = destY;
		this.destFacing = destFacing;
		this.color = color;
	}

	/**
	 * Add a condition that must be met to use this portal.
	 * 
	 * @param condition The condition to add
	 */
	public void addCondition(PortalCondition condition) {
		conditions.add(condition);
	}

	/**
	 * Add an action to execute when entering this portal.
	 * 
	 * @param action The action to add
	 */
	public void addAction(PortalAction action) {
		actions.add(action);
	}

	/**
	 * Get the list of actions (for passing to transition strategies).
	 * 
	 * @return The list of actions, or null if empty
	 */
	public List<PortalAction> getActions() {
		return actions.isEmpty() ? null : actions;
	}

	@Override
	public void actorStep(Actor a) {
	}

	@Override
	public boolean actorBeforeStep(Actor a) {
		if (!(a instanceof PlayerActor)) {
			return true; // Only handle player actors
		}

		PlayerActor playerActor = (PlayerActor) a;
		CutscenePlayer cutscenes = playerActor.getCutscenePlayer();
		
		// Calculate the actual movement direction based on player position -> tile position
		DIRECTION movementDirection = calculateMovementDirection(
			playerActor.getX(), playerActor.getY(), tileX, tileY);
		
		System.out.println("Player at (" + playerActor.getX() + "," + playerActor.getY() + 
		                   ") moving to tile (" + tileX + "," + tileY + ") direction: " + movementDirection);

		// Check all conditions
		for (PortalCondition condition : conditions) {
			if (!condition.canTravel(playerActor)) {
				// Show blocked message
				cutscenes.queueEvent(new DialogueEvent(condition.getBlockedMessage()));
				
				// Force step back in opposite direction
				DIRECTION oppositeDir = getOppositeDirection(movementDirection);
				cutscenes.queueEvent(new ActorWalkEvent(a, oppositeDir));
				cutscenes.queueEvent(new ActorWalkEvent(a, oppositeDir));
				
				return true; // Block the teleport
			}
		}

		// Determine the appropriate transition strategy
		TransitionStrategy strategy = determineTransitionStrategy(cutscenes, playerActor);
		
		// Get door references if applicable
		Door sourceDoor = getSourceDoor();
		Door targetDoor = getTargetDoor(cutscenes);

		// Execute the transition using MOVEMENT direction (not facing direction)
		System.out.println("Initiating teleport to " + worldName + " using " + strategy.getClass().getSimpleName());
		strategy.queueTransitionEvents(a, cutscenes, movementDirection, destFacing, 
		                                worldName, destX, destY, color, sourceDoor, targetDoor);

		return false;
	}

	/**
	 * Calculate the direction the player needs to move to reach the tile.
	 * 
	 * @param playerX The player's current X position
	 * @param playerY The player's current Y position
	 * @param targetX The tile's X position
	 * @param targetY The tile's Y position
	 * @return The direction to move
	 */
	private DIRECTION calculateMovementDirection(int playerX, int playerY, int targetX, int targetY) {
		int dx = targetX - playerX;
		int dy = targetY - playerY;
		
		// Prioritize the larger delta, or default to the direction with movement
		if (Math.abs(dx) > Math.abs(dy)) {
			return dx > 0 ? DIRECTION.EAST : DIRECTION.WEST;
		} else if (dy != 0) {
			return dy > 0 ? DIRECTION.NORTH : DIRECTION.SOUTH;
		} else if (dx != 0) {
			return dx > 0 ? DIRECTION.EAST : DIRECTION.WEST;
		}
		
		// Player is already on the tile, use the destination facing
		return destFacing;
	}

	/**
	 * Determine which transition strategy to use based on context.
	 * 
	 * @param cutscenes The cutscene player
	 * @param player The player actor
	 * @return The appropriate transition strategy
	 */
	private TransitionStrategy determineTransitionStrategy(CutscenePlayer cutscenes, PlayerActor player) {
		Door sourceDoor = getSourceDoor();
		Door targetDoor = getTargetDoor(cutscenes);

		if (sourceDoor != null) {
			// Entering through a door
			return new DoorEntryTransition(getActions());
		} else if (targetDoor != null) {
			// Exiting to a location with a door
			return new DoorExitTransition(getActions());
		} else {
			// Outdoor transition
			return new OutdoorTransition(getActions());
		}
	}

	/**
	 * Get the door at this tile (source), if any.
	 * 
	 * @return The door object, or null if none
	 */
	private Door getSourceDoor() {
		if (this.getObject() != null && this.getObject() instanceof Door) {
			return (Door) this.getObject();
		}
		return null;
	}

	/**
	 * Get the door at the target location, if any.
	 * 
	 * @param cutscenes The cutscene player to access world data
	 * @return The door object at target, or null if none
	 */
	private Door getTargetDoor(CutscenePlayer cutscenes) {
		World nextWorld = cutscenes.getWorld(worldName);
		if (nextWorld != null) {
			Tile targetTile = nextWorld.getMap().getTile(destX, destY);
			if (targetTile != null) {
				WorldObject targetObj = targetTile.getObject();
				if (targetObj instanceof Door) {
					return (Door) targetObj;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the opposite direction.
	 * 
	 * @param dir The direction to get the opposite of
	 * @return The opposite direction
	 */
	private DIRECTION getOppositeDirection(DIRECTION dir) {
		switch (dir) {
			case NORTH:
				return DIRECTION.SOUTH;
			case SOUTH:
				return DIRECTION.NORTH;
			case EAST:
				return DIRECTION.WEST;
			case WEST:
				return DIRECTION.EAST;
			default:
				return DIRECTION.SOUTH;
		}
	}
}