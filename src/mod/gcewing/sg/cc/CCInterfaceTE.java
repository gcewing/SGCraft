//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import java.util.*;
import net.minecraft.world.*;
import net.minecraft.tileentity.*;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.*;

import gcewing.sg.*;

public class CCInterfaceTE extends SGInterfaceTE implements IComputerInterface {

    Set<IComputerAccess> attachedComputers = new HashSet<IComputerAccess>();

    public void postEvent(TileEntity source, String name, Object... args) {
        //System.out.printf("CCInterfaceTE.postEvent: %s\n", name);
        for (IComputerAccess cpu : attachedComputers)
            cpu.queueEvent(name, prependArgs(cpu.getAttachmentName(), args));
    }
    
}
