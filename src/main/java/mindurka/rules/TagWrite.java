package mindurka.rules;

import mindustry.game.Rules;
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
}
