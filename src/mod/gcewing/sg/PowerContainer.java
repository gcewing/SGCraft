//------------------------------------------------------------------------------------------------
//
//   SG Craft - Power unit gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class PowerContainer extends BaseContainer {

    PowerTE te;
    
    public static PowerContainer create(EntityPlayer player, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PowerTE)
            return new PowerContainer(player, (PowerTE)te);
        else
            return null;
    }
    
    public PowerContainer(EntityPlayer player, PowerTE te) {
        super(PowerScreen.guiWidth, PowerScreen.guiHeight);
        this.te = te;
    }

}
