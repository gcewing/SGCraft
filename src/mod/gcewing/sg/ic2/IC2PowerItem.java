//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Stargate Power Unit Item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import java.util.*;

import net.minecraft.block.*;
//import net.minecraft.entity.player.*;
import net.minecraft.item.*;
//import net.minecraft.nbt.*;
//import net.minecraft.util.*;

import gcewing.sg.*;

public class IC2PowerItem extends PowerItem {

    public IC2PowerItem(Block block) {
        super(block, "EU", IC2PowerTE.maxEnergyBuffer);
    }

}
