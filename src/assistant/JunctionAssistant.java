package assistant;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

/**
 * @author minri2
 * Create by 2024/10/2
 */
public class JunctionAssistant{
    private static int lastX, lastY;

    private static final int maxInstantReplacement = 2;

    private static final Block rotateReplacement = Blocks.sorter,
    instantReplacement = Blocks.invertedSorter;

    public static void init(){
        // Just occasion after updating line plans and before rendering.
        Events.run(Trigger.preDraw, () -> {
            if(!Core.settings.getBool("junction-assistant")) return;

            if(!Vars.mobile && (!Core.input.keyDown(Binding.select) || Core.scene.hasMouse())) return;

            InputHandler input = Vars.control.input;
            if(input.block == null) return;

            int cursorX = World.toTile(Core.input.mouseWorldX());
            int cursorY = World.toTile(Core.input.mouseWorldY());

            if(lastX != cursorX || lastY != cursorY){
                lastX = cursorX;
                lastY = cursorY;

                if(input.block instanceof Junction junction){
                    updateJunctionLine(junction, input.linePlans);
                }else if(input.block instanceof Conveyor conveyor){
                    updateConveyorLine(conveyor, input.linePlans);
                }
            }
        });

        for(Block block : Vars.content.blocks()){
            if(block instanceof Junction junction && junction.size == 1){
                junction.allowDiagonal = true;
                junction.conveyorPlacement = true;
            }
        }

        Vars.ui.settings.game.checkPref("junction-assistant", true);
    }

    private static void updateJunctionLine(Junction junction, Seq<BuildPlan> plans){
        if(junction.size != 1 || plans.size <= 1) return;

        BuildPlan firstPlan = plans.get(0), secondPlan = plans.get(1);

        int dir = Tile.relativeTo(firstPlan.x, firstPlan.y, secondPlan.x, secondPlan.y);
        int instantCount = 0;

        BuildPlan lastPlan = firstPlan;
        for(int i = 1; i < plans.size - 1; i++){
            BuildPlan plan = plans.get(i), nextPlan = plans.get(i + 1);

            int nextDir = Tile.relativeTo(plan.x, plan.y, nextPlan.x, nextPlan.y);

            if(nextDir != dir){
                plan.block = rotateReplacement;
                dir = nextDir;

                // rotate is in priority.
                if(++instantCount > maxInstantReplacement){
                    lastPlan.block = junction;
                    instantCount = 1;
                }
            }else if(instantCount < maxInstantReplacement){
                plan.block = instantReplacement;
                instantCount++;
            }else{
                instantCount = 0;
            }

            lastPlan = plan;
        }

        if(instantCount < maxInstantReplacement){
            plans.peek().block = instantReplacement;
//            instantCount++;
        }

//        for(int i = 0; i < plans.size; i++){
//            BuildPlan plan = plans.get(i);
//            BuildPlan nextPlan = i + 1 < plans.size ? plans.get(i + 1) : null;
//
//            if(lastPlan != null && nextPlan != null &&
//            Tile.relativeTo(lastPlan.x, lastPlan.y, plan.x, plan.y) != Tile.relativeTo(plan.x, plan.y, nextPlan.x, nextPlan.y)){
//                plan.block = Blocks.underflowGate;
//            }else{
//                for(Point2 point : Geometry.d4){
//                    int x = plan.x + point.x;
//                    int y = plan.y + point.y;
//
//                    Building building = Vars.world.build(x, y);
//
//                    if(building != null && acceptTypes.contains(c -> building.getClass().isAssignableFrom(c)) && visitedBuildings.add(building)){
//                        plan.block = Blocks.underflowGate;
//                    }
//                }
//            }
//
//            lastPlan = plan;
//        }
    }

    private static void updateConveyorLine(Conveyor conveyor, Seq<BuildPlan> plans){
        if(conveyor.size != 1 || plans.size <= 1) return;

        if(buildCost(conveyor) < buildCost(instantReplacement)){
            return;
        }

        int instantCount = 0;
        int lastRotation = plans.first().rotation;
        for(int i = 1; i < plans.size; i++){
            BuildPlan plan = plans.get(i);

            if(lastRotation == plan.rotation && instantCount < maxInstantReplacement){
                plan.block = instantReplacement;
                instantCount++;
            }else{
                instantCount = 0;
            }

            lastRotation = plan.rotation;
        }
    }

    private static float buildCost(Block block){
        float sum = 0;
        for(ItemStack stack : block.requirements){
            sum += stack.item.cost;
        }
        return sum;
    }
}
