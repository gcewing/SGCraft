//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class SGBaseScreen extends SGScreen {

    static final int guiWidth = 256;
    static final int guiHeight = 208; //92;
    static final String screenTitle = "Stargate Address";
    final SGBaseTE te;
    String address;
    String formattedAddress;
    boolean addressValid;

    public SGBaseScreen(EntityPlayer player, SGBaseTE te) {
        super(new SGBaseContainer(player, te), guiWidth, guiHeight);
        this.te = te;
        getAddress();
        if (addressValid) {
            setClipboardString(formattedAddress);
        }
    }

    public static SGBaseScreen create(EntityPlayer player, World world, BlockPos pos) {
        SGBaseTE te = SGBaseTE.at(world, pos);
        if (te != null)
            return new SGBaseScreen(player, te);
        else
            return null;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/sg_gui.png"), 256, 256);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);
        int cx = xSize / 2;
        if (addressValid)
            drawAddressSymbols(cx, 22, address);
        setTextColor(0x004c66);
        drawCenteredString(screenTitle, cx, 8);
        drawCenteredString(formattedAddress, cx, 72);
        if (SGBaseTE.numCamouflageSlots > 0)
            drawCenteredString("Base Camouflage", 92, 92);
    }

    void getAddress() {
        if (te.homeAddress != null) {
            address = te.homeAddress;
            formattedAddress = SGAddressing.formatAddress(address, "-", "-");
            addressValid = true;
        } else {
            address = "";
            formattedAddress = te.addressError;
            addressValid = false;
        }
    }

}
