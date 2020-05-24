//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Ore block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BaseOreBlock extends BaseBlock {

    private Random rand = new Random();

    public BaseOreBlock() {
        super(Material.rock);
    }
    
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(0, random, fortune)) {
            int j = random.nextInt(fortune + 2) - 1;
            if (j < 0)
                j = 0;
            return this.quantityDropped(random) * (j + 1);
        }
        else
            return this.quantityDropped(random);
    }

}
