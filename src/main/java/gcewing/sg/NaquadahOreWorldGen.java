// ------------------------------------------------------------------------------------------------
//
// SG Craft - Naquadah ore world generation
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.common.IWorldGenerator;

public class NaquadahOreWorldGen implements IWorldGenerator {

    static int genUnderLavaOdds = 4;
    static int maxNodesUnderLava = 8;
    static int genIsolatedOdds = 8;
    static int maxIsolatedNodes = 4;

    static boolean debugLava = false;
    static boolean debugRandom = false;
    static int debugLevel = 0;

    Random random;
    World world;
    Chunk chunk;
    int x0, z0;

    Block stone = Blocks.stone;
    Block lava = Blocks.lava;
    Block naquadah = SGCraft.naquadahOre;

    public static void configure(BaseConfiguration cfg) {
        genUnderLavaOdds = cfg.getInteger("naquadah", "genUnderLavaOdds", genUnderLavaOdds);
        maxNodesUnderLava = cfg.getInteger("naquadah", "maxNodesUnderLava", maxNodesUnderLava);
        genIsolatedOdds = cfg.getInteger("naquadah", "genIsolatedOdds", genIsolatedOdds);
        maxIsolatedNodes = cfg.getInteger("naquadah", "maxIsolatedNodes", maxIsolatedNodes);
        debugLava = cfg.getBoolean("naquadah", "debugLava", debugLava);
        debugRandom = cfg.getBoolean("naquadah", "debugRandom", debugRandom);
        debugLevel = cfg.getInteger("naquadah", "debugLevel", debugLevel);
    }

    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
            IChunkProvider chunkProvider) {
        this.random = random;
        this.world = world;
        x0 = chunkX * 16;
        z0 = chunkZ * 16;
        chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        generateChunk();
    }

    public void regenerate(Chunk chunk) {
        this.chunk = chunk;
        world = chunk.worldObj;
        int chunkX = chunk.xPosition;
        int chunkZ = chunk.zPosition;
        long worldSeed = world.getSeed();
        random = new Random(worldSeed);
        long xSeed = random.nextLong() >> 2 + 1L;
        long zSeed = random.nextLong() >> 2 + 1L;
        random.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ worldSeed);
        x0 = chunkX * 16;
        z0 = chunkZ * 16;
        generateChunk();
    }

    Block getBlock(int x, int y, int z) {
        return chunk.getBlock(x, y, z);
    }

    void setBlock(int x, int y, int z, Block id) {
        chunk.func_150807_a(x, y, z, id, 0);
    }

    void generateNode(Block id, int x, int y, int z, int sx, int sy, int sz) {
        int dx = random.nextInt(sx);
        int dy = random.nextInt(sy);
        int dz = random.nextInt(sz);
        int h = world.getHeight();
        if (debugRandom && debugLevel >= 1) SGCraft.log.debug(
                String.format(
                        "NaquadahOreWorldGen: %d x %d x %d node at (%d, %d, %d)",
                        dx + 1,
                        dy + 1,
                        dz + 1,
                        x0 + x,
                        y,
                        z0 + z));
        for (int i = x; i <= x + dx; i++) for (int j = y; j <= y + dy; j++)
            for (int k = z; k <= z + dz; k++) if (i < 16 && j < h && k < 16) if (getBlock(i, j, k) == stone) {
                if (debugRandom && debugLevel >= 2)
                    SGCraft.log.debug(String.format("NaquadahOreWorldGen: block at (%d, %d, %d)", x0 + i, j, z0 + k));
                setBlock(i, j, k, id);
            }
    }

    boolean odds(int n) {
        return random.nextInt(n) == 0;
    }

    void generateChunk() {
        SGChunkData.forChunk(chunk).oresGenerated = true;
        if (odds(genUnderLavaOdds)) {
            int n = random.nextInt(maxNodesUnderLava) + 1;
            for (int i = 0; i < n; i++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                for (int y = 0; y < 64; y++) if (getBlock(x, y, z) == stone && getBlock(x, y + 1, z) == lava) {
                    if (debugLava) SGCraft.log.debug(
                            String.format(
                                    "NaquadahOreWorldGen: generating under lava at (%d, %d, %d)",
                                    x0 + x,
                                    y,
                                    z0 + z));
                    generateNode(naquadah, x, y, z, 3, 1, 3);
                }
            }
        }
        if (!odds(genIsolatedOdds)) {
            return;
        }

        int n = random.nextInt(maxIsolatedNodes) + 1;
        for (int i = 0; i < n; i++) {
            int x = random.nextInt(16);
            int y = random.nextInt(64);
            int z = random.nextInt(16);
            if (getBlock(x, y, z) == stone) {
                if (debugRandom) SGCraft.log.debug(
                        String.format("NaquadahOreWorldGen: generating randomly at (%d, %d, %d)", x0 + x, y, z0 + z));
                generateNode(naquadah, x, y, z, 2, 2, 2);
            }
        }
    }
}
