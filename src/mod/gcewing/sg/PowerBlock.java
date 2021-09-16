//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PowerBlock<TE extends PowerTE> extends BaseBlock<TE> {

	PowerTE lastRemovedTE;

	public PowerBlock(Class teClass, IOrientationHandler orient) {
		super(SGCraft.machineMaterial, orient, teClass);
		setHardness(1.5F);
		setResistance(10.0F);
		setStepSound(soundTypeMetal);
		setHarvestLevel("pickaxe", 0);
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = getTileEntity(world, pos);
		if (te instanceof PowerTE) {
			lastRemovedTE = (PowerTE) te;    		
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float cx, float cy, float cz) {
		SGCraft.mod.openGui(player, SGGui.PowerUnit, world, pos);
		return true;
	}

	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		Item item = getItemDropped(state, ((World)world).rand, fortune);
		ItemStack stack = new ItemStack(item, 1);
		PowerTE te = lastRemovedTE;
		if (te != null && te.energyBuffer > 0) {
			NBTTagCompound nbt = new NBTTagCompound();
			te.writeContentsToNBT(nbt);
			stack.setTagCompound(nbt);
			lastRemovedTE = null;
		}
		ret.add(stack);
		return ret;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		TileEntity t = getTileEntity(world, pos);
		if (t instanceof PowerTE) {
			PowerTE te = (PowerTE) t;
			NBTTagCompound nbt = stack.getTagCompound();
			if (te != null && nbt != null) {
				te.readContentsFromNBT(nbt);
			}
		}

	}

}
