//------------------------------------------------------------------------------------------------
//
//   SG Craft - Iris Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import io.netty.buffer.*;
import net.minecraft.entity.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.tileentity.*;
import net.minecraftforge.common.util.*;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.registry.*;
import net.minecraft.util.*;

public class IrisEntity extends Entity implements IEntityAdditionalSpawnData {

    int blockX, blockY, blockZ;
    int rot;
    
    public IrisEntity(World world) {
        super(world);
    }

    public IrisEntity(TileEntity te, int rot) {
        this(te.getWorldObj());
        init(te.xCoord, te.yCoord, te.zCoord, rot);
    }
    
    void init(int blockX, int blockY, int blockZ, int rot) {
        //System.out.printf("IrisEntity.init: %s at (%s, %s, %s) rot %s\n",
        //	this, blockX, blockY, blockZ, rot);
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.rot = rot;
        double x = blockX + 0.5;
        double y = blockY + 2.5;
        double z = blockZ + 0.5;
        double radius = 2.0;
        setPosition(x, y, z);
        double hx = radius, hy = radius, hz = radius;
        double d = SGBaseTE.irisZPosition;
        double thickness = SGBaseTE.irisThickness;
        switch (rot) {
            case 0: z += d; hz = thickness; break;
            case 1: x += d; hx = thickness; break;
            case 2: z -= d; hz = thickness; break;
            case 3: x -= d; hx = thickness; break;
        }
        boundingBox.setBounds(
            x - hx, y - hy, z - hz,
            x + hx, y + hy, z + hz
        );
    }
    
    @Override
    protected void entityInit() {
    }

    SGBaseTE getBaseTE() {
        TileEntity te = worldObj.getTileEntity(blockX, blockY, blockZ);
        if (te instanceof SGBaseTE)
            return (SGBaseTE)te;
        else
            return null;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        boolean result;
        SGBaseTE te = getBaseTE();
        if (te != null)
            result = te.irisIsClosed();
        else
            result = false;
        //System.out.printf("IrisEntity.canBeCollidedWith: %s\n", result);
        return result;
    }
    
    @Override
    public AxisAlignedBB getBoundingBox() {
        if (canBeCollidedWith())
            return boundingBox;
        else
            return null;
    }
    
    @Override
    public boolean canBePushed() {
        return false;
    }
    
//	@Override
//	public boolean hitByEntity(Entity e) {
//		if (!worldObj.isRemote) {
//			System.out.printf("IrisEntity.hitByEntity: %s with box %s\n", this, boundingBox);
//			worldObj.removeEntity(this);
//		}
//		return false;
//	}
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        int blockX = nbt.getInteger("blockX");
        int blockY = nbt.getInteger("blockY");
        int blockZ = nbt.getInteger("blockZ");
        int rot = nbt.getInteger("rot");
        init(blockX, blockY, blockZ, rot);
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("blockX", blockX);
        nbt.setInteger("blockY", blockY);
        nbt.setInteger("blockZ", blockZ);
        nbt.setInteger("rot", rot);
    }

    @Override
    protected boolean shouldSetPosAfterLoading() {
        return false; // To prevent bounding box being reset
    }

    // --------------------------- Spawn Data ---------------------------

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            DataOutput data = new ByteBufOutputStream(buffer);
            data.writeInt(blockX);
            data.writeInt(blockY);
            data.writeInt(blockZ);
            data.writeInt(rot);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            DataInput data = new ByteBufInputStream(buffer);
            int blockX = data.readInt();
            int blockY = data.readInt();
            int blockZ = data.readInt();
            int rot = data.readInt();
            init(blockX, blockY, blockZ, rot);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
