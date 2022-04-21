//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers Interface GUI Container
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.inventory.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;

import gcewing.sg.*;
import static gcewing.sg.BaseBlockUtils.*;

public class OCInterfaceContainer extends BaseContainer {

    final static int guiWidth = 176;
    final static int guiHeight = 125;
    final static int slotsLeft = 8;
    final static int slotsTop = 17;

    OCInterfaceTE te;

    public OCInterfaceContainer(EntityPlayer player, World world, BlockPos pos) {
        super(guiWidth, guiHeight);
        te = (OCInterfaceTE)getWorldTileEntity(world, pos);
        addSlots(te, slotsLeft, slotsTop, 1, UpgradeSlot.class);
        addPlayerSlots(player);
    }

    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        SlotRange range = new SlotRange();
        range.firstSlot = 0;
        range.numSlots = 1;
        return range;
    }

    public static class UpgradeSlot extends Slot {
    
        public UpgradeSlot(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }
        
        @Override
        public boolean isItemValid(ItemStack stack) {
            return OCInterfaceTE.isNetworkCard(stack);
        }
        
        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    
    }

}

