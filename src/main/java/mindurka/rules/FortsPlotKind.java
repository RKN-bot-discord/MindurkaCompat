package mindurka.rules;

import arc.util.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import mindustry.game.Rules;
import mindustry.maps.Map;

public abstract class FortsPlotKind {
    public static final String PREFIX = Forts.PREFIX+".plot";

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract class Impl {
        protected final MRules customRules;
        protected final Rules rules;
        protected final Map map;

        public final FortsPlotKind factory() { return FortsPlotKind.this; }
        public final String name() { return factory().name(); }

        abstract void remove();
    }

    public abstract String name();
    abstract Impl create(MRules customRules, Rules rules, Map map);

    private static FortsPlotKind[] plotKinds;

    private static boolean initialized = false;
    public static void init() {
        if (initialized) throw new IllegalStateException("Already initialized");
        initialized = true;

        plotKinds = new FortsPlotKind[] {
                new FortsPlotKindSquare(),
                new FortsPlotKind() {
                    @Override
                    public String name() {
                        return "none";
                    }

                    @Override
                    Impl create(MRules customRules, Rules rules, Map map) {
                        return new Impl(customRules, rules, map) {
                            @Override void remove() {}
                        };
                    }
                },
        };

    }

    public static @Nullable FortsPlotKind forName(String name) {
        for (FortsPlotKind factory : plotKinds) if (factory.name().equals(name)) return factory;
        return null;
    }

    public static FortsPlotKind[] values() {
        return plotKinds;
    }
}
