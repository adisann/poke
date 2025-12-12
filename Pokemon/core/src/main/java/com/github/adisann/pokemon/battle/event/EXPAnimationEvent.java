package com.github.adisann.pokemon.battle.event;

import com.badlogic.gdx.math.Interpolation;
import com.github.adisann.pokemon.battle.BATTLE_PARTY;
import com.github.adisann.pokemon.model.Pokemon;
import com.github.adisann.pokemon.ui.DetailedStatusBox;
import com.github.adisann.pokemon.ui.StatusBox;

/**
 * A BattleEvent that animates EXP gain and updates the EXP bar.
 */
public class EXPAnimationEvent extends BattleEvent {

    private int expBefore;
    private int expAfter;
    private int expToNext;
    private int newLevel;
    private float duration;

    private BattleEventPlayer eventPlayer;
    private float timer;
    private boolean finished;

    /**
     * @param expBefore EXP before gain
     * @param expAfter  EXP after gain
     * @param expToNext EXP needed for next level
     * @param newLevel  New level after potential level up
     * @param duration  Animation duration in seconds
     */
    public EXPAnimationEvent(int expBefore, int expAfter, int expToNext, int newLevel, float duration) {
        this.expBefore = expBefore;
        this.expAfter = expAfter;
        this.expToNext = expToNext;
        this.newLevel = newLevel;
        this.duration = duration;
        this.timer = 0f;
        this.finished = false;
    }

    @Override
    public void begin(BattleEventPlayer player) {
        super.begin(player);
        this.eventPlayer = player;
    }

    @Override
    public void update(float delta) {
        timer += delta;
        if (timer > duration) {
            finished = true;
            timer = duration;
        }

        float progress = (duration > 0) ? timer / duration : 1f;
        int currentExp = (int) Interpolation.linear.apply(expBefore, expAfter, Math.min(progress, 1f));

        StatusBox statusBox = eventPlayer.getStatusBox(BATTLE_PARTY.PLAYER);
        if (statusBox instanceof DetailedStatusBox) {
            DetailedStatusBox detailedBox = (DetailedStatusBox) statusBox;
            detailedBox.setEXPText(currentExp, expToNext);

            // Also update name/level if leveled up
            if (finished && newLevel > 0) {
                Pokemon playerPoke = eventPlayer.getPlayerPokemon();
                if (playerPoke != null) {
                    detailedBox.setNameAndLevel(playerPoke.getName(), newLevel);
                }
            }
        }
    }

    @Override
    public boolean finished() {
        return finished;
    }
}
