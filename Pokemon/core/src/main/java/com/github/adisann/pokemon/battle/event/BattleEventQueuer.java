package com.github.adisann.pokemon.battle.event;

/** */
public interface BattleEventQueuer {

	public void queueEvent(BattleEvent event);
	
}