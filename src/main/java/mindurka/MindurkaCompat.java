package mindurka;

import arc.Events;
import mindurka.rules.MRules;
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
            MVars.rules = new MRules(Vars.state.rules, Vars.state.map);
        });

        Events.on(EventType.ClientLoadEvent.class, event -> {
            MIcons.load();
            Injects.load();
        });

        // TODO: Packet bullshit (i.e. fill).
    }
}
