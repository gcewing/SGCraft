// ------------------------------------------------------------------------------------------------
//
// SG Craft - Iris Entity
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseBlockUtils.getTileEntityWorld;
import static gcewing.sg.BaseBlockUtils.getWorldTileEntity;
import static gcewing.sg.BaseUtils.newAxisAlignedBB;

import java.io.DataInput;
import java.io.DataOutput;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

public class IrisEntity extends BaseEntity implements IEntityAdditionalSpawnData {

    public static boolean debugIrisEntity = false;

    BlockPos blockPos;

    public IrisEntity(World world) {
        super(world);
    }

    public IrisEntity(SGBaseTE te) {
        this(getTileEntityWorld(te));
        double radius = 2;
        double thickness = SGBaseTE.irisThickness;
        double cx = 0;
        double cy = 2;
        double cz = SGBaseTE.irisZPosition;
        double hx = radius;
        double hy = radius;
        double hz = thickness;
        AxisAlignedBB localBox = newAxisAlignedBB(cx - hx, cy - hy, cz - hz, cx + hx, cy + hy, cz + hz);
        Trans3 t = te.localToGlobalTransformation();
        AxisAlignedBB globalBox = t.t(localBox);
        init(te.getPos(), globalBox);
    }

    void init(BlockPos pos, AxisAlignedBB box) {
        if (debugIrisEntity) SGCraft.log.debug(String.format("IrisEntity.init: %s at %s box %s", this, pos, box));
        this.blockPos = pos;
        setPosition(box.minX, box.minY, box.minZ);
        setBoundingBox(box);
    }

    @Override
    protected void entityInit() {}

    SGBaseTE getBaseTE() {
        TileEntity te = getWorldTileEntity(worldObj, blockPos);
        if (te instanceof SGBaseTE) return (SGBaseTE) te;
        return null;
    }

    @Override
    public boolean canBeCollidedWith() {
        boolean result;
        SGBaseTE te = getBaseTE();
        if (te != null) result = te.irisIsClosed();
        else result = false;
        return result;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        if (canBeCollidedWith()) return super.getEntityBoundingBox();
        return null;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        if (debugIrisEntity) SGCraft.log.debug(String.format("IrisEntity.readEntityFromNBT: %s", nbt));
        int blockX = nbt.getInteger("blockX");
        int blockY = nbt.getInteger("blockY");
        int blockZ = nbt.getInteger("blockZ");
        BlockPos pos = new BlockPos(blockX, blockY, blockZ);
        double minX = nbt.getDouble("minX");
        double minY = nbt.getDouble("minY");
        double minZ = nbt.getDouble("minZ");
        double maxX = nbt.getDouble("maxX");
        double maxY = nbt.getDouble("maxY");
        double maxZ = nbt.getDouble("maxZ");
        AxisAlignedBB box = newAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        init(pos, box);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("blockX", blockPos.getX());
        nbt.setInteger("blockY", blockPos.getY());
        nbt.setInteger("blockZ", blockPos.getZ());
        AxisAlignedBB box = getEntityBoundingBox();
        nbt.setDouble("minX", box.minX);
        nbt.setDouble("minY", box.minY);
        nbt.setDouble("minZ", box.minZ);
        nbt.setDouble("maxX", box.maxX);
        nbt.setDouble("maxY", box.maxY);
        nbt.setDouble("maxZ", box.maxZ);
        if (debugIrisEntity) SGCraft.log.debug(String.format("IrisEntity.writeEntityToNBT: %s", nbt));
    }

    @Override
    protected boolean shouldSetPosAfterLoading() {
        return false; // To prevent bounding box being reset
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            DataOutput data = new ByteBufOutputStream(buffer);
            BaseUtils.writeBlockPos(data, blockPos);
            AxisAlignedBB box = getEntityBoundingBox();
            data.writeDouble(box.minX);
            data.writeDouble(box.minY);
            data.writeDouble(box.minZ);
            data.writeDouble(box.maxX);
            data.writeDouble(box.maxY);
            data.writeDouble(box.maxZ);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            DataInput data = new ByteBufInputStream(buffer);
            BlockPos pos = BaseUtils.readBlockPos(data);
            double minX = data.readDouble();
            double minY = data.readDouble();
            double minZ = data.readDouble();
            double maxX = data.readDouble();
            double maxY = data.readDouble();
            double maxZ = data.readDouble();
            AxisAlignedBB box = newAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
            init(pos, box);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
