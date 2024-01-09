// ------------------------------------------------------------------------------------------------
//
// SG Craft - Open Computers Integration Module
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import gcewing.sg.BaseConfiguration;
import gcewing.sg.IntegrationBase;
import gcewing.sg.SGBaseTE;
import gcewing.sg.SGCraft;
import gcewing.sg.SGGui;

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
        mod.newRecipe(
                ocInterface,
                1,
                "ini",
                "cmc",
                "ibi",
                'i',
                Items.iron_ingot,
                'n',
                "ingotNaquadahAlloy",
                'c',
                cable,
                'm',
                microchip1,
                'b',
                pcb);
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
