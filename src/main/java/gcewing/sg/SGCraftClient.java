//------------------------------------------------------------------------------------------------
//
//   SG Craft - Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

public class SGCraftClient extends BaseModClient<SGCraft> {

    public SGCraftClient(SGCraft mod) {
        super(mod);
    }
    
    @Override
    protected void registerScreens() {
        addScreen(SGGui.SGBase, SGBaseScreen.class);
        addScreen(SGGui.SGController, DHDScreen.class);
        addScreen(SGGui.DHDFuel, DHDFuelScreen.class);
        addScreen(SGGui.PowerUnit, PowerScreen.class);
    }

    @Override
    protected void registerTileEntityRenderers() {
        addTileEntityRenderer(SGBaseTE.class, new SGBaseTERenderer());
        addTileEntityRenderer(DHDTE.class, new DHDTERenderer());
    }
    
    @Override
    protected void registerEntityRenderers() {
        addEntityRenderer(IrisEntity.class, IrisRenderer.class);
    }

}
