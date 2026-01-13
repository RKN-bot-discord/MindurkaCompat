package mindurka.rules;

import arc.graphics.Color;
import mindustry.game.Team;

// I did have fun with those yes
public enum FortsPlotState {
    /** Plot is disabled and will be removed from the map as the game starts.
     *  <p>
     *  This is the only plot type that does not respect the assigned team.
     */
    disabled,
    /** Plot can be placed. */
    enabled,
    /** Plot is placed. */
    placed,
    /**
     * Plot is placed and cannot be destroyed while team still exists. Otherwise,
     * acts as if plot is {@link #placed}.
     */
    locked,
    /** Plot cannot be destroyed. */
    static_,
    /** Game pretends there's a plot there. */
    ghost,

    ;

    private static final Color[] stroke = new Color[] {
            new Color(0x555555ff),
            new Color(0xe0a8a2ff),
            new Color(0xe0a8a2ff),
            Color.cyan,
            new Color(0xffffffff),
            new Color(0xccccccaa),
    };
    private static final Color[] fill = new Color[] {
            Color.clear,
            Color.clear,
            null,
            null,
            null,
            Color.clear,
    };

    @Override
    public String toString() {
        if (this == static_) return "static";
        return name();
    }

    public Color stroke() { return stroke[ordinal()]; }
    public Color fill(Color copyTo, Team team) {
        Color color = copyTo == null ? new Color() : copyTo;
        Color target = fill[ordinal()];
        if (target == null) target = team.color;
        color.set(target);
        if (color.a > 0.4f) color.a = 0.4f;
        return color;
    }

    public boolean placed() {
        switch (this) {
            case placed:
            case locked:
            case static_:
                return true;
            default:
                return false;
        }
    }
}
