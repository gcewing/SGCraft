//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Block Utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
// import net.minecraft.block.state.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

import cpw.mods.fml.relauncher.*;

import static gcewing.sg.BaseUtils.*;

public class BaseBlockUtils {

    public static String getNameForBlock(Block block) {
        if (block != null)
            return Block.blockRegistry.getNameForObject(block).toString();
        else
            return "";
    }
    
    /*
     *   Test whether a block is receiving a redstone signal from a source
     *   other than itself. For blocks that can both send and receive in
     *   any direction.
     */
    public static boolean blockIsGettingExternallyPowered(World world, BlockPos pos) {
        for (EnumFacing side : facings) {
            if (isPoweringSide(world, pos.offset(side), side))
                    return true;
        }
        return false;
    }
    
    static boolean isPoweringSide(World world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlock(pos.x, pos.y, pos.z);
        if (block.isProvidingWeakPower(world, pos.x, pos.y, pos.z, side.ordinal()) > 0)
            return true;
        if (block.shouldCheckWeakPower(world, pos.x, pos.y, pos.z, side.ordinal())) {
            for (EnumFacing side2 : facings)
                if (side2 != oppositeFacing(side)) {
                    BlockPos pos2 = pos.offset(side2);
                    Block block2 = world.getBlock(pos2.x, pos2.y, pos2.z);
                    if (block2.isProvidingStrongPower(world, pos2.x, pos2.y, pos2.z, side2.ordinal()) > 0)
                        return true;
                }
        }
        return false;
    }
    
    public static IBlockState getBlockStateFromItemStack(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        int meta = 0;
        if (stack.getItem().getHasSubtypes())
            meta = stack.getItem().getMetadata(stack.getItemDamage());
        if (block instanceof BaseBlock)
            return ((BaseBlock)block).getStateFromMeta(meta);
        else
            return new MetaBlockState(block, meta);
    }
    
    public static IBlockState getBlockStateFromMeta(Block block, int meta) {
        if (block instanceof BaseBlock)
            return ((BaseBlock)block).getStateFromMeta(meta);
        else
            return new MetaBlockState(block, meta);
    }
    
    public static int getMetaFromBlockState(IBlockState state) {
        if (state instanceof MetaBlockState)
            return ((MetaBlockState)state).meta;
        else
            return ((BaseBlock)state.getBlock()).getMetaFromState(state);
    }
    
    public static Block getWorldBlock(IBlockAccess world, BlockPos pos) {
        return world.getBlock(pos.x, pos.y, pos.z);
    }
    
    public static IBlockState getWorldBlockState(IBlockAccess world, BlockPos pos) {
        Block block = world.getBlock(pos.x, pos.y, pos.z);
        int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);
        if (block instanceof BaseBlock)
            return ((BaseBlock)block).getStateFromMeta(meta);
        else
            return new MetaBlockState(block, meta);
    }
    
    public static boolean setWorldBlockState(World world, BlockPos pos, IBlockState state, int flags) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        return world.setBlock(pos.x, pos.y, pos.z, block, meta, flags);
    }
    
    public static void markWorldBlockForUpdate(World world, BlockPos pos) {
        world.markBlockForUpdate(pos.x, pos.y, pos.z);
    }   
    
    public static void notifyWorldNeighborsOfStateChange(World world, BlockPos pos, Block block) {
        world.notifyBlocksOfNeighborChange(pos.x, pos.y, pos.z, block);
    }   
    
    public static TileEntity getWorldTileEntity(IBlockAccess world, BlockPos pos) {
        return world.getTileEntity(pos.x, pos.y, pos.z);
    }
    
    public static World getTileEntityWorld(TileEntity te) {
        return te.getWorldObj();
    }
    
    public static BlockPos getTileEntityPos(TileEntity te) {
        return new BlockPos(te.xCoord, te.yCoord, te.zCoord);
    }
    
    public static boolean blockCanRenderInLayer(Block block, EnumWorldBlockLayer layer) {
        if (block instanceof BaseBlock)
            return ((BaseBlock)block).canRenderInLayer(layer);
        else
            switch (layer) {
                case SOLID:
                    return block.canRenderInPass(0);
                case TRANSLUCENT:
                    return block.canRenderInPass(1);
                default:
                    return false;
            }
    }
    
    public static IBlockState getDefaultBlockState(Block block) {
        if (block instanceof BaseBlock)
            return ((BaseBlock)block).getDefaultState();
        else
            return new MetaBlockState(block, 0);
    }

    public static void playWorldAuxSFX(World world, int fxId, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        int stateId = (meta << 12) | Block.getIdFromBlock(block);
        world.playAuxSFX(fxId, pos.getX(), pos.getY(), pos.getZ(), stateId);
    }
    
    public static float getBlockHardness(Block block, World world, BlockPos pos) {
        return block.getBlockHardness(world, pos.x, pos.y, pos.z);
    }
    
    public static String getBlockHarvestTool(IBlockState state) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        return block.getHarvestTool(meta);
    }
    
    public static int getBlockHarvestLevel(IBlockState state) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        return block.getHarvestLevel(meta);
    }

    public static float getPlayerBreakSpeed(EntityPlayer player, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        return player.getBreakSpeed(block, false, meta, pos.x, pos.y, pos.z);
    }
    
    @SideOnly(Side.CLIENT)
    public static IIcon getSpriteForBlockState(IBlockState state) {
        if (state != null) {
            Block block = state.getBlock();
            int meta = getMetaFromBlockState(state);
            return block.getIcon(2, meta);
        }
        else
            return null;
    }
    
    public static void spawnBlockStackAsEntity(World world, BlockPos pos, ItemStack stack) {
        spawnItemStackAsEntity(world, pos, stack);
    }

    public static void spawnItemStackAsEntity(World world, BlockPos pos, ItemStack stack) {
        float var6 = 0.7F;
        double var7 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * 0.5D;
        double var9 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * 0.5D;
        double var11 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * 0.5D;
        EntityItem var13 = new EntityItem(world, pos.x + var7, pos.y + var9, pos.z + var11, stack);
        var13.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(var13);
    }

    public static ItemStack blockStackWithState(IBlockState state, int size) {
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        return new ItemStack(block, size, meta);
    }
    
    public static ItemStack newBlockStack(IBlockState state) {
        Block block = state.getBlock();
        int meta = BaseBlockUtils.getMetaFromBlockState(state);
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            return new ItemStack(item, 1, meta);
        }
        return null;
    }
    
    public static NBTTagCompound nbtFromBlockPos(BlockPos pos) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        return nbt;
    }
    
    public static BlockPos blockPosFromNBT(NBTTagCompound nbt) {
        return new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
    }
    
    //------------------------------------------------------------------------------------------------

    protected static class MetaBlockState implements IBlockState {
    
        protected Block block;
        public int meta;
        
        public MetaBlockState(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }
    
        public Collection<IProperty> getPropertyNames() {return null;}
        public <T extends Comparable<T>> T getValue(IProperty<T> property) {return null;}
        public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {return null;}
        public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {return null;}
        public ImmutableMap<IProperty, Comparable> getProperties() {return null;}

        public Block getBlock() {
            return block;
        }

    }

}
