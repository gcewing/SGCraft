//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Computer Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public class SGInterfaceBlock<TE extends TileEntity> extends BaseBlock<TE> {

    public SGInterfaceBlock(Material material, Class<TE> teClass) {
        super(material, BaseOrientation.orient4WaysByState, teClass);
    }

    @Override
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient4WaysByState;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

}
