//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate controller fuelling gui screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import org.lwjgl.opengl.*;

import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import java.text.DecimalFormat;

public class DHDFuelScreen extends SGScreen {

    private final String screenTitle = "Stargate Controller";
    final static int guiWidth = 256;
    final static int guiHeight = 208;
    private final int fuelGaugeWidth = 16;
    private final int fuelGaugeHeight = 34;
    private final int fuelGaugeX = 214;
    private final int fuelGaugeY = 84;
    private final int fuelGaugeU = 0;
    private final int fuelGaugeV = 208;
    private final DecimalFormat dFormat = new DecimalFormat("###,###,###");

    private DHDTE te;
    private SGBaseTE baseTe;
    private double energyPerFuelItem;

    public static DHDFuelScreen create(EntityPlayer player, World world, BlockPos pos) {
        DHDTE te = DHDTE.at(world, pos);
        if (te != null) {
            return new DHDFuelScreen(player, te);
        } else {
            return null;
        }
    }

    public DHDFuelScreen(EntityPlayer player, DHDTE te) {
        super(new DHDFuelContainer(player, te), guiWidth, guiHeight);
        this.te = te;
        this.baseTe = te.getLinkedStargateTE();
        this.energyPerFuelItem = SGBaseTE.energyPerFuelItem;
    }

    @Override
    protected void drawBackgroundLayer() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/dhd_fuel_gui.png"), 256, 256);

        drawTexturedRect(0, 0, this.guiWidth, this.guiHeight, 0, 0);
        drawFuelGauge();
        int cx = this.xSize / 2;
        setTextColor(0x004c66);
        drawCenteredString(this.screenTitle, cx, 8);

        int naquadahUnits = this.te.getInventory().getStackInSlot(0).getCount() + this.te.getInventory().getStackInSlot(1).getCount() + this.te.getInventory().getStackInSlot(2).getCount() + this.te.getInventory().getStackInSlot(3).getCount();

        if (SGCraft.displayGuiPowerDebug) {
            if (this.baseTe != null) {
                // DHD Buffer Available
                drawRightAlignedString("Gate Buffer:", 125, 30);
                drawString(dFormat.format(Math.min(Math.max(this.baseTe.energyInBuffer, 0), this.baseTe.maxEnergyBuffer)), 130, 30);
            }

            // DHD Buffer Available
            drawRightAlignedString("DHD Buffer:", 125, 40);
            drawString(dFormat.format(Math.min(Math.max(this.te.energyInBuffer, 0), this.te.maxEnergyBuffer)), 130, 40);

            // Buffer Max
            drawRightAlignedString("Buffer Max:", 125, 50);
            drawString(dFormat.format(this.te.maxEnergyBuffer), 130, 50);

            // Naquadah Units
            drawRightAlignedString("Naquadah:", 125, 60);
            drawString(dFormat.format(naquadahUnits), 130, 60);

            // Naquadah Power Units
            drawRightAlignedString("Available Power Units:", 125, 70);
            drawString(dFormat.format(naquadahUnits * this.energyPerFuelItem), 130, 70);
        }

        if (this.te.numFuelSlots > 0)
            drawString("Fuel", 150, 96);
    }

    private void drawFuelGauge() {
        int level = (int)(this.fuelGaugeHeight * this.te.energyInBuffer / this.te.maxEnergyBuffer);
        if (level > this.fuelGaugeHeight)
            level = this.fuelGaugeHeight;
        GL11.glEnable(GL11.GL_BLEND);
        drawTexturedRect(this.fuelGaugeX, this.fuelGaugeY + this.fuelGaugeHeight - level,
            this.fuelGaugeWidth, level, this.fuelGaugeU, this.fuelGaugeV);
        GL11.glDisable(GL11.GL_BLEND);
    }
}