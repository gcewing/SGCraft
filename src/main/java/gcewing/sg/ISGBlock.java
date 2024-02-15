// ------------------------------------------------------------------------------------------------
//
// SG Craft - Interface for stargate ring and base blocks
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.world.IBlockAccess;

public interface ISGBlock {

    SGBaseTE getBaseTE(IBlockAccess world, BlockPos pos);

    boolean isMerged(IBlockAccess world, BlockPos pos);

}
