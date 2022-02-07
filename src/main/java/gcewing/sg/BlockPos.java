//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - 1.8 Compatibility - Block position
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class BlockPos {

    public int x, y, z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public BlockPos(TileEntity te) {
        this(te.xCoord, te.yCoord, te.zCoord);
    }
    
    public int getX() {return x;}
    public int getY() {return y;}
    public int getZ() {return z;}

    public BlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }
    
    public BlockPos offset(EnumFacing dir) {
        return new BlockPos(x + dir.getFrontOffsetX(), y + dir.getFrontOffsetY(), z + dir.getFrontOffsetZ());
    }
    
    public boolean equals(BlockPos other) {
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }
    
    @Override
    public String toString() {
        return String.format("(%s,%s,%s)", x, y, z);
    }

}
