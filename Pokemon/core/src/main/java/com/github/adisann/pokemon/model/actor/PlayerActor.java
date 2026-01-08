package com.github.adisann.pokemon.model.actor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.adisann.pokemon.model.world.World;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;
import com.github.adisann.pokemon.util.AnimationSet;

/** */
public class PlayerActor extends Actor {
	
	private CutscenePlayer cutscenePlayer;

	public PlayerActor(World world, int x, int y, AnimationSet animations, CutscenePlayer cutscenePlayer) {
		super(world, x, y, animations);
		this.cutscenePlayer = cutscenePlayer;
	}
	
	public CutscenePlayer getCutscenePlayer() {
		return cutscenePlayer;
	}
}