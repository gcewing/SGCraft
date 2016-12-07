//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Generic Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.lang.reflect.*;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.nbt.*;
import net.minecraft.network.play.server.*;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import net.minecraftforge.common.*;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import gcewing.sg.BaseMod.IBlock;
import static gcewing.sg.BaseReflectionUtils.*;

public class BaseTileEntity extends TileEntity
    implements BaseMod.ITileEntity
{

    public byte side, turn;
    public Ticket chunkTicket;
    protected boolean updateChunk;

    public int getX() {return pos.getX();}
    public int getY() {return pos.getY();}
    public int getZ() {return pos.getZ();}

    public void setSide(int side) {
        this.side = (byte)side;
    }
    
    public void setTurn(int turn) {
        this.turn = (byte)turn;
    }
    
    public Trans3 localToGlobalRotation() {
        return localToGlobalTransformation(Vector3.zero);
    }

    public Trans3 localToGlobalTransformation() {
        return localToGlobalTransformation(Vector3.blockCenter(pos));
    }

//  public Trans3 localToGlobalTransformation(double x, double y, double z) {
//      return localToGlobalTransformation(new Vector3(x + 0.5, y + 0.5, z + 0.5);
//  }
    
    public Trans3 localToGlobalTransformation(Vector3 origin) {
        IBlockState state = worldObj.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IBlock)
            return ((IBlock)block).localToGlobalTransformation(worldObj, pos, state, origin);
        else {
            System.out.printf("BaseTileEntity.localToGlobalTransformation: Wrong block type at %s\n", pos);
            return new Trans3(origin);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        //System.out.printf("BaseTileEntity.getDescriptionPacket for %s, updateChunk = %s\n", this, updateChunk);
        if (syncWithClient()) {
            NBTTagCompound nbt = new NBTTagCompound();
            writeToNBT(nbt);
            if (updateChunk) {
                nbt.setBoolean("updateChunk", true);
                updateChunk = false;
            }
            return new S35PacketUpdateTileEntity(pos, 0, nbt);
        }
        else
            return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        //System.out.printf("BaseTileEntity.onDataPacket for %s\n", this);
        NBTTagCompound nbt = pkt.getNbtCompound();
        readFromNBT(nbt);
        if (nbt.getBoolean("updateChunk"))
            worldObj.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    boolean syncWithClient() {
        return true;
    }
    
    public void markBlockForUpdate() {
        updateChunk = true;
        worldObj.markBlockForUpdate(pos);
    }
    
    protected static Method getPlayerInstance = getMethodDef(PlayerManager.class,
        "getPlayerInstance", "func_72690_a", int.class, int.class, boolean.class);
    
    protected static Field flagsYAreasToUpdate = getFieldDef(
        classForName("net.minecraft.server.management.PlayerManager$PlayerInstance"),
        "flagsYAreasToUpdate", "field_73260_f");

    public void markForUpdate() {
        if (!worldObj.isRemote) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            PlayerManager pm = ((WorldServer)worldObj).getPlayerManager();
            Object entry = invokeMethod(pm, getPlayerInstance, x >> 4, z >> 4, false);
            if (entry != null) {
                int oldFlags = getIntField(entry, flagsYAreasToUpdate);
                worldObj.markBlockForUpdate(pos);
                setIntField(entry, flagsYAreasToUpdate, oldFlags);
            }
        }
    }

    public void playSoundEffect(String name, float volume, float pitch) {
        worldObj.playSoundEffect(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, name, volume, pitch);
    }
    
    @Override
    public void onAddedToWorld() {
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        side = nbt.getByte("side");
        turn = nbt.getByte("turn");
        readContentsFromNBT(nbt);
    }
    
    public void readFromItemStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
            readFromItemStackNBT(nbt);
    }
    
    public void readFromItemStackNBT(NBTTagCompound nbt) {
        readContentsFromNBT(nbt);
    }
    
    public void readContentsFromNBT(NBTTagCompound nbt) {
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (side != 0)
            nbt.setByte("side", side);
        if (turn != 0)
            nbt.setByte("turn", turn);
        writeContentsToNBT(nbt);
    }

    public void writeToItemStackNBT(NBTTagCompound nbt) {
        writeContentsToNBT(nbt);
    }
    
    public void writeContentsToNBT(NBTTagCompound nbt) {
    }
    
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

}
