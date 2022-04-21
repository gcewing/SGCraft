//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Chunk manager for tile entities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;

import net.minecraftforge.common.*;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class BaseTEChunkManager implements ForgeChunkManager.LoadingCallback {

    public boolean debug = false;
    BaseMod base;
    
    public BaseTEChunkManager(BaseMod mod) {
        base = mod;
        ForgeChunkManager.setForcedChunkLoadingCallback(mod, this);
        if (debug)
            System.out.printf("%s: BaseTEChunkManager: Chunk loading callback installed\n",
                base.modPackage);
    }
    
    protected Ticket newTicket(World world) {
        if (debug)
            System.out.printf("%s: BaseTEChunkManager.newTicket for %s\n", base.modPackage, world);
        return ForgeChunkManager.requestTicket(base, world, Type.NORMAL);
    }
    
    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        if (debug)
            System.out.printf("%s: BaseTEChunkManager.ticketsLoaded for %s\n", base.modPackage, world);
        for (Ticket ticket : tickets) {
            NBTTagCompound nbt = ticket.getModData();
            if (nbt != null)
                if (nbt.getString("type").equals("TileEntity")) {
                    int x = nbt.getInteger("xCoord");
                    int y = nbt.getInteger("yCoord");
                    int z = nbt.getInteger("zCoord");
                    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                    if (debug)
                        System.out.printf("%s: BaseTEChunkManager.ticketsLoaded: Ticket for %s at (%d, %d, %d)\n",
                            base.modPackage, te, x, y, z);
                    if (!(te instanceof BaseTileEntity && reinstateChunkTicket((BaseTileEntity)te, ticket))) {
                        if (debug)
                            System.out.printf("%s: BaseTEChunkManager.ticketsLoaded: : Unable to reinstate ticket\n", base.modPackage);
                        ForgeChunkManager.releaseTicket(ticket);
                    }
                }
        }
    }
    
    public void setForcedChunkRange(BaseTileEntity te, int minX, int minZ, int maxX, int maxZ) {
        te.releaseChunkTicket();
        Ticket ticket = getChunkTicket(te);
        if (ticket != null) {
            BlockPos pos = te.getPos();
            NBTTagCompound nbt = ticket.getModData();
            nbt.setString("type", "TileEntity");
            nbt.setInteger("xCoord", pos.getX());
            nbt.setInteger("yCoord", pos.getY());
            nbt.setInteger("zCoord", pos.getZ());
            nbt.setInteger("rangeMinX", minX);
            nbt.setInteger("rangeMinZ", minZ);
            nbt.setInteger("rangeMaxX", maxX);
            nbt.setInteger("rangeMaxZ", maxZ);
            forceChunkRangeOnTicket(te, ticket);
        }
    }
    
    public void clearForcedChunkRange(BaseTileEntity te) {
        te.releaseChunkTicket();
    }

    protected void forceChunkRangeOnTicket(BaseTileEntity te, Ticket ticket) {
        NBTTagCompound nbt = ticket.getModData();
        int minX = nbt.getInteger("rangeMinX");
        int minZ = nbt.getInteger("rangeMinZ");
        int maxX = nbt.getInteger("rangeMaxX");
        int maxZ = nbt.getInteger("rangeMaxZ");
        if (debug)
            System.out.printf("BaseChunkLoadingTE: Forcing range (%s,%s)-(%s,%s) in dimension %s\n",
                minX, minZ, maxX, maxZ, te.getWorld().provider.getDimension());
        BlockPos pos = te.getPos();
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (int i = minX; i <= maxX; i++)
            for (int j = minZ; j <= maxZ; j++) {
                int x = chunkX + i, z = chunkZ + j;
                ForgeChunkManager.forceChunk(ticket, new ChunkPos(x, z));
            }
    }

    protected Ticket getChunkTicket(BaseTileEntity te) {
        if (te.chunkTicket == null)
            te.chunkTicket = newTicket(te.getWorld());
        return te.chunkTicket;
    }
    
    public boolean reinstateChunkTicket(BaseTileEntity te, Ticket ticket) {
        if (te.chunkTicket == null) {
            if (debug)
                System.out.printf("BaseChunkLoadingTE: Reinstating chunk ticket %s\n", ticket);
            te.chunkTicket = ticket;
            forceChunkRangeOnTicket(te, ticket);
            return true;
        }
        else
            return false;
    }
    
    public void dumpChunkLoadingState(BaseTileEntity te, String label) {
        System.out.printf("%s: Chunk loading state:\n", label);
        System.out.printf("Chunk ticket = %s\n", te.chunkTicket);
        if (te.chunkTicket != null) {
            System.out.printf("Loaded chunks:");
            for (Object item : te.chunkTicket.getChunkList()) {
                ChunkPos coords = (ChunkPos)item;
                System.out.printf(" (%d,%d)", coords.x, coords.z);
            }
            System.out.printf("\n");
        }
    }

}
