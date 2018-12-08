//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class SGBaseScreen extends SGScreen {

    static String screenTitle = "Stargate Address";
    static final int guiWidth = 256;
    static final int guiHeight = 208; //92;
    static final int fuelGaugeWidth = 16;
    static final int fuelGaugeHeight = 34;
    static final int fuelGaugeX = 214;
    static final int fuelGaugeY = 84;
    static final int fuelGaugeU = 0;
    static final int fuelGaugeV = 208;
    
    private SGBaseTE te;
    private String address;
    private String formattedAddress;
    private boolean addressValid;
    
    public static SGBaseScreen create(EntityPlayer player, World world, BlockPos pos) {
        SGBaseTE te = SGBaseTE.at(world, pos);
        if (te != null)
            return new SGBaseScreen(player, te);
        else
            return null;
    }

    public SGBaseScreen(EntityPlayer player, SGBaseTE te) {
        super(new SGBaseContainer(player, te), guiWidth, guiHeight);
        this.te = te;
        getAddress();
        if (addressValid) {
            //System.out.printf("SGBaseScreen: Copying address %s to clipboard\n", formattedAddress);
            if (SGCraft.saveAddressToClipboard) {
                setClipboardString(formattedAddress);
            }
        }
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/sg_gui.png"), 256, 256);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);
        //drawFuelGauge();
        int cx = xSize / 2;
        if (addressValid)
            drawAddressSymbols(cx, 22, address);
        setTextColor(0x004c66);
        drawCenteredString(screenTitle, cx, 8);
        drawCenteredString(formattedAddress, cx, 72);
//      if (this.te.numFuelSlots > 0)
//          drawString("Fuel", 150, 96);
//      if (this.te.numUpgradeSlots > 0)
//          drawCenteredString("Upgrade", 56, 102);
        if (this.te.numCamouflageSlots > 0)
            drawCenteredString("Base Camouflage", 92, 92);
    }

    void getAddress() {
        if (te.homeAddress != null) {
            address = te.homeAddress;
            formattedAddress = SGAddressing.formatAddress(address, "-", "-");
            addressValid = true;
        }
        else {
            address = "";
            formattedAddress = te.addressError;
            addressValid = false;
        }
    }
}
