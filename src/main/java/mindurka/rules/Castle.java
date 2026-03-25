package mindurka.rules;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.math.geom.Point2;
import mindurka.ui.RulesWrite;
import mindurka.util.Schematic;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
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
    // lighter way to don't have all item and other stuff ctrl c+v
    public static final String COST = ".cost";
    public static final String INCOME = ".income";
    public static final String INTERVAL = ".interval";
    public static final String AMOUNT = ".amount";
    public static final String DROP = ".drop";
    public static final String DURATION = ".duration";
    public static final String ALLY = ".ally";
    public static final String DELAY = ".delay";
    public static final String DRILL =  ".drill";

    public static final int PLATFORM_SIZE = 6;

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

            final Rules rules = rc.rules;

            try (TagRead read = TagRead.of(rc.rules)) {
                CastleCosts.load();
                block = read.r(TURRET, Blocks.duo);
                item = read.r(ITEM, Items.copper);
                unit = read.r(UNIT, UnitTypes.flare);
                status = read.r(STATUS, StatusEffects.none);
                blockCost = read.r(TURRET+block+COST, CastleCosts.turrets.get((Turret) block,0));
                CastleCosts.UnitData unitData = CastleCosts.units.get(unit);
                unitCost = read.r(UNIT + unit + COST,
                        unitData != null ? unitData.cost() : 0
                );

                unitDrop = read.r(UNIT + unit + DROP,
                        unitData != null ? unitData.drop() : 0
                );

                unitIncome = read.r(UNIT + unit + INCOME,
                        unitData != null ? unitData.income() : 0
                );
                CastleCosts.ItemData itemData = CastleCosts.items.get(item);
                itemCostMap.put(item,     read.r(ITEM+item+COST,
                        itemData != null ? itemData.cost() : 0
                ));
                itemIntervalMap.put(item, read.r(ITEM+item+INTERVAL,
                        itemData != null ? itemData.interval() : 0
                ));
                itemAmountMap.put(item,   read.r(ITEM+item+AMOUNT,
                        itemData != null ? itemData.amount() : 0
                ));
                drillMap.put(item,        read.r(ITEM+item+DRILL,    drillGet(item)));
                CastleCosts.EffectData effectData = CastleCosts.effects.get(status);
                statusDelayMap.put(status, read.r(STATUS+status+DELAY,
                        effectData != null ? effectData.delay() : 0
                ));
                statusDurationMap.put(status, read.r(STATUS+status+DURATION,
                        effectData != null ? effectData.duration() : 0
                ));
                statusCostMap.put(status,     read.r(STATUS+status+COST,
                        effectData != null ? effectData.cost() : 0
                ));
                statusAllyMap.put(status,     read.r(STATUS+status+ALLY,
                        effectData == null || effectData.ally()
                ));
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
            write.selectionRaw("rules.mindurka.castle.turret", addEntry -> {
                for (Block b : mindustry.Vars.content.blocks()) {
                    if (b instanceof Turret t) {
                        addEntry.add(t.localizedName + t.emoji(), t);
                    }
                }
            }, this::block, block);
            {
                RulesWrite blockTable = write.table();
                blockTable.i("rules.mindurka.castle.turret.cost", this::blockCost, this::blockCost);
            }
            write.spacer();
            write.selectionRaw("rules.mindurka.castle.unit", addEntry -> {
                for (UnitType u : mindustry.Vars.content.units()) addEntry.add(u.localizedName+u.emoji(), u);
            }, this::unit, unit);
            {
                RulesWrite unitTable = write.table();
                unitTable.i("rules.mindurka.castle.unit.cost", this::unitCost, this::unitCost);
                unitTable.i("rules.mindurka.castle.unit.income", this::unitIncome, this::unitIncome);
                unitTable.i("rules.mindurka.castle.unit.drop", this::unitDrop, this::unitDrop);
            }
            write.spacer();

            write.selectionRaw("rules.mindurka.castle.item", addEntry -> {
                for (Item i : mindustry.Vars.content.items()) addEntry.add(i.localizedName+i.emoji(), i);
            }, this::item, item);
            {
                RulesWrite itemTable = write.table();
                itemTable.i("rules.mindurka.castle.item.interval", this::itemInterval, this::itemInterval);
                itemTable.i("rules.mindurka.castle.item.amount",   this::itemAmount,   this::itemAmount);
                itemTable.i("rules.mindurka.castle.item.cost",     this::itemCost,     this::itemCost);
                itemTable.block("rules.mindurka.castle.drill",     this::drill,        this::drill).filter(d -> true);
            }
            write.spacer();

            write.selectionRaw("rules.mindurka.castle.status", addEntry -> {
                for (StatusEffect s : mindustry.Vars.content.statusEffects()) addEntry.add(s.localizedName+s.emoji(), s);
            }, this::status, status);
            {
                RulesWrite statusTable = write.table();
                statusTable.i("rules.mindurka.castle.status.cost",     this::statusCost,     this::statusCost);
                statusTable.i("rules.mindurka.castle.status.delay",    this::statusDelay,    this::statusDelay);
                statusTable.i("rules.mindurka.castle.status.duration", this::statusDuration, this::statusDuration);
                statusTable.b("rules.mindurka.castle.status.ally",     this::statusAlly,     this::statusAlly);
            }
            write.spacer();

            // TODO: make this a tool instead of this
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
            // removing ALL tags what seted by castle
            rules.tags.each((key, value) -> {;
                if (key.startsWith("mdrk.castle.")) toRemove.add(key);
            });
            toRemove.each(rules.tags::remove);
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
            // anyways plugin dont care
            rules.modeName = "Castle Wars";
            rules.onlyDepositCore = false;
            rules.coreIncinerates = true;
            rules.buildCostMultiplier = 1f;
            // why?
            rules.buildSpeedMultiplier = 0.5f;
            rules.deconstructRefundMultiplier = 0.5f;
            rules.blockHealthMultiplier = 1f;
            rules.unitDamageMultiplier = 1f;
            // i sure i love defense with aegeris
            rules.unitCrashDamageMultiplier = 2f;
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

        private Block block;
        public Block block() { return block; }
        public Impl block(Block value) {
            block = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                blockCost = read.r(TURRET+block+COST, CastleCosts.turrets.get((Turret) block,0));
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(TURRET, value); }
            return this;
        }

        private int blockCost;
        public int blockCost() { return blockCost; }
        public Impl blockCost(int value) {
            blockCost = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(TURRET+block+COST, value); }
            return this;
        }

        private UnitType unit;
        public UnitType unit() { return unit; }
        public Impl unit(UnitType value) {
            unit = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                // these variables do not update normally so doing this there should get real value
                CastleCosts.UnitData unitData = CastleCosts.units.get(unit);
                unitCost = read.r(UNIT + unit + COST,
                        unitData != null ? unitData.cost() : 0
                );

                unitDrop = read.r(UNIT + unit + DROP,
                        unitData != null ? unitData.drop() : 0
                );

                unitIncome = read.r(UNIT + unit + INCOME,
                        unitData != null ? unitData.income() : 0
                );
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT, value); }
            return this;
        }

        private int unitCost;
        public int unitCost() { return unitCost; }
        public Impl unitCost(int value) {
            unitCost = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT+unit+COST, value); }
            return this;
        }

        private int unitIncome;
        public int unitIncome() { return unitIncome; }
        public Impl unitIncome(int value) {
            unitIncome = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT+unit+INCOME, value); }
            return this;
        }

        private int unitDrop;
        public int unitDrop() { return unitDrop; }
        public Impl unitDrop(int value) {
            unitDrop = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT+unit+DROP, value); }
            return this;
        }

        private Item item;
        public Item item() { return item; }
        public Impl item(Item value) {
            item = value;
            // Load previously-saved values for this item (or defaults).
            try (TagRead read = TagRead.of(rc.rules)) {
                CastleCosts.ItemData itemData = CastleCosts.items.get(item);
                itemCostMap.put(item,     read.r(ITEM+item+COST,
                        itemData != null ? itemData.cost() : 0
                ));
                itemIntervalMap.put(item, read.r(ITEM+item+INTERVAL,
                        itemData != null ? itemData.interval() : 0
                ));
                itemAmountMap.put(item,   read.r(ITEM+item+AMOUNT,
                        itemData != null ? itemData.amount() : 0
                ));
                drillMap.put(item,        read.r(ITEM+item+DRILL,    drillGet(item)));
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM, value); }
            return this;
        }

        private final ObjectMap<Item, Integer> itemCostMap     = new ObjectMap<>();
        private final ObjectMap<Item, Integer> itemIntervalMap = new ObjectMap<>();
        private final ObjectMap<Item, Integer> itemAmountMap   = new ObjectMap<>();
        private final ObjectMap<Item, Block>   drillMap        = new ObjectMap<>();

        public int itemCost() { return itemCostMap.get(item, 0); }
        public Impl itemCost(int value) {
            itemCostMap.put(item, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+COST, value); }
            return this;
        }

        public int itemInterval() { return itemIntervalMap.get(item, 0); }
        public Impl itemInterval(int value) {
            itemIntervalMap.put(item, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+INTERVAL, value); }
            return this;
        }

        public int itemAmount() { return itemAmountMap.get(item, 0); }
        public Impl itemAmount(int value) {
            itemAmountMap.put(item, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+AMOUNT, value); }
            return this;
        }

        public Block drill() { return drillMap.get(item, Blocks.laserDrill); }
        public Impl drill(Block value) {
            drillMap.put(item, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+DRILL, value); }
            return this;
        }

        private StatusEffect status;
        public StatusEffect status() { return status; }
        public Impl status(StatusEffect value) {
            status = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                CastleCosts.EffectData effectData = CastleCosts.effects.get(status);
                statusDelayMap.put(status, read.r(STATUS+status+DELAY,
                        effectData != null ? effectData.delay() : 0
                ));
                statusDurationMap.put(status, read.r(STATUS+status+DURATION,
                        effectData != null ? effectData.duration() : 0
                ));
                statusCostMap.put(status,     read.r(STATUS+status+COST,
                        effectData != null ? effectData.cost() : 0
                ));
                statusAllyMap.put(status,     read.r(STATUS+status+ALLY,
                        effectData != null && effectData.ally()
                ));
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS, value); }
            return this;
        }

        private final ObjectMap<StatusEffect, Integer> statusDelayMap    = new ObjectMap<>();
        private final ObjectMap<StatusEffect, Integer> statusDurationMap = new ObjectMap<>();
        private final ObjectMap<StatusEffect, Integer> statusCostMap     = new ObjectMap<>();
        private final ObjectMap<StatusEffect, Boolean> statusAllyMap     = new ObjectMap<>();

        public int statusDelay() { return statusDelayMap.get(status, 0); }
        public Impl statusDelay(int value) {
            statusDelayMap.put(status, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+DELAY, value); }
            return this;
        }

        public int statusCost() { return statusCostMap.get(status, 0); }
        public Impl statusCost(int value) {
            statusCostMap.put(status, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+COST, value); }
            return this;
        }

        public int statusDuration() { return statusDurationMap.get(status, 0); }
        public Impl statusDuration(int value) {
            statusDurationMap.put(status, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+DURATION, value); }
            return this;
        }

        public boolean statusAlly() { return statusAllyMap.get(status, false); }
        public Impl statusAlly(boolean value) {
            statusAllyMap.put(status, value);
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+ALLY, value); }
            return this;
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

        // ── Platforms  ─────────────────────────────────────────────────────────────

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
