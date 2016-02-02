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
        super(Material.rock, MapColor.greenColor);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
    }

}
