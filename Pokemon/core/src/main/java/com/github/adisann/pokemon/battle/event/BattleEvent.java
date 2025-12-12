package com.github.adisann.pokemon.battle.event;

/**
 * Any visual change in a Battle. These are queued up and displayed chonologically on the BattleScreen.
 * */
public abstract class BattleEvent {
	
	private BattleEventPlayer player;
	
	public void begin(BattleEventPlayer player) {
		this.player = player;
	}
	
	public abstract void update(float delta);
	
	public abstract boolean finished();
	
	protected BattleEventPlayer getPlayer() {
		return player;
	}
}