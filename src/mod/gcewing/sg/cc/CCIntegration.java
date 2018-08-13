//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import net.minecraft.block.*;
import net.minecraft.init.*;
import dan200.computercraft.api.*;
import gcewing.sg.*;

public class CCIntegration extends BaseSubsystem implements IIntegration {

    public static Block ccInterface;

    @Override
    public void registerBlocks() {
        System.out.printf("CCIntegration.registerBlocks\n");
        ccInterface = SGCraft.mod.newBlock("ccInterface", CCInterfaceBlock.class);
    }

    @Override
    public void registerRecipes() {
        if (ccInterface != null) {
            if (mod.config.getBoolean("recipes", "ccInterfaceBlock", true)) {
                SGCraft.mod.newRecipe("cc_interface", ccInterface, 1, "SnS", "SrS", "SSS", 'S', Blocks.STONE, 'n', "ingotNaquadahAlloy", 'r', Items.REDSTONE);
            }
        }
    }
    
    @Override
    public void registerOther() {
        ComputerCraftAPI.registerPeripheralProvider(new CCPeripheralProvider());
        CCMethodQueue.init();
    }
    
    @Override
    public void onServerTick() {
        //System.out.printf("CCIntegration.onServerTick\n");
        CCMethodQueue.onServerTick();
    }

}
