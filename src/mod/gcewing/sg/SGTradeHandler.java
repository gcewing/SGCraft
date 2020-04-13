//------------------------------------------------------------------------------------------------
//
//   SG Craft - Villager trade handler
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.Random;

public class SGTradeHandler implements IVillageTradeHandler {

    @SuppressWarnings("unchecked")
    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipes, Random random) {

        recipes.add(new MerchantRecipe(
                new ItemStack(Items.emerald, 8),
                new ItemStack(Items.diamond, 1),
                new ItemStack(SGCraft.sgCoreCrystal)));

        recipes.add(new MerchantRecipe(
                new ItemStack(Items.emerald, 16),
                new ItemStack(Items.diamond, 1),
                new ItemStack(SGCraft.sgControllerCrystal)));
    }

}
