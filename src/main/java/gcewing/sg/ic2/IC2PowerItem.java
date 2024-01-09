// ------------------------------------------------------------------------------------------------
//
// SG Craft - IC2 Stargate Power Unit Item
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import net.minecraft.block.Block;

import gcewing.sg.PowerItem;

public class IC2PowerItem extends PowerItem {

    public IC2PowerItem(Block block) {
        super(block, "EU", IC2PowerTE.maxEnergyBuffer);
    }

}
