package com.github.adisann.pokemon.battle.event;

import com.badlogic.gdx.math.Interpolation;
import com.github.adisann.pokemon.battle.BATTLE_PARTY;
import com.github.adisann.pokemon.ui.DetailedStatusBox;
import com.github.adisann.pokemon.ui.HPBar;
import com.github.adisann.pokemon.ui.StatusBox;

/**
 * A BattleEvent where HP can be seen, depleting.
 * */
public class HPAnimationEvent extends BattleEvent {

	private BATTLE_PARTY party;

	private int hpBefore;
	private int hpAfter;
	private int hpTotal;
	private float duration;

	private BattleEventPlayer eventPlayer;
	private float timer;
	private boolean finished;

	public HPAnimationEvent(BATTLE_PARTY party, int hpBefore, int hpAfter, int hpTotal, float duration) {
		this.party = party;
		this.hpBefore = hpBefore;
		this.hpAfter = hpAfter;
		this.hpTotal = hpTotal;
		this.duration = duration;
		this.timer = 0f;
		this.finished = false;
	}

	@Override
	public void update(float delta) {
		timer += delta;
		if (timer > duration) {
			finished = true;
		}

		float progress = (duration > 0) ? timer / duration : 1f;
		float hpProgress = Interpolation.linear.apply(hpBefore, hpAfter, Math.min(progress, 1f));
		float hpProgressRelative = (hpTotal > 0) ? hpProgress / hpTotal : 0f;

		HPBar hpbar = eventPlayer.getStatusBox(party).getHPBar();
		hpbar.displayHPLeft(hpProgressRelative);

		StatusBox statusBox = eventPlayer.getStatusBox(party);
		if (statusBox instanceof DetailedStatusBox) {
			((DetailedStatusBox) statusBox).setHPText((int) hpProgress, hpTotal);
		}
	}

	@Override
	public void begin(BattleEventPlayer player) {
		super.begin(player);
		this.eventPlayer = player;
	}

	@Override
	public boolean finished() {
		return finished;
	}

}