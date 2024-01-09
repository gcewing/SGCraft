// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 version B - Generic inventory container
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static java.lang.Math.min;

import java.lang.reflect.Constructor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BaseContainer extends Container {

    int xSize, ySize;
    SlotRange playerSlotRange; // Slots containing player inventory
    SlotRange containerSlotRange; // Default slot range for shift-clicking into from player inventory

    public BaseContainer(int width, int height) {
        xSize = width;
        ySize = height;
    }

    public BaseContainer(int width, int height, EntityPlayer player) {
        this(width, height);
        addPlayerSlots(player);
    }

    // Slots added between beginContainerSlots and endContainerSlots will be included in
    // containerSlotRange.

    protected void beginContainerSlots() {
        containerSlotRange = new SlotRange();
    }

    protected void endContainerSlots() {
        containerSlotRange.end();
    }

    protected void beginPlayerSlots() {
        playerSlotRange = new SlotRange();
    }

    protected void endPlayerSlots() {
        playerSlotRange.end();
    }

    // Call one of the addPlayerSlots methods from the constructor to add player inventory
    // slots and set playerSlotRange.

    // Add player inventory slots in the standard position and layout at bottom centre
    // of the gui.
    public void addPlayerSlots(EntityPlayer player) {
        addPlayerSlots(player, (xSize - 160) / 2, ySize - 82);
    }

    // Add player inventory slots in the standard layout with top left corner at x, y.
    public void addPlayerSlots(EntityPlayer player, int x, int y) {
        beginPlayerSlots();
        InventoryPlayer inventory = player.inventory;
        for (int var3 = 0; var3 < 3; ++var3) for (int var4 = 0; var4 < 9; ++var4)
            this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, x + var4 * 18, y + var3 * 18));
        for (int var3 = 0; var3 < 9; ++var3) this.addSlotToContainer(new Slot(inventory, var3, x + var3 * 18, y + 58));
        endPlayerSlots();
    }

    public void addPlayerSlotsRotated(EntityPlayer player, int x, int y) {
        beginPlayerSlots();
        InventoryPlayer inventory = player.inventory;
        for (int var3 = 0; var3 < 3; ++var3) for (int var4 = 0; var4 < 9; ++var4)
            this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, x + 58 - var3 * 18, y + var4 * 18));
        for (int var3 = 0; var3 < 9; ++var3) this.addSlotToContainer(new Slot(inventory, var3, x, y + var3 * 18));
        endPlayerSlots();
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
            Class slotClass) {
        SlotRange range = new SlotRange();
        try {
            Constructor slotCon = slotClass.getConstructor(IInventory.class, int.class, int.class, int.class);
            int numCols = (numSlots + numRows - 1) / numRows;
            for (int i = 0; i < numSlots; i++) {
                int row = i / numCols;
                int col = i % numCols;
                addSlotToContainer((Slot) slotCon.newInstance(inventory, firstSlot + i, x + col * 18, y + row * 18));
            }
        } catch (Exception e) {
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
        for (int i = 0; i < crafters.size(); i++) {
            ICrafting crafter = (ICrafting) crafters.get(i);
            sendStateTo(crafter);
        }
    }

    // To enable shift-clicking, check validitity of items here and call
    // mergeItemStack as appropriate.
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(index);
        ItemStack stack = slot.getStack();
        if (slot != null && slot.getHasStack()) {
            SlotRange destRange = transferSlotRange(index, stack);
            if (destRange != null) {
                result = stack.copy();
                if (!mergeItemStackIntoRange(stack, destRange)) return null;
                if (stack.stackSize == 0) slot.putStack(null);
                else slot.onSlotChanged();
            }
        }
        return result;
    }

    protected boolean mergeItemStackIntoRange(ItemStack stack, SlotRange range) {
        return mergeItemStack(stack, range.firstSlot, range.firstSlot + range.numSlots, range.reverseMerge);
    }

    // A better version of mergeItemStack that respects Slot.isItemValid() and Slot.getStackLimit().
    @Override
    protected boolean mergeItemStack(ItemStack stack, int startSlot, int endSlot, boolean reverse) {
        boolean result = false;
        int n = endSlot - startSlot;
        if (stack.isStackable()) {
            for (int i = 0; i < n && stack.stackSize > 0; i++) {
                int k = reverse ? endSlot - 1 - i : startSlot + i;
                Slot slot = (Slot) this.inventorySlots.get(k);
                ItemStack slotStack = slot.getStack();
                if (slotStack != null && slotStack.isStackable()
                        && slotStack.getItem() == stack.getItem()
                        && (!stack.getHasSubtypes() || stack.getItemDamage() == slotStack.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(stack, slotStack)) {
                    if (transferToSlot(stack, slot)) result = true;
                }
            }
        }
        for (int i = 0; i < n && stack.stackSize > 0; i++) {
            int k = reverse ? endSlot - 1 - i : startSlot + i;
            Slot slot = (Slot) this.inventorySlots.get(k);
            if (!slot.getHasStack() && slot.isItemValid(stack)) {
                ItemStack newStack = stack.copy();
                newStack.stackSize = 0;
                slot.putStack(newStack);
                if (transferToSlot(stack, slot)) result = true;
            }
        }
        return result;
    }

    protected boolean transferToSlot(ItemStack stack, Slot slot) {
        ItemStack slotStack = slot.getStack();
        int slotLimit = min(stack.getMaxStackSize(), slot.getSlotStackLimit());
        int oldSlotSize = slotStack.stackSize;
        int newSlotSize = min(oldSlotSize + stack.stackSize, slotLimit);
        int transferSize = newSlotSize - oldSlotSize;
        if (transferSize > 0) {
            stack.stackSize -= transferSize;
            slotStack.stackSize += transferSize;
            slot.onSlotChanged();
            return true;
        } else return false;
    }

    // Return the range of slots into which the given stack should be moved by a shift-click.
    // Default implementation transfers between playerSlotRange and containerSlotRange.
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        if (playerSlotRange.contains(srcSlotIndex)) return containerSlotRange;
        else if (containerSlotRange != null && containerSlotRange.contains(srcSlotIndex)) return playerSlotRange;
        else return null;
    }

    void sendStateTo(ICrafting crafter) {}

    public void updateProgressBar(int i, int value) {}

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

        @Override
        public String toString() {
            return String.format("SlotRange(%s to %s)", firstSlot, firstSlot + numSlots - 1);
        }
    }

}
