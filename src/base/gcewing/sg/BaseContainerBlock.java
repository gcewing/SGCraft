//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Block with Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.item.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import cpw.mods.fml.common.registry.*;

//import static gcewing.sg.BaseIcons.*;

public class BaseContainerBlock<TE extends TileEntity>
	extends BlockContainer implements BaseMod.IBlock
{

	static Random random = new Random();

	public int renderID = 0;
	Class<? extends TileEntity> tileEntityClass = null;
	protected String[] iconNames = null;
	protected IIcon[] icons;

	public BaseContainerBlock(Material material) {
		this(material, null);
	}

	public BaseContainerBlock(Material material, Class<TE> teClass) {
		super(material);
		tileEntityClass = teClass;
		if (teClass != null) {
			try {
				GameRegistry.registerTileEntity(teClass, teClass.getName());
			}
			catch (IllegalArgumentException e) {
				// Ignore redundant registration
			}
		}
	}

	// -------------------------- Icon Registration -----------------------------
	
	public static IIcon getIcon(Block block, IIconRegister reg, String name) {
		if (name.indexOf(":") < 0) {
			String assetKey = block.getClass().getPackage().getName().replace(".", "_");
			name = assetKey + ":" + name;
		}
		return reg.registerIcon(name);
	}
	
	protected IIcon getIcon(IIconRegister reg, String name) {
		return getIcon(this, reg, name);
	}

	public static IIcon[] getIcons(Block block, IIconRegister reg, String... names) {
		IIcon[] result = new IIcon[names.length];
		for (int i = 0; i < names.length; i++)
			result[i] = getIcon(block, reg, names[i]);
		return result;
	}
	
	protected IIcon[] getIcons(IIconRegister reg, String... names) {
		return getIcons(this, reg, names);
	}
	
//	public static IIcon[] getIconArray(Block block, IIconRegister reg, int numCols, String name) {
//		IIcon[] result = new IIcon[numCols];
//		IIcon base = getIcon(block, reg, name);
//		for (int i = 0; i < numCols; i++)
//			result[i] = base; //new SubIcon(base, i * 16, 0);
//		return result;
//	}
//	
//	protected IIcon[] getIconArray(IIconRegister reg, int numCols, String name) {
//		return getIconArray(this, reg, numCols, name);
//	}

	@Override
	public void registerBlockIcons(IIconRegister reg) {
		if (iconNames != null)
			icons = getIcons(reg, iconNames);
		else {
			//icons = getIcons(reg, getUnlocalizedName().substring(5));
			//icons = getIcons(reg, this.getTextureName());
			super.registerBlockIcons(reg);
			icons = new IIcon[] {blockIcon};
		}
	}
	
	public void setIconNames(String... names) {
		iconNames = names;
	}
	
	public void setPrefixedIconNames(String prefix, String... suffixes) {
		String[] names = new String[suffixes.length];
		for (int i = 0; i < names.length; i++)
			names[i] = prefix + "-" + suffixes[i];
		setIconNames(names);
	}

	// -------------------------- Rendering -----------------------------
	
	@Override
	public int getRenderType() {
		return renderID;
	}

	@Override
	public void setRenderType(int id) {
		renderID = id;
	}
	
	@Override
	public String getQualifiedRendererClassName() {
		String name = getRendererClassName();
		if (name != null)
			name = getClass().getPackage().getName() + "." + name;
		return name;
	}
	
	protected String getRendererClassName() {
		return null;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return renderID == 0;
	}
	
	@Override
	public IIcon getIcon(int side, int data) {
		return getLocalIcon(side, data);
	}
	
	IIcon getLocalIcon(int side, int data) {
		if (icons != null) {
			if (side < icons.length)
				return icons[side];
			else
				return icons[icons.length - 1];
		}
		else
			return null;
	}
	
	// -------------------------- Tile Entity -----------------------------
	
	@Override
	public boolean hasTileEntity(int metadata) {
		return tileEntityClass != null;
	}
	
	public TE getTileEntity(IBlockAccess world, int x, int y, int z) {
		if (hasTileEntity())
			return (TE)world.getTileEntity(x, y, z);
		else
			return null;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		if (tileEntityClass != null) {
			try {
				return tileEntityClass.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else
			return null;
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		TileEntity te = getTileEntity(world, x, y, z);
		if (te instanceof BaseMod.ITileEntity)
			((BaseMod.ITileEntity)te).onAddedToWorld();
	}

//	public void setMetadata(World world, int x, int y, int z, int data, boolean notify) {
//		if (notify)
//			world.setBlockMetadataWithNotify(x, y, z, data, 0x1);
//		else
//			world.setBlockMetadataWithNotify(x, y, z, data, 0x0);
//	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof IInventory) {
			IInventory var7 = (IInventory)te;
			if (var7 != null) {
				for (int var8 = 0; var8 < var7.getSizeInventory(); ++var8) {
					ItemStack var9 = var7.getStackInSlot(var8);
					if (var9 != null)
						scatterNearby(world, x, y, z, var9);
				}
			}
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}
	
	public void scatterNearby(World world, int x, int y, int z, ItemStack stack) {
		float var10 = this.random.nextFloat() * 0.8F + 0.1F;
		float var11 = this.random.nextFloat() * 0.8F + 0.1F;
		float var12 = this.random.nextFloat() * 0.8F + 0.1F; 
		while (stack.stackSize > 0) {
			int var13 = this.random.nextInt(21) + 10;
			if (var13 > stack.stackSize)
				var13 = stack.stackSize;
			stack.stackSize -= var13;
			EntityItem var14 = new EntityItem(world, x + var10, y + var11, z + var12,
				new ItemStack(stack.getItem(), var13, stack.getItemDamage()));
			float var15 = 0.05F;
			var14.motionX = (double)((float)this.random.nextGaussian() * var15);
			var14.motionY = (double)((float)this.random.nextGaussian() * var15 + 0.2F);
			var14.motionZ = (double)((float)this.random.nextGaussian() * var15);
			if (stack.hasTagCompound())
				var14.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
			world.spawnEntityInWorld(var14);
		}
	}

}

