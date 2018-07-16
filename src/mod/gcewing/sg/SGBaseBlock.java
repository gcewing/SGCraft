//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

import gcewing.sg.BaseMod.*;

public class SGBaseBlock extends SGBlock<SGBaseTE>  {

    static boolean debugMerge = false;
    static int explosionRadius = 10;
    static boolean fieryExplosion = true;
    static boolean smokyExplosion = true;

    static int pattern[][] = {
        {2, 1, 2, 1, 2},
        {1, 0, 0, 0, 1},
        {2, 0, 0, 0, 2},
        {1, 0, 0, 0, 1},
        {2, 1, 0, 1, 2},
    };

    protected static String[] textures = {"stargateblock", "stargatering", "stargatebase_front"};
    protected static ModelSpec model = new ModelSpec("block/sg_base_block.smeg", textures);
    
    public static void configure(BaseConfiguration config) {
        explosionRadius = config.getInteger("stargate", "explosionRadius", explosionRadius);
        fieryExplosion = config.getBoolean("stargate", "explosionFlame", fieryExplosion);
        smokyExplosion = config.getBoolean("stargate", "explosionSmoke", smokyExplosion);
    }
    
    public SGBaseBlock() {
        super(Material.ROCK, SGBaseTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.MISC);
    }
    
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // So that translucent camouflage blocks render correctly
    }

    @Override
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient4WaysByState;
    }
    
    @Override
    public String[] getTextureNames() {
        return textures;
    }
    
    @Override
    public ModelSpec getModelSpec(IBlockState state) {
        return model;
    }
    
    @Override
    public SGBaseTE getBaseTE(IBlockAccess world, BlockPos pos) {
        return getTileEntity(world, pos);
    }

    @Override
    protected String getRendererClassName() {
        return "SGRingBlockRenderer";
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isMerged(IBlockAccess world, BlockPos pos) {
        SGBaseTE te = getTileEntity(world, pos);
        return te != null && te.isMerged;
    }
    
    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGBaseBlock.onBlockAdded: at %d\n", pos);
        checkForMerge(world, pos);
    }

//     @Override
//     public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
//         int data = Math.round((180 - player.rotationYaw) / 90) & 3;
//         world.setBlockState(pos, state);
//         if (!world.isRemote) {
//             if (debugMerge)
//                 System.out.printf("SGBaseBlock.onBlockPlacedBy: yaw = %.1f state = %s\n", player.rotationYaw, state);
//             checkForMerge(world, pos);
//         }
//     }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumHand hand, EnumFacing side, float cx, float cy, float cz)
    {
        String Side = world.isRemote ? "Client" : "Server";
        SGBaseTE te = getTileEntity(world, pos);
        //System.out.printf("SGBaseBlock.onBlockActivated: %s: Tile entity = %s\n", Side, te);
        if (te != null) {
            if (debugMerge)
                System.out.printf("SGBaseBlock.onBlockActivated: %s: isMerged = %s\n", Side, te.isMerged);
            //if (!world.isRemote)
            //  te.dumpChunkLoadingState("SGBaseBlock.onBlockActivated");
            if (te.isMerged) {
                SGCraft.mod.openGui(player, SGGui.SGBase, world, pos);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
        return true;
    }
    
    @Override    
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos from) {
        //System.out.printf("SGBaseBlock.neighborChanged: %s\n", pos);
        neighbourChanged(world, pos);
    }

    protected void neighbourChanged(IBlockAccess world, BlockPos pos) {
        SGBaseTE te = getTileEntity(world, pos);
        if (te != null)
            te.onNeighborBlockChange();
    }
    
    void checkForMerge(World world, BlockPos pos) {
        if (debugMerge)
            System.out.printf("SGBaseBlock.checkForMerge at %s\n", pos);
        if (!isMerged(world, pos)) {
            Trans3 t = localToGlobalTransformation(world, pos);
            for (int i = -2; i <= 2; i++)
                for (int j = 0; j <= 4; j++) 
                    if (!(i == 0 && j == 0)) {
                        //BlockPos rp = pos.add(i * dx, j, i * dz);
                        BlockPos rp = t.p(i, j, 0).blockPos();
                        int type = getRingBlockType(world, rp);
                        int pat = pattern[4 - j][2 + i];
                        if (pat != 0 && type != pat) {
                            if (debugMerge)
                                System.out.printf("SGBaseBlock: world %d != pattern %d at %s\n",
                                    type, pattern[j][2 + i], rp);
                            return;
                        }
                    }
            if (debugMerge)
                System.out.printf("SGBaseBlock: Merging\n");
            SGBaseTE te = getTileEntity(world, pos);
            te.setMerged(true);
            BaseBlockUtils.markBlockForUpdate(world, pos);
            for (int i = -2; i <= 2; i++)
                for (int j = 0; j <= 4; j++) 
                    if (!(i == 0 && j == 0)) {
                        //BlockPos rp = pos.add(i * dx, j, i * dz);
                        BlockPos rp = t.p(i, j, 0).blockPos();
                        Block block = world.getBlockState(rp).getBlock();
                        if (block instanceof SGRingBlock)
                            ((SGRingBlock)block).mergeWith(world, rp, pos);
                    }
            te.checkForLink();
        }
    }
    
    int getRingBlockType(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.AIR)
            return 0;
        if (block == SGCraft.sgRingBlock) {
            if (!SGCraft.sgRingBlock.isMerged(world, pos)) {
                switch (state.getValue(SGRingBlock.VARIANT)) {
                    case 0: return 1;
                    case 1: return 2;
                }
            }
        }
        return -1;
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        unmerge(world, pos);
        dropUpgrades(world, pos);
        super.breakBlock(world, pos, state);
    }
    
    void dropUpgrades(World world, BlockPos pos) {
        SGBaseTE te = getTileEntity(world, pos);
        if (te != null) {
            if (te.hasChevronUpgrade)
                spawnAsEntity(world, pos, new ItemStack(SGCraft.sgChevronUpgrade));
            if (te.hasIrisUpgrade)
                spawnAsEntity(world, pos, new ItemStack(SGCraft.sgIrisUpgrade));
        }
    }
    
    public void unmerge(World world, BlockPos pos) {
        SGBaseTE te = getTileEntity(world, pos);
        boolean goBang = false;
        if (te != null /*&& te.isMerged*/) {
            if (te.isMerged && te.state == SGState.Connected) {
                te.state = SGState.Idle;
                goBang = true;
            }
            te.disconnect();
            te.unlinkFromController();
            te.setMerged(false);
            BaseBlockUtils.markBlockForUpdate(world, pos);
            unmergeRing(world, pos);
        }
        if (goBang && explosionRadius > 0)
            explode(world, new Vector3(pos).add(0.5, 2.5, 0.5), explosionRadius);
    }
    
    void explode(World world, Vector3 p, double s) {
        world.newExplosion(null, p.x, p.y, p.z, (float)s, fieryExplosion, smokyExplosion);
    }
    
    void unmergeRing(World world, BlockPos pos) {
        for (int i = -2; i <= 2; i++)
            for (int j = 0; j <= 4; j++)
                for (int k = -2; k <= 2; k++)
                    unmergeRingBlock(world, pos, pos.add(i, j, k));
    }
    
    void unmergeRingBlock(World world, BlockPos pos, BlockPos ringPos) {
        //System.out.printf("SGBaseBlock.unmergeRingBlock at (%d,%d,%d)\n", xr, yr, zr);
        Block block = world.getBlockState(ringPos).getBlock();
        if (debugMerge)
            System.out.printf("SGBaseBlock.unmergeRingBlock: found %s at %s\n", block, ringPos);
        if (block instanceof SGRingBlock) {
            //System.out.printf("SGBaseBlock: unmerging ring block\n");
            ((SGRingBlock)block).unmergeFrom(world, ringPos, pos);
        }
    }
    
    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }
    
    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return getWeakPower(state, world, pos, side);
    }
    
    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        SGBaseTE te = getTileEntity(world, pos);
        return (te != null && te.state != SGState.Idle) ? 15 : 0;
    }
    
    protected static Trans3 itemTrans = Trans3.sideTurn(0, 2);
    
    @Override
    public Trans3 itemTransformation() {
        return itemTrans;
    }
    
}
