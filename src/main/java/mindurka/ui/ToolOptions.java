package mindurka.ui;

import arc.util.Nullable;
import mindurka.MVars;
import mindurka.rules.FortsPlotState;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.world.Block;

public class ToolOptions {
    public static class Slot {
        public Block selectedBlock = Blocks.coreShard;
        public Team team = Team.sharded;

        public void reset() {
            selectedBlock = Blocks.stone;
            team = Team.sharded;
        }
    }

    // uwu
    public Slot[] available = new Slot[] {
            new Slot(), new Slot(), new Slot(), new Slot(), new Slot(),
            new Slot(), new Slot(), new Slot(), new Slot(), new Slot(),
    };
    public Slot current = available[0];

    public boolean fortsCarverPlace = true;
    public int radius = 1;
    public EditorTool tool = EditorTool.pencil;
    public EditorMode mode = EditorMode.normal;

    public Block selectedBlock() {
        if (mode.downsizeBlock) {
            if (current.selectedBlock.isOverlay()) return Blocks.oreCopper;
            else if (current.selectedBlock.isFloor()) return Blocks.stone;
            else return Blocks.stoneWall;
        }

        return current.selectedBlock;
    }
    public Team team() {
        return current.team;
    }

    public @Nullable BitMap fakeCliffsMap;
    public boolean cliffAuto;
    public byte cliffSides;
    public Blend blend;
    public FortsPlotState fortsPlotState = FortsPlotState.enabled;
    public boolean floorsAsOverlays = false;
    public String hubServer = "";

    public @Nullable BitMap fakeCliffsMap() {
        if (!cliffAuto) return null;
        if (fakeCliffsMap == null || Vars.world.width() != fakeCliffsMap.width || Vars.world.height() != fakeCliffsMap.height)
            fakeCliffsMap = BitMap.of(Vars.world.width(), Vars.world.height());
        return fakeCliffsMap;
    }

    public void reset() {
        for (Slot slot : available) slot.reset();

        tool = EditorTool.pencil;
        mode = EditorMode.normal;
        radius = 1;
        fortsCarverPlace = true;
        fortsPlotState = FortsPlotState.enabled;
        cliffAuto = false;
        cliffSides = (byte) 0;
        blend = Blend.normal;
        hubServer = "";
    }
}
