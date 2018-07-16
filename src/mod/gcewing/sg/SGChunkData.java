//------------------------------------------------------------------------------------------------
//
//   SG Craft - Extra data saved with a chunk
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.util.HashMap;

public class SGChunkData {

    static boolean debug = false;

    static HashMap <ChunkPos, SGChunkData> map = new HashMap <ChunkPos, SGChunkData> ();
    
    public boolean oresGenerated;
    
    public static SGChunkData forChunk(Chunk chunk) {
        return forChunk(chunk, null);
    }

    public static SGChunkData forChunk(Chunk chunk, NBTTagCompound nbt) {
        //System.out.printf("SGChunkData.forChunk: (%d, %d): %s\n",
        //    chunk.xPosition, chunk.zPosition, chunk);
        ChunkPos coords = new ChunkPos(chunk.x, chunk.z);
        SGChunkData data = map.get(coords);
        if (data == null) {
            //System.out.printf("SGChunkData.forChunk: Creating new chunk data\n");
            data = new SGChunkData();
            if (nbt != null) {
                //System.out.printf("SGChunkData.forChunk: Reading from nbt\n");
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
        //System.out.printf("SGChunkData.onChunkLoad: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
        SGChunkData data = SGChunkData.forChunk(chunk, e.getData());
        //if (data.oresGenerated)
        //  System.out.printf("SGChunkData.onChunkLoad: Ores already added to chunk (%d, %d)\n",
        //      chunk.xPosition, chunk.zPosition);
        if (!data.oresGenerated && SGCraft.addOresToExistingWorlds) {
            if (debug)
                System.out.printf("SGChunkData.onChunkLoad: Adding ores to chunk (%d, %d)\n", chunk.x, chunk.z);
            SGCraft.naquadahOreGenerator.regenerate(chunk);
        }
    }
    
    public static void onChunkSave(ChunkDataEvent.Save e) {
        Chunk chunk = e.getChunk();
        //System.out.printf("SGChunkData.onChunkLoad: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
        SGChunkData data = SGChunkData.forChunk(chunk);
        data.writeToNBT(e.getData());
    }

}
