package mindurka;

import arc.Events;
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
            MVars.rules.sync();
            Stats.updateStats();
        });

        Events.on(EventType.ClientLoadEvent.class, event -> {
            MIcons.load();
            Injects.load();
        });

        // TODO: Packet bullshit (i.e. fill).
    }
}
