//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Computer Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.material.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

public class SGInterfaceBlock<TE extends TileEntity> extends Base4WayCtrBlock<TE> {

    public SGInterfaceBlock(Material material, Class<TE> teClass) {
        super(material, teClass);
    }

//	SGBaseTE getBaseTE(World world, int x, int y, int z) {
//		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
//			TileEntity te = world.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
//			if (te instanceof SGRingTE)
//				return ((SGRingTE)te).getBaseTE();
//		}
//		return null;
//	}
    
    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

}
