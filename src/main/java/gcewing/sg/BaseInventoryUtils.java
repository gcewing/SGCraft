// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.8 - Inventory Utilities
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class BaseInventoryUtils {

    public static InventorySide inventorySide(IInventory base, int side) {
        if (base instanceof ISidedInventory) return new SidedInventorySide((ISidedInventory) base, side);
        else return new UnsidedInventorySide(base);
    }

    public static abstract class InventorySide {

        public int size;

        public abstract ItemStack get(int slot);

        public abstract boolean set(int slot, ItemStack stack);

        public abstract ItemStack extract(int slot);
    }

    public static class UnsidedInventorySide extends InventorySide {

        IInventory base;

        public UnsidedInventorySide(IInventory base) {
            this.base = base;
            size = base.getSizeInventory();
        }

        public ItemStack get(int slot) {
            return base.getStackInSlot(slot);
        }

        public boolean set(int slot, ItemStack stack) {
            base.setInventorySlotContents(slot, stack);
            return true;
        }

        public ItemStack extract(int slot) {
            return get(slot);
        }

    }

    public static class SidedInventorySide extends InventorySide {

        ISidedInventory base;
        int side;
        int[] slots;

        public SidedInventorySide(ISidedInventory base, int side) {
            this.base = base;
            this.side = side;
            slots = base.getAccessibleSlotsFromSide(side);
            size = slots.length;
        }

        public ItemStack get(int i) {
            return base.getStackInSlot(slots[i]);
        }

        public boolean set(int i, ItemStack stack) {
            int slot = slots[i];
            if (base.canInsertItem(slot, stack, side)) {
                base.setInventorySlotContents(slot, stack);
                return true;
            } else return false;
        }

        public ItemStack extract(int i) {
            int slot = slots[i];
            ItemStack stack = base.getStackInSlot(slot);
            if (base.canExtractItem(slot, stack, side)) return stack;
            else return null;
        }

    }

    public static void clearInventory(IInventory inv) {
        int n = inv.getSizeInventory();
        for (int i = 0; i < n; i++) inv.setInventorySlotContents(i, null);
    }

}
