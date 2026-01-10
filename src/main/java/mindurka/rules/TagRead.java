package mindurka.rules;

import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Rules;
import mindustry.world.Block;

public class TagRead implements AutoCloseable {
    private TagRead() {}

    private Rules rules;
    private final Seq<Rules> prevRules = new Seq<>();

    private static final TagRead read = new TagRead();

    public static TagRead of(Rules rules) {
        if (read.rules != null) read.prevRules.add(read.rules);
        read.rules = rules;
        return read;
    }

    @Override
    public void close() {
        rules = null;
        if (prevRules.size > 0) rules = prevRules.pop();
    }

    public int r(String key, int de) {
        if (rules == null) return de;
        return rules.tags.getInt(key, de);
    }
    public float r(String key, float de) {
        if (rules == null) return de;
        return rules.tags.getFloat(key, de);
    }
    public boolean r(String key, boolean de) {
        if (rules == null) return de;
        String value = rules.tags.get(key);
        if (value == null) return de;
        return value.equals("true");
    }
    public Block r(String key, Block de) {
        if (rules == null) return de;
        String value = rules.tags.get(key);
        if (value == null) return de;
        Block block = Vars.content.block(value);
        if (block == null) return de;
        return block;
    }
    public String r(String key, String de) {
        if (rules == null) return de;
        String value = rules.tags.get(key);
        if (value == null) return de;
        return value;
    }
}
