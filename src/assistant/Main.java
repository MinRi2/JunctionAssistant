package assistant;

import mindustry.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;

public class Main extends Mod{
    @Override
    public void init(){
        super.init();

        JunctionAssistant.init();

        LoadedMod mod = Vars.mods.locateMod("junction-assistant");
        mod.meta.description = mod.root.child("description").readString();
    }
}
