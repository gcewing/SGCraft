//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import dan200.computercraft.api.peripheral.IComputerAccess;
import gcewing.sg.IComputerInterface;
import gcewing.sg.SGInterfaceTE;
import net.minecraft.tileentity.TileEntity;

import java.util.HashSet;
import java.util.Set;

public class CCInterfaceTE extends SGInterfaceTE implements IComputerInterface {

    Set<IComputerAccess> attachedComputers = new HashSet<>();

    public void postEvent(TileEntity source, String name, Object... args) {
        for (IComputerAccess cpu : attachedComputers)
            cpu.queueEvent(name, prependArgs(cpu.getAttachmentName(), args));
    }
    
}
