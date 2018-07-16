//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class DHDFuelContainer extends BaseContainer {

    static final int numFuelSlotColumns = 2;
    static final int fuelSlotsX = 174;
    static final int fuelSlotsY = 84;
    static final int playerSlotsX = 48;
    static final int playerSlotsY = 124;

    DHDTE te;
    
    public static DHDFuelContainer create(EntityPlayer player, World world, BlockPos pos) {
        DHDTE te = DHDTE.at(world, pos);
        if (te != null)
            return new DHDFuelContainer(player, te);
        else
            return null;
    }
    
    public DHDFuelContainer(EntityPlayer player, DHDTE te) {
        super(DHDFuelScreen.guiWidth, DHDFuelScreen.guiHeight);
        this.te = te;
        addFuelSlots();
        addPlayerSlots(player, playerSlotsX, playerSlotsY);
    }
    
    void addFuelSlots() {
        int b = DHDTE.firstFuelSlot;
        int n = DHDTE.numFuelSlots;
        for (int i = 0; i < n; i++) {
            int row = i / numFuelSlotColumns;
            int col = i % numFuelSlotColumns;
            int x = fuelSlotsX + col * 18;
            int y = fuelSlotsY + row * 18;
            addSlotToContainer(new FuelSlot(te, b + i, x, y));
        }
    }

    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = DHDTE.firstFuelSlot;
        range.numSlots = DHDTE.numFuelSlots;
        return range;
    }
    
//  @Override
//  void sendStateTo(ICrafting crafter) {
//      crafter.sendProgressBarUpdate(this, 0, (int)((10000 * te.energyInBuffer) / te.maxEnergyBuffer));
//  }
//
//  @Override
//  public void updateProgressBar(int i, int value) {
//      switch (i) {
//          case 0: te.energyInBuffer = (value * te.maxEnergyBuffer) / 10000; break;
//      }
//  }

}

//------------------------------------------------------------------------------------------------

class FuelSlot extends Slot {

    public FuelSlot(IInventory inv, int i, int x, int y) {
        super(inv, i, x, y);
    }
    
    public boolean isItemValid(ItemStack stack) {
        return DHDTE.isValidFuelItem(stack);
    }

}

