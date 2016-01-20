//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Controller Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

public class DHDBlock extends Base4WayCtrBlock<DHDTE> {

	IIcon topTexture, bottomTexture, sideTexture;

    public DHDBlock() {
        super(Material.rock /*SGRingBlock.ringMaterial*/, DHDTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public int getRenderType() {
        return -1;
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

//	@Override
//	public void registerBlockIcons(IIconRegister reg) {
//		topTexture = getIcon(reg, "controller_top");
//		bottomTexture = getIcon(reg, "controller_bottom");
//		sideTexture = getIcon(reg, "controller_side");
//	}
//	
//	@Override
//	public IIcon getIcon(int side, int data) {
//		switch (side) {
//			case 0: return bottomTexture;
//			case 1: return topTexture;
//			default: return sideTexture;
//		}
//	}

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("DHDBlock.onBlockAdded: at (%d,%d,%d)\n", x, y, z);
        checkForLink(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, player, stack);
        checkForLink(world, x, y, z);
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block id, int data) {
        DHDTE cte = getTileEntity(world, x, y, z);
        super.breakBlock(world, x, y, z, id, data);
        if (cte == null) {
            System.out.printf("DHDBlock.breakBlock: No tile entity at (%d,%d,%d)\n",
                x, y, z);
        }
        else if (cte.isLinkedToStargate) {
            SGBaseTE gte = cte.getLinkedStargateTE();
            if (gte != null)
                gte.clearLinkToController();
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
        int side, float cx, float cy, float cz)
    {
        SGGui id = cy > 0.5 ? SGGui.SGController : SGGui.DHDFuel;
        SGCraft.mod.openGui(player, id, world, x, y, z);
        return true;
    }
    
    public void checkForLink(World world, int x, int y, int z) {
        //System.out.printf("DHDBlock.checkForLink at (%s, %s, %s)\n", x, y, z);
        DHDTE te = getTileEntity(world, x, y, z);
        if (te != null)
            te.checkForLink();
        else
            System.out.printf("DHDBlock.breakBlock: No tile entity at (%d,%d,%d)\n",
                x, y, z);
    }
    
}
