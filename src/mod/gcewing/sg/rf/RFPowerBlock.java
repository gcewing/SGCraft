//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import gcewing.sg.*;

public class RFPowerBlock extends PowerBlock<RFPowerTE> {

    public RFPowerBlock() {
        super(RFPowerTE.class, null);
        setModelAndTextures("block/power.smeg",
            "rfPowerUnit-bottom", "rfPowerUnit-top", "rfPowerUnit-side");
    }
    
}
