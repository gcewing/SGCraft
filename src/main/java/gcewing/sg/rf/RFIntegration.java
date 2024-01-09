// ------------------------------------------------------------------------------------------------
//
// SG Craft - RF Integration Module
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import gcewing.sg.BaseSubsystem;
import gcewing.sg.SGCraft;
import gcewing.sg.SGCraftClient;

public class RFIntegration extends BaseSubsystem<SGCraft, SGCraftClient> {

    @Override
    public void registerBlocks() {
        mod.rfPowerUnit = mod.newBlock("rfPowerUnit", RFPowerBlock.class);
    }

}
