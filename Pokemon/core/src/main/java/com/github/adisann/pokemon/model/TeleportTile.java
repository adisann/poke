package com.github.adisann.pokemon.model;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.actor.PlayerActor;
import com.github.adisann.pokemon.model.world.Door;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.model.world.WorldObject;
import com.github.adisann.pokemon.model.world.cutscene.ActorVisibilityEvent;
import com.github.adisann.pokemon.model.world.cutscene.ActorWalkEvent;
import com.github.adisann.pokemon.model.world.cutscene.ChangeWorldEvent;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;
import com.github.adisann.pokemon.model.world.cutscene.DialogueEvent;
import com.github.adisann.pokemon.model.world.cutscene.DoorEvent;
import com.github.adisann.pokemon.model.world.cutscene.WaitEvent;
import com.github.adisann.pokemon.worldloader.LTerrain;

/** */
public class TeleportTile extends Tile {

	/* destination */
	private String worldName;
	private int x, y;
	private DIRECTION facing;

	/* transition color */
	private Color color;

	public TeleportTile(LTerrain terrain, String worldName, int x, int y, DIRECTION facing, Color color) {
		super(terrain);
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.facing = facing;
		this.color = color;
	}

	@Override
	public void actorStep(Actor a) {
	}

	@Override
	public boolean actorBeforeStep(Actor a) {
		if (a instanceof PlayerActor) {
			PlayerActor playerActor = (PlayerActor) a;
			CutscenePlayer cutscenes = playerActor.getCutscenePlayer();
			DIRECTION playerDirection = playerActor.getFacing();

			// Block travel to Oldale if Pokemon is fainted
			if (worldName != null && worldName.equals("oldale_town")) {
				if (cutscenes.isPlayerPokemonFainted()) {
					// Show fainted message
					cutscenes.queueEvent(new DialogueEvent(
							"Your Pokemon is fainted. You cannot enter Oldale."));
					// Force step back in opposite direction
					DIRECTION oppositeDir = getOppositeDirection(playerDirection);
					cutscenes.queueEvent(new ActorWalkEvent(a, oppositeDir));
					cutscenes.queueEvent(new ActorWalkEvent(a, oppositeDir));
					return true; // Block the teleport
				}
			}

			if (this.getObject() != null) { // the teleport tile has an object
				if (this.getObject() instanceof Door) { // entering a door
					Door door = (Door) this.getObject();
					cutscenes.queueEvent(new DoorEvent(door, true));
					cutscenes.queueEvent(new ActorWalkEvent(a, playerDirection));
					cutscenes.queueEvent(new ActorVisibilityEvent(a, true));
					cutscenes.queueEvent(new DoorEvent(door, false));
					cutscenes.queueEvent(new ChangeWorldEvent(worldName, x, y, facing, color));
					cutscenes.queueEvent(new ActorVisibilityEvent(a, false));
					cutscenes.queueEvent(new ActorWalkEvent(a, facing));
					return false;
				}
			} else { // the teleport tile does not have an object
				System.out.println("Initiating teleport to " + worldName);

				World nextWorld = cutscenes.getWorld(worldName);

				if (nextWorld.getMap().getTile(x, y).getObject() != null) { // the target tile has an object
					WorldObject targetObj = nextWorld.getMap().getTile(x, y).getObject();
					if (targetObj instanceof Door) {
						Door targetDoor = (Door) targetObj;
						cutscenes.queueEvent(new ActorWalkEvent(a, playerDirection));
						cutscenes.queueEvent(new ActorVisibilityEvent(a, true));
						cutscenes.queueEvent(new ChangeWorldEvent(worldName, x, y, facing, color));
						cutscenes.queueEvent(new DoorEvent(targetDoor, true));
						cutscenes.queueEvent(new WaitEvent(0.2f));
						cutscenes.queueEvent(new ActorVisibilityEvent(a, false));
						cutscenes.queueEvent(new WaitEvent(0.2f));
						cutscenes.queueEvent(new ActorWalkEvent(a, facing));
						cutscenes.queueEvent(new DoorEvent(targetDoor, false));
						return false;
					}
				} else { // the target tile does not have an object
					// Use player's current direction for the walk, then destination facing
					cutscenes.queueEvent(new ActorWalkEvent(a, playerDirection));
					cutscenes.queueEvent(new ActorVisibilityEvent(a, true));
					cutscenes.queueEvent(new ChangeWorldEvent(worldName, x, y, facing, color));
					cutscenes.queueEvent(new ActorVisibilityEvent(a, false));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the opposite direction.
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