// ------------------------------------------------------------------------------------------------
//
// SG Craft - Stargate controller gui screen
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseBlockUtils.getWorldTileEntity;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ENABLE_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class DHDScreen extends SGScreen {

    final static int dhdWidth = 320;
    final static int dhdHeight = 120;
    final static double dhdRadius1 = dhdWidth * 0.1;
    final static double dhdRadius2 = dhdWidth * 0.275;
    final static double dhdRadius3 = dhdWidth * 0.45;

    World world;
    BlockPos pos;
    int dhdTop, dhdCentreX, dhdCentreY;
    int closingDelay = 0;
    int addressLength;
    DHDTE cte;

    public DHDScreen(EntityPlayer player, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
        cte = getControllerTE();
        SGBaseTE te = getStargateTE();
        if (te != null) addressLength = te.getNumChevrons();
    }

    SGBaseTE getStargateTE() {
        if (cte != null) return cte.getLinkedStargateTE();
        else return null;
    }

    DHDTE getControllerTE() {
        TileEntity te = getWorldTileEntity(world, pos);
        if (te instanceof DHDTE) return (DHDTE) te;
        else return null;
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
        int x = -(mx - dhdCentreX);
        int y = -(my - dhdCentreY);
        // Check top half of orange dome
        if (y > 0 && Math.hypot(x, y) <= dhdRadius1) return 0;
        // Scale to circular coords and check rest of buttons
        y = y * dhdWidth / dhdHeight;
        double r = Math.hypot(x, y);
        if (r > dhdRadius3) return -1;
        if (r <= dhdRadius1) return 0;
        double a = Math.toDegrees(Math.atan2(y, x));
        if (a < 0) a += 360;
        int i0, nb;
        if (r > dhdRadius2) {
            i0 = 1;
            nb = 26;
        } else {
            i0 = 27;
            nb = 11;
        }
        return i0 + (int) Math.floor(a * nb / 360);
    }

    void dhdButtonPressed(int i) {
        buttonSound();
        if (i == 0) orangeButtonPressed(false);
        else if (i >= 37) backspace();
        else enterCharacter(SGBaseTE.symbolToChar(i - 1));
    }

    void buttonSound() {
        EntityPlayer player = mc.thePlayer;
        ISound sound = new PositionedSoundRecord(
                new ResourceLocation("random.click"),
                1.0F,
                1.0F,
                (float) player.posX,
                (float) player.posY,
                (float) player.posZ);
        mc.getSoundHandler().playSound(sound);
    }

    @Override
    public void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE) close();
        else if (key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE) backspace();
        else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) orangeButtonPressed(true);
        else {
            String C = String.valueOf(c).toUpperCase();
            if (SGAddressing.isValidSymbolChar(C)) enterCharacter(C.charAt(0));
        }
    }

    void orangeButtonPressed(boolean connectOnly) {
        SGBaseTE te = getStargateTE();
        if (te != null) {
            if (te.state == SGState.Idle) sendConnectOrDisconnect(te, getEnteredAddress());
            else if (!connectOnly) sendConnectOrDisconnect(te, "");
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
            if (n > 0) setEnteredAddress(a.substring(0, n - 1));
        }
    }

    void enterCharacter(char c) {
        if (stargateIsIdle()) {
            buttonSound();
            String a = getEnteredAddress();
            int n = a.length();
            if (n < addressLength) setEnteredAddress(a + c);
        }
    }

    boolean stargateIsIdle() {
        SGBaseTE te = getStargateTE();
        return (te != null && te.state == SGState.Idle);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        SGBaseTE te = getStargateTE();
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
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
        if (te == null || !te.isMerged) setColor(0.2, 0.2, 0.2);
        else if (connected) setColor(1.0, 0.5, 0.0);
        else setColor(0.5, 0.25, 0.0);
        double rx = dhdWidth * 48 / 512.0;
        double ry = dhdHeight * 48 / 256.0;
        drawTexturedRect(dhdCentreX - rx, dhdCentreY - ry - 6, 2 * rx, 1.5 * ry, 64, 0, 64, 48);
        resetColor();
        if (connected) {
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            double d = 5;
            drawTexturedRect(dhdCentreX - rx - d, dhdCentreY - ry - d - 6, 2 * (rx + d), ry + d, 0, 0, 64, 32);
            drawTexturedRect(dhdCentreX - rx - d, dhdCentreY - 6, 2 * (rx + d), 0.5 * ry + d, 0, 32, 64, 32);
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
