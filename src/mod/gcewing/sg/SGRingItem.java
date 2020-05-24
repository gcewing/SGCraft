//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

public class SGRingItem extends BaseItemBlock {

    public SGRingItem(Block block) {
        super(block);
        setHasSubtypes(true);
    }
    
    @Override
    public int getMetadata(int i) {
        return i;
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return String.format("%s.%s", super.getUnlocalizedName(stack), stack.getItemDamage());
    }
    
}
