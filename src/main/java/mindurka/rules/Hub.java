package mindurka.rules;

import mindustry.Vars;

public class Hub extends Gamemode {
    public static final String PREFIX = MRules.PREFIX+".hub";
    public static final String NAME = "hub";

    public static final String SERVERS = PREFIX+".servers";

    @Override
    public String name() { return NAME; }

    @Override
    Impl create(RulesContext rc) {
        return new Impl(rc);
    }

    public class Impl extends Gamemode.Impl {
        protected Impl(RulesContext rc) {
            super(rc);
        }

        @Override
        void remove() {
            rc.rules.tags.remove(SERVERS);
        }

        @Override
        protected void _setRules() {
            rc.rules.revealedBlocks.clear();
            rc.rules.bannedBlocks.addAll(Vars.content.blocks().select(x -> !x.isHidden()));
            rc.rules.hideBannedBlocks = true;
            rc.rules.waves = false;
            rc.rules.loadout.clear();
            rc.rules.reactorExplosions = false;
            rc.rules.canGameOver = false;
            rc.rules.modeName = "Hub";
            rc.customRules.overdriveIgnoresCheat(false);
        }
    }
}
