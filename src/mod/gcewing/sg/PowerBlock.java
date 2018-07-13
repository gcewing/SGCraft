//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class PowerBlock<TE extends PowerTE> extends BaseBlock<TE> {

    PowerTE lastRemovedTE;

    public PowerBlock(Class teClass) {
        super(SGCraft.machineMaterial, teClass);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 0);
    }
    
    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

//     @Override
//     public String getRendererClassName() {
//         return "BaseBlockRenderer";
//     }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        lastRemovedTE = getTileEntity(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumHand hand, EnumFacing side, float cx, float cy, float cz)
    {
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
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
        ItemStack stack)
    {
        PowerTE te = getTileEntity(world, pos);
        NBTTagCompound nbt = stack.getTagCompound();
        if (te != null && nbt != null)
            te.readContentsFromNBT(nbt);
    }

}
