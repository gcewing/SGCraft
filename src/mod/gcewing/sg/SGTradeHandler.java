//------------------------------------------------------------------------------------------------
//
//   SG Craft - Villager trade handler
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

// import net.minecraft.block.*;
// import net.minecraft.entity.*;
// import net.minecraft.entity.passive.*;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.village.*;

// import net.minecraftforge.fml.common.registry.*;


public class SGTradeHandler implements ITradeList {

    @Override
    public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipes, Random random) {

        recipes.add(new MerchantRecipe(
            new ItemStack(Items.EMERALD, 8),
            new ItemStack(Items.DIAMOND, 1),
            new ItemStack(SGCraft.sgCoreCrystal)));

        recipes.add(new MerchantRecipe(
            new ItemStack(Items.EMERALD, 16),
            new ItemStack(Items.DIAMOND, 1),
            new ItemStack(SGCraft.sgControllerCrystal)));
    }
}
