//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.nbt.*;

// import ic2.api.energy.event.*; [IC2]
// import ic2.api.energy.tile.*;

import static gcewing.sg.BaseUtils.*;

public abstract class PowerTE extends BaseTileEntity implements ISGEnergySource {

    boolean debugOutput = false;

    public double energyBuffer = 0;
    public double energyMax;
    public double energyPerSGEnergyUnit;
    private int update = 0;

    public PowerTE(double energyMax, double energyPerSGEnergyUnit) {
        this.energyMax = energyMax;
        this.energyPerSGEnergyUnit = energyPerSGEnergyUnit;
    }

    public abstract String getScreenTitle();

    public abstract String getUnitName();

    @Override
    public void readContentsFromNBT(NBTTagCompound nbt) {
        super.readContentsFromNBT(nbt);
        if (nbt.hasKey("energyBuffer")) {
            energyBuffer = nbt.getDouble("energyBuffer");
            energyMax = nbt.getDouble("energyMax");
        }
    }

    public void writeContentsToNBT(NBTTagCompound nbt) {
        super.writeContentsToNBT(nbt);
        nbt.setDouble("energyBuffer", energyBuffer);
        nbt.setDouble("energyMax", energyMax);
    }

    //------------------------- ISGEnergySource -------------------------

    @Override
    public double availableEnergy() {
        double available = energyBuffer / energyPerSGEnergyUnit;
        if (debugOutput)
            System.out.printf("SGCraft: PowerTE: %s SGU available\n", available);
        return available;
    }

    public double totalAvailableEnergy() {
        return energyBuffer;
    }

    public double drawEnergy(double request) {
        //                10000 / 20
        double available = energyBuffer / energyPerSGEnergyUnit;
        double supply = min(request, available);
        energyBuffer -= supply * energyPerSGEnergyUnit;
        if (update++ > 10) { // We dont' need 20 packets per second to the client....
            markChanged();
            update = 0;
        }
        if(debugOutput)
            System.out.printf("SGCraft: PowerTE: Supplying %s SGU of %s requested\n", supply, request);
        return supply;
    }
}
