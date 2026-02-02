package mindurka;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Reflect;
import mindurka.rules.MRules;
import mindurka.util.Report;
import mindustry.Vars;
import mindustry.game.EventType;

public class MindurkaCompat {
    private MindurkaCompat() {}

    private static boolean initialized = false;
    public static void init() {
        if (initialized) return;
        initialized = true;

        try { // It ain't a server mod. Hop on Mindurka now, it's free!
            Class.forName("mindustry.server.ServerControl");
            return;
        } catch (ClassNotFoundException ignore) {}

        Events.on(EventType.WorldLoadEndEvent.class, event -> {
            MVars.rules = new MRules(Vars.state.rules, Vars.world.width(), Vars.world.height());
            if (MVars.rules.gamemode() != null && !MVars.mapEditor.isLoading()) MVars.rules.gamemode().onStart();
        });

        Events.on(EventType.ClientLoadEvent.class, event -> {
            MVars.patchEditorLoaded = Vars.mods.getMod("patch-editor") != null;

            MIcons.load();
            if (MVars.patchEditorLoaded) {
                // MindurkaCompat MUST apply patches last for consistent results.
                // Or does that mean patch editor goes last? Who knows!
                Core.app.post(() -> Core.app.post(() -> {
                    Injects.load();
                    try {
                        Class<?> eui = Class.forName("MinRi2.PatchEditor.ui.EUI");
                        Reflect.invoke(eui, null, "addUI", Util.noargs);
                    } catch (Throwable e) {
                        Report.withException(e);
                    }
                }));
            } else Injects.load();
        });
    }
}