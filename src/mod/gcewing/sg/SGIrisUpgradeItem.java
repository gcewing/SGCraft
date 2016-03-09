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
import net.minecraft.util.EnumChatFormatting;
import static gcewing.sg.BaseBlockUtils.*;

public class SGIrisUpgradeItem extends BaseItem {
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add(EnumChatFormatting.GOLD + "Install this in a stargate to provide a redstone or computer controlled");
        list.add(EnumChatFormatting.GOLD + "impenetrable barrier capable of blocking incoming gate travel");
    }
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
        BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        //System.out.printf("SGIrisUpgradeItem.onItemUse: at %s\n", pos);
        Block block = getWorldBlock(world, pos);
        if (block instanceof ISGBlock) {
            SGBaseTE te = ((ISGBlock)block).getBaseTE(world, pos);
            if (te != null)
                return te.applyIrisUpgrade(stack, player);
        }
        return false;
    }

}
