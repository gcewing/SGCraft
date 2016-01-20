//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah ore block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.creativetab.*;

import net.minecraftforge.common.*;

public class NaquadahOreBlock extends BlockOre {

    //static int texture = 0x40;

    public NaquadahOreBlock() {
        super();
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 3);
        setCreativeTab(CreativeTabs.tabBlock);
    }
    
    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3) {
        return SGCraft.naquadah;
    }
    
    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(5);
    }

}
