//------------------------------------------------------------------------------------------------
//
//   SG Craft - RF Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.rf;

import static java.lang.Math.*;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;

import cofh.api.energy.*;

import gcewing.sg.*;
import static gcewing.sg.BaseUtils.*;
import static gcewing.sg.Utils.*;

public class RFPowerTE extends PowerTE implements IEnergyHandler {

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
    
    //------------------------- IEnergyConnection -------------------------

    public boolean canConnectEnergy(ForgeDirection dir) {
        return true;
    }

    //------------------------- IEnergyHandler -------------------------
    
    public int receiveEnergy(ForgeDirection dir, int energy, boolean query) {
    int e = (int)min(this.energyMax - this.energyBuffer, energy);
    if (!query)
      addEnergy(e);
    return e;
    }
    
    public int extractEnergy(ForgeDirection dir, int energy, boolean query) {
    int e = (int)Math.min(this.energyBuffer, energy);
    if (!query)
      addEnergy(-e);
    return e;
  }
  
  void addEnergy(int e) {
        this.energyBuffer += e;
        markChanged();
  }
    
    public int getEnergyStored(ForgeDirection dir) {
        return (int)energyBuffer;
    }
    
    public int getMaxEnergyStored(ForgeDirection dir) {
        return (int)energyMax;
    }

}
