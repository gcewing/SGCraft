//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import net.minecraftforge.common.util.*;

import gcewing.sg.*;

public class OCInterfaceBlock extends SGInterfaceBlock<OCInterfaceTE> {

    public OCInterfaceBlock() {
        super(SGCraft.machineMaterial, OCInterfaceTE.class);
        setModelAndTextures("block/interface.smeg",
            "ocInterface-bottom", "ocInterface-top", "ocInterface-side", "ocInterface-side");
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumFacing side, float hx, float hy, float hz)
    {
        if (!world.isRemote)
            SGCraft.mod.openGui(player, SGGui.OCInterface, world, pos);
        return true;
    }
    
}
