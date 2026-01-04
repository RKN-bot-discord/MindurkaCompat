package mindurka;

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
    protected_,
    /** Plot cannot be destroyed. */
    locked,
    /** Game pretends there's a plot there. */
    ghost,
    /** Only specified team can place a plot. */
    restricted,

    ;

    @Override
    public String toString() {
        if (this == protected_) return "protected";
        return name();
    }
}
