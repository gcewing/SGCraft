//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring tile entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
// import net.minecraft.util.BlockPos;

import static gcewing.sg.BaseBlockUtils.*;

public class SGRingTE extends BaseTileEntity {

    public boolean isMerged;
    public BlockPos basePos = new BlockPos(0, 0, 0);

    @Override
    public void readContentsFromNBT(NBTTagCompound nbt) {
        super.readContentsFromNBT(nbt);
        isMerged = nbt.getBoolean("isMerged");
        int baseX = nbt.getInteger("baseX");
        int baseY = nbt.getInteger("baseY");
        int baseZ = nbt.getInteger("baseZ");
        basePos = new BlockPos(baseX, baseY, baseZ);
    }
    
    @Override
    public void writeContentsToNBT(NBTTagCompound nbt) {
        super.writeContentsToNBT(nbt);
        nbt.setBoolean("isMerged", isMerged);
        nbt.setInteger("baseX", basePos.getX());
        nbt.setInteger("baseY", basePos.getY());
        nbt.setInteger("baseZ", basePos.getZ());
    }
    
    public SGBaseTE getBaseTE() {
        if (isMerged) {
            TileEntity bte = getWorldTileEntity(worldObj, basePos);
            if (bte instanceof SGBaseTE)
                return (SGBaseTE)bte;
        }
        return null;
    }

}
