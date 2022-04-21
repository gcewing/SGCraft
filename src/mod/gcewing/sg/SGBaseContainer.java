//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base gui container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class SGBaseContainer extends BaseContainer {

    static final int numFuelSlotColumns = 2;
//  static final int fuelSlotsX = 174;
//  static final int fuelSlotsY = 84;
//  static final int upgradeSlotsX = 48;
//  static final int upgradeSlotsY = 84;
//  static final int playerSlotsX = 48;
//  static final int playerSlotsY = 124;

    SGBaseTE te;

    public static SGBaseContainer create(EntityPlayer player, World world, BlockPos pos) {
        SGBaseTE te = SGBaseTE.at(world, pos);
        if (te != null)
            return new SGBaseContainer(player, te);
        else
            return null;
    }

    public SGBaseContainer(EntityPlayer player, SGBaseTE te) {
        super(256, 208);
        this.te = te;
//      addFuelSlots();
//      addUpgradeSlots();
        addCamouflageSlots();
        addPlayerSlots(player); //(player, playerSlotsX, playerSlotsY);
    }
    
    void addCamouflageSlots() {
        addSlots(te, 0, te.numCamouflageSlots, 48, 104, 1, CamouflageSlot.class);
    }

    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = 0;
        range.numSlots = SGBaseTE.numCamouflageSlots;
        return range;
    }
    
//  void addFuelSlots() {
//      int b = SGBaseTE.firstFuelSlot;
//      int n = SGBaseTE.numFuelSlots;
//      //System.out.printf("SGBaseContainer: %s fuel slots\n", n);
//      for (int i = 0; i < n; i++) {
//          int row = i / numFuelSlotColumns;
//          int col = i % numFuelSlotColumns;
//          int x = fuelSlotsX + col * 18;
//          int y = fuelSlotsY + row * 18;
//          //System.out.printf("SGBaseContainer: adding fuel slot %s at (%s, %s)\n", i, x, y);
//          addSlotToContainer(new Slot(te, b + i, x, y));
//      }
//  }
    
//  void addUpgradeSlots() {
//      int b = SGBaseTE.firstUpgradeSlot;
//      int n = SGBaseTE.numUpgradeSlots;
//      for (int i = 0; i < n; i++) {
//          int x = upgradeSlotsX + i * 18;
//          int y = upgradeSlotsY;
//          addSlotToContainer(new Slot(te, b + i, x, y));
//      }
//  }

}

class CamouflageSlot extends Slot {

    public CamouflageSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
    
    @Override
    public int getSlotStackLimit() {
        return 1;
    }

}
