//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;

public class SGRingBlock extends BaseContainerBlock<SGRingTE>  implements ISGBlock {

    //static final int textureBase = 0x02;
    //static final int topAndBottomTexture = 0x00;
    static final int numSubBlocks = 2;
    static final int subBlockMask = 0x1;
    
    //public static Material ringMaterial = new Material(MapColor.stoneColor);
    
    IIcon topAndBottomTexture;
    IIcon sideTextures[] = new IIcon[numSubBlocks];
    
    static String[] subBlockTitles = {
        "Stargate Ring Block",
        "Stargate Chevron Block",
    };

    public SGRingBlock() {
        super(Material.rock, SGRingTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.tabMisc);
//		registerSubItemNames();
    }
    
    @Override
    protected String getRendererClassName() {
        return "SGRingBlockRenderer";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        topAndBottomTexture = getIcon(reg, "stargateBlock");
        sideTextures[0] = getIcon(reg, "stargateRing");
        sideTextures[1] = getIcon(reg, "stargateChevron");
    }
    
    @Override
    public boolean canRenderInPass(int pass) {
        return true; // So that translucent camouflage blocks render correctly
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }
    
    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return true;
    }
    
    @Override
    public int damageDropped(int data) {
        return data;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
        int side, float cx, float cy, float cz)
    {
        //System.out.printf("SGRingBlock.onBlockActivated at (%d, %d, %d)\n", x, y, z);
        SGRingTE te = getTileEntity(world, x, y, z);
        if (te.isMerged) {
            System.out.printf("SGRingBlock.onBlockActivated: base at (%d, %d, %d)\n",
                te.baseX, te.baseY, te.baseZ);
            Block block = world.getBlock(te.baseX, te.baseY, te.baseZ);
            if (block instanceof SGBaseBlock)
                block.onBlockActivated(world, te.baseX, te.baseY, te.baseZ, player,
                    side, cx, cy, cz);
            return true;
        }
        return false;
    }
    
    @Override
    public SGBaseTE getBaseTE(IBlockAccess world, int x, int y, int z) {
        SGRingTE rte = getTileEntity(world, x, y, z);
        if (rte != null)
            return rte.getBaseTE();
        else
            return null;
    }

//	@Override
//	public SGBaseTE getBaseTE(IBlockAccess world, int x, int y, int z) {
//		SGRingTE rte = getTileEntity(world, x, y, z);
//		if (rte.isMerged) {
//			TileEntity bte = world.getTileEntity(rte.baseX, rte.baseY, rte.baseZ);
//			if (bte instanceof SGBaseTE)
//				return (SGBaseTE)bte;
//		}
//		return null;
//	}

    @Override
    public IIcon getIcon(int side, int data) {
        if (side <= 1)
            return topAndBottomTexture;
        else
            return sideTextures[data & subBlockMask];
    }
    
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < numSubBlocks; i++)
            list.add(new ItemStack(item, 1, i));
    }
    
//	void registerSubItemNames() {
//		LanguageRegistry registry = LanguageRegistry.instance();
//		for (int i = 0; i < SGRingBlock.numSubBlocks; i++) {
//			String name = SGRingItem.subItemName(i);
//			String title = subBlockTitles[i];
//			//System.out.printf("SGRingBlock.registerSubItemNames: %s --> %s\n", name, title);
//			//registry.addStringLocalization(name + ".name", title);
//			SGCraft.mod.addNameTranslation(name, title);
//		}
//	}
    
    public boolean isMerged(IBlockAccess world, int x, int y, int z) {
        SGRingTE te = getTileEntity(world, x, y, z);
        return te.isMerged;
    }
    
    public void mergeWith(World world, int x, int y, int z, int xb, int yb, int zb) {
        SGRingTE te = getTileEntity(world, x, y, z);
        te.isMerged = true;
        te.baseX = xb;
        te.baseY = yb;
        te.baseZ = zb;
        //te.onInventoryChanged();
        world.markBlockForUpdate(x, y, z);
    }
    
    public void unmergeFrom(World world, int x, int y, int z, int xb, int yb, int zb) {
        SGRingTE te = getTileEntity(world, x, y, z);
        if (te.isMerged && te.baseX == xb && te.baseY == yb && te.baseZ == zb) {
            //System.out.printf("SGRingBlock.unmergeFrom: unmerging\n");
            te.isMerged = false;
            //te.onInventoryChanged();
            world.markBlockForUpdate(x, y, z);
        }
    }
    
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGRingBlock.onBlockAdded: at (%d,%d,%d)\n", x, y, z);
        SGRingTE te = getTileEntity(world, x, y, z);
        updateBaseBlocks(world, x, y, z, te);
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, Block id, int data) {
        SGRingTE te = getTileEntity(world, x, y, z);
        super.breakBlock(world, x, y, z, id, data);
        if (te != null && te.isMerged)
            updateBaseBlocks(world, x, y, z, te);
    }
    
    void updateBaseBlocks(World world, int x, int y, int z, SGRingTE te) {
        //System.out.printf("SGRingBlock.updateBaseBlocks: merged = %s, base = (%d,%d,%d)\n",
        //	te.isMerged, te.baseX, te.baseY, te.baseZ);
        for (int i = -2; i <= 2; i++)
            for (int j = -4; j <= 0; j++)
                for (int k = -2; k <= 2; k++) {
                    int xb = x + i;
                    int yb = y + j;
                    int zb = z + k;
                    Block block = world.getBlock(xb, yb, zb);
                    if (block instanceof SGBaseBlock) {
                        //System.out.printf("SGRingBlock.updateBaseBlocks: found base at (%d,%d,%d)\n",
                        //	xb, yb, zb);
                        SGBaseBlock base = (SGBaseBlock)block;
                        if (!te.isMerged)
                            base.checkForMerge(world, xb, yb, zb);
                        else if (te.baseX == xb && te.baseY == yb && te.baseZ == zb)
                            base.unmerge(world, xb, yb, zb);
                }
        }
    }
    
}
