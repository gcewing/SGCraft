//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah ore block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.*;
import net.minecraft.item.*;

public class NaquadahOreBlock extends BlockOre {

    //static int texture = 0x40;

    public NaquadahOreBlock() {
        super();
        setHardness(5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 3);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
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
