// ------------------------------------------------------------------------------------------------
//
// SG Craft - Interface implemented by tile entities that provide computer connections
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.tileentity.TileEntity;

public interface IComputerInterface {

    void postEvent(TileEntity source, String name, Object... args);

}
