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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;

public class IC2PowerTE extends PowerTE implements IEnergySink, ITickable {

    boolean debugLoad = false;
    boolean debugInput = false;

    // The below is intended to set the classes first variables to config values.
    static int maxSafeInput = SGCraft.Ic2SafeInput;
    static int maxEnergyBuffer = SGCraft.Ic2EnergyBuffer;
    static double euPerSGEnergyUnit = SGCraft.Ic2euPerSGEnergyUnit;

    boolean loaded = false;

    public IC2PowerTE() {
        super(maxEnergyBuffer, euPerSGEnergyUnit);
    }

    @Override
    public void readContentsFromNBT(NBTTagCompound nbttagcompound) {
        super.readContentsFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("input")) {
            maxSafeInput = nbttagcompound.getInteger("input");
            maxEnergyBuffer = nbttagcompound.getInteger("buffer");
            euPerSGEnergyUnit = nbttagcompound.getDouble("units");
            super.energyMax = (double) this.maxEnergyBuffer;
        } else {
            maxEnergyBuffer = SGCraft.Ic2EnergyBuffer;
            euPerSGEnergyUnit = SGCraft.Ic2euPerSGEnergyUnit;
            super.energyMax = SGCraft.Ic2EnergyBuffer;
        }
    }

    @Override
    public void writeContentsToNBT(NBTTagCompound nbttagcompound) {
        super.writeContentsToNBT(nbttagcompound);
        nbttagcompound.setInteger("input", maxSafeInput);
        nbttagcompound.setInteger("buffer", maxEnergyBuffer);
        nbttagcompound.setDouble("units", euPerSGEnergyUnit);
    }

    @Override
    public String getScreenTitle() {
        return "IC2 SGPU";
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

    @Override
    public void update() {
        if (!world.isRemote && !loaded) {
            if(debugLoad)
                System.out.printf("SGCraft: IC2PowerTE: Adding to energy network\n");
            loaded = true;
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        }
    }

    void unload() {
        if (!world.isRemote && loaded) {
            if(debugLoad)
                System.out.printf("SGCraft: IC2PowerTE: Removing from energy network\n");
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
        double eu = min(maxEnergyBuffer - energyBuffer, maxSafeInput);
        if(debugInput)
            System.out.printf("SGCraft: IC2PowerTE: Demanding %s EU\n", eu);
        return eu;
    }

    @Override
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        energyBuffer += amount;
        markChanged();
        if(debugInput)
            System.out.printf("SGCraft: IC2PowerTE: Injected %s EU giving %s\n", amount, energyBuffer);
        return 0;
    }

    @Override
    public int getSinkTier() {
        return 3;
    }

}
