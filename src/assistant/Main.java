package assistant;

import mindustry.mod.*;

public class Main extends Mod{
    @Override
    public void init(){
        super.init();

        JunctionAssistant.init();
    }
}
