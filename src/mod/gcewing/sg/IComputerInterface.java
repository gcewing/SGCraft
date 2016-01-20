//------------------------------------------------------------------------------------------------
//
//   SG Craft - Interface implemented by tile entities that provide computer connections
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.tileentity.*;

public interface IComputerInterface {

    public void postEvent(TileEntity source, String name, Object... args);

}
