// ------------------------------------------------------------------------------------------------
//
// SG Craft - Stargate ring block item
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

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
