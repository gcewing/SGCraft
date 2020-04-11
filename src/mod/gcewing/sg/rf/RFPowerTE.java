//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import cofh.api.energy.IEnergyHandler;
import gcewing.sg.PowerTE;
import net.minecraftforge.common.util.ForgeDirection;

import static java.lang.Math.min;

public class RFPowerTE extends PowerTE implements IEnergyHandler {

    final static int maxEnergyBuffer = 4000000;
    final static double rfPerSGEnergyUnit = 80.0;
    boolean debugInput = false;

    public RFPowerTE() {
        super(maxEnergyBuffer, rfPerSGEnergyUnit);
    }

    @Override
    public String getScreenTitle() {
        return "RF SGPU";
    }

    @Override
    public String getUnitName() {
        return "RF";
    }

    //------------------------- IEnergyConnection -------------------------

    public boolean canConnectEnergy(ForgeDirection dir) {
        return true;
    }

    //------------------------- IEnergyHandler -------------------------

    public int receiveEnergy(ForgeDirection dir, int energy, boolean query) {
        int e = (int) min(this.energyMax - this.energyBuffer, energy);
        if (!query)
            addEnergy(e);
        return e;
    }

    public int extractEnergy(ForgeDirection dir, int energy, boolean query) {
        int e = (int) Math.min(this.energyBuffer, energy);
        if (!query)
            addEnergy(-e);
        return e;
    }

    void addEnergy(int e) {
        this.energyBuffer += e;
        markChanged();
    }

    public int getEnergyStored(ForgeDirection dir) {
        return (int) energyBuffer;
    }

    public int getMaxEnergyStored(ForgeDirection dir) {
        return (int) energyMax;
    }

}
