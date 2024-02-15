// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 Version B - Generic Tile Entity
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseBlockUtils.getWorldBlockState;
import static gcewing.sg.BaseUtils.classForName;
import static gcewing.sg.BaseUtils.getFieldDef;
import static gcewing.sg.BaseUtils.getIntField;
import static gcewing.sg.BaseUtils.getMethodDef;
import static gcewing.sg.BaseUtils.invokeMethod;
import static gcewing.sg.BaseUtils.setIntField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import gcewing.sg.BaseMod.IBlock;

public class BaseTileEntity extends TileEntity implements BaseMod.ITileEntity {

    public byte side, turn;
    public Ticket chunkTicket;
    protected boolean updateChunk;

    public BlockPos getPos() {
        return new BlockPos(xCoord, yCoord, zCoord);
    }

    public int getX() {
        return xCoord;
    }

    public int getY() {
        return yCoord;
    }

    public int getZ() {
        return zCoord;
    }

    public void setSide(int side) {
        this.side = (byte) side;
    }

    public void setTurn(int turn) {
        this.turn = (byte) turn;
    }

    public Trans3 localToGlobalRotation() {
        return localToGlobalTransformation(Vector3.zero);
    }

    public Trans3 localToGlobalTransformation() {
        return localToGlobalTransformation(Vector3.blockCenter(xCoord, yCoord, zCoord));
    }

    public Trans3 localToGlobalTransformation(Vector3 origin) {
        BlockPos pos = getPos();
        IBlockState state = getWorldBlockState(worldObj, pos);
        Block block = state.getBlock();
        if (block instanceof IBlock) {
            return ((IBlock) block).localToGlobalTransformation(worldObj, pos, state, origin);
        }

        SGCraft.log.debug(String.format("BaseTileEntity.localToGlobalTransformation: Wrong block type at %s", pos));
        return new Trans3(origin);
    }

    @Override
    public Packet getDescriptionPacket() {
        if (!syncWithClient()) {
            return null;
        }

        NBTTagCompound nbt = new NBTTagCompound();
        super.writeToNBT(nbt);
        writeClientStateToNBT(nbt);
        if (updateChunk) {
            nbt.setBoolean("updateChunk", true);
            updateChunk = false;
        }
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.func_148857_g();
        super.readFromNBT(nbt);
        readClientStateFromNBT(nbt);
        if (nbt.getBoolean("updateChunk")) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    boolean syncWithClient() {
        return true;
    }

    public void markBlockForUpdate() {
        updateChunk = true;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    protected static Method getOrCreateChunkWatcher = getMethodDef(
            PlayerManager.class,
            "getOrCreateChunkWatcher",
            "func_72690_a",
            int.class,
            int.class,
            boolean.class);

    protected static Field flagsYAreasToUpdate = getFieldDef(
            classForName("net.minecraft.server.management.PlayerManager$PlayerInstance"),
            "flagsYAreasToUpdate",
            "field_73260_f");

    public void markForUpdate() {
        if (worldObj.isRemote) {
            return;
        }

        PlayerManager pm = ((WorldServer) worldObj).getPlayerManager();
        Object watcher = invokeMethod(pm, getOrCreateChunkWatcher, xCoord >> 4, zCoord >> 4, false);
        if (watcher == null) {
            return;
        }

        int oldFlags = getIntField(watcher, flagsYAreasToUpdate);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        setIntField(watcher, flagsYAreasToUpdate, oldFlags);
    }

    public void playSoundEffect(String name, float volume, float pitch) {
        worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, name, volume, pitch);
    }

    @Override
    public void onAddedToWorld() {}

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readPersistentStateFromNBT(nbt);
    }

    protected void readPersistentStateFromNBT(NBTTagCompound nbt) {
        side = nbt.getByte("side");
        turn = nbt.getByte("turn");
        readContentsFromNBT(nbt);
    }

    public void readFromItemStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) readFromItemStackNBT(nbt);
    }

    public void readFromItemStackNBT(NBTTagCompound nbt) {
        readContentsFromNBT(nbt);
    }

    protected void readClientStateFromNBT(NBTTagCompound nbt) {
        readPersistentStateFromNBT(nbt);
    }

    public void readContentsFromNBT(NBTTagCompound nbt) {}

    @Override
    public final void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        writePersistentStateToNBT(nbt);
    }

    protected void writePersistentStateToNBT(NBTTagCompound nbt) {
        if (side != 0) nbt.setByte("side", side);
        if (turn != 0) nbt.setByte("turn", turn);
        writeContentsToNBT(nbt);
    }

    public void writeToItemStackNBT(NBTTagCompound nbt) {
        writeContentsToNBT(nbt);
    }

    protected void writeClientStateToNBT(NBTTagCompound nbt) {
        writePersistentStateToNBT(nbt);
    }

    public void writeContentsToNBT(NBTTagCompound nbt) {}

    // Save to disk, update client and re-render block
    public void markChanged() {
        markDirty();
        markForUpdate();
    }

    public void markBlockChanged() {
        markDirty();
        markBlockForUpdate();
    }

    @Override
    public void invalidate() {
        releaseChunkTicket();
        super.invalidate();
    }

    public void releaseChunkTicket() {
        if (chunkTicket != null) {
            ForgeChunkManager.releaseTicket(chunkTicket);
            chunkTicket = null;
        }
    }

    public static ItemStack blockStackWithTileEntity(Block block, int size, BaseTileEntity te) {
        return blockStackWithTileEntity(block, size, 0, te);
    }

    public static ItemStack blockStackWithTileEntity(Block block, int size, int meta, BaseTileEntity te) {
        ItemStack stack = new ItemStack(block, size, meta);
        if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToItemStackNBT(tag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    public ItemStack newItemStack(int size) {
        return blockStackWithTileEntity(getBlockType(), size, this);
    }

    @Override
    public boolean canUpdate() {
        return this instanceof ITickable;
    }

    @Override
    public void updateEntity() {
        ((ITickable) this).update();
    }

}
