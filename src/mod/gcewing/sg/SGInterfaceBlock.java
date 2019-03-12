//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Computer Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.material.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class SGInterfaceBlock<TE extends TileEntity> extends BaseBlock<TE> {

    public SGInterfaceBlock(Material material, Class<TE> teClass) {
        super(material, BaseOrientation.orient4WaysByState, teClass);
    }
    
    @Override
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient4WaysByState;
    }
    
    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

}
