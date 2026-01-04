package mindurka;

import arc.func.Cons;
import arc.func.Cons2;
import arc.struct.IntMap;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.type.ItemStack;

public class MRules {
    public static final MRules Default = new MRules();

    public void sync() {
        String gamemodeName = Vars.state.rules.tags.get("mdrk.gamemode");

        gamemode = Gamemode.unknown;
        if (gamemodeName == null)
            gamemode = Gamemode.none;
        else if (!gamemodeName.equals("none"))
            for (Gamemode gamemode : Gamemode.values()) {
                if (gamemode.name().equals(gamemodeName)) {
                    this.gamemode = gamemode;
                    break;
                }
            }

        fortsTeamRules.clear();

        if (gamemode == Gamemode.forts) {
            impactReactorEnabled = readBool(Gamemode.forts, "mdrk.impact.enabled", Default.impactReactorEnabled);
            impactReactorShieldDuration = readFloat(Gamemode.forts, "mdrk.impact.shieldDuration", Default.impactReactorShieldDuration);
            impactReactorDelay = readFloat(Gamemode.forts, "mdrk.impact.reactorDelay", Default.impactReactorDelay);
            impactReactorCooldown = readFloat(Gamemode.forts, "mdrk.impact.reactorCooldown", Default.impactReactorCooldown);

            thorReactorEnabled = readBool(Gamemode.forts, "mdrk.thor.enabled", Default.thorReactorEnabled);
            thorReactorPowerMultiplier = readFloat(Gamemode.forts, "mdrk.thor.powerMultiplier", Default.thorReactorPowerMultiplier);
            thorReactorDelay = readFloat(Gamemode.forts, "mdrk.thor.reactorDelay", Default.thorReactorDelay);
            thorReactorCooldown = readFloat(Gamemode.forts, "mdrk.thor.reactorCooldown", Default.thorReactorCooldown);

            FortsPlotKind.sync(Vars.state.rules, this);

            for (Team team : Team.all) {
                FortsTeamRules r = FortsTeamRules.loadOrDefault(team, Vars.state.rules, this);
                if (r != FortsTeamRules.Default) fortsTeamRules.put(team.id, r);
            }
        }
    }

    public void save() {
        gamemode.save();

        if (gamemode == Gamemode.forts) {
            writeBool(Gamemode.forts, "mdrk.impact.enabled", impactReactorEnabled, Default.impactReactorEnabled);
            writeFloat(Gamemode.forts, "mdrk.impact.shieldDuration", impactReactorShieldDuration, Default.impactReactorShieldDuration);
            writeFloat(Gamemode.forts, "mdrk.impact.reactorDelay", impactReactorDelay, Default.impactReactorDelay);
            writeFloat(Gamemode.forts, "mdrk.impact.reactorCooldown", impactReactorCooldown, Default.impactReactorCooldown);

            writeBool(Gamemode.forts, "mdrk.thor.enabled", thorReactorEnabled, Default.thorReactorEnabled);
            writeFloat(Gamemode.forts, "mdrk.thor.powerMultiplier", thorReactorPowerMultiplier, Default.thorReactorPowerMultiplier);
            writeFloat(Gamemode.forts, "mdrk.thor.reactorDelay", thorReactorDelay, Default.thorReactorDelay);
            writeFloat(Gamemode.forts, "mdrk.thor.reactorCooldown", thorReactorCooldown, Default.thorReactorCooldown);

            fortsPlotKind.save(Vars.state.rules, this);

            for (FortsTeamRules r : fortsTeamRules.values()) {
                r.save(Vars.state.rules);
            }
        }
    }

    public Gamemode gamemode = Gamemode.none;

    // FIXME: Switching gamemodes is a rule-mashing disaster
    public enum Gamemode {
        none(rules -> {}),
        unknown(rules -> {}),
        forts(rules -> {
            for (Team team : Team.all) {
                Rules.TeamRule t = rules.teams.get(team);
                t.cheat = true;
            }
            rules.planet = Planets.sun;
            rules.env = Vars.defaultEnv;
            rules.unitCapVariable = false;
            rules.unitCap = 75;
            rules.waves = false;
            rules.modeName = "Forts";
            rules.onlyDepositCore = false;
            rules.coreIncinerates = true;
            rules.buildCostMultiplier = 1.5f;
            rules.buildSpeedMultiplier = 4f;
            rules.deconstructRefundMultiplier = 0.5f;
            rules.blockHealthMultiplier = 0.66f;
            rules.unitDamageMultiplier = 1.4f;
            rules.unitCrashDamageMultiplier = 3.5f;
            rules.unitMineSpeedMultiplier = 1f;
            rules.unitHealthMultiplier = 1f;
            rules.attackMode = false;
            rules.possessionAllowed = false;
            rules.enemyCoreBuildRadius = 30f;
            rules.schematicsAllowed = false;
            rules.loadout.clear();
            rules.loadout.add(ItemStack.with(
                    Items.copper, 1000,
                    Items.lead, 1000,
                    Items.metaglass, 300,
                    Items.graphite, 300,
                    Items.titanium, 300,
                    Items.thorium, 300,
                    Items.scrap, 300,
                    Items.silicon, 300,
                    Items.beryllium, 300
            ));
            rules.bannedBlocks.addAll(
                    Blocks.cryofluidMixer,
                    Blocks.surgeWall,
                    Blocks.surgeWallLarge,
                    Blocks.scrapWall,
                    Blocks.carbideWall,
                    Blocks.mendProjector,
                    Blocks.overdriveProjector, // May be re-enabled in future
                    Blocks.overdriveDome,
                    Blocks.shockMine,
                    Blocks.overflowDuct,
                    Blocks.overflowGate,
                    Blocks.ductUnloader,
                    Blocks.reinforcedPump,
                    Blocks.reinforcedConduit,
                    Blocks.reinforcedLiquidJunction,
                    Blocks.reinforcedBridgeConduit,
                    Blocks.reinforcedLiquidRouter,
                    Blocks.reinforcedLiquidContainer,
                    Blocks.reinforcedLiquidTank,
                    Blocks.powerNode,
                    Blocks.powerNodeLarge,
                    Blocks.surgeTower,
                    Blocks.battery,
                    Blocks.batteryLarge,
                    Blocks.combustionGenerator,
                    Blocks.thermalGenerator,
                    Blocks.steamGenerator,
                    Blocks.differentialGenerator,
                    Blocks.rtgGenerator,
                    Blocks.solarPanel,
                    Blocks.largeSolarPanel,
                    Blocks.beamNode,
                    Blocks.beamLink,
                    Blocks.beamTower,
                    Blocks.turbineCondenser,
                    Blocks.chemicalCombustionChamber,
                    Blocks.fluxReactor, // I'll put a note in that one
                    Blocks.waterExtractor,
                    Blocks.oilExtractor,
                    Blocks.ventCondenser,
                    Blocks.cliffCrusher,
                    Blocks.largeCliffCrusher,
                    Blocks.impactDrill,
                    Blocks.eruptionDrill,
                    Blocks.coreShard,
                    Blocks.unloader,
                    Blocks.reinforcedContainer,
                    Blocks.reinforcedVault,
                    Blocks.groundFactory,
                    Blocks.tetrativeReconstructor,
                    Blocks.tankFabricator,
                    Blocks.tankRefabricator,
                    Blocks.tankAssembler,
                    Blocks.basicAssemblerModule,
                    Blocks.reinforcedPayloadConveyor,
                    Blocks.reinforcedPayloadRouter,
                    Blocks.smallDeconstructor,
                    Blocks.largeConstructor,
                    Blocks.switchBlock,
                    Blocks.microProcessor,
                    Blocks.logicProcessor,
                    Blocks.hyperProcessor,
                    Blocks.memoryCell,
                    Blocks.memoryBank,
                    Blocks.logicDisplay,
                    Blocks.largeLogicDisplay,
                    Blocks.logicDisplayTile,
                    Blocks.reinforcedMessage
            );
            rules.hideBannedBlocks = true;
        }),
        spvp(rules -> {
            rules.infiniteResources = true;
            rules.modeName = "Sandbox PvP";
            rules.schematicsAllowed = false;
        }),
        hexed(rules -> {
            // TODO: Look up whatever the fuck does Hexed set.
        }),
        survival(rules -> {}),
        attack(rules -> {}),
        sandbox(rules -> {
            rules.infiniteResources = true;
            rules.canGameOver = false;
        }),
        hub(rules -> {
            rules.bannedBlocks.addAll(Vars.content.blocks());
            rules.hideBannedBlocks = true;
            rules.modeName = "Hub";
        });

        ;

        private final Cons<Rules> rulesAssigner;

        Gamemode(Cons<Rules> rulesAssigner) {
            this.rulesAssigner = rulesAssigner;
        }

        public void assign(Rules rules) {
            for (Team team : Team.all) {
                Rules.TeamRule t = rules.teams.get(team);
                t.cheat = false;
            }
            rules.modeName = "";
            rules.onlyDepositCore = false;
            rules.coreIncinerates = false;
            rules.attackMode = false;
            rules.possessionAllowed = true;
            rules.enemyCoreBuildRadius = 30f;
            rules.schematicsAllowed = true;
            rules.loadout.clear();
            rules.bannedBlocks.clear();
            rules.hideBannedBlocks = false;
            rulesAssigner.get(rules);
        }

        public void save() {
            if (this == Gamemode.none) {
                Vars.state.rules.tags.remove("mdrk.gamemode");
                Vars.state.rules.tags.remove("mdrk.format");
            } else {
                Vars.state.rules.tags.put("mdrk.gamemode", name());
                Vars.state.rules.tags.put("mdrk.format", "1");
            }
        }
    }

    /**
     * Forts plot kind.
     * <p>
     * Plot size (and similar) inside the plot, and not of the wall.
     */
    public enum FortsPlotKind {
        /**
         * Square plots.
         * <p>
         * <ul>
         *     <li>i1: plot size</li>
         *     <li>i3: wall thickness</li>
         *     <li>i4: shift x</li>
         *     <li>i5: shift y</li>
         * </ul>
         **/
        square(
                (rules, customRules) -> {
                    rules.tags.put("mdrk.forts.plot.size", Integer.toString(customRules.fortsPlotParam1i));
                    rules.tags.put("mdrk.forts.plot.wall", Integer.toString(customRules.fortsPlotParam3i));
                    rules.tags.put("mdrk.forts.plot.shiftX", Integer.toString(customRules.fortsPlotParam4i));
                    rules.tags.put("mdrk.forts.plot.shiftY", Integer.toString(customRules.fortsPlotParam5i));
                },
                rules -> {
                    rules.fortsPlotParam1i = Default.fortsPlotParam1i;
                    rules.fortsPlotParam3i = Default.fortsPlotParam3i;
                    rules.fortsPlotParam4i = Default.fortsPlotParam4i;
                    rules.fortsPlotParam5i = Default.fortsPlotParam5i;
                },
                (rules, customRules) -> {
                    customRules.fortsPlotParam1i = rules.tags.getInt("mdrk.forts.plot.size", Default.fortsPlotParam1i);
                    customRules.fortsPlotParam3i = rules.tags.getInt("mdrk.forts.plot.wall", Default.fortsPlotParam3i);
                    customRules.fortsPlotParam4i = rules.tags.getInt("mdrk.forts.plot.shiftX", Default.fortsPlotParam4i);
                    customRules.fortsPlotParam5i = rules.tags.getInt("mdrk.forts.plot.shiftY", Default.fortsPlotParam5i);
                }
        ),
        /**
         * Rectangular plots.
         * <p>
         * <ul>
         *     <li>i1: plot width</li>
         *     <li>i2: plot height</li>
         *     <li>i3: wall thickness</li>
         *     <li>i4: shift x</li>
         *     <li>i5: shift y</li>
         * </ul>
         **/
        rect(
                (rules, customRules) -> {
                    rules.tags.put("mdrk.forts.plot.width", Integer.toString(customRules.fortsPlotParam1i));
                    rules.tags.put("mdrk.forts.plot.height", Integer.toString(customRules.fortsPlotParam2i));
                    rules.tags.put("mdrk.forts.plot.wall", Integer.toString(customRules.fortsPlotParam3i));
                    rules.tags.put("mdrk.forts.plot.shiftX", Integer.toString(customRules.fortsPlotParam4i));
                    rules.tags.put("mdrk.forts.plot.shiftY", Integer.toString(customRules.fortsPlotParam5i));
                },
                rules -> {
                    rules.fortsPlotParam1i = Default.fortsPlotParam1i;
                    rules.fortsPlotParam2i = Default.fortsPlotParam2i;
                    rules.fortsPlotParam3i = Default.fortsPlotParam3i;
                    rules.fortsPlotParam4i = Default.fortsPlotParam4i;
                    rules.fortsPlotParam5i = Default.fortsPlotParam5i;
                },
                (rules, customRules) -> {
                    customRules.fortsPlotParam1i = rules.tags.getInt("mdrk.forts.plot.width", Default.fortsPlotParam1i);
                    customRules.fortsPlotParam2i = rules.tags.getInt("mdrk.forts.plot.height", Default.fortsPlotParam2i);
                    customRules.fortsPlotParam3i = rules.tags.getInt("mdrk.forts.plot.wall", Default.fortsPlotParam3i);
                    customRules.fortsPlotParam4i = rules.tags.getInt("mdrk.forts.plot.shiftX", Default.fortsPlotParam4i);
                    customRules.fortsPlotParam5i = rules.tags.getInt("mdrk.forts.plot.shiftY", Default.fortsPlotParam5i);
                }
        ),

        ;

        private final Cons2<Rules, MRules> saveFn;
        private final Cons<MRules> resetFn;
        private final Cons2<Rules, MRules> syncFn;

        FortsPlotKind(Cons2<Rules, MRules> saveFn, Cons<MRules> resetFn, Cons2<Rules, MRules> syncFn) {
            this.saveFn = saveFn;
            this.resetFn = resetFn;
            this.syncFn = syncFn;
        }

        public void reset(MRules customRules) {
            if (customRules.gamemode != Gamemode.forts) return;

            resetFn.get(customRules);
        }

        public static void sync(Rules rules, MRules customRules) {
            if (customRules.gamemode != Gamemode.forts) return;

            customRules.fortsPlotKind = square;
            customRules.fortsPlotParam1i = Default.fortsPlotParam1i;
            @Nullable String name = rules.tags.get("mdrk.forts.plot");
            for (FortsPlotKind kind : values()) {
                if (kind.name().equals(name)) {
                    customRules.fortsPlotKind = kind;
                    customRules.fortsPlotKind.syncFn.get(rules, customRules);
                    break;
                }
            }
        }

        public void save(Rules rules, MRules customRules) {
            if (customRules.gamemode == Gamemode.forts) {
                rules.tags.put("mdrk.forts.plot", name());
                this.saveFn.get(rules, customRules);
            }
            else {
                rules.tags.remove("mdrk.forts.plot");
            }
        }
    }

    // Forts-specific options
    public FortsPlotKind fortsPlotKind = FortsPlotKind.square;
    public int fortsPlotParam1i = 6;
    public int fortsPlotParam2i = 6;
    public int fortsPlotParam3i = 1;
    public int fortsPlotParam4i = 0;
    public int fortsPlotParam5i = 0;
    public IntMap<FortsTeamRules> fortsTeamRules = new IntMap<>();

    public int fortsPlotWidth() {
        if (gamemode != Gamemode.forts) throw new IllegalStateException("Forts plot size can only be obtained on Forts gamemode");

        switch (fortsPlotKind) {
            case rect:
            case square:
                return fortsPlotParam1i;
        }

        throw new IllegalStateException("Plot kind " + fortsPlotKind.name() + " has no defined size");
    }

    public int fortsPlotHeight() {
        if (gamemode != Gamemode.forts) throw new IllegalStateException("Forts plot size can only be obtained on Forts gamemode");

        switch (fortsPlotKind) {
            case rect: return fortsPlotParam2i;
            case square: return fortsPlotParam1i;
        }

        throw new IllegalStateException("Plot kind " + fortsPlotKind.name() + " has no defined size");
    }

    public int fortsWallSize() {
        if (gamemode != Gamemode.forts) throw new IllegalStateException("Forts plot size can only be obtained on Forts gamemode");

        switch (fortsPlotKind) {
            case rect:
            case square:
                return fortsPlotParam3i;
        }

        throw new IllegalStateException("Plot kind " + fortsPlotKind.name() + " has no defined size");
    }

    public @Nullable FortsTeamRules fortsTeamRules(Team team) {
        if (gamemode != Gamemode.forts) throw new IllegalStateException("Forts team rules can only be obtained on Forts gamemode");

        return fortsTeamRules.get(team.id);
    }

    public void fortsTeamEnabled(Team team) {
        FortsTeamRules teamRules = FortsTeamRules.of(team);
        fortsTeamRules.put(team.id, teamRules);
        teamRules.save(Vars.state.rules);
    }

    public void fortsTeamDisabled(Team team) {
        @Nullable FortsTeamRules teamRules = fortsTeamRules.remove(team.id);
        if (teamRules != null) teamRules.remove(Vars.state.rules);
    }

    public boolean impactReactorEnabled = true;
    public float impactReactorShieldDuration = 0.75f;
    public float impactReactorCooldown = 10f;
    public float impactReactorDelay = 0.5f;

    public boolean thorReactorEnabled = true;
    public float thorReactorPowerMultiplier = 1f;
    public float thorReactorCooldown = 3f;
    public float thorReactorDelay = 0.5f;

    void writeBool(Gamemode ifGamemode, String path, boolean value, boolean defaultValue) {
        if (gamemode != ifGamemode || value == defaultValue)
            Vars.state.rules.tags.remove(path);
        else
            Vars.state.rules.tags.put(path, Boolean.toString(value));
    }
    void writeFloat(Gamemode ifGamemode, String path, float value, float defaultValue) {
        if (gamemode != ifGamemode || value == defaultValue)
            Vars.state.rules.tags.remove(path);
        else
            Vars.state.rules.tags.put(path, Float.toString(value));
    }

    boolean readBool(Gamemode ifGamemode, String path, boolean defaultValue) {
        if (gamemode != ifGamemode)
            return defaultValue;

        @Nullable String value = Vars.state.rules.tags.get(path);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    float readFloat(Gamemode ifGamemode, String path, float defaultValue) {
        if (gamemode != ifGamemode)
            return defaultValue;

        @Nullable String value = Vars.state.rules.tags.get(path);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
