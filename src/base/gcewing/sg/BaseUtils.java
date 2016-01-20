//------------------------------------------------------------------------------------------------
//
//   Mod Base - Utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;
import net.minecraft.item.*;
import net.minecraft.inventory.*;

public class BaseUtils {

	public static int min(int x, int y) {
		return x < y ? x : y;
	}
	
	public static int max(int x, int y) {
		return x > y ? x : y;
	}

	public static double min(double x, double y) {
		return x < y ? x : y;
	}
	
	public static double max(double x, double y) {
		return x > y ? x : y;
	}
	
	public static String[] split(String sep, String string) {
		List<String> list = new ArrayList<String>();
		String[] result = new String[0];
		int i = 0;
		while (i < string.length()) {
			int j = string.indexOf(sep, i);
			if (j < 0)
				j = string.length();
			list.add(string.substring(i, j));
			i = j + sep.length();
		}
		result = list.toArray(result);
		return result;
	}
	
	public static String join(String sep, String[] strings) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (String s : strings) {
			if (first)
				first = false;
			else
				result.append(sep);
			result.append(s);
		}
		return result.toString();
	}
	
	public static InventorySide inventorySide(IInventory base, int side) {
		if (base instanceof ISidedInventory)
			return new SidedInventorySide((ISidedInventory)base, side);
		else
			return new UnsidedInventorySide(base);
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
			}
			else
				return false;
		}
		
		public ItemStack extract(int i) {
			int slot = slots[i];
			ItemStack stack = base.getStackInSlot(slot);
			if (base.canExtractItem(slot, stack, side))
				return stack;
			else
				return null;
		}
		
	}

}
