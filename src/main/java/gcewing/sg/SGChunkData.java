//------------------------------------------------------------------------------------------------
//
//   SG Craft - Extra data saved with a chunk
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.util.HashMap;

public class SGChunkData {

    static boolean debug = false;

    static HashMap <ChunkCoordIntPair, SGChunkData> map = new HashMap<>();
    
    public boolean oresGenerated;
    
    public static SGChunkData forChunk(Chunk chunk) {
        return forChunk(chunk, null);
    }

    public static SGChunkData forChunk(Chunk chunk, NBTTagCompound nbt) {
        ChunkCoordIntPair coords = new ChunkCoordIntPair(chunk.xPosition, chunk.zPosition);
        SGChunkData data = map.get(coords);
        if (data == null) {
            data = new SGChunkData();
            if (nbt != null) {
                data.readFromNBT(nbt);
            }
            map.put(coords, data);
        }
        return data;
    }
    
    public void readFromNBT(NBTTagCompound nbt) {
        oresGenerated = nbt.getBoolean("gcewing.sg.oresGenerated");
    }
    
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("gcewing.sg.oresGenerated", oresGenerated);
    }
    
    public static void onChunkLoad(ChunkDataEvent.Load e) {
        Chunk chunk = e.getChunk();
        SGChunkData data = SGChunkData.forChunk(chunk, e.getData());
        if (!data.oresGenerated && SGCraft.addOresToExistingWorlds) {
            if (debug) SGCraft.log.debug(String.format("SGChunkData.onChunkLoad: Adding ores to chunk (%d, %d)", chunk.xPosition, chunk.zPosition));
            SGCraft.naquadahOreGenerator.regenerate(chunk);
        }
    }
    
    public static void onChunkSave(ChunkDataEvent.Save e) {
        Chunk chunk = e.getChunk();
        SGChunkData data = SGChunkData.forChunk(chunk);
        data.writeToNBT(e.getData());
    }

}
