//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Power Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import static net.minecraftforge.fml.common.Loader.*;
import gcewing.sg.*;

public class RFIntegration extends BaseSubsystem<SGCraft, SGCraftClient> {

    @Override
    public void registerBlocks() {
        mod.rfPowerUnit = mod.newBlock("rfPowerUnit", RFPowerBlock.class); //[RF]
    }
    
    @Override
    public void registerRecipes() {
        if (isModLoaded("ThermalExpansion"))
            addThermalExpansionPowerBlockRecipe();
        else
            addGenericPowerBlockRecipe();
    }

    protected void addThermalExpansionPowerBlockRecipe() {
        Item frame = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thermalexpansion", "Frame"));
        Item coil = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thermalexpansion", "material"));
        ItemStack hardenedEnergyFrame = new ItemStack(frame, 1, 4);
        ItemStack receptionCoil = new ItemStack(coil, 1, 1);
        ItemStack transmissionCoil = new ItemStack(coil, 1, 2);
        mod.newRecipe(mod.rfPowerUnit, 1, "ttt", "hrh", "ici",
            't', transmissionCoil, 'h', hardenedEnergyFrame, 'r', receptionCoil,
            'i', "ingotInvar", 'c', "ingotCopper");
    }

    protected void addGenericPowerBlockRecipe() {
        mod.newRecipe(mod.rfPowerUnit, 1, "cgc", "gIg", "crc",
            'c', mod.ic2Capacitor, 'g', "ingotGold",
            'I', "blockIron", 'r', Items.REDSTONE);
    }

}
