//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

public abstract class SGBlock<TE extends TileEntity> extends BaseBlock<TE> implements ISGBlock {

    public SGBlock(Material material, Class<TE> teClass) {
        super(material, teClass);
    }

    @Override    
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        if (player.capabilities.isCreativeMode && isConnected(world, new BlockPos(x, y, z))) {
            if (world.isRemote)
                SGBaseTE.sendChatMessage(player, "Disconnect stargate before breaking");
            return false;
        }
        return super.removedByPlayer(world, player, x, y, z);
    }
    
    boolean isConnected(World world, BlockPos pos) {
        SGBaseTE bte = getBaseTE(world, pos);
        return bte != null && bte.isConnected();
    }

}
