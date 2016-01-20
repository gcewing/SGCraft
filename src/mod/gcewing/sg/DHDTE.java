//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Controller Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraft.util.*;

import static gcewing.sg.BaseUtils.*;
import static gcewing.sg.Utils.*;

public class DHDTE extends BaseTileInventory implements ISGEnergySource {

    // Configuration options
    public static int linkRangeX = 5; // either side
    public static int linkRangeY = 1; // up or down
    public static int linkRangeZ = 6; // in front

    // Inventory slots
    public static final int firstFuelSlot = 0;
    public static final int numFuelSlots = 4;
    public static final int numSlots = numFuelSlots;

    // Persisted fields
    public boolean isLinkedToStargate;
    public int linkedX, linkedY, linkedZ;
    public String enteredAddress = "";
    IInventory inventory = new InventoryBasic("DHD", false, numSlots);
    
    static AxisAlignedBB bounds;
    static double maxEnergyBuffer;

    double energyInBuffer;

    public static void configure(BaseConfiguration cfg) {
        linkRangeX = cfg.getInteger("dhd", "linkRangeX", linkRangeX);
        linkRangeY = cfg.getInteger("dhd", "linkRangeY", linkRangeY);
        linkRangeZ = cfg.getInteger("dhd", "linkRangeZ", linkRangeZ);
        maxEnergyBuffer = SGBaseTE.energyPerFuelItem;
    }
    
    public static DHDTE at(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof DHDTE)
            return (DHDTE)te;
        else
            return null;
    }
    
    public static DHDTE at(IBlockAccess world, NBTTagCompound nbt) {
        return DHDTE.at(world, nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
    }
    
    public void setEnteredAddress(String address) {
        enteredAddress = address;
        markDirty();
        markBlockForUpdate();
    }
    
    @Override
    public boolean canUpdate() {
        return false;
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return bounds.addCoord(xCoord + 0.5, yCoord, zCoord + 0.5);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 32768.0;
    }

    @Override
    protected IInventory getInventory() {
        return inventory;
    }
    
    public DHDBlock getBlock() {
        return (DHDBlock)getBlockType();
    }
    
    public Trans3 localToGlobalTransformation() {
        return getBlock().localToGlobalTransformation(xCoord, yCoord, zCoord, getBlockMetadata(), this);
    }
    
    public int getRotation() {
        return getBlock().rotationInWorld(getBlockMetadata(), this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        isLinkedToStargate = nbt.getBoolean("isLinkedToStargate");
        energyInBuffer = nbt.getDouble("energyInBuffer");
        linkedX = nbt.getInteger("linkedX");
        linkedY = nbt.getInteger("linkedY");
        linkedZ = nbt.getInteger("linkedZ");
        enteredAddress = nbt.getString("enteredAddress");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isLinkedToStargate", isLinkedToStargate);
        nbt.setDouble("energyInBuffer", energyInBuffer);
        nbt.setInteger("linkedX", linkedX);
        nbt.setInteger("linkedY", linkedY);
        nbt.setInteger("linkedZ", linkedZ);
        nbt.setString("enteredAddress", enteredAddress);
    }

    SGBaseTE getLinkedStargateTE() {
        if (isLinkedToStargate) {
            TileEntity gte = worldObj.getTileEntity(linkedX, linkedY, linkedZ);
            if (gte instanceof SGBaseTE)
                return (SGBaseTE)gte;
        }
        return null;
    }

    void checkForLink() {
        if (SGBaseBlock.debugMerge)
            System.out.printf("DHDTE.checkForLink at (%d,%d,%d): isLinkedToStargate = %s\n",
                xCoord, yCoord, zCoord, isLinkedToStargate);
        if (!isLinkedToStargate) {
            Trans3 t = localToGlobalTransformation();
            for (int i = -linkRangeX; i <= linkRangeX; i++)
                for (int j = -linkRangeY; j <= linkRangeY; j++)
                    for (int k = 1; k <= linkRangeZ; k++) {
                        Vector3 p = t.p(i, j, -k);
                        //System.out.printf("DHDTE: Looking for stargate at (%d,%d,%d)\n",
                        //	p.floorX(), p.floorY(), p.floorZ());
                        TileEntity te = worldObj.getTileEntity(p.floorX(), p.floorY(), p.floorZ());
                        if (te instanceof SGBaseTE) {
                            if (SGBaseBlock.debugMerge)
                                System.out.printf("DHDTE.checkForLink: Found stargate at (%d,%d,%d)\n",
                                    te.xCoord, te.yCoord, te.zCoord);
                            if (linkToStargate((SGBaseTE)te))
                                return;
                        }
                    }
        }
    }
    
    boolean linkToStargate(SGBaseTE gte) {
        if (!isLinkedToStargate && !gte.isLinkedToController && gte.isMerged) {
            if (SGBaseBlock.debugMerge)
                System.out.printf(
                    "DHDTE.linkToStargate: Linking controller at (%d, %d, %d) with stargate at (%d, %d, %d)\n",
                    xCoord, yCoord, zCoord, gte.xCoord, gte.yCoord, gte.zCoord);
            linkedX = gte.xCoord;
            linkedY = gte.yCoord;
            linkedZ = gte.zCoord;
            isLinkedToStargate = true;
            markBlockForUpdate();
            gte.linkedX = xCoord;
            gte.linkedY = yCoord;
            gte.linkedZ = zCoord;
            gte.isLinkedToController = true;
            gte.markBlockForUpdate();
            return true;
        }
        return false;
    }
    
    public void clearLinkToStargate() {
        System.out.printf("DHDTE: Unlinking controller at (%d, %d, %d) from stargate\n",
            xCoord, yCoord, zCoord);
        isLinkedToStargate = false;
        markBlockForUpdate();
    }
    
    @Override
    public double availableEnergy() {
        double energy = energyInBuffer;
        for (int i = 0; i < numFuelSlots; i++) {
            ItemStack stack = fuelStackInSlot(i);
            if (stack != null)
                energy += stack.stackSize * SGBaseTE.energyPerFuelItem;
        }
        return energy;
    }
    
    @Override
    public double drawEnergy(double amount) {
        double energyDrawn = 0;
        while (energyDrawn < amount) {
            if (energyInBuffer == 0) {
                if (!useFuelItem())
                    break;
            }
            double e = min(amount, energyInBuffer);
            energyDrawn += e;
            energyInBuffer -= e;
        }
        if (SGBaseTE.debugEnergyUse)
            System.out.printf("DHDTE.drawEnergy: %s; supplied: %s; buffered: %s\n",
                amount, energyDrawn, energyInBuffer);
        markDirty();
        markBlockForUpdate();
        return energyDrawn;
    }
    
    boolean useFuelItem() {
        for (int i = numFuelSlots - 1; i >= 0; i--) {
            ItemStack stack = fuelStackInSlot(i);
            if (stack != null) {
                decrStackSize(i, 1);
                energyInBuffer += SGBaseTE.energyPerFuelItem;
                return true;
            }
        }
        return false;
    }
    
    ItemStack fuelStackInSlot(int i) {
        ItemStack stack = getStackInSlot(firstFuelSlot + i);
        if (isValidFuelItem(stack))
            return stack;
        else
            return null;
    }
    
    public static boolean isValidFuelItem(ItemStack stack) {
        return stack != null && stack.getItem() == SGCraft.naquadah && stack.stackSize > 0;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return isValidFuelItem(stack);
    }

}
