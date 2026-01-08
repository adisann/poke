package com.github.adisann.pokemon.model;

public enum StatusCondition {
    NONE,
    BURN,
    FREEZE,
    PARALYSIS,
    POISON,
    SLEEP;

    public boolean canMove() {
        // Simple logic stub
        if (this == FREEZE || this == SLEEP)
            return false;
        if (this == PARALYSIS)
            return Math.random() > 0.25;
        return true;
    }
}
