//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Generic inventory container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.lang.reflect.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;

public class BaseContainer extends Container {

    int xSize, ySize;
    SlotRange playerSlotRange;
    
//  public BaseContainer() {}
    
    public BaseContainer(int width, int height) {
        xSize = width;
        ySize = height;
    }

    public void addPlayerSlots(EntityPlayer player) {
        addPlayerSlots(player, (xSize - 160) / 2, ySize - 82);
    }

    public void addPlayerSlots(EntityPlayer player, int x, int y) {
        playerSlotRange = new SlotRange();
        InventoryPlayer inventory = player.inventory;
        for (int var3 = 0; var3 < 3; ++var3)
            for (int var4 = 0; var4 < 9; ++var4)
                this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, x + var4 * 18, y + var3 * 18));
        for (int var3 = 0; var3 < 9; ++var3)
            this.addSlotToContainer(new Slot(inventory, var3, x + var3 * 18, y + 58));
        playerSlotRange.end();
    }
    
    public SlotRange addSlots(IInventory inventory, int x, int y, int numRows) {
        return addSlots(inventory, 0, inventory.getSizeInventory(), x, y, numRows);
    }

    public SlotRange addSlots(IInventory inventory, int x, int y, int numRows, Class slotClass) {
        return addSlots(inventory, 0, inventory.getSizeInventory(), x, y, numRows, slotClass);
    }

    public SlotRange addSlots(IInventory inventory, int firstSlot, int numSlots, int x, int y, int numRows) {
        return addSlots(inventory, firstSlot, numSlots, x, y, numRows, Slot.class);
    }

    public SlotRange addSlots(IInventory inventory, int firstSlot, int numSlots, int x, int y, int numRows,
        Class slotClass)
    {
        SlotRange range = new SlotRange();
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
        range.end();
        return range;
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < listeners.size(); i++) {
            IContainerListener listener = listeners.get(i);
            sendStateTo(listener);
        }
    }

    // To enable shift-clicking, check validitity of items here and call
    // mergeItemStack as appropriate.
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        ItemStack stack = slot.getStack();
        if (slot != null && slot.getHasStack()) {
            SlotRange destRange = transferSlotRange(index, stack);
            if (destRange != null) {
                if (index >= destRange.numSlots) {
                    result = stack.copy();
                    if (!mergeItemStackIntoRange(stack, destRange))
                        return ItemStack.EMPTY;
                    if (stack.getCount() == 0)
                        slot.putStack(ItemStack.EMPTY);
                    else
                        slot.onSlotChanged();
                }
                else
                    player.inventory.addItemStackToInventory(stack);
            }
        }
        return result;
    }
    
    protected boolean mergeItemStackIntoRange(ItemStack stack, SlotRange range) {
        return mergeItemStack(stack, range.firstSlot, range.numSlots, range.reverseMerge);
    }
    
    // Return the range of slots into which the given stack should be moved by
    // a shift-click.
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        return null;
    }

    void sendStateTo(IContainerListener listener) {
    }

    public void updateProgressBar(int i, int value) {
    }
    
    public class SlotRange {
        public int firstSlot;
        public int numSlots;
        public boolean reverseMerge;
        
        public SlotRange() {
            firstSlot = inventorySlots.size();
        }
        
        public void end() {
            numSlots = inventorySlots.size() - firstSlot;
        }
        
        public boolean contains(int slot) {
            return slot >= firstSlot && slot < firstSlot + numSlots;
        }
    }

}
