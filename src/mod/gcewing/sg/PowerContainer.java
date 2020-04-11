//------------------------------------------------------------------------------------------------
//
//   SG Craft - Power unit gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static gcewing.sg.BaseBlockUtils.getWorldTileEntity;

public class PowerContainer extends BaseContainer {

    final PowerTE te;

    public PowerContainer(EntityPlayer player, PowerTE te) {
        super(PowerScreen.guiWidth, PowerScreen.guiHeight);
        this.te = te;
    }

    public static PowerContainer create(EntityPlayer player, World world, BlockPos pos) {
        TileEntity te = getWorldTileEntity(world, pos);
        if (te instanceof PowerTE)
            return new PowerContainer(player, (PowerTE) te);
        else
            return null;
    }

}
