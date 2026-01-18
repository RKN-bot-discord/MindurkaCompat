package mindurka.rules;

import arc.Core;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindurka.MVars;
import mindustry.Vars;
import mindustry.game.Rules;
import mindustry.maps.Map;
import mindustry.mod.DataPatcher;

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
    public static final String GAMEMODE = PREFIX+".gamemode";
    public static final String GAMEMODE_LEGACY = "mindurkaGamemode"; // Does not use `mdrk.*` convention as it's a legacy key.
                                                                     // But it's a great legacy, so we depend on it.

    private final Rules rules;
    private final int mapWidth, mapHeight;

    public MRules(Rules rules) { this(rules, Vars.world.width(), Vars.world.height()); }
    public MRules(Rules rules, int mapWidth, int mapHeight) {
        this.rules = rules;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

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
                gamemodeName = rules.tags.get(GAMEMODE_LEGACY);
                rules.tags.put(GAMEMODE, gamemodeName);
            }
            if (gamemodeName == null) {
                Vars.ui.showErrorMessage("MindurkaCompat: Format version 1 requires gamemode to be specified.");
                return;
            }
            @Nullable Gamemode factory = Gamemode.forName(gamemodeName);
            if (factory == null) {
                Log.err("Unknown gamemode '" + gamemodeName + "', some features may not be supported.");
                gamemode = Gamemode.UNKNOWN.create(newRulesContext());
            }
            else gamemode = factory.create(newRulesContext());
        }
    }

    private RulesContext newRulesContext() {
        return new RulesContext(this, rules, mapWidth, mapHeight);
    }

    private void remove() {
        rules.tags.remove(FORMAT);
        rules.tags.remove(GAMEMODE);
        rules.tags.remove(GAMEMODE_LEGACY);
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
        a: {
            if (Vars.state.patcher.patches.size == 0) break a;
            DataPatcher.PatchSet patches = Vars.state.patcher.patches.first();
            if (!patches.name.equals("Mindurka Default Patch")) break a;
            Vars.state.patcher.patches.remove(0);
        }

        if (gamemode != null && (gamemode.factory() != newValue || !Core.input.shift())) gamemode.remove();
        if (newValue == null) remove();
        else {
            if (!Core.input.shift() || gamemode.factory() != newValue) gamemode = newValue.create(newRulesContext());
            rules.tags.put(FORMAT, FORMAT_VER);
            rules.tags.put(GAMEMODE, newValue.name());
            rules.tags.put(GAMEMODE_LEGACY, newValue.name());
            rules.tags.put(PATCH, PATCH_VER);
            if (!Core.input.shift()) gamemode.setRules();
        }
        if (MVars.editorDialog.isShown()) MVars.editorDialog.refreshTools();

        try {
            Seq<String> patches = Vars.state.patcher.patches.map(x -> x.patch);
            b: if (gamemode != null) {
                String patch = gamemode.builtInContentPatch();
                if (patch == null) break b;
                patches.add("name: Mindurka Default Patch\n" + patch);
                Log.info("Should have applied a patch");
            }
            Vars.state.patcher.apply(patches);
        } catch (Exception error) {
            Log.err(error);
            Vars.ui.showException(error);
        }

        return this;
    }
}
