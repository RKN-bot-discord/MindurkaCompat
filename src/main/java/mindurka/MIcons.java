package mindurka;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import mindustry.gen.Icon;

public class MIcons {
    private MIcons() {}

    private static final TextureRegionDrawable cliffIcons = new TextureRegionDrawable(Core.atlas.find("mindurkacompat-cliff-icons"));
    public static final TextureRegionDrawable cliffAuto = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 128, 128, 128, 128));
    public static final TextureRegionDrawable cliff0 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 256, 128, 128, 128));
    public static final TextureRegionDrawable cliff1 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 256, 256, 128, 128));
    public static final TextureRegionDrawable cliff2 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 128, 256, 128, 128));
    public static final TextureRegionDrawable cliff3 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 0, 256, 128, 128));
    public static final TextureRegionDrawable cliff4 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 0, 128, 128, 128));
    public static final TextureRegionDrawable cliff5 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 0, 0, 128, 128));
    public static final TextureRegionDrawable cliff6 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 128, 0, 128, 128));
    public static final TextureRegionDrawable cliff7 = new TextureRegionDrawable(new TextureRegion(cliffIcons.getRegion(), 256, 0, 128, 128));

    private static final TextureRegionDrawable icons = new TextureRegionDrawable(Core.atlas.find("mindurkacompat-icons"));
    public static final TextureRegionDrawable fortsPlotToggle = new TextureRegionDrawable(new TextureRegion(icons.getRegion(), 0, 0, 128, 128));
    public static final TextureRegionDrawable fortsPlotCarver = new TextureRegionDrawable(new TextureRegion(icons.getRegion(), 128, 0, 128, 128));
    public static final TextureRegionDrawable blendNormal = new TextureRegionDrawable(new TextureRegion(icons.getRegion(), 0, 128, 128, 128));
    public static final TextureRegionDrawable blendReplace = new TextureRegionDrawable(new TextureRegion(icons.getRegion(), 128, 128, 128, 128));
    public static final TextureRegionDrawable blendUnder = new TextureRegionDrawable(new TextureRegion(icons.getRegion(), 256, 128, 128, 128));

    public static void load() {
        Icon.icons.put("cliff-auto", cliffAuto);
        Icon.icons.put("cliff-0", cliff0);
        Icon.icons.put("cliff-1", cliff1);
        Icon.icons.put("cliff-2", cliff2);
        Icon.icons.put("cliff-3", cliff3);
        Icon.icons.put("cliff-4", cliff4);
        Icon.icons.put("cliff-5", cliff5);
        Icon.icons.put("cliff-6", cliff6);
        Icon.icons.put("cliff-7", cliff7);
        Icon.icons.put("fortsPlotToggle", fortsPlotToggle);
        Icon.icons.put("fortsPlotCarver", fortsPlotCarver);
        Icon.icons.put("blend-normal", blendNormal);
        Icon.icons.put("blend-replace", blendReplace);
        Icon.icons.put("blend-under", blendUnder);
    }
}
