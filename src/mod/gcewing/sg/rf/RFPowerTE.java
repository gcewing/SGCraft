//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;

import cofh.api.energy.*;

import gcewing.sg.*;
import static gcewing.sg.BaseUtils.*;
import static gcewing.sg.Utils.*;

public class RFPowerTE extends PowerTE implements IEnergyProvider, IEnergyReceiver {

    boolean debugInput = false;

    final static int maxEnergyBuffer = 4000000;
    final static double rfPerSGEnergyUnit = 80.0;

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
    
    protected void addEnergy(int e) {
        this.energyBuffer += e;
        markChanged();
    }    

    //------------------------- IEnergyConnection -------------------------

    @Override
    public boolean canConnectEnergy(EnumFacing dir) {
        return true;
    }

    //------------------------- IEnergyHandler -------------------------

    @Override
    public int getEnergyStored(EnumFacing dir) {
        return (int)energyBuffer;
    }
    
    @Override
    public int getMaxEnergyStored(EnumFacing dir) {
        return (int)energyMax;
    }

    //------------------------- IEnergyReceiver -------------------------
    
    @Override
    public int receiveEnergy(EnumFacing dir, int energy, boolean query) {
        int e = (int)min(this.energyMax - this.energyBuffer, energy);
        if (!query)
            addEnergy(e);
        return e;
    }
    
    //------------------------- IEnergyProvider -------------------------
    
    @Override
    public int extractEnergy(EnumFacing dir, int energy, boolean query) {
        int e = (int)Math.min(this.energyBuffer, energy);
        if (!query)
            addEnergy(-e);
        return e;
    }
  
}
