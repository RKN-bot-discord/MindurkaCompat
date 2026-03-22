package mindurka.rules;

import mindurka.util.Schematic;
import mindustry.game.Rules;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;

public class TagWrite implements AutoCloseable {
    private Rules rules;

    private static final TagWrite write = new TagWrite();

    public static TagWrite of(Rules rules) {
        write.rules = rules;
        return write;
    }

    @Override
    public void close() {
        rules = null;
    }

    public void w(String key, int v) {
        if (rules == null) return;
        rules.tags.put(key, Integer.toString(v));
    }
    public void w(String key, float v) {
        if (rules == null) return;
        rules.tags.put(key, Float.toString(v));
    }
    public void w(String key, boolean v) {
        if (rules == null) return;
        rules.tags.put(key, Boolean.toString(v));
    }
    public void w(String key, Block v) {
        if (rules == null) return;
        rules.tags.put(key, v.name);
    }
    public void w(String key, UnitType v) {
        if (rules == null) return;
        rules.tags.put(key, v.name);
    }
    public void w(String key, Item v) {
        if (rules == null) return;
        rules.tags.put(key, v.name);
    }
    public void w(String key, StatusEffect v) {
        if (rules == null) return;
        rules.tags.put(key, v.name);
    }
    public void w(String key, String v) {
        if (rules == null) return;
        rules.tags.put(key, v);
    }
    public void w(String key, Schematic v) {
        if (rules == null) return;
        rules.tags.put(key, v.serialize());
    }
}
