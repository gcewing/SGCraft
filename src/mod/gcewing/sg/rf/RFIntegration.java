//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Power Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import static net.minecraftforge.fml.common.Loader.isModLoaded;

import gcewing.sg.BaseSubsystem;
import gcewing.sg.SGCraft;
import gcewing.sg.SGCraftClient;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RFIntegration extends BaseSubsystem<SGCraft, SGCraftClient> {

    @Override
    public void registerBlocks() {
        mod.rfPowerUnit = mod.newBlock("rfPowerUnit", RFPowerBlock.class); //[RF]
    }

    @Override
    public void registerRecipes() {
        if (isModLoaded("thermalexpansion"))
            addThermalExpansionPowerBlockRecipe();
        else
            addGenericPowerBlockRecipe();
    }

    protected void addThermalExpansionPowerBlockRecipe() {
        Item frame = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thermalexpansion", "frame"));
        Item coil = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thermalfoundation", "material"));
        if (frame != null && coil != null) {
            ItemStack hardenedEnergyFrame = new ItemStack(frame, 1, 129);
            ItemStack receptionCoil = new ItemStack(coil, 1, 513);
            ItemStack transmissionCoil = new ItemStack(coil, 1, 514);
            if (hardenedEnergyFrame != null && receptionCoil != null && transmissionCoil != null) {
                if (mod.config.getBoolean("recipes", "teRFPowerUnitBlock", true)) {
                    mod.newRecipe("rfPowerUnit", mod.rfPowerUnit, 1, "ttt", "hrh", "ici", 't', transmissionCoil, 'h', hardenedEnergyFrame, 'r', receptionCoil, 'i', "ingotInvar", 'c', "ingotCopper");
                }
            }
        }
    }

    protected void addGenericPowerBlockRecipe() {
        if (isModLoaded("ic2") && mod.ic2Capacitor != null) {
            if (mod.config.getBoolean("recipes", "genericRFPowerUnitBlock", true)) {
                mod.newRecipe("rfPowerUnit", mod.rfPowerUnit, 1, "cgc", "gIg", "crc", 'c', mod.ic2Capacitor, 'g', "ingotGold", 'I', "blockIron", 'r', Items.REDSTONE);
            }
        }
    }
}
