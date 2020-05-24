//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Integration Module
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import gcewing.sg.*;

public class RFIntegration extends BaseSubsystem<SGCraft, SGCraftClient> {

    @Override
    public void registerBlocks() {
        mod.rfPowerUnit = mod.newBlock("rfPowerUnit", RFPowerBlock.class);
    }

}
