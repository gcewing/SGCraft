// ------------------------------------------------------------------------------------------------
//
// SG Craft - Naquadah ore block
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.Random;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class NaquadahOreBlock extends BaseOreBlock {

    public NaquadahOreBlock() {
        super();
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 3);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return SGCraft.naquadah;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(5);
    }

}
