// ------------------------------------------------------------------------------------------------
//
// SG Craft - Open Computers Interface GUI Container
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import static gcewing.sg.BaseBlockUtils.getWorldTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import gcewing.sg.BaseContainer;
import gcewing.sg.BlockPos;

public class OCInterfaceContainer extends BaseContainer {

    final static int guiWidth = 176;
    final static int guiHeight = 125;
    final static int slotsLeft = 8;
    final static int slotsTop = 17;

    OCInterfaceTE te;

    public OCInterfaceContainer(EntityPlayer player, World world, BlockPos pos) {
        super(guiWidth, guiHeight);
        te = (OCInterfaceTE) getWorldTileEntity(world, pos);
        addPlayerSlots(player);
        addSlots(te, slotsLeft, slotsTop, 1, UpgradeSlot.class);
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
