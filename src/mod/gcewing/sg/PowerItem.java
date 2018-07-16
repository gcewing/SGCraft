//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Power Unit Item
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PowerItem extends ItemBlock {

    String unitName;
    double maxEnergy;

    public PowerItem(Block block, String unitName, double maxEnergy) {
        super(block);
        this.unitName = unitName;
        this.maxEnergy = maxEnergy;
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) {
            double eu = nbt.getDouble("energyBuffer");
            list.add(String.format("%F %s / %F", eu, unitName, maxEnergy));
        }
    }

}
