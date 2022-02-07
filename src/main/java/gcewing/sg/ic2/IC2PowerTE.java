//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Stargate Power Unit Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import gcewing.sg.ITickable;
import gcewing.sg.PowerTE;
import gcewing.sg.SGCraft;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import static java.lang.Math.min;

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
    public void update() {
        load();
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
    
    void load() {
        if (!worldObj.isRemote && !loaded) {
            if(debugLoad) SGCraft.log.debug("IC2PowerTE: Adding to energy network");
            loaded = true;
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        }
    }           
    
    void unload() {
        if (!worldObj.isRemote && loaded) {
            if(debugLoad) SGCraft.log.debug("IC2PowerTE: Removing from energy network");
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            loaded = false;
        }
    }
    
    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }
    
    @Override
    public double getDemandedEnergy() {
        double eu = min(maxEnergyBuffer - energyBuffer, maxSafeInput);
        if(debugInput) SGCraft.log.debug(String.format("IC2PowerTE: Demanding %s EU", eu));
        return eu;
    }
    
    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        energyBuffer += amount;
        markChanged();
        if(debugInput) SGCraft.log.debug(String.format("IC2PowerTE: Injected %s EU giving %s", amount, energyBuffer));
        return 0;
    }

    @Override
    public int getSinkTier() {
        return 3;
    }
    
}
