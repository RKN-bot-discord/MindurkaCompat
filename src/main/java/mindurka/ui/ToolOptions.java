package mindurka.ui;

import arc.util.Nullable;
import mindurka.MVars;
import mindurka.rules.Castle;
import mindurka.rules.FortsPlotState;
import mindurka.rules.Gamemode;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.world.Block;


public class ToolOptions {
    public boolean fortsCarverPlace = true;
    public int radius = 1;
    public Block selectedBlock = Blocks.coreShard;
    public Team team = Team.sharded;
    public EditorTool tool = EditorTool.pencil;

    public @Nullable BitMap fakeCliffsMap;
    public boolean cliffAuto;
    public byte cliffSides;
    public Blend blend;
    public FortsPlotState fortsPlotState = FortsPlotState.enabled;
    public boolean floorsAsOverlays = false;
    public String hubServer = "";
    public Item selectedItemCastle = Items.copper;
    public boolean invincible = false;
    public int blockCost = 0;
    public int minerCost = 0;
    public int minerInterval = 0;
    public int minerAmount = 0;
    public Block minerDrill = Blocks.blastDrill;

    private Gamemode.Impl gamemode() {
        return MVars.rules.gamemode();
    }

    public int blockCostFor(Block block) {
        Gamemode.Impl gm = gamemode();
        if (gm instanceof Castle.Impl) {
            blockCost = ((Castle.Impl) gm).blockCostFor(block);
            return blockCost;
        }
        return 0;
    }

    public int minerIntervalFor(Item item) {
        Gamemode.Impl gm = gamemode();
        if (gm instanceof Castle.Impl) {
            minerInterval = ((Castle.Impl) gm).itemIntervalFor(item);
            return minerInterval;
        }
        return 0;
    }

    public int minerCostFor(Item item) {
        Gamemode.Impl gm = gamemode();
        if (gm instanceof Castle.Impl) {
            minerCost = ((Castle.Impl) gm).itemCostFor(item);
            return minerCost;
        }
        return 0;
    }

    public int minerAmountFor(Item item) {
        Gamemode.Impl gm = gamemode();
        if (gm instanceof Castle.Impl) {
            minerAmount = ((Castle.Impl) gm).itemAmountFor(item);
            return minerAmount;
        }
        return 0;
    }


    public @Nullable BitMap fakeCliffsMap() {
        if (!cliffAuto) return null;
        if (fakeCliffsMap == null || Vars.world.width() != fakeCliffsMap.width || Vars.world.height() != fakeCliffsMap.height)
            fakeCliffsMap = BitMap.of(Vars.world.width(), Vars.world.height());
        return fakeCliffsMap;
    }

    public void reset() {
        selectedBlock = Blocks.stone;
        team = Team.sharded;
        tool = EditorTool.pencil;
        radius = 1;
        fortsCarverPlace = true;
        fortsPlotState = FortsPlotState.enabled;
        cliffAuto = false;
        cliffSides = (byte) 0;
        blend = Blend.normal;
        hubServer = "";
    }
}
