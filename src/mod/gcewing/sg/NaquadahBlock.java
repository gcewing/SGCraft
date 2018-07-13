//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah alloy block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.material.*;

public class NaquadahBlock extends Block {

    //static int texture = 0x43;

    public NaquadahBlock() {
        super(Material.ROCK, MapColor.GREEN);
        setHardness(5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
    }

}
