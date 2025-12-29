package com.github.adisann.pokemon.model.world.cutscene;

import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.actor.Actor;
import com.github.adisann.pokemon.model.actor.Actor.MOVEMENT_STATE;

/**
 * Event that makes an actor walk one tile in a direction.
 * Fixed: Now calculates target position only when actor is STILL to prevent
 * wrong direction bugs.
 */
public class ActorWalkEvent extends CutsceneEvent {

	private Actor a;
	private DIRECTION dir;

	private int targetX, targetY;
	private boolean targetCalculated = false;

	private boolean finished = false;

	public ActorWalkEvent(Actor a, DIRECTION dir) {
		this.a = a;
		this.dir = dir;
	}

	@Override
	public void begin(CutscenePlayer player) {
		super.begin(player);
		// Don't calculate target here - wait until actor is STILL in update()
		targetCalculated = false;
	}

	@Override
	public void update(float delta) {
		// Only calculate target when actor is still and hasn't been calculated yet
		if (!targetCalculated && a.getMovementState() == MOVEMENT_STATE.STILL) {
			targetX = a.getX() + dir.getDX();
			targetY = a.getY() + dir.getDY();
			targetCalculated = true;
		}

		if (!targetCalculated) {
			// Wait for actor to stop before calculating target
			return;
		}

		if (a.getX() != targetX || a.getY() != targetY) {
			if (a.getMovementState() == MOVEMENT_STATE.STILL) {
				a.moveWithoutNotifications(dir);
			}
		} else {
			if (a.getMovementState() == MOVEMENT_STATE.STILL) {
				finished = true;
			}
		}
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public void screenShow() {
	}

}