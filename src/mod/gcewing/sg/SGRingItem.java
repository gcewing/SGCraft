//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

public class SGRingItem extends ItemBlock {

    public SGRingItem(Block block) {
        super(block);
        setHasSubtypes(true);
    }
    
    @Override
    public IIcon getIconFromDamage(int i) {
        return SGCraft.sgRingBlock.getIcon(0, i);
    }
    
    @Override
    public int getMetadata(int i) {
        return i;
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return String.format("%s.%s", super.getUnlocalizedName(stack), stack.getItemDamage());
    }
    
//	public static String subItemName(int i) {
//		return "tile.gcewing.sg.stargateRing." + i;
//	}

}
