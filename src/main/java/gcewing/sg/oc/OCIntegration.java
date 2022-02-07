//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
//import li.cil.oc.api.*;
import gcewing.sg.*;

public class OCIntegration extends IntegrationBase {

    public static Block ocInterface;
    public static ItemStack networkCard;
    
    static ItemStack ocItem(String name) {
        return li.cil.oc.api.Items.get(name).createItemStack(1);
    }
    
    @Override
    public void configure(BaseConfiguration config) {
        OCWirelessEndpoint.configure(config);
    }
    
    @Override
    public void registerBlocks() {
        SGCraft.log.debug("OCIntegration.registerBlocks");
        ocInterface = mod.newBlock("ocInterface", OCInterfaceBlock.class);
    }
    
    @Override
    public void registerItems() {
        networkCard = ocItem("lanCard");
    }
    
    @Override
    public void registerRecipes() {
        ItemStack cable = ocItem("cable");
        ItemStack microchip1 = ocItem("chip1");
        ItemStack pcb = ocItem("printedCircuitBoard");
        mod.newRecipe(ocInterface, 1, "ini", "cmc", "ibi",
            'i', Items.iron_ingot, 'n', "ingotNaquadahAlloy",
            'c', cable, 'm', microchip1, 'b', pcb);
    }
    
    @Override
    public void registerContainers() {
        mod.addContainer(SGGui.OCInterface, OCInterfaceContainer.class);
    }
    
    @Override
    public void registerScreens() {
        mod.client.addScreen(SGGui.OCInterface, OCInterfaceScreen.class);
    }
    
    public void onSGBaseTEAdded(SGBaseTE te) {
        te.ocWirelessEndpoint = new OCWirelessEndpoint(te);
    }

}
