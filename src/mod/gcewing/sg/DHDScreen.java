//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.audio.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import net.minecraftforge.client.*;

import static gcewing.sg.BaseBlockUtils.*;

public class DHDScreen extends SGScreen {

    final static int dhdWidth = 320;
    final static int dhdHeight = 120;
    final static double dhdRadius1 = dhdWidth * 0.1;
    final static double dhdRadius2 = dhdWidth * 0.275;
    final static double dhdRadius3 = dhdWidth * 0.45;

    World world;
    BlockPos pos;
    int dhdTop, dhdCentreX, dhdCentreY;
    //String enteredAddress = "";
    int closingDelay = 0;
    int addressLength;
    DHDTE cte;
    
    public DHDScreen(EntityPlayer player, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
        cte = getControllerTE();
        SGBaseTE te = getStargateTE();
        if (te != null)
            addressLength = te.getNumChevrons();
    }
    
    SGBaseTE getStargateTE() {
        if (cte != null)
            return cte.getLinkedStargateTE();
        else
            return null;
    }
    
    DHDTE getControllerTE() {
        TileEntity te = getWorldTileEntity(world, pos);
        if (te instanceof DHDTE)
            return (DHDTE)te;
        else
            return null;
    }
    
    String getEnteredAddress() {
        return cte.enteredAddress;
    }
    
    void setEnteredAddress(String address) {
        cte.enteredAddress = address;
        SGChannel.sendEnteredAddressToServer(cte, address);
    }

    @Override
    public void initGui() {
        dhdTop = height - dhdHeight;
        dhdCentreX = width / 2;
        dhdCentreY = dhdTop + dhdHeight / 2;
    }
    
//  @Override
//  public void onGuiClosed() {
//  }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (closingDelay > 0) {
            if (--closingDelay == 0) {
                setEnteredAddress("");
                close();
            }
        }
    }
    
    @Override
    protected void mousePressed(int x, int y, int mouseButton) {
        //System.out.printf("DHDScreen.mousePressed: %d, %d, %d\n", x, y, mouseButton);
        if (mouseButton == 0) {
            int i = findDHDButton(x, y);
            if (i >= 0) {
                dhdButtonPressed(i);
                return;
            }
        }
    }
    
    void closeAfterDelay(int ticks) {
        closingDelay = ticks;
    }
    
    int findDHDButton(int mx, int my) {
        //System.out.printf("DHDScreen.findDHDButton: mx = %d, my = %d, cx = %d, cy = %d\n",
        //  mx, my, dhdCentreX, dhdCentreY);
        int x = -(mx - dhdCentreX);
        int y = -(my - dhdCentreY);
        // Check top half of orange dome
        if (y > 0 && Math.hypot(x, y) <= dhdRadius1)
            return 0;
        // Scale to circular coords and check rest of buttons
        y = y * dhdWidth / dhdHeight;
        //System.out.printf("DHDScreen.findDHDButton: x = %d, y = %d\n", x, y);
        double r = Math.hypot(x, y);
        if (r > dhdRadius3)
            return -1;
        if (r <= dhdRadius1)
            return 0;
        double a = Math.toDegrees(Math.atan2(y, x));
        //System.out.printf("DHDScreen.findDHDButton: a = %s\n", a);
        if (a < 0)
            a += 360;
        //int i0 = (r > dhdRadius2) ? 1 : 15;
        //return i0 + (int)Math.floor(a * 14 / 360);
        int i0, nb;
        if (r > dhdRadius2) {
            i0 = 1; nb = 26;
        }
        else {
            i0 = 27; nb = 11;
        }
        int i = i0 + (int)Math.floor(a * nb / 360);
        System.out.printf("DHDScreen.findDHDButton: i = %d\n", i);
        return i;
    }
    
    void dhdButtonPressed(int i) {
        //System.out.printf("DHDScreen.dhdButtonPressed: %d\n", i);
        buttonSound();
        if (i == 0)
            orangeButtonPressed(false);
        else if (i >= 37)
            backspace();
        else
            enterCharacter(SGBaseTE.symbolToChar(i - 1));
    }
    
    void buttonSound() {
        //mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
        EntityPlayer player = mc.thePlayer;
        ISound sound = new PositionedSoundRecord(
            new ResourceLocation("ui.button.click"), SoundCategory.BLOCKS,
            1.0F, 1.0F,
            false, 0, ISound.AttenuationType.LINEAR,
            (float)player.posX, (float)player.posY, (float)player.posZ);
        mc.getSoundHandler().playSound(sound);
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE)
            close();
        else if (key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE)
            backspace();
        else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER)
            orangeButtonPressed(true);
        else {
            String C = String.valueOf(c).toUpperCase();
            if (SGAddressing.isValidSymbolChar(C))
                enterCharacter(C.charAt(0));
        }
    }
    
    void orangeButtonPressed(boolean connectOnly) {
        SGBaseTE te = getStargateTE();
        if (te != null) {
            if (te.state == SGState.Idle)
                sendConnectOrDisconnect(te, getEnteredAddress());
            else if (!connectOnly)
                sendConnectOrDisconnect(te, "");
        }
    }
    
    void sendConnectOrDisconnect(SGBaseTE te, String address) {
        SGChannel.sendConnectOrDisconnectToServer(te, address);
        closeAfterDelay(10);
    }
        
    void backspace() {
        if (stargateIsIdle()) {
            buttonSound();
            String a = getEnteredAddress();
            int n = a.length();
            if (n > 0)
                setEnteredAddress(a.substring(0, n - 1));
        }
    }
    
    void enterCharacter(char c) {
        if (stargateIsIdle()) {
            buttonSound();
            String a = getEnteredAddress();
            int n = a.length();
            if (n < addressLength)
                setEnteredAddress(a + c);
        }
    }
    
    boolean stargateIsIdle() {
        SGBaseTE te = getStargateTE();
        return (te != null && te.state == SGState.Idle);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        SGBaseTE te = getStargateTE();
        glPushAttrib(GL_ENABLE_BIT|GL_COLOR_BUFFER_BIT);
        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_ALPHA_TEST);
        drawBackgroundImage();
        drawOrangeButton();
        if (te != null) {
            if (te.state == SGState.Idle) {
                drawEnteredSymbols();
                drawEnteredString();
            }
        }
        glPopAttrib();
    }

    void drawBackgroundImage() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/dhd_gui.png"));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        drawTexturedRect((width - dhdWidth) / 2, height - dhdHeight, dhdWidth, dhdHeight);
    }
    
    void drawOrangeButton() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/dhd_centre.png"), 128, 64);
        SGBaseTE te = getStargateTE();
        boolean connected = te != null && te.isActive();
        if (te == null || !te.isMerged)
            setColor(0.2, 0.2, 0.2);
        else if (connected)
            setColor(1.0, 0.5, 0.0);
        else
            setColor(0.5, 0.25, 0.0);
        double rx = dhdWidth * 48 / 512.0;
        double ry = dhdHeight * 48 / 256.0;
//         Tessellator.instance.disableColor();
        drawTexturedRect(dhdCentreX - rx, dhdCentreY - ry - 6, 2 * rx, 1.5 * ry,
            64, 0, 64, 48);
        resetColor();
        if (connected) {
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            double d = 5;
            drawTexturedRect(dhdCentreX - rx - d, dhdCentreY - ry - d - 6, 2 * (rx + d), ry + d,
                0, 0, 64, 32);
            drawTexturedRect(dhdCentreX - rx - d, dhdCentreY - 6, 2 * (rx + d), 0.5 * ry + d,
                0, 32, 64, 32);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }
    
    void drawEnteredSymbols() {
        drawAddressSymbols(width / 2, dhdTop - 80, getEnteredAddress());
    }
    
    void drawEnteredString() {
        String address = SGAddressing.padAddress(getEnteredAddress(), "|", addressLength);
        drawAddressString(width / 2, dhdTop - 20, address);
    }

}
