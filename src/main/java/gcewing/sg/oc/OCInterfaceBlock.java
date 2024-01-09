// ------------------------------------------------------------------------------------------------
//
// SG Craft - Open Computers Interface Block
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import gcewing.sg.BlockPos;
import gcewing.sg.IBlockState;
import gcewing.sg.SGCraft;
import gcewing.sg.SGGui;
import gcewing.sg.SGInterfaceBlock;

public class OCInterfaceBlock extends SGInterfaceBlock<OCInterfaceTE> {

    public OCInterfaceBlock() {
        super(SGCraft.machineMaterial, OCInterfaceTE.class);
        setModelAndTextures(
                "block/interface.smeg",
                "ocInterface-bottom",
                "ocInterface-top",
                "ocInterface-side",
                "ocInterface-side");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
            float hx, float hy, float hz) {
        if (!world.isRemote) SGCraft.mod.openGui(player, SGGui.OCInterface, world, pos);
        return true;
    }

}
