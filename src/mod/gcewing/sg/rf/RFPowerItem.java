//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.item.*;

import gcewing.sg.*;

public class RFPowerItem extends PowerItem {

    public RFPowerItem(Block block) {
        super(block, "RF", RFPowerTE.maxEnergyBuffer);
    }

}
