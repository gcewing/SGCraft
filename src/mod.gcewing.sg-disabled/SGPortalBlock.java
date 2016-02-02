//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate portal block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

public class SGPortalBlock extends Block {

    public SGPortalBlock() {
        super(Material.rock);
        setBlockBounds(0, 0, 0, 0, 0, 0);
    }
    
    @Override
    public int getRenderType() {
        return -1;
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
        return null;
    }

    @Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }

    SGBaseTE getStargateTE(World world, int x, int y, int z) {
        for (int i = -1; i <= 1; i++)
            for (int j = -3; j <= -1; j++)
                for (int k = -1; k <= 1; k++) {
                    TileEntity te = world.getTileEntity(x + i, y + j, z + k);
                    if (te instanceof SGBaseTE)
                        return (SGBaseTE)te;
                }
        return null;
    }

}
