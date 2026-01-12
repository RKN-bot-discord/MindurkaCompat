package mindurka.rules;

import arc.util.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import mindurka.ui.RulesWrite;
import mindustry.game.Team;

public abstract class FortsPlotKind {
    public static final String PREFIX = Forts.PREFIX+".plot";

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract class Impl {
        protected final RulesContext rc;

        public final FortsPlotKind factory() { return FortsPlotKind.this; }
        public final String name() { return factory().name(); }

        abstract void remove();

        public abstract void writeRules(RulesWrite write);
        public abstract void drawEditorGuides();
        public abstract void setPlotInfo(int x, int y, FortsPlotState state, Team team);
    }

    public abstract String name();
    abstract Impl create(RulesContext rc);

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
                    Impl create(RulesContext rc) {
                        return new Impl(rc) {
                            @Override void remove() {}
                            @Override public void writeRules(RulesWrite write) {}
                            @Override public void drawEditorGuides() {}
                            @Override public void setPlotInfo(int x, int y, FortsPlotState state, Team team) {}
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
