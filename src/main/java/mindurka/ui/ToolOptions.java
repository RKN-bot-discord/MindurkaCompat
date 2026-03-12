package mindurka.ui;

import arc.util.Nullable;
import mindurka.rules.FortsPlotState;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
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
