//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

import gcewing.sg.*;

public class CCInterfaceBlock extends Base4WayCtrBlock<CCInterfaceTE> {

    public CCInterfaceBlock() {
        super(SGCraft.machineMaterial, CCInterfaceTE.class);
        setPrefixedIconNames("gcewing_sg:ccInterface", "bottom", "top", "side", "front", "side");
    }
    
    SGBaseTE getBaseTE(World world, int x, int y, int z) {
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity te = world.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
            if (te instanceof SGRingTE)
                return ((SGRingTE)te).getBaseTE();
        }
        return null;
    }
    
    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

}
