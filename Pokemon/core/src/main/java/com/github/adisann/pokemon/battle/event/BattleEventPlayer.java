package com.github.adisann.pokemon.battle.event;

import com.badlogic.gdx.graphics.Texture;
import com.github.adisann.pokemon.battle.BATTLE_PARTY;
import com.github.adisann.pokemon.battle.animation.BattleAnimation;
import com.github.adisann.pokemon.ui.DialogueBox;
import com.github.adisann.pokemon.ui.StatusBox;

import aurelienribon.tweenengine.TweenManager;

/**
 * @author hydrozoa
 */
public interface BattleEventPlayer {
	
	public void playBattleAnimation(BattleAnimation  animation, BATTLE_PARTY party);
	
	public void setPokemonSprite(Texture region, BATTLE_PARTY party);
	
	public DialogueBox getDialogueBox();
	
	public StatusBox getStatusBox(BATTLE_PARTY party);
	
	public BattleAnimation  getBattleAnimation();
	
	public TweenManager getTweenManager();
	
	public void queueEvent(BattleEvent event);
}


