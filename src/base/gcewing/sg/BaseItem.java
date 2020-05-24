//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Generic Item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import gcewing.sg.BaseMod.*;
import static gcewing.sg.BaseUtils.*;

public class BaseItem extends Item implements IItem {

    public String[] getTextureNames() {
        return null;
    }

    public ModelSpec getModelSpec(ItemStack stack) {
        return null;
    }
    
    public int getNumSubtypes() {
        return 1;
    }
    
    @Override
    public boolean getHasSubtypes() {
        return getNumSubtypes() > 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
        int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(stack, player, world, new BlockPos(x, y, z),
            facings[side], hitX, hitY, hitZ);
    }
    
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
        BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

}
