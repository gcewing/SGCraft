//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft tile entity peripheral provider
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import net.minecraft.block.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import dan200.computercraft.api.peripheral.*;

import gcewing.sg.*;

public class CCPeripheralProvider implements IPeripheralProvider {

    @Override
    public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof CCInterfaceTE)
            return new CCSGPeripheral((CCInterfaceTE)te);
        else
            return null;
    }

}
