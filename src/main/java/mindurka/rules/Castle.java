package mindurka.rules;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.math.geom.Point2;
import mindurka.ui.RulesWrite;
import mindurka.util.Schematic;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.Env;

import static mindustry.Vars.state;

public class Castle extends Gamemode {

    public static final String PREFIX = MRules.PREFIX+".castle";
    public static final String UTILS = PREFIX+".utils.";
    public static final String TURRET = PREFIX+".turret.";
    public static final String UNIT = PREFIX+".unit.";
    public static final String ITEM = PREFIX+".item.";
    public static final String STATUS = PREFIX+".status.";
    public static final String NAME = "castle";
    public static final String COST = ".cost";
    public static final String INCOME = ".income";
    public static final String INTERVAL = ".interval";
    public static final String AMOUNT = ".amount";
    public static final String DROP = ".drop";
    public static final String DURATION = ".duration";
    public static final String ALLY = ".ally";
    public static final String DELAY = ".delay";
    public static final String DRILL =  ".drill";

    public static final int PLATFORM_SIZE = 5;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    Impl create(RulesContext rc) {
        return new Impl(rc);
    }

    public class Impl extends Gamemode.Impl {
        private Impl(RulesContext rc) {
            super(rc);

            try (TagRead read = TagRead.of(rc.rules)) {
                CastleCosts.load();
                shopFloor = read.r(UTILS+"shopFloor", Blocks.empty);
                groundSpawn = read.r(UTILS+"groundSpawn", new Seq<>());
                navalSpawn = read.r(UTILS+"navalSpawn", new Seq<>());
                airSpawn = read.r(UTILS+"airSpawn", new Seq<>());
                attackUnitCap = read.r(UTILS+"attackUnitCap", -1);
                defenseUnitCap = read.r(UTILS+"defenseUnitCap", 75);
                divideCap = read.r(UTILS+"divideCap", true);
                betterGroundValid = read.r(UTILS+"betterGroundValid", true);
                noPlatform = read.r(UTILS+"noPlatform", false);
                platformSource = new Seq<>();
                int psCount = read.r(UTILS+"platformSource.count", 0);
                for (int i = 0; i < psCount; i++) {
                    platformSource.add(read.r(UTILS+"platformSource."+i, Schematic.EMPTY));
                }
            }
        }

        @Override
        public void writeGamemodeRules(RulesWrite write) {
            write.tree("@rules.mindurka.castle.turret", turretRoot -> {
                for (Block b : mindustry.Vars.content.blocks()) {
                    if (!(b instanceof Turret)) continue;
                    Turret t = (Turret) b;
                    turretRoot.tree(t.localizedName + t.emoji(), cfg -> {
                        cfg.i("rules.mindurka.castle.turret.cost",
                                () -> blockCostFor(t),
                                v -> blockCostFor(t, v));
                    });
                }
            });
            write.tree("@rules.mindurka.castle.unit", unitRoot -> {
                for (UnitType u : mindustry.Vars.content.units()) {
                    unitRoot.tree(u.localizedName + u.emoji(), cfg -> {
                        cfg.i("rules.mindurka.castle.unit.cost",
                                () -> unitCostFor(u),
                                v -> unitCostFor(u, v));
                        cfg.i("rules.mindurka.castle.unit.income",
                                () -> unitIncomeFor(u),
                                v -> unitIncomeFor(u, v));
                        cfg.i("rules.mindurka.castle.unit.drop",
                                () -> unitDropFor(u),
                                v -> unitDropFor(u, v));
                    });
                }
            });
            write.tree("@rules.mindurka.castle.item", itemRoot -> {
                for (Item i : mindustry.Vars.content.items()) {
                    itemRoot.tree(i.localizedName + i.emoji(), cfg -> {
                        cfg.i("rules.mindurka.castle.item.cost",
                                () -> itemCostFor(i),
                                v -> itemCostFor(i, v));
                        cfg.i("rules.mindurka.castle.item.interval",
                                () -> itemIntervalFor(i),
                                v -> itemIntervalFor(i, v));
                        cfg.i("rules.mindurka.castle.item.amount",
                                () -> itemAmountFor(i),
                                v -> itemAmountFor(i, v));
                        cfg.block("rules.mindurka.castle.drill",
                                d -> drillFor(i, d),
                                () -> drillFor(i)).filter(d -> true);
                    });
                }
            });
            write.tree("@rules.mindurka.castle.status", statusRoot -> {
                for (StatusEffect s : mindustry.Vars.content.statusEffects()) {
                    statusRoot.tree(s.localizedName + s.emoji(), cfg -> {
                        cfg.i("rules.mindurka.castle.status.cost",
                                () -> statusCostFor(s),
                                v -> statusCostFor(s, v));
                        cfg.i("rules.mindurka.castle.status.delay",
                                () -> statusDelayFor(s),
                                v -> statusDelayFor(s, v));
                        cfg.i("rules.mindurka.castle.status.duration",
                                () -> statusDurationFor(s),
                                v -> statusDurationFor(s, v));
                        cfg.b("rules.mindurka.castle.status.ally",
                                () -> statusAllyFor(s),
                                v -> statusAllyFor(s, v));
                    });
                }
            });

            write.spacer();

            write.points("rules.mindurka.castle.groundSpawn", this::groundSpawn, this::groundSpawn);
            write.points("rules.mindurka.castle.airSpawn",    this::airSpawn,    this::airSpawn);
            write.points("rules.mindurka.castle.navalSpawn",  this::navalSpawn,  this::navalSpawn);
            write.spacer();

            write.i("rules.mindurka.castle.defenseUnitCap", this::defenseUnitCap, this::defenseUnitCap);
            write.i("rules.mindurka.castle.attackUnitCap",  this::attackUnitCap,  this::attackUnitCap);
            write.b("rules.mindurka.castle.divideCap",      this::divideCap,      this::divideCap);
            write.spacer();

            write.platformSources("rules.mindurka.castle.platformSource",
                    this::platformSource,
                    this::addPlatformSource,
                    this::removePlatformSource);
            write.spacer();

            write.b("rules.mindurka.castle.noPlatform",       this::noPlatform,       this::noPlatform);
            write.b("rules.mindurka.castle.betterGroundValid", this::betterGroundValid, this::betterGroundValid);
            write.block("rules.mindurka.castle.shopFloor",    this::shopFloor,        this::shopFloor);
        }

        @Override
        void remove() {
            final Rules rules = rc.rules;
            Seq<String> toRemove = new Seq<>();
            rules.tags.each((key, value) -> {
                if (key.startsWith("mdrk.castle.")) toRemove.add(key);
            });
            toRemove.each(rules.tags::remove);
        }

        public int blockCostFor(Block b) {
            try (TagRead read = TagRead.of(rc.rules)) {
                return read.r(TURRET + b + COST, CastleCosts.turrets.get((Turret) b, 0));
            }
        }
        public Impl blockCostFor(Block b, int value) {
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(TURRET + b + COST, value); }
            return this;
        }

        private final ObjectMap<UnitType, Integer> unitCostMap   = new ObjectMap<>();
        private final ObjectMap<UnitType, Integer> unitIncomeMap = new ObjectMap<>();
        private final ObjectMap<UnitType, Integer> unitDropMap   = new ObjectMap<>();

        public int unitCostFor(UnitType u) {
            return unitCostMap.get(u, () -> {
                CastleCosts.UnitData d = CastleCosts.units.get(u);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(UNIT + u + COST, d != null ? d.cost() : 0);
                }
            });
        }
        public Impl unitCostFor(UnitType u, int value) {
            unitCostMap.put(u, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(UNIT + u + COST, value); }
            return this;
        }

        public int unitIncomeFor(UnitType u) {
            return unitIncomeMap.get(u, () -> {
                CastleCosts.UnitData d = CastleCosts.units.get(u);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(UNIT + u + INCOME, d != null ? d.income() : 0);
                }
            });
        }
        public Impl unitIncomeFor(UnitType u, int value) {
            unitIncomeMap.put(u, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(UNIT + u + INCOME, value); }
            return this;
        }

        public int unitDropFor(UnitType u) {
            return unitDropMap.get(u, () -> {
                CastleCosts.UnitData d = CastleCosts.units.get(u);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(UNIT + u + DROP, d != null ? d.drop() : 0);
                }
            });
        }
        public Impl unitDropFor(UnitType u, int value) {
            unitDropMap.put(u, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(UNIT + u + DROP, value); }
            return this;
        }

        private final ObjectMap<Item, Integer> itemCostMap     = new ObjectMap<>();
        private final ObjectMap<Item, Integer> itemIntervalMap = new ObjectMap<>();
        private final ObjectMap<Item, Integer> itemAmountMap   = new ObjectMap<>();
        private final ObjectMap<Item, Block>   drillMap        = new ObjectMap<>();

        public int itemCostFor(Item i) {
            return itemCostMap.get(i, () -> {
                CastleCosts.ItemData d = CastleCosts.items.get(i);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(ITEM + i + COST, d != null ? d.cost() : 0);
                }
            });
        }
        public Impl itemCostFor(Item i, int value) {
            itemCostMap.put(i, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(ITEM + i + COST, value); }
            return this;
        }

        public int itemIntervalFor(Item i) {
            return itemIntervalMap.get(i, () -> {
                CastleCosts.ItemData d = CastleCosts.items.get(i);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(ITEM + i + INTERVAL, d != null ? d.interval() : 0);
                }
            });
        }
        public Impl itemIntervalFor(Item i, int value) {
            itemIntervalMap.put(i, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(ITEM + i + INTERVAL, value); }
            return this;
        }

        public int itemAmountFor(Item i) {
            return itemAmountMap.get(i, () -> {
                CastleCosts.ItemData d = CastleCosts.items.get(i);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(ITEM + i + AMOUNT, d != null ? d.amount() : 0);
                }
            });
        }
        public Impl itemAmountFor(Item i, int value) {
            itemAmountMap.put(i, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(ITEM + i + AMOUNT, value); }
            return this;
        }

        public Block drillFor(Item i) {
            return drillMap.get(i, () -> {
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(ITEM + i + DRILL, drillGet(i));
                }
            });
        }
        public Impl drillFor(Item i, Block value) {
            drillMap.put(i, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(ITEM + i + DRILL, value); }
            return this;
        }

        private final ObjectMap<StatusEffect, Integer> statusDelayMap    = new ObjectMap<>();
        private final ObjectMap<StatusEffect, Integer> statusDurationMap = new ObjectMap<>();
        private final ObjectMap<StatusEffect, Integer> statusCostMap     = new ObjectMap<>();
        private final ObjectMap<StatusEffect, Boolean> statusAllyMap     = new ObjectMap<>();

        public int statusCostFor(StatusEffect s) {
            return statusCostMap.get(s, () -> {
                CastleCosts.EffectData d = CastleCosts.effects.get(s);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(STATUS + s + COST, d != null ? d.cost() : 0);
                }
            });
        }
        public Impl statusCostFor(StatusEffect s, int value) {
            statusCostMap.put(s, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(STATUS + s + COST, value); }
            return this;
        }

        public int statusDelayFor(StatusEffect s) {
            return statusDelayMap.get(s, () -> {
                CastleCosts.EffectData d = CastleCosts.effects.get(s);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(STATUS + s + DELAY, d != null ? d.delay() : 0);
                }
            });
        }
        public Impl statusDelayFor(StatusEffect s, int value) {
            statusDelayMap.put(s, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(STATUS + s + DELAY, value); }
            return this;
        }

        public int statusDurationFor(StatusEffect s) {
            return statusDurationMap.get(s, () -> {
                CastleCosts.EffectData d = CastleCosts.effects.get(s);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(STATUS + s + DURATION, d != null ? d.duration() : 0);
                }
            });
        }
        public Impl statusDurationFor(StatusEffect s, int value) {
            statusDurationMap.put(s, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(STATUS + s + DURATION, value); }
            return this;
        }

        public boolean statusAllyFor(StatusEffect s) {
            return statusAllyMap.get(s, () -> {
                CastleCosts.EffectData d = CastleCosts.effects.get(s);
                try (TagRead read = TagRead.of(rc.rules)) {
                    return read.r(STATUS + s + ALLY, d != null && d.ally());
                }
            });
        }
        public Impl statusAllyFor(StatusEffect s, boolean value) {
            statusAllyMap.put(s, value);
            try (TagWrite w = TagWrite.of(rc.rules)) { w.w(STATUS + s + ALLY, value); }
            return this;
        }

        private Block drillGet(Item item) {
            if (item == Items.lead || item == Items.copper || item == Items.titanium
                    || item == Items.metaglass || item == Items.coal || item == Items.scrap || item == Items.plastanium
                    || item == Items.surgeAlloy || item == Items.pyratite || item == Items.blastCompound
                    || item == Items.sporePod) return Blocks.laserDrill;
            if (item == Items.beryllium || item == Items.tungsten || item == Items.oxide
                    || item == Items.carbide || item == Items.fissileMatter || item == Items.dormantCyst) return Blocks.impactDrill;

            return state.rules.hasEnv(Env.scorching) ? Blocks.impactDrill : Blocks.laserDrill;
        }

        @Override
        protected void _setRules() {
            final Rules rules = rc.rules;

            for (Team team : Team.all) {
                Rules.TeamRule t = rules.teams.get(team);
                t.cheat = true;
                t.infiniteResources = false;
            }
            rules.unitCapVariable = false;
            rules.unitCap = 500;
            rules.waves = false;
            rules.modeName = "Castle Wars";
            rules.onlyDepositCore = false;
            rules.coreIncinerates = true;
            rules.buildCostMultiplier = 1f;
            rules.buildSpeedMultiplier = 0.5f;
            rules.deconstructRefundMultiplier = 0.5f;
            rules.blockHealthMultiplier = 1f;
            rules.unitDamageMultiplier = 1f;
            rules.unitCrashDamageMultiplier = 2.5f;
            rules.unitMineSpeedMultiplier = 1f;
            rules.unitHealthMultiplier = 1f;
            rules.attackMode = false;
            rules.possessionAllowed = true;
            rules.polygonCoreProtection = true;
            rules.loadout.clear();
            rules.loadout.add(ItemStack.with(
                    Items.copper, 100,
                    Items.lead, 100
            ));
            rules.hideBannedBlocks = true;
            rc.customRules.overdriveIgnoresCheat(false);
        }

        private Seq<Point2> groundSpawn;
        public Seq<Point2> groundSpawn() { return groundSpawn; }
        public Impl groundSpawn(Seq<Point2> value) {
            groundSpawn = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"groundSpawn", value); }
            return this;
        }

        private Seq<Point2> airSpawn;
        public Seq<Point2> airSpawn() { return airSpawn; }
        public Impl airSpawn(Seq<Point2> value) {
            airSpawn = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"airSpawn", value); }
            return this;
        }

        private Seq<Point2> navalSpawn;
        public Seq<Point2> navalSpawn() { return navalSpawn; }
        public Impl navalSpawn(Seq<Point2> value) {
            navalSpawn = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"navalSpawn", value); }
            return this;
        }

        // ── Unit caps ─────────────────────────────────────────────────────────────

        private int defenseUnitCap;
        public int defenseUnitCap() { return defenseUnitCap; }
        public Impl defenseUnitCap(int value) {
            defenseUnitCap = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"defenseUnitCap", value); }
            return this;
        }

        private int attackUnitCap;
        public int attackUnitCap() { return attackUnitCap; }
        public Impl attackUnitCap(int value) {
            attackUnitCap = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"attackUnitCap", value); }
            return this;
        }

        private boolean divideCap;
        public boolean divideCap() { return divideCap; }
        public Impl divideCap(boolean value) {
            divideCap = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"divideCap", value); }
            return this;
        }

        private boolean betterGroundValid;
        public boolean betterGroundValid() { return betterGroundValid; }
        public Impl betterGroundValid(boolean value) {
            betterGroundValid = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"betterGroundValid", value); }
            return this;
        }

        private Seq<Schematic> platformSource;
        public Seq<Schematic> platformSource() { return platformSource; }

        private void savePlatformSource() {
            try (TagWrite write = TagWrite.of(rc.rules)) {
                write.w(UTILS+"platformSource.count", platformSource.size);
                for (int i = 0; i < platformSource.size; i++) {
                    write.w(UTILS+"platformSource."+i, platformSource.get(i));
                }
            }
        }

        public Impl addPlatformSource(Schematic value) {
            platformSource.add(value);
            savePlatformSource();
            return this;
        }

        public Impl removePlatformSource(int index) {
            if (index < 0 || index >= platformSource.size) return this;
            platformSource.remove(index);
            try (TagWrite write = TagWrite.of(rc.rules)) {
                write.w(UTILS+"platformSource.count", platformSource.size);
                rc.rules.tags.remove(UTILS+"platformSource."+platformSource.size);
                for (int i = 0; i < platformSource.size; i++) {
                    write.w(UTILS+"platformSource."+i, platformSource.get(i));
                }
            }
            return this;
        }

        private boolean noPlatform;
        public boolean noPlatform() { return noPlatform; }
        public Impl noPlatform(boolean value) {
            noPlatform = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"noPlatform", value); }
            return this;
        }

        private Block shopFloor;
        public Block shopFloor() { return shopFloor; }
        public Impl shopFloor(Block value) {
            shopFloor = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"shopFloor", value); }
            return this;
        }
    }
}