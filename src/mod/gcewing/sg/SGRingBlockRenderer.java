//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block renderer
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.client.renderer.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import net.minecraft.tileentity.*;
import net.minecraftforge.common.util.*;

public class SGRingBlockRenderer extends BaseBlockRenderer {

    static CamouflageBlockAccess camoBlockAccess = new CamouflageBlockAccess();
    static RenderBlocks camoRenderBlocks = new RenderBlocks(camoBlockAccess);

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
        int modelId, RenderBlocks rb)
    {
        return renderRingBlock(world, x, y, z, block, modelId, rb, this);
    }
    
    public static boolean renderRingBlock(IBlockAccess world, int x, int y, int z, Block block,
        int modelId, RenderBlocks rb, BaseBlockRenderer renderer)
    {
        ISGBlock ringBlock = (ISGBlock)block;
        if (rb.overrideBlockTexture != null || !ringBlock.isMerged(world, x, y, z))
            return renderer.renderStandardWorldBlock(world, x, y, z, block, modelId, rb);
        else {
            SGBaseTE te = ringBlock.getBaseTE(world, x, y, z);
            if (te != null) {
                ItemStack stack = te.getCamouflageStack(x, y, z);
                if (stack != null) {
                    Item item = stack.getItem();
                    if (item instanceof ItemBlock) {
                        Block camoBlock = Block.getBlockFromItem(item);
                        int camoMeta = stack.getItemDamage() & 0xf;
                        if (!camoBlock.hasTileEntity(camoMeta)) {
                            camoBlockAccess.setup(rb.blockAccess, x, y, z, camoMeta);
                            camoRenderBlocks.renderBlockAllFaces(camoBlock, x, y, z);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

}

class CamouflageBlockAccess implements IBlockAccess {

    IBlockAccess base;
    int targetX, targetY, targetZ;
    int metadata;
    
    void setup(IBlockAccess base, int x, int y, int z, int data) {
        this.base = base;
        targetX = x;
        targetY = y;
        targetZ = z;
        metadata = data;
    }
    
    public Block getBlock(int x, int y, int z) {
        return base.getBlock(x, y, z);
    }

    public TileEntity getTileEntity(int x, int y, int z) {
        return base.getTileEntity(x, y, z);
    }

    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int w) {
        return base.getLightBrightnessForSkyBlocks(x, y, z, w);
    }

    public int getBlockMetadata(int x, int y, int z) {
        if (x == targetX && y == targetY && z == targetZ)
            return metadata;
        else
            return base.getBlockMetadata(x, y, z);
    }

    public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
        return base.isBlockProvidingPowerTo(x, y, z, side);
    }

    public boolean isAirBlock(int x, int y, int z) {
        return base.isAirBlock(x, y, z);
    }

    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return base.getBiomeGenForCoords(x, z);
    }

    public int getHeight() {
        return base.getHeight();
    }

    public boolean extendedLevelsInChunkCache() {
        return base.extendedLevelsInChunkCache();
    }

    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return base.isSideSolid(x, y, z, side, _default);
    }

}