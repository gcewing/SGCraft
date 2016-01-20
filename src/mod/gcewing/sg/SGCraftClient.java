//------------------------------------------------------------------------------------------------
//
//   SG Craft - Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.client.gui.*;
import net.minecraft.entity.player.*;
import net.minecraft.world.*;

import gcewing.sg.ic2.*;

public class SGCraftClient extends BaseModClient<SGCraft> {

    public SGCraftClient(SGCraft mod) {
        super(mod);
        debugSound = true;
    }
    
    @Override
    void registerScreens() {
        //System.out.printf("SGCraft: ProxyClient.registerScreens\n");
        addScreen(SGGui.SGBase, SGBaseScreen.class);
        addScreen(SGGui.SGController, DHDScreen.class);
        addScreen(SGGui.DHDFuel, DHDFuelScreen.class);
        addScreen(SGGui.PowerUnit, PowerScreen.class);
    }

    @Override
    void registerRenderers() {
        addTileEntityRenderer(SGBaseTE.class, new SGBaseTERenderer());
        addTileEntityRenderer(DHDTE.class, new DHDTERenderer());
        addEntityRenderer(IrisEntity.class, new IrisRenderer());
    }

}
