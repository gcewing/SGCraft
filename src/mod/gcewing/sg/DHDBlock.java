//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Controller Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.BaseMod.ModelSpec;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DHDBlock extends BaseBlock<DHDTE> {

    protected static String[] textures = {
        "dhd_top",
        "dhd_side",
        "stargateblock",
        "dhd_button_dim",
    };
    protected static ModelSpec model = new ModelSpec("block/dhd.smeg", new Vector3(0, -0.5, 0), textures);

    public DHDBlock() {
        super(Material.ROCK, DHDTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.MISC);
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
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient4WaysByState;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("DHDBlock.onBlockAdded: at %d\n", pos);
        checkForLink(world, pos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, player, stack);
        checkForLink(world, pos);
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return SGCraft.canHarvestDHD;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        DHDTE cte = getTileEntity(world, pos);
        super.breakBlock(world, pos, state);
        if (cte == null) {
            System.out.printf("DHDBlock.breakBlock: No tile entity at %s\n", pos);
        }
        else if (cte.isLinkedToStargate) {
            SGBaseTE gte = cte.getLinkedStargateTE();
            if (gte != null)
                gte.clearLinkToController();
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumHand hand, EnumFacing side, float cx, float cy, float cz)
    {
        SGGui id = cy > 0.5 ? SGGui.SGController : SGGui.DHDFuel;
        SGCraft.mod.openGui(player, id, world, pos);
        return true;
    }
    
    public void checkForLink(World world, BlockPos pos) {
        //System.out.printf("DHDBlock.checkForLink at %s\n", pos);
        DHDTE te = getTileEntity(world, pos);
        if (te != null)
            te.checkForLink();
        else
            System.out.printf("DHDBlock.breakBlock: No tile entity at %d\n", pos);
    }
    
}
