package com.github.adisann.pokemon.battle;

import java.util.ArrayList;
import java.util.List;

import com.github.adisann.pokemon.model.Pokemon;

/** */
public class Trainer {

	private List<Pokemon> team;

	public Trainer(Pokemon pokemon) {
		team = new ArrayList<Pokemon>();
		team.add(pokemon);
	}

	public boolean addPokemon(Pokemon pokemon) {
		if (team.size() >= 6) {
			return false;
		} else {
			team.add(pokemon);
			return true;
		}
	}

	public Pokemon getPokemon(int index) {
		return team.get(index);
	}

	public int getTeamSize() {
		return team.size();
	}

	public List<Pokemon> getTeam() {
		return team;
	}

	/**
	 * Heals all Pokemon in the team to full HP.
	 */
	public void healAll() {
		for (Pokemon pokemon : team) {
			pokemon.setCurrentHitpoints(pokemon.getStat(STAT.HITPOINTS));
		}
	}
}