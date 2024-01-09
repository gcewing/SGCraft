// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base - Chunk manager for tile entities
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class BaseTEChunkManager implements ForgeChunkManager.LoadingCallback {

    public boolean debug = false;
    BaseMod base;

    public BaseTEChunkManager(BaseMod mod) {
        base = mod;
        ForgeChunkManager.setForcedChunkLoadingCallback(mod, this);
        if (debug) SGCraft.log
                .debug(String.format("%s: BaseTEChunkManager: Chunk loading callback installed", base.modPackage));
    }

    protected Ticket newTicket(World world) {
        if (debug) SGCraft.log.debug(String.format("%s: BaseTEChunkManager.newTicket for %s", base.modPackage, world));
        return ForgeChunkManager.requestTicket(base, world, Type.NORMAL);
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        if (debug)
            SGCraft.log.debug(String.format("%s: BaseTEChunkManager.ticketsLoaded for %s", base.modPackage, world));
        for (Ticket ticket : tickets) {
            NBTTagCompound nbt = ticket.getModData();
            if (nbt != null) if (nbt.getString("type").equals("TileEntity")) {
                int x = nbt.getInteger("xCoord");
                int y = nbt.getInteger("yCoord");
                int z = nbt.getInteger("zCoord");
                TileEntity te = world.getTileEntity(x, y, z);
                if (debug) SGCraft.log.debug(
                        String.format(
                                "%s: BaseTEChunkManager.ticketsLoaded: Ticket for %s at (%d, %d, %d)",
                                base.modPackage,
                                te,
                                x,
                                y,
                                z));
                if (!(te instanceof BaseTileEntity && reinstateChunkTicket((BaseTileEntity) te, ticket))) {
                    if (debug) SGCraft.log.debug(
                            String.format(
                                    "%s: BaseTEChunkManager.ticketsLoaded: : Unable to reinstate ticket",
                                    base.modPackage));
                    ForgeChunkManager.releaseTicket(ticket);
                }
            }
        }
    }

    public void setForcedChunkRange(BaseTileEntity te, int minX, int minZ, int maxX, int maxZ) {
        te.releaseChunkTicket();
        Ticket ticket = getChunkTicket(te);
        if (ticket != null) {
            NBTTagCompound nbt = ticket.getModData();
            nbt.setString("type", "TileEntity");
            nbt.setInteger("xCoord", te.xCoord);
            nbt.setInteger("yCoord", te.yCoord);
            nbt.setInteger("zCoord", te.zCoord);
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
        if (debug) SGCraft.log.debug(
                String.format(
                        "BaseChunkLoadingTE: Forcing range (%s,%s)-(%s,%s) in dimension %s",
                        minX,
                        minZ,
                        maxX,
                        maxZ,
                        te.getWorldObj().provider.dimensionId));
        int chunkX = te.xCoord >> 4;
        int chunkZ = te.zCoord >> 4;
        for (int i = minX; i <= maxX; i++) for (int j = minZ; j <= maxZ; j++) {
            int x = chunkX + i, z = chunkZ + j;
            ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(x, z));
        }
    }

    protected Ticket getChunkTicket(BaseTileEntity te) {
        if (te.chunkTicket == null) te.chunkTicket = newTicket(te.getWorldObj());
        return te.chunkTicket;
    }

    public boolean reinstateChunkTicket(BaseTileEntity te, Ticket ticket) {
        if (te.chunkTicket == null) {
            if (debug) SGCraft.log.debug(String.format("BaseChunkLoadingTE: Reinstating chunk ticket %s", ticket));
            te.chunkTicket = ticket;
            forceChunkRangeOnTicket(te, ticket);
            return true;
        } else return false;
    }

    public void dumpChunkLoadingState(BaseTileEntity te, String label) {
        StringBuilder chunks = new StringBuilder();
        SGCraft.log.debug(String.format("%s: Chunk loading state:", label));
        SGCraft.log.debug(String.format("Chunk ticket = %s", te.chunkTicket));
        if (te.chunkTicket != null) {
            SGCraft.log.debug("Loaded chunks:");
            for (Object item : te.chunkTicket.getChunkList()) {
                ChunkCoordIntPair coords = (ChunkCoordIntPair) item;
                chunks.append(String.format(" (%d,%d)", coords.chunkXPos, coords.chunkZPos));
            }
            SGCraft.log.debug(chunks.toString());
        }
    }

}
