//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.nbt.*;
import net.minecraft.network.play.server.*;
import net.minecraft.tileentity.*;

import net.minecraftforge.common.*;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class BaseTileEntity extends TileEntity implements BaseMod.ITileEntity {

	public Ticket chunkTicket;
	
	@Override
	public Packet getDescriptionPacket() {
		//System.out.printf("BaseTileEntity.getDescriptionPacket for %s\n", this);
		if (syncWithClient()) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
		}
		else
			return null;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		//System.out.printf("BaseTileEntity.onDataPacket for %s\n", this);
		readFromNBT(pkt.func_148857_g());
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	boolean syncWithClient() {
		return true;
	}
	
	public void markBlockForUpdate() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public void playSoundEffect(String name, float volume, float pitch) {
		worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, name, volume, pitch);
	}
	
	@Override
	public void onAddedToWorld() {
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readContentsFromNBT(nbt);
	}

	public void readContentsFromNBT(NBTTagCompound nbt) {
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeContentsToNBT(nbt);
	}

	public void writeContentsToNBT(NBTTagCompound nbt) {
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
	
}
