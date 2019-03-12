//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import gcewing.sg.BaseMod.ModelSpec;

public class SGRingBlock extends SGBlock<SGRingTE> {

    static final int numSubBlocks = 2;

    public static IProperty<Integer> VARIANT = PropertyInteger.create("variant", 0, 1);

    static String[] textures = {"stargateblock", "stargatering", "stargatechevron"};
    static ModelSpec models[] = {
        new ModelSpec("block/sg_ring_block.smeg", "stargateblock", "stargatering"),
        new ModelSpec("block/sg_ring_block.smeg", "stargateblock", "stargatechevron")
    };
    
    static String[] subBlockTitles = {
        "Stargate Ring Block",
        "Stargate Chevron Block",
    };

    public SGRingBlock() {
        super(Material.ROCK, SGRingTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.MISC);
    }
    
    protected void defineProperties() {
        super.defineProperties();
        addProperty(VARIANT);
    }
    
    @Override
    public int getNumSubtypes() {
        return VARIANT.getAllowedValues().size();
    }

    @Override
    public String[] getTextureNames() {
        return textures;
    }
    
    @Override
    public ModelSpec getModelSpec(IBlockState state) {
        return models[state.getValue(VARIANT)];
    }

    @Override
    protected String getRendererClassName() {
        return "SGRingBlockRenderer";
    }
    
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // So that translucent camouflage blocks render correctly
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }
    
    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return SGCraft.canHarvestSGRingBlock;
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
         EnumHand hand, EnumFacing side, float cx, float cy, float cz)
    {
        //System.out.printf("SGRingBlock.onBlockActivated at (%d, %d, %d)\n", x, y, z);
        SGRingTE te = getTileEntity(world, pos);
        if (te.isMerged) {
            //System.out.printf("SGRingBlock.onBlockActivated: base at %s\n", te.basePos);
            IBlockState baseState = world.getBlockState(te.basePos);
            Block block = baseState.getBlock();
            if (block instanceof SGBaseBlock)
                block.onBlockActivated(world, te.basePos, baseState, player, hand, side,
                    cx, cy, cz);
            return true;
        }
        return false;
    }
    
    @Override
    public SGBaseTE getBaseTE(IBlockAccess world, BlockPos pos) {
        SGRingTE rte = getTileEntity(world, pos);
        if (rte != null)
            return rte.getBaseTE();
        else
            return null;
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (int i = 0; i < numSubBlocks; i++) {
            ItemStack item = new ItemStack(this,1,i);
            list.add(item);
        }
            // Update: may be incorrect, needs testing.
    }

    @Override
    public boolean isMerged(IBlockAccess world, BlockPos pos) {
        SGRingTE te = getTileEntity(world, pos);
        return te != null && te.isMerged;
    }
    
    public void mergeWith(World world, BlockPos pos, BlockPos basePos) {
        SGRingTE te = getTileEntity(world, pos);
        te.isMerged = true;
        te.basePos = basePos;
        //te.onInventoryChanged();
        BaseBlockUtils.markBlockForUpdate(world, pos);
    }
    
    public void unmergeFrom(World world, BlockPos pos, BlockPos basePos) {
        SGRingTE te = getTileEntity(world, pos);
        if (te.isMerged && te.basePos.equals(basePos)) {
            //System.out.printf("SGRingBlock.unmergeFrom: unmerging\n");
            te.isMerged = false;
            te.markBlockChanged();
        }
    }
    
    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGRingBlock.onBlockAdded: at %s\n", pos);
        SGRingTE te = getTileEntity(world, pos);
        updateBaseBlocks(world, pos, te);
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        SGRingTE te = getTileEntity(world, pos);
        super.breakBlock(world, pos, state);
        if (te != null && te.isMerged)
            updateBaseBlocks(world, pos, te);
    }
    
    void updateBaseBlocks(World world, BlockPos pos, SGRingTE te) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGRingBlock.updateBaseBlocks: merged = %s, base = %s\n",
                te.isMerged, te.basePos);
        for (int i = -2; i <= 2; i++)
            for (int j = -4; j <= 0; j++)
                for (int k = -2; k <= 2; k++) {
                    BlockPos bp = pos.add(i, j, k);
                    Block block = world.getBlockState(bp).getBlock();
                    if (block instanceof SGBaseBlock) {
                         if (SGBaseBlock.debugMerge)
                            System.out.printf("SGRingBlock.updateBaseBlocks: found base at %s\n", bp);
                        SGBaseBlock base = (SGBaseBlock)block;
                        if (!te.isMerged)
                            base.checkForMerge(world, bp);
                        else if (te.basePos.equals(bp))
                            base.unmerge(world, bp);
                }
        }
    }
    
}
