//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;

import ic2.api.energy.event.*;
import ic2.api.energy.tile.*;

import gcewing.sg.*;
import static gcewing.sg.BaseUtils.*;
import static gcewing.sg.Utils.*;

public class IC2PowerTE extends PowerTE implements IEnergySink, ITickable {

    boolean debugLoad = false;
    boolean debugInput = false;

    final static int maxSafeInput = 2048;
    final static int maxEnergyBuffer = 1000000;
    final static double euPerSGEnergyUnit = 20.0;

    boolean loaded = false;
    
    public IC2PowerTE() {
        super(maxEnergyBuffer, euPerSGEnergyUnit);
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
        if (!worldObj.isRemote && !loaded) {
            if(debugLoad)
                System.out.printf("SGCraft: IC2PowerTE: Adding to energy network\n");
            loaded = true;
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        }
    }           
    
    void unload() {
        if (!worldObj.isRemote && loaded) {
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
