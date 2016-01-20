//------------------------------------------------------------------------------------------------
//
//   SG Craft - Iris upgrade item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.world.*;

public class SGIrisUpgradeItem extends Item {

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
        int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        System.out.printf("SGIrisUpgradeItem.onItemUse: at (%s, %s, %s)\n", x, y, z);
        Block block = world.getBlock(x, y, z);
        if (block instanceof ISGBlock) {
            SGBaseTE te = ((ISGBlock)block).getBaseTE(world, x, y, z);
            if (te != null)
                return te.applyIrisUpgrade(stack, player);
        }
        return false;
    }

}
