package com.github.adisann.pokemon.model.world.cutscene;

import com.badlogic.gdx.graphics.Color;
import com.github.adisann.pokemon.model.DIRECTION;
import com.github.adisann.pokemon.model.world.World;

/** */
public interface CutscenePlayer {

	/**
	 * Smooth transition to another world.
	 * 
	 * @param newWorld
	 * @param x
	 * @param y
	 * @param facing
	 * @param color
	 */
	public void changeLocation(World newWorld, int x, int y, DIRECTION facing, Color color);

	/**
	 * Get a loaded World from name
	 * 
	 * @param worldName
	 * @return
	 */
	public World getWorld(String worldName);

	public void queueEvent(CutsceneEvent event);

	/**
	 * Heals the player's Pokemon team to full HP.
	 */
	public void healPlayerTeam();

	/**
	 * Checks if player's first Pokemon has fainted (HP = 0).
	 */
	public boolean isPlayerPokemonFainted();

	/**
	 * Shows a dialogue message on screen.
	 */
	public void showDialogue(String text);

	/**
	 * Hides the dialogue box.
	 */
	public void hideDialogue();
}