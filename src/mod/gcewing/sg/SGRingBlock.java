//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.BaseMod.ModelSpec;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

import static gcewing.sg.BaseBlockUtils.*;


public class SGRingBlock extends SGBlock<SGRingTE> {

    static final int numSubBlocks = 2;

    public static final IProperty<Integer> VARIANT = PropertyInteger.create("variant", 0, 1);

    static final String[] textures = {"stargateBlock", "stargateRing", "stargateChevron"};
    static final ModelSpec[] models = {
            new ModelSpec("block/sg_ring_block.smeg", "stargateBlock", "stargateRing"),
            new ModelSpec("block/sg_ring_block.smeg", "stargateBlock", "stargateChevron")
    };

    public SGRingBlock() {
        super(Material.rock, SGRingTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.tabMisc);
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
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return true; // So that translucent camouflage blocks render correctly
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float cx, float cy, float cz) {
        SGRingTE te = getTileEntity(world, pos);
        if (te.isMerged) {
            IBlockState baseState = getWorldBlockState(world, te.basePos);
            Block block = baseState.getBlock();
            if (block instanceof SGBaseBlock)
                ((SGBaseBlock) block).onBlockActivated(world, te.basePos, baseState, player, side, cx, cy, cz);
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
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < numSubBlocks; i++)
            list.add(new ItemStack(item, 1, i));
    }

    public boolean isMerged(IBlockAccess world, BlockPos pos) {
        SGRingTE te = getTileEntity(world, pos);
        return te != null && te.isMerged;
    }

    public void mergeWith(World world, BlockPos pos, BlockPos basePos) {
        SGRingTE te = getTileEntity(world, pos);
        te.isMerged = true;
        te.basePos = basePos;
        markWorldBlockForUpdate(world, pos);
    }

    public void unmergeFrom(World world, BlockPos pos, BlockPos basePos) {
        SGRingTE te = getTileEntity(world, pos);
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGRingBlock.unmergeFrom: ring at %s base at %s te.isMerged = %s te.basePos = %s\n",
                    pos, basePos, te.isMerged, te.basePos);
        if (te.isMerged && te.basePos.equals(basePos)) {
            if (SGBaseBlock.debugMerge)
                System.out.print("SGRingBlock.unmergeFrom: unmerging\n");
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
                    Block block = getWorldBlock(world, bp);
                    if (block instanceof SGBaseBlock) {
                        if (SGBaseBlock.debugMerge)
                            System.out.printf("SGRingBlock.updateBaseBlocks: found base at %s\n", bp);
                        SGBaseBlock base = (SGBaseBlock) block;
                        if (!te.isMerged)
                            base.checkForMerge(world, bp);
                        else if (te.basePos.equals(bp))
                            base.unmerge(world, bp);
                    }
                }
    }
}
