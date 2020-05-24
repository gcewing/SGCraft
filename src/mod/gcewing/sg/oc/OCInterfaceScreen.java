//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers Interface GUI Screen
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import gcewing.sg.*;

public class OCInterfaceScreen extends BaseGui.Screen {

    final static int bgUSize = 256;
    final static int bgVSize = 128;

    public OCInterfaceScreen(OCInterfaceContainer container) {
        super(container);
    }
    
    @Override
    protected void drawBackgroundLayer() {
        bindTexture("gui/oc_sg_interface_gui.png", bgUSize, bgVSize);
        drawTexturedRect(0, 0, xSize, ySize, 0, 0);
        drawCenteredString("OC Stargate Interface", xSize / 2, 5);
    }
        
}
