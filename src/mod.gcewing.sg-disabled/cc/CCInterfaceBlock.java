//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

import gcewing.sg.*;

public class CCInterfaceBlock extends SGInterfaceBlock<CCInterfaceTE> {

    public CCInterfaceBlock() {
        super(SGCraft.machineMaterial, CCInterfaceTE.class);
        setModelAndTextures("block/interface.smeg",
            "ccInterface-bottom", "ccInterface-top", "ccInterface-front", "ccInterface-side");
    }

}
