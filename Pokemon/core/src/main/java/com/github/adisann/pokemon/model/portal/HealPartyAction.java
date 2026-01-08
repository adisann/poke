package com.github.adisann.pokemon.model.portal;

import com.github.adisann.pokemon.model.actor.PlayerActor;
import com.github.adisann.pokemon.model.world.cutscene.CutscenePlayer;

/**
 * Action that heals the player's Pokemon party when entering a portal.
 * 
 * This replaces the hard-coded check in ChangeWorldEvent that healed
 * the party when entering "test_map_indoor" (Mom's house).
 */
public class HealPartyAction implements PortalAction {
    
    @Override
    public void execute(PlayerActor player, CutscenePlayer cutscenes) {
        cutscenes.healPlayerTeam();
        System.out.println("Party healed by HealPartyAction");
    }
}
