//------------------------------------------------------------------------------------------------
//
//   SG Craft - Iris upgrade item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import static gcewing.sg.BaseBlockUtils.*;

public class SGIrisUpgradeItem extends BaseItem {

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
        BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        Block block = getWorldBlock(world, pos);
        if (block instanceof ISGBlock) {
            SGBaseTE te = ((ISGBlock)block).getBaseTE(world, pos);
            if (te != null)
                return te.applyIrisUpgrade(stack, player);
        }
        return false;
    }

}
