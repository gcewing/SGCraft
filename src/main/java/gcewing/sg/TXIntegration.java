//------------------------------------------------------------------------------------------------
//
//   SG Craft - Thermal Expansion Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import cpw.mods.fml.common.registry.GameRegistry;
import gcewing.sg.*;

public class TXIntegration extends BaseSubsystem<SGCraft, SGCraftClient> {

    @Override
    public void registerRecipes() {
        //Item cell = GameRegistry.findItem("ThermalExpansion", "Cell");
        //ItemStack hardenedEnergyCell = new ItemStack(cell, 1, 2);
        Item frame = GameRegistry.findItem("ThermalExpansion", "Frame");
        Item coil = GameRegistry.findItem("ThermalExpansion", "material");
        ItemStack hardenedEnergyFrame = new ItemStack(frame, 1, 4);
        ItemStack receptionCoil = new ItemStack(coil, 1, 1);
        ItemStack transmissionCoil = new ItemStack(coil, 1, 2);
        mod.newRecipe(mod.rfPowerUnit, 1, "ttt", "hrh", "ici",
            't', transmissionCoil, 'h', hardenedEnergyFrame, 'r', receptionCoil,
            'i', "ingotInvar", 'c', "ingotCopper");
    }
    
}
