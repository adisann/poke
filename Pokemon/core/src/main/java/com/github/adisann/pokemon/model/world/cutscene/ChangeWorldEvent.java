package com.github.adisann.pokemon.model.world.cutscene;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.PlayerActor;
import com.github.adisann.pokemon.model.portal.PortalAction;
import com.github.adisann.pokemon.model.world.World;

/**
 * Event that changes the player's current world/map.
 * 
 * This event handles the actual map transition and can execute
 * portal actions (like healing) when the transition occurs.
 */
public class ChangeWorldEvent extends CutsceneEvent {

	private String newWorld;
	private int newX;
	private int newY;
	private DIRECTION newFacing;
	private Color transitionColor;
	private List<PortalAction> actions;

	private boolean isFinished = false;

	/**
	 * Create a change world event without any actions.
	 * (Backward compatible constructor)
	 */
	public ChangeWorldEvent(String newWorld, int x, int y, DIRECTION facing, Color color) {
		this(newWorld, x, y, facing, color, null);
	}
	
	/**
	 * Create a change world event with portal actions.
	 * 
	 * @param newWorld The destination world name
	 * @param x The destination x coordinate
	 * @param y The destination y coordinate
	 * @param facing The direction to face at destination
	 * @param color The transition color effect
	 * @param actions List of actions to execute on entry (can be null)
	 */
	public ChangeWorldEvent(String newWorld, int x, int y, DIRECTION facing, Color color, 
							List<PortalAction> actions) {
		this.newWorld = newWorld;
		this.newX = x;
		this.newY = y;
		this.newFacing = facing;
		this.transitionColor = color;
		this.actions = actions;
	}

	@Override
	public void begin(CutscenePlayer player) {
		super.begin(player);
		World world = player.getWorld(newWorld);
		player.changeLocation(world, newX, newY, newFacing, transitionColor);

		// Execute portal actions if any are defined
		if (actions != null && !actions.isEmpty()) {
			PlayerActor playerActor = player.getPlayerActor();
			for (PortalAction action : actions) {
				action.execute(playerActor, player);
			}
		}

		isFinished = true;
	}

	@Override
	public void update(float delta) {
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