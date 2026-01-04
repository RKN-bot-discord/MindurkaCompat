package mindurka;

import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class Stats {
    private static Stats previousStats = null;

    private final float polyHealth = UnitTypes.poly.health;
    private final float flareHealth = UnitTypes.flare.health;

    private final float cycloneMetaglassSplashDamage = ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.metaglass).splashDamage;
    private final float cycloneBlashCompoundSplashDamage = ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.blastCompound).splashDamage;
    private final float cyclonePlastaniumSplashDamage = ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.plastanium).splashDamage;
    private final float cycloneSurgeAlloySplashDamage = ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.surgeAlloy).splashDamage;

    private final float titanThoriumBuildingDamageMultiplier = ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).buildingDamageMultiplier;
    private final float titanThoriumSplashDamage = ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamage;
    private final float titanThoriumSplashDamageRadius = ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamageRadius;
    private final boolean titanThoriumSplashDamagePierce = ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamagePierce;
    private final float titanReload = ((ItemTurret) Blocks.titan).reload;

    private Stats() {}

    private void apply() {
        UnitTypes.poly.health = polyHealth;
        UnitTypes.flare.health = flareHealth;

        ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.metaglass).splashDamage = cycloneMetaglassSplashDamage;
        ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.blastCompound).splashDamage = cycloneBlashCompoundSplashDamage;
        ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.plastanium).splashDamage = cyclonePlastaniumSplashDamage;
        ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.surgeAlloy).splashDamage = cycloneSurgeAlloySplashDamage;

        ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).buildingDamageMultiplier = titanThoriumBuildingDamageMultiplier;
        ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamage = titanThoriumSplashDamage;
        ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamagePierce = titanThoriumSplashDamagePierce;
        ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamageRadius = titanThoriumSplashDamageRadius;
        ((ItemTurret) Blocks.titan).reload = titanReload;
    }

    public static void updateStats() {
        if (previousStats == null == Vars.state.rules.tags.get("mindurkaGamemode", "").equals("forts")) {
            if (previousStats == null) {
                previousStats = new Stats();

                UnitTypes.poly.health = 90f;
                UnitTypes.flare.health = 150f;

                ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.metaglass).splashDamage = 65f;
                ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.blastCompound).splashDamage = 100f;
                ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.plastanium).splashDamage = 95f;
                ((ItemTurret) Blocks.cyclone).ammoTypes.get(Items.surgeAlloy).splashDamage = 125f;

                ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).buildingDamageMultiplier = 0.01f;
                ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamage = 80f;
                ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamagePierce = true;
                ((ItemTurret) Blocks.titan).ammoTypes.get(Items.thorium).splashDamageRadius = 45f;
                ((ItemTurret) Blocks.titan).reload = 180f;
            }
            else {
                previousStats.apply();
                previousStats = null;
            }
        }
    }
}
