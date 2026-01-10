package mindurka.rules;

import arc.util.Log;
import arc.util.Nullable;
import mindurka.MVars;
import mindustry.Vars;
import mindustry.game.Rules;
import mindustry.maps.Map;

// What I've learned:
// - Fuck saving object allocations.
// - Enforce strong variants.
// - This could literally be a JavaScript file and be just as good in terms of performance, none of this is
//   performance critical.
// - `save()` and `sync()` are useless if point 2 is true. `sync()` is your constructor, and `save()` are your methods.
//   In fact, this actually saves performance cuz we aren't re-saving this entire disaster.

public class MRules {
    public static final String PREFIX = "mdrk";
    public static final String FORMAT = PREFIX+".format";
    // Used
    public static final String PATCH = PREFIX+".patch";
    public static final String FORMAT_VER = "1";
    public static final String PATCH_VER = "1";
    public static final String GAMEMODE = "mindurkaGamemode"; // Does not use `mdrk.*` convention as it's a legacy key.
                                                              // But it's a great legacy, so we depend on it.

    private final Rules rules;
    private final Map map;

    public MRules(Map map) {
        this(map.rules(), map);
    }
    public MRules(Rules rules, Map map) {
        this.rules = rules;
        this.map = map;

        {
            @Nullable String format = rules.tags.get(FORMAT);
            if (format == null) {
                return;
            }
            if (!format.equals(FORMAT_VER)) {
                Vars.ui.showErrorMessage("MindurkaCompat: Invalid format verison " + FORMAT_VER);
                return;
            }
        }

        {
            @Nullable String gamemodeName = rules.tags.get(GAMEMODE);
            if (gamemodeName == null) {
                Vars.ui.showErrorMessage("MindurkaCompat: Format version 1 requires gamemode to be specified.");
                return;
            }
            @Nullable Gamemode factory = Gamemode.forName(gamemodeName);
            if (factory == null) {
                Log.err("Unknown gamemode '" + gamemodeName + "', some features may not be supported.");
                gamemode = Gamemode.UNKNOWN.create(this, rules, map);
            }
            else gamemode = factory.create(this, rules, map);
        }
    }

    private void remove() {
        rules.tags.remove(FORMAT);
        rules.tags.remove(GAMEMODE);
        rules.tags.remove(PATCH);

        if (gamemode != null) {
            gamemode.remove();
            gamemode = null;
        }
    }

    private @Nullable Gamemode.Impl gamemode;
    public @Nullable Gamemode.Impl gamemode() { return gamemode; }
    public @Nullable Gamemode gamemodeFactory() { return gamemode == null ? null : gamemode.factory(); }
    public MRules gamemode(@Nullable Gamemode newValue) {
        if (gamemode != null) gamemode.remove();
        if (newValue == null) remove();
        else {
            gamemode = newValue.create(this, rules, map);
            rules.tags.put(FORMAT, FORMAT_VER);
            rules.tags.put(GAMEMODE, newValue.name());
            rules.tags.put(PATCH, PATCH_VER);
        }
        if (MVars.editorDialog.isShown()) MVars.editorDialog.refreshTools();
        return this;
    }
}
