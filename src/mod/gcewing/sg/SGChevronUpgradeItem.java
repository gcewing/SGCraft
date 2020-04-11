//------------------------------------------------------------------------------------------------
//
//   SG Craft - Chevron upgrade item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static gcewing.sg.BaseBlockUtils.getWorldBlock;

public class SGChevronUpgradeItem extends BaseItem {

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        System.out.printf("SGChevronUpgradeItem.onItemUse: at %s\n", pos);
        Block block = getWorldBlock(world, pos);
        if (block instanceof ISGBlock) {
            SGBaseTE te = ((ISGBlock) block).getBaseTE(world, pos);
            if (te != null)
                return te.applyChevronUpgrade(stack, player);
        }
        return false;
    }

}
