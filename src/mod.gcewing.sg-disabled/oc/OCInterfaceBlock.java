//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;

import net.minecraftforge.common.util.*;

import gcewing.sg.*;

public class OCInterfaceBlock extends SGInterfaceBlock<OCInterfaceTE> {

    public OCInterfaceBlock() {
        super(SGCraft.machineMaterial, OCInterfaceTE.class);
        setPrefixedIconNames("gcewing_sg:ocInterface", "bottom", "top", "side");
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side,
        float hx, float hy, float hz)
    {
        if (!world.isRemote)
            SGCraft.mod.openGui(player, SGGui.OCInterface, world, x, y, z);
        return true;
    }
    
}
