// ------------------------------------------------------------------------------------------------
//
// SG Craft - Computercraft tile entity peripheral provider
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class CCPeripheralProvider implements IPeripheralProvider {

    @Override
    public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof CCInterfaceTE) return new CCSGPeripheral((CCInterfaceTE) te);
        else return null;
    }

}
