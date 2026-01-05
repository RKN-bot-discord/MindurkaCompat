package mindurka;

import arc.Core;
import arc.input.KeyCode;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.input.InputHandler;
import mindustry.input.MobileInput;
import mindustry.input.PlaceMode;
import mindustry.world.Block;
import mindustry.world.Tile;

public class OMobileInput extends MobileInput {
    public static void inject() {
        if (Vars.control.input instanceof MobileInput) {
            Vars.control.setInput(new OMobileInput());
        }
    }

    @Override
    public boolean tap(float x, float y, int count, KeyCode button) {
        if (Vars.state.isMenu() || lineMode || locked()) return false;
        Log.info("Tapped!");

        float worldx = Core.input.mouseWorld(x, y).x, worldy = Core.input.mouseWorld(x, y).y;

        //get tile on cursor
        Tile cursor = Reflect.invoke(InputHandler.class, this, "tileAt", new Object[] { x, y }, float.class, float.class);

        //ignore off-screen taps *elsewhere*
        boolean offscreen = cursor == null || Core.scene.hasMouse(x, y);
        Log.info("Off-screen? " + offscreen);
        //if(cursor == null || Core.scene.hasMouse(x, y)) return false;

        if (!offscreen) Call.tileTap(Vars.player, cursor);

        Tile linked = offscreen || cursor.build == null ? cursor : cursor.build.tile;

        if (!Vars.player.dead()) {
            Reflect.invoke(MobileInput.class, this, "checkTargets", new Object[] { worldx, worldy }, float.class, float.class);
        }

        //remove if plan present
        if (!offscreen && Reflect.<Boolean>invoke(MobileInput.class, this, "hasPlan", new Object[] { cursor }, Tile.class) && !commandMode){
            Reflect.invoke(MobileInput.class, this, "removePlan", new Object[] { Reflect.<BuildPlan>invoke(MobileInput.class, this, "getPlan", new Object[] { cursor }, Tile.class) }, BuildPlan.class);
        } else if (!offscreen && mode == PlaceMode.placing && isPlacing() && validPlace(cursor.x, cursor.y, block, rotation) &&
                !Reflect.<Boolean>invoke(MobileInput.class, this, "checkOverlapPlacement", new Object[] { cursor.x, cursor.y, block }, int.class, int.class, Block.class)) {
            //add to selection queue if it's a valid place position
            selectPlans.add(lastPlaced = new BuildPlan(cursor.x, cursor.y, rotation, block, block.nextConfig()));
            block.onNewPlan(lastPlaced);
        } else if (!offscreen && mode == PlaceMode.breaking && validBreak(linked.x, linked.y) && !Reflect.<Boolean>invoke(MobileInput.class, this, "hasPlan", new Object[] { linked }, Tile.class)){
            //add to selection queue if it's a valid BREAK position
            selectPlans.add(new BuildPlan(linked.x, linked.y));
        }else if((commandMode && selectedUnits.size > 0) || commandBuildings.size > 0){
            //handle selecting units with command mode
            commandTap(x, y, queueCommandMode);
        }else if(commandMode){
            tapCommandUnit();
        } else if (!offscreen) {
            //control units
            if (count == 2) {
                //reset payload target
                payloadTarget = null;

                //control a unit/block detected on first tap of double-tap
                if(unitTapped != null && Vars.state.rules.possessionAllowed && unitTapped.isAI() && unitTapped.team == Vars.player.team() && !unitTapped.dead && unitTapped.playerControllable()){
                    Call.unitControl(Vars.player, unitTapped);
                    recentRespawnTimer = 1f;
                }else if(buildingTapped != null && Vars.state.rules.possessionAllowed){
                    Call.buildingControlSelect(Vars.player, buildingTapped);
                    recentRespawnTimer = 1f;
                }else if(!Reflect.<Boolean>invoke(InputHandler.class, this, "checkConfigTap", new Object[0]) &&
                        !Reflect.<Boolean>invoke(InputHandler.class, this, "tryBeginMine", new Object[] { cursor }, Tile.class)){
                    Reflect.<Boolean>invoke(InputHandler.class, this, "tileTapped", new Object[] { linked.build }, Building.class);
                }
                return false;
            }

            unitTapped = selectedUnit();
            buildingTapped = selectedControlBuild();

            //prevent mining if placing/breaking blocks
            if(!Reflect.<Boolean>invoke(InputHandler.class, this, "tryRepairDerelict", new Object[] { cursor }, Tile.class) &&
                    !Reflect.<Boolean>invoke(InputHandler.class, this, "tryStopMine", new Object[0]) &&
                    !Reflect.<Boolean>invoke(InputHandler.class, this, "canTapPlayer", new Object[] { worldx, worldy }, float.class, float.class) &&
                    !Reflect.<Boolean>invoke(InputHandler.class, this, "checkConfigTap", new Object[0]) &&
                    !Reflect.<Boolean>invoke(InputHandler.class, this, "tileTapped", new Object[] { linked.build }, Building.class) &&
                    mode == PlaceMode.none && !Core.settings.getBool("doubletapmine")){
                Reflect.<Boolean>invoke(InputHandler.class, this, "tryBeginMine", new Object[] { cursor }, Tile.class);
            }
        }

        return false;
    }
}
