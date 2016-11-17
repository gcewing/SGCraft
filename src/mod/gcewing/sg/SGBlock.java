//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public abstract class SGBlock<TE extends TileEntity> extends BaseBlock<TE> implements ISGBlock {

    public SGBlock(Material material, Class<TE> teClass) {
        super(material, teClass);
    }

    @Override    
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
        boolean willHarvest)
    {
        if (player.capabilities.isCreativeMode && isConnected(world, pos)) {
            if (world.isRemote)
                SGBaseTE.sendChatMessage(player, "Disconnect stargate before breaking");
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    boolean isConnected(World world, BlockPos pos) {
        SGBaseTE bte = getBaseTE(world, pos);
        return bte != null && bte.isConnected();
    }

}
