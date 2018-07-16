//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import gcewing.sg.*;

public class OCInterfaceBlock extends SGInterfaceBlock<OCInterfaceTE> {

    public OCInterfaceBlock() {
        super(SGCraft.machineMaterial, OCInterfaceTE.class);
        setModelAndTextures("block/interface.smeg",
            "ocInterface-bottom", "ocInterface-top", "ocInterface-side", "ocInterface-side");
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumHand hand, EnumFacing side, float hx, float hy, float hz)
    {
        if (!world.isRemote)
            SGCraft.mod.openGui(player, SGGui.OCInterface, world, pos);
        return true;
    }
    
}
