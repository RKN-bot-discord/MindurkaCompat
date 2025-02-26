package mindurka;

import arc.Events;
import mindustry.game.EventType;
import mindustry.mod.Plugin;

public class Main extends Plugin {
    @Override
    public void init() {
        Events.on(EventType.WorldLoadEndEvent.class, event -> {
            Stats.updateStats();
        });

        Injects.load();
    }
}
