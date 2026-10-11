package com.mod.htgt6.common.handler.recipes.modpackgenerated;

/**
 * Safety profile for recipes generated from the modpack scripts.
 *
 * Missing optional references use WARN and crafting recipes are restricted to
 * the vanilla 3x3 grid. The legacy scripts have been removed, so HTGT6 is the
 * authoritative runtime source for the migrated modpack recipes.
 */
public final class GeneratedRecipeSettings {

    public enum State {
        DISABLED,
        ENABLED
    }

    public enum MissingReferencePolicy {
        IGNORE,
        WARN,
        FAIL
    }

    public static final GeneratedRecipeSettings DEFAULT = new GeneratedRecipeSettings(
            State.ENABLED,
            MissingReferencePolicy.WARN,
            3,
            3);

    private final State state;
    private final MissingReferencePolicy missingReferencePolicy;
    private final int maximumWidth;
    private final int maximumHeight;

    public GeneratedRecipeSettings(State state, MissingReferencePolicy missingReferencePolicy,
            int maximumWidth, int maximumHeight) {
        if (state == null) throw new IllegalArgumentException("state");
        if (missingReferencePolicy == null) throw new IllegalArgumentException("missingReferencePolicy");
        if (maximumWidth < 1 || maximumWidth > 3 || maximumHeight < 1 || maximumHeight > 3) {
            throw new IllegalArgumentException("Crafting grid must fit inside 3x3");
        }
        this.state = state;
        this.missingReferencePolicy = missingReferencePolicy;
        this.maximumWidth = maximumWidth;
        this.maximumHeight = maximumHeight;
    }

    public boolean isEnabled() {
        return state == State.ENABLED;
    }

    public State getState() {
        return state;
    }

    public MissingReferencePolicy getMissingReferencePolicy() {
        return missingReferencePolicy;
    }

    public int getMaximumWidth() {
        return maximumWidth;
    }

    public int getMaximumHeight() {
        return maximumHeight;
    }
}
