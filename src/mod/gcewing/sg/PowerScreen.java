//------------------------------------------------------------------------------------------------
//
//   SG Craft - Power unit gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CURRENT_BIT;
import static org.lwjgl.opengl.GL11.GL_ENABLE_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPushAttrib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.text.DecimalFormat;

public class PowerScreen extends BaseGui.Screen {

    final static int guiWidth = 148;
    final static int guiHeight = 64;
    private final DecimalFormat dFormat = new DecimalFormat("###,###,###");

    private PowerTE te;

    public static PowerScreen create(EntityPlayer player, World world, BlockPos pos) {
        PowerContainer container = PowerContainer.create(player, world, pos);
        if (container != null)
            return new PowerScreen(container);
        else
            return null;
    }

    private PowerScreen(PowerContainer container) {
        super(container, guiWidth, guiHeight);
        this.te = container.te;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/power_gui.png"), 148, 64);
        drawTexturedRect(0, 0, guiWidth, guiHeight, 0, 0);
        int cx = this.xSize / 2;
        drawCenteredString(te.getScreenTitle(), cx, 8);
        drawRightAlignedString(te.getUnitName() + ":", 90, 23);
        drawRightAlignedString(this.dFormat.format(Math.min(Math.max(this.te.energyBuffer, 0), this.te.energyMax)), 141, 23);
        if (SGCraft.displayGuiPowerDebug) {
            drawRightAlignedString("SGPU:", 90, 34);
            drawRightAlignedString(this.dFormat.format(Math.min(Math.max(this.te.energyBuffer / this.te.energyPerSGEnergyUnit, 0), this.te.energyMax)), 141, 34);
        }
        drawRightAlignedString("Max:", 90, 45);
        drawRightAlignedString(this.dFormat.format(this.te.energyMax), 141, 45);
        drawPowerGauge();
    }

    private void drawPowerGauge() {
        gSave();
        glPushAttrib(GL_ENABLE_BIT | GL_CURRENT_BIT);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        setColor(1, 0, 0);
        drawRect(23, 28, 29 * this.te.energyBuffer / this.te.energyMax, 10);
        glBlendFunc(GL_ONE, GL_ZERO);
        glPopAttrib();
        gRestore();
    }

}
