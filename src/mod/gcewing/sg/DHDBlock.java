//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Controller Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
// import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import gcewing.sg.BaseMod.ModelSpec;

public class DHDBlock extends BaseBlock<DHDTE> {

    protected static String[] textures = {
        "dhd_top",
        "dhd_side",
        "stargateBlock",
        "dhd_button_dim",
    };
    protected static ModelSpec model = new ModelSpec("dhd.json", new Vector3(0, -0.5, 0), textures);

    public DHDBlock() {
        super(Material.rock /*SGRingBlock.ringMaterial*/, DHDTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.tabMisc);
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
    public int getRenderType() {
        return -1;
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("DHDBlock.onBlockAdded: at %s\n", pos);
        checkForLink(world, pos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, player, stack);
        checkForLink(world, pos);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        DHDTE cte = getTileEntity(world, pos);
        super.breakBlock(world, pos, state);
        if (cte == null) {
            System.out.printf("DHDBlock.breakBlock: No tile entity at %d\n", pos);
        }
        else if (cte.isLinkedToStargate) {
            SGBaseTE gte = cte.getLinkedStargateTE();
            if (gte != null)
                gte.clearLinkToController();
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumFacing side, float cx, float cy, float cz)
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
