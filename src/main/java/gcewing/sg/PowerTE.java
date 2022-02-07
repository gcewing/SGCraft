//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.nbt.NBTTagCompound;

import static java.lang.Math.min;

public abstract class PowerTE extends BaseTileEntity implements ISGEnergySource {

    boolean debugOutput = false;

    public double energyBuffer = 0;
    public double energyMax;
    double energyPerSGEnergyUnit;
    
    public PowerTE(double energyMax, double energyPerSGEnergyUnit) {
        this.energyMax = energyMax;
        this.energyPerSGEnergyUnit = energyPerSGEnergyUnit;
    }
    
    public abstract String getScreenTitle();
    public abstract String getUnitName();
    
    @Override
    public void readContentsFromNBT(NBTTagCompound nbt) {
        super.readContentsFromNBT(nbt);
        energyBuffer = nbt.getDouble("energyBuffer");
    }
    
    public void writeContentsToNBT(NBTTagCompound nbt) {
        super.writeContentsToNBT(nbt);
        nbt.setDouble("energyBuffer", energyBuffer);
    }

    @Override
    public double availableEnergy() {
        double available = energyBuffer / energyPerSGEnergyUnit;
        if (debugOutput) SGCraft.log.debug(String.format("SGCraft: PowerTE: %s SGU available", available));
        return available;
    }
    
    public double drawEnergy(double request) {
        double available = energyBuffer / energyPerSGEnergyUnit;
        double supply = min(request, available);
        energyBuffer -= supply * energyPerSGEnergyUnit;
        markChanged();
        if(debugOutput) SGCraft.log.debug(String.format("SGCraft: PowerTE: Supplying %s SGU of %s requested", supply, request));
        return supply;
    }

}
