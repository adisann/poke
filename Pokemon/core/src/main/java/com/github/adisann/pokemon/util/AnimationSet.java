package com.github.adisann.pokemon.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.adisann.pokemon.model.DIRECTION;

public class AnimationSet {
	
	private Map<DIRECTION, Animation<TextureRegion>> walking;
	private Map<DIRECTION, Animation<TextureRegion>> running;
	private Map<DIRECTION, TextureRegion> standing;
	private Map<DIRECTION, Animation<TextureRegion>> biking;
	
	public AnimationSet(Animation<TextureRegion> walkNorth, 
			Animation<TextureRegion> walkSouth, 
			Animation<TextureRegion> walkEast, 
			Animation<TextureRegion> walkWest, 
			TextureRegion standNorth, 
			TextureRegion standSouth, 
			TextureRegion standEast, 
			TextureRegion standWest) {
		walking = new HashMap<DIRECTION, Animation<TextureRegion>>();
		walking.put(DIRECTION.NORTH, walkNorth);
		walking.put(DIRECTION.SOUTH, walkSouth);
		walking.put(DIRECTION.EAST, walkEast);
		walking.put(DIRECTION.WEST, walkWest);
		standing = new HashMap<DIRECTION, TextureRegion>();
		standing.put(DIRECTION.NORTH, standNorth);
		standing.put(DIRECTION.SOUTH, standSouth);
		standing.put(DIRECTION.EAST, standEast);
		standing.put(DIRECTION.WEST, standWest);
	}
	
	public void addBiking(Animation<TextureRegion> bikeNorth, Animation<TextureRegion> bikeSouth, Animation<TextureRegion> bikeEast, Animation<TextureRegion> bikeWest) {
		biking = new HashMap<DIRECTION, Animation<TextureRegion>>();
		biking.put(DIRECTION.NORTH, bikeNorth);
		biking.put(DIRECTION.SOUTH, bikeSouth);
		biking.put(DIRECTION.EAST, bikeEast);
		biking.put(DIRECTION.WEST, bikeWest);
	}
	
	public void addRunning(Animation<TextureRegion> runNorth, Animation<TextureRegion> runSouth, Animation<TextureRegion> runEast, Animation<TextureRegion> runWest) {
		running = new HashMap<DIRECTION, Animation<TextureRegion>>();
		running.put(DIRECTION.NORTH, runNorth);
		running.put(DIRECTION.SOUTH, runSouth);
		running.put(DIRECTION.EAST, runEast);
		running.put(DIRECTION.WEST, runWest);
	}
	
	public Animation<TextureRegion> getBiking(DIRECTION dir) {
		return biking.get(dir);
	}
	
	public Animation<TextureRegion> getRunning(DIRECTION dir) {
		return running.get(dir);
	}
	
	public Animation<TextureRegion> getWalking(DIRECTION dir) {
		return walking.get(dir);
	}
	
	public TextureRegion getStanding(DIRECTION dir) {
		return standing.get(dir);
	}

}