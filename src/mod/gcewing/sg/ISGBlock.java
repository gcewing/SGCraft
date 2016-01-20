//------------------------------------------------------------------------------------------------
//
//   SG Craft - Interface for stargate ring and base blocks
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.world.*;

public interface ISGBlock {

    public SGBaseTE getBaseTE(IBlockAccess world, int x, int y, int z);
    public boolean isMerged(IBlockAccess world, int x, int y, int z);

}
