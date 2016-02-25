//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Generic inventory container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.lang.reflect.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;

public class BaseContainer extends Container {

    int xSize, ySize;
    
//  public BaseContainer() {}
    
    public BaseContainer(int width, int height) {
        xSize = width;
        ySize = height;
    }

    public void addPlayerSlots(EntityPlayer player) {
        addPlayerSlots(player, (xSize - 160) / 2, ySize - 82);
    }

    public void addPlayerSlots(EntityPlayer player, int x, int y) {
        InventoryPlayer inventory = player.inventory;
        for (int var3 = 0; var3 < 3; ++var3)
            for (int var4 = 0; var4 < 9; ++var4)
                this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, x + var4 * 18, y + var3 * 18));
        for (int var3 = 0; var3 < 9; ++var3)
            this.addSlotToContainer(new Slot(inventory, var3, x + var3 * 18, y + 58));
    }
    
    public void addSlots(IInventory inventory, int x, int y, int numRows) {
        addSlots(inventory, 0, inventory.getSizeInventory(), x, y, numRows);
    }

    public void addSlots(IInventory inventory, int x, int y, int numRows, Class slotClass) {
        addSlots(inventory, 0, inventory.getSizeInventory(), x, y, numRows, slotClass);
    }

    public void addSlots(IInventory inventory, int firstSlot, int numSlots, int x, int y, int numRows) {
        addSlots(inventory, firstSlot, numSlots, x, y, numRows, Slot.class);
    }

    public void addSlots(IInventory inventory, int firstSlot, int numSlots, int x, int y, int numRows,
        Class slotClass)
    {
        try {
            Constructor slotCon = slotClass.getConstructor(IInventory.class, int.class, int.class, int.class);
            int numCols = (numSlots + numRows - 1) / numRows;
            for (int i = 0; i < numSlots; i++) {
                int row = i /numCols;
                int col = i % numCols;
                addSlotToContainer((Slot)slotCon.newInstance(inventory, firstSlot + i, x + col * 18, y + row * 18));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < crafters.size(); i++) {
            ICrafting crafter = (ICrafting)crafters.get(i);
            sendStateTo(crafter);
        }
    }
    
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        // TODO: Try to come up with a generic way of implementing this
        return null;
    }

    void sendStateTo(ICrafting crafter) {
    }

    public void updateProgressBar(int i, int value) {
    }

}
