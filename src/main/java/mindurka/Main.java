// Welcome to MindurkaCompat's source code, brave one!
//
// The land of endless frustration, and thus with appropriate comments scattered around.

package mindurka;

import arc.Events;
import mindustry.game.EventType;
import mindustry.mod.Plugin;

public class Main extends Plugin {
    @Override
    public void init() {
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
