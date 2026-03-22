package mindurka.rules;

import arc.struct.Seq;
import mindurka.ui.RulesWrite;
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
    public static final String NAME = "castle";
    public static final String TURRET = PREFIX+".turret.";
    public static final String UNIT = PREFIX+".unit.";
    public static final String ITEM = PREFIX+".item.";
    public static final String STATUS = PREFIX+".status.";
    public static final String DRILL = ".drill";
    public static final String COST = ".cost";
    public static final String INCOME = ".income";
    public static final String INTERVAL = ".interval";
    public static final String AMOUNT = ".amount";
    public static final String DROP = ".drop";
    public static final String DURATION = ".duration";
    public static final String ALLY = ".ally";
    public static final String DELAY = ".delay";
    public static final String UTILS = PREFIX+".utils.";

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
                block = read.r(TURRET, Blocks.duo);
                item = read.r(ITEM,Items.copper);
                unit = read.r(UNIT, UnitTypes.flare);
                status = read.r(STATUS, StatusEffects.none);
                drill = read.r(ITEM+item+DRILL, drillGet(item));
                blockCost = read.r(TURRET+block+COST, -1);
                unitCost = read.r(UNIT+unit+COST, -1);
                unitDrop = read.r(UNIT+unit+DROP, -1);
                unitIncome = read.r(UNIT+unit+INCOME, -1);
                itemCost = read.r(ITEM+item+COST, -1);
                itemInetrval = read.r(ITEM+item+INTERVAL, -1);
                itemAmount = read.r(ITEM+item+AMOUNT, -1);
                statusAlly = read.r(STATUS+status+ALLY,false);
                statusDuration = read.r(STATUS+status+DURATION,-1);
                statusDelay = read.r(STATUS+status+DELAY,-1);
                statusCost = read.r(STATUS+status+COST,-1);
                shopFloor = read.r(UTILS+"shopFloor", Blocks.empty);
                groundSpawn = read.r(UTILS+"groundSpawn","-1 -1");
                navalSpawn = read.r(UTILS+"navalSpawn","-1 -1");
                airSpawn = read.r(UTILS+"airSpawn","-1 -1");
                attackUnitCap = read.r(UTILS+"attackUnitCap",-1);
                defenseUnitCap = read.r(UTILS+"defenseUnitCap",75);
                divideCap = read.r(UTILS+"divideCap",true);
                bettergroundvalid = read.r(UTILS+"bettergroundvalid",true);
                noplatform = read.r(UTILS+"noplatform",true);
                platformSource = read.r(UTILS+"platformSource","-1 -1");
            }
        }

        @Override
        public void writeGamemodeRules(RulesWrite write) {
            write.block("rules.mindurka.castle.turret", this::block,  this::block).filter(block -> block instanceof Turret && !block.isFloor());
            write.i("rules.mindurka.castle.turret.cost",this::blockCost,this::blockCost);
            write.spacer();

            write.unit("rules.mindurka.castle.unit", this::unit,   this::unit).filter(unit -> !unit.isBanned());
            write.i("rules.mindurka.castle.unit.cost",this::unitCost,this::unitCost);
            write.i("rules.mindurka.castle.unit.income",this::unitIncome,this::unitIncome);
            write.i("rules.mindurka.castle.unit.drop",this::unitDrop,this::unitDrop);
            write.spacer();

            write.item("rules.mindurka.castle.item", this::item,   this::item).filter(item -> true);
            write.i("rules.mindurka.castle.item.interval",this::itemInetrval,this::itemInetrval);
            write.i("rules.mindurka.castle.item.amount",this::itemAmount,this::itemAmount);
            write.i("rules.mindurka.castle.item.cost",this::itemCost,this::itemCost);
            write.block("rules.mindurka.castle.drill", this::drill,  this::drill).filter(drill -> true);
            write.spacer();

            write.status("rules.mindurka.castle.status", this::status, this::status).filter(item -> true);
            write.i("rules.mindurka.castle.status.cost",this::statusCost,this::statusCost);
            write.i("rules.mindurka.castle.status.delay",this::statusDelay,this::statusDelay);
            write.i("rules.mindurka.castle.status.duration",this::statusDuration,this::statusDuration);
            write.b("rules.mindurka.castle.status.ally",this::statusAlly,this::statusAlly);
            write.spacer();
            // TODO: make this is tool instead of this
            write.s("rules.mindurka.castle.groundSpawn",this::groundSpawn, this::groundSpawn);
            write.s("rules.mindurka.castle.airSpawn",this::airSpawn, this::airSpawn);
            write.s("rules.mindurka.castle.navalSpawn",this::navalSpawn, this::navalSpawn);
            write.spacer();

            write.i("rules.mindurka.castle.defenseUnitCap",this::defenseUnitCap,this::defenseUnitCap);
            write.i("rules.mindurka.castle.attackUnitCap",this::attackUnitCap,this::attackUnitCap);
            write.b("rules.mindurka.castle.divideCap",this::divideCap,this::divideCap);
            write.spacer();

            write.s("rules.mindurka.castle.platformSource",this::platformSource, this::platformSource);
            write.spacer();

            write.b("rules.mindurka.castle.noplatform", this::noplatform, this::noplatform);
            write.b("rules.mindurka.castle.bettergroundvalid", this::bettergroundvalid, this::bettergroundvalid);
            write.block("rules.mindurka.castle.shopFloor", this::shopFloor, this::shopFloor);
        }

        @Override
        void remove() {
            final Rules rules = rc.rules;
            Seq<String> toRemove = new Seq<>();
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
        public Castle.Impl block(Block value) {
            block = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                blockCost = read.r(TURRET+block+COST, -1);
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(TURRET, value); }
            return this;
        }

        private int blockCost;
        public int blockCost() { return blockCost; }
        public Castle.Impl blockCost(int value) {
            blockCost = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(TURRET+block+COST, value); }
            return this;
        }

        private UnitType unit;
        public UnitType unit() { return unit; }
        public Castle.Impl unit(UnitType value) {
            unit = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                unitCost = read.r(UNIT+unit+COST, -1);
                unitDrop = read.r(UNIT+unit+DROP, -1);
                unitIncome = read.r(UNIT+unit+INCOME, -1);

            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT, value); }
            return this;
        }

        private int unitCost;
        public int unitCost() { return unitCost; }
        public Castle.Impl unitCost(int value) {
            unitCost = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT+unit+COST, value); }
            return this;
        }

        private int unitIncome;
        public int unitIncome() { return unitIncome; }
        public Castle.Impl unitIncome(int value) {
            unitIncome = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT+unit+INCOME, value); }
            return this;
        }

        private int unitDrop;
        public int unitDrop() { return unitDrop; }
        public Castle.Impl unitDrop(int value) {
            unitDrop = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UNIT+unit+DROP, value); }
            return this;
        }

        private Item item;
        public Item item() { return item; }
        public Castle.Impl item(Item value) {
            item = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                itemCost = read.r(ITEM+item+COST, -1);
                itemInetrval = read.r(ITEM+item+INTERVAL, -1);
                itemAmount = read.r(ITEM+item+AMOUNT, -1);
                drill = read.r(ITEM+item+DRILL, drillGet(item));
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM, value); }
            return this;
        }

        private int itemCost;
        public int itemCost() { return itemCost; }
        public Castle.Impl itemCost(int value) {
            itemCost = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+COST, value); }
            return this;
        }
        private int itemInetrval;
        public int itemInetrval() { return itemInetrval; }
        public Castle.Impl itemInetrval(int value) {
            itemInetrval = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+INTERVAL, value); }
            return this;
        }
        private int itemAmount;
        public int itemAmount() { return itemAmount; }
        public Castle.Impl itemAmount(int value) {
            itemAmount = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+AMOUNT, value); }
            return this;
        }
        private Block drill;
        public Block drill() { return drill; }
        public Castle.Impl drill(Block value) {
            drill = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(ITEM+item+DRILL, value); }
            return this;
        }

        private StatusEffect status;
        public StatusEffect status() { return status; }
        public Castle.Impl status(StatusEffect value) {
            status = value;
            try (TagRead read = TagRead.of(rc.rules)) {
                statusDelay = read.r(STATUS+status+DELAY, -1);
                statusDuration = read.r(STATUS+status+DURATION, -1);
                statusAlly = read.r(STATUS+status+ALLY, false);
            }
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS, value); }
            return this;
        }
        private int statusDelay;
        public int statusDelay() { return statusDelay; }
        public Castle.Impl statusDelay(int value) {
            statusDelay = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+DELAY, value); }
            return this;
        }
        private int statusCost;
        public int statusCost() { return statusCost; }
        public Castle.Impl statusCost(int value) {
            statusCost = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+COST, value); }
            return this;
        }
        private int statusDuration;
        public int statusDuration() { return statusDuration; }
        public Castle.Impl statusDuration(int value) {
            statusDuration = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+DURATION, value); }
            return this;
        }
        private boolean statusAlly;
        public boolean statusAlly() { return statusAlly; }
        public Castle.Impl statusAlly(boolean value) {
            statusAlly = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(STATUS+status+ALLY, value); }
            return this;
        }
        private String groundSpawn;
        public String groundSpawn() { return groundSpawn; }
        public Castle.Impl groundSpawn(String value) {
            groundSpawn = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"groundSpawn", value); }
            return this;
        }
        private String airSpawn;
        public String airSpawn() { return airSpawn; }
        public Castle.Impl airSpawn(String value) {
            airSpawn = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"airSpawn", value); }
            return this;
        }
        private String navalSpawn;
        public String navalSpawn() { return navalSpawn; }
        public Castle.Impl navalSpawn(String value) {
            navalSpawn = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"navalSpawn", value); }
            return this;
        }
        private int defenseUnitCap;
        public int defenseUnitCap() { return defenseUnitCap; }
        public Castle.Impl defenseUnitCap(int value) {
            defenseUnitCap = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"defenseUnitCap", value); }
            return this;
        }
        private int attackUnitCap;
        public int attackUnitCap() { return attackUnitCap; }
        public Castle.Impl attackUnitCap(int value) {
            attackUnitCap = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"attackUnitCap", value); }
            return this;
        }
        private boolean divideCap;
        public boolean divideCap() { return divideCap; }
        public Castle.Impl divideCap(boolean value) {
            divideCap = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"divideCap", value); }
            return this;
        }
        private boolean bettergroundvalid;
        public boolean bettergroundvalid() { return bettergroundvalid; }
        public Castle.Impl bettergroundvalid(boolean value) {
            bettergroundvalid = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"bettergroundvalid", value); }
            return this;
        }
        private boolean noplatform;
        public boolean noplatform() { return noplatform; }
        public Castle.Impl noplatform(boolean value) {
            noplatform = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"noplatform", value); }
            return this;
        }
        private Block shopFloor;
        public Block shopFloor() { return shopFloor; }
        public Castle.Impl shopFloor(Block value) {
            shopFloor = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"shopFloor", value); }
            return this;
        }
        private String platformSource;
        public String platformSource() { return platformSource; }
        public Castle.Impl platformSource(String value) {
            platformSource = value;
            try (TagWrite write = TagWrite.of(rc.rules)) { write.w(UTILS+"platformSource", value); }
            return this;
        }
    }
}
