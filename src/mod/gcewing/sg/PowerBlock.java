//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

public class PowerBlock<TE extends PowerTE> extends BaseContainerBlock<TE> {

    PowerTE lastRemovedTE;

    public PowerBlock(Class teClass) {
        super(SGCraft.machineMaterial, teClass);
        setHardness(1.5F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
    }
    
    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public String getQualifiedRendererClassName() {
        return "gcewing.sg.BaseBlockRenderer";
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int data) {
        lastRemovedTE = getTileEntity(world, x, y, z);
        super.breakBlock(world, x, y, z, block, data);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
        int side, float cx, float cy, float cz)
    {
        SGCraft.mod.openGui(player, SGGui.PowerUnit, world, x, y, z);
        return true;
    }
    
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        Item item = getItemDropped(metadata, world.rand, fortune);
        ItemStack stack = new ItemStack(item, 1);
        PowerTE te = lastRemovedTE;
        if (te != null && te.energyBuffer > 0) {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeContentsToNBT(nbt);
            stack.stackTagCompound = nbt;
            lastRemovedTE = null;
        }
        ret.add(stack);
        return ret;
    }
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        PowerTE te = getTileEntity(world, x, y, z);
        NBTTagCompound nbt = stack.stackTagCompound;
        if (te != null && nbt != null)
            te.readContentsFromNBT(nbt);
    }

}
