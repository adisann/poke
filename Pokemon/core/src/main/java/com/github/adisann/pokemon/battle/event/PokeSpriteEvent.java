package com.github.adisann.pokemon.battle.event;

import com.badlogic.gdx.graphics.Texture;
import com.github.adisann.pokemon.battle.BATTLE_PARTY;

/**
 * A BattleEvent where a Pokemon's sprite is changed.
 * This event takes no time.
 * */
public class PokeSpriteEvent extends BattleEvent {

	private String spriteName;
	private BATTLE_PARTY party;

	public PokeSpriteEvent(String spriteName, BATTLE_PARTY party) {
		this.spriteName = spriteName;
		this.party = party;
	}

	@Override
	public void begin(BattleEventPlayer player) {
		super.begin(player);
		player.setPokemonSprite(spriteName, party);
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public boolean finished() {
		return true;
	}

}
