//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import static gcewing.sg.BaseUtils.min;

import gcewing.sg.PowerTE;
import gcewing.sg.SGCraft;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

public class IC2PowerTE extends PowerTE implements IEnergySink, ITickable {

    private boolean debugLoad = false;
    private boolean debugInput = false;

    // The below is intended to set the classes first variables to config values.
    private int maxSafeInput = SGCraft.Ic2SafeInput;
    private int powerTier = SGCraft.Ic2PowerTETier;
    private int update = 0;
    private boolean loaded = false;

    public IC2PowerTE() {
        super(SGCraft.Ic2MaxEnergyBuffer, SGCraft.Ic2euPerSGEnergyUnit);
    }

    @Override
    public void readContentsFromNBT(NBTTagCompound nbttagcompound) {
        super.readContentsFromNBT(nbttagcompound);
        // Check if Key doesn't exist or if Admin is trying to update all the DHD's with new values.
        if (!nbttagcompound.hasKey("input") || SGCraft.forceIC2CfgUpdate) {
            maxSafeInput = SGCraft.Ic2SafeInput;
            powerTier = SGCraft.Ic2PowerTETier;
            energyMax = SGCraft.Ic2MaxEnergyBuffer;
            energyPerSGEnergyUnit = SGCraft.Ic2euPerSGEnergyUnit;
        } else {
            maxSafeInput = nbttagcompound.getInteger("input");
            powerTier = nbttagcompound.getInteger("tier");
        }
    }

    @Override
    public void writeContentsToNBT(NBTTagCompound nbttagcompound) {
        super.writeContentsToNBT(nbttagcompound);
        nbttagcompound.setInteger("input", maxSafeInput);
        nbttagcompound.setInteger("tier", powerTier);
    }


    @Override
    public String getScreenTitle() {
        return "IC2 SGPU";
    }

    @Override
    public void update() {
        if (!world.isRemote && !loaded) {
            if(debugLoad) {
                System.out.printf("SGCraft: IC2PowerTE: Adding to energy network\n");
            }
            loaded = true;
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        }
    }

    @Override
    public String getUnitName() {
        return "EU";
    }

    @Override
    public void invalidate() {
        unload();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        unload();
        super.onChunkUnload();
    }

    public static IC2PowerTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof IC2PowerTE ? (IC2PowerTE) te : null;
    }

    void unload() {
        if (!world.isRemote && loaded) {
            if(debugLoad) {
                System.out.printf("SGCraft: IC2PowerTE: Removing from energy network\n");
            }
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            loaded = false;
        }
    }

    //------------------------- IEnergyAcceptor -------------------------

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
        return true;
    }

    //------------------------- IEnergySink -------------------------

    @Override
    public double getDemandedEnergy() {
        double eu = min(energyMax - energyBuffer, maxSafeInput);
        if(debugInput) {
            System.out.printf("SGCraft: IC2PowerTE: Demanding %s EU\n", eu);
        }
        return eu;
    }

    @Override
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        energyBuffer += amount;
        if (update++ > 10) { // We dont' need 20 packets per second to the client....
            markChanged();
            update = 0;
        }
        if(debugInput) {
            System.out.printf("SGCraft: IC2PowerTE: Injected %s EU giving %s\n", amount, energyBuffer);
        }
        return 0;
    }

    @Override
    public int getSinkTier() {
        return powerTier;  //HV
    }

    @Override public double totalAvailableEnergy() {
        return energyBuffer;
    }
}
