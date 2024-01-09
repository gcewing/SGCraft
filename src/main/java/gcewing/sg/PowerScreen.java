// ------------------------------------------------------------------------------------------------
//
// SG Craft - Power unit gui screen
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PowerScreen extends BaseGui.Screen {

    final static int guiWidth = 128;
    final static int guiHeight = 64;
    // final static String screenTitle = "IC2 SGPU";

    PowerTE te;

    public static PowerScreen create(EntityPlayer player, World world, BlockPos pos) {
        PowerContainer container = PowerContainer.create(player, world, pos);
        if (container != null) return new PowerScreen(container);
        else return null;
    }

    public PowerScreen(PowerContainer container) {
        super(container, guiWidth, guiHeight);
        this.te = container.te;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/power_gui.png"), 128, 64);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);
        int cx = xSize / 2;
        // textColor = 0x004c66;
        drawCenteredString(te.getScreenTitle(), cx, 8);
        drawRightAlignedString(te.getUnitName(), 72, 28);
        drawRightAlignedString(String.format("%.0f", te.energyBuffer), 121, 28);
        drawRightAlignedString("Max", 72, 42);
        drawRightAlignedString(String.format("%.0f", te.energyMax), 121, 42);
        drawPowerGauge();
    }

    void drawPowerGauge() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        setColor(1, 0, 0);
        drawRect(19, 27, 25 * te.energyBuffer / te.energyMax, 10);
    }

}
