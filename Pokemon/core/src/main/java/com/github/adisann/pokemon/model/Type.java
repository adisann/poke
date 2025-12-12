package com.github.adisann.pokemon.model;

/**
 * Elemental types for Pokemon and Moves.
 */
public enum Type {
    NORMAL,
    FIRE,
    WATER,
    GRASS,
    ELECTRIC,
    ICE,
    FIGHTING,
    POISON,
    GROUND,
    FLYING,
    PSYCHIC,
    BUG,
    ROCK,
    GHOST,
    DRAGON,
    STEEL,
    DARK;

    public static float getEffectiveness(Type moveType, Type targetType) {
        if (moveType == null || targetType == null)
            return 1f;

        // Simple effectiveness chart (subset for demonstration, expandable)
        switch (moveType) {
            case FIRE:
                if (targetType == GRASS || targetType == ICE || targetType == BUG || targetType == STEEL)
                    return 2f;
                if (targetType == FIRE || targetType == WATER || targetType == ROCK || targetType == DRAGON)
                    return 0.5f;
                break;
            case WATER:
                if (targetType == FIRE || targetType == GROUND || targetType == ROCK)
                    return 2f;
                if (targetType == WATER || targetType == GRASS || targetType == DRAGON)
                    return 0.5f;
                break;
            case GRASS:
                if (targetType == WATER || targetType == GROUND || targetType == ROCK)
                    return 2f;
                if (targetType == FIRE || targetType == GRASS || targetType == POISON || targetType == FLYING
                        || targetType == BUG || targetType == DRAGON || targetType == STEEL)
                    return 0.5f;
                break;
            case ELECTRIC:
                if (targetType == WATER || targetType == FLYING)
                    return 2f;
                if (targetType == ELECTRIC || targetType == GRASS || targetType == DRAGON)
                    return 0.5f;
                if (targetType == GROUND)
                    return 0f;
                break;
            case NORMAL:
            case ICE:
            case FIGHTING:
            case POISON:
            case GROUND:
            case FLYING:
            case PSYCHIC:
            case BUG:
            case ROCK:
            case GHOST:
            case DRAGON:
            case STEEL:
            case DARK:
            default:
                break;
            // ... Add more as needed
        }
        return 1f;
    }
}
