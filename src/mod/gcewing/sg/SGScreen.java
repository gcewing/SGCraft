//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate gui base class
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.inventory.Container;

import static org.lwjgl.opengl.GL11.*;

//------------------------------------------------------------------------------------------------

public class SGScreen extends BaseGui.Screen {

    final static String symbolTextureFile = "symbols48.png";
    final static int symbolsPerRowInTexture = 10;
    final static int USIZSE = 256;
    final static int VSIZSE = 128;
    final static int frameHeight = 44;
    final static int cellSize = 24;


    public SGScreen() {
        super(new BaseContainer(0, 0));
    }

    public SGScreen(Container container, int width, int height) {
        super(container, width, height);
    }

    void drawAddressSymbols(int x, int y, String address) {
        int x0 = x - address.length() * cellSize / 2;
        int y0 = y + frameHeight / 2 - cellSize / 2;
        bindSGTexture();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        int n = address.length();
        for (int i = 0; i < n; i++) {
            int s = SGBaseTE.charToSymbol(address.charAt(i));
            int row = s / symbolsPerRowInTexture;
            int col = s % symbolsPerRowInTexture;
            drawTexturedRect(x0 + i * cellSize, y0, cellSize, cellSize,
                    col * cellSize, row * cellSize);
        }
    }

    void drawAddressString(int x, int y, String address) {
        drawCenteredString(this.fontRendererObj, address, x, y, 0xffffff);
    }

    void bindSGTexture() {
        bindTexture(SGCraft.mod.resourceLocation("textures/gui/" + SGScreen.symbolTextureFile), USIZSE, VSIZSE);
    }

}
