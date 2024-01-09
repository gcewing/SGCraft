// ------------------------------------------------------------------------------------------------
//
// SG Craft - RF Stargate Power Unit Item
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import net.minecraft.block.Block;

import gcewing.sg.PowerItem;

public class RFPowerItem extends PowerItem {

    public RFPowerItem(Block block) {
        super(block, "RF", RFPowerTE.maxEnergyBuffer);
    }

}
