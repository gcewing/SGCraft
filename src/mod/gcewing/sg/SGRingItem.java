//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.item.*;

public class SGRingItem extends ItemBlock {

    public SGRingItem(Block block) {
        super(block);
        setHasSubtypes(true);
    }
    
    @Override
    public int getMetadata(int i) {
        return i;
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        return String.format("%s.%s", super.getTranslationKey(stack), stack.getItemDamage());
    }
    
}
