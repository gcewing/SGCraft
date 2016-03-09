//------------------------------------------------------------------------------------------------
//
//   SG Craft - Chevron upgrade item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.util.EnumChatFormatting;
import static gcewing.sg.BaseBlockUtils.*;

public class SGChevronUpgradeItem extends BaseItem {
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add(EnumChatFormatting.GOLD + "Install this in a stargate to upgrade it from 7 to 9 chevrons");
        list.add(EnumChatFormatting.GOLD + "This will allow a stargate to connect to stargates on other worlds");
    }
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
        BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        System.out.printf("SGChevronUpgradeItem.onItemUse: at %s\n", pos);
        Block block = getWorldBlock(world, pos);
        if (block instanceof ISGBlock) {
            SGBaseTE te = ((ISGBlock)block).getBaseTE(world, pos);
            if (te != null)
                return te.applyChevronUpgrade(stack, player);
        }
        return false;
    }

}
