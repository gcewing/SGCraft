//------------------------------------------------------------------------------------------------
//
//   SG Craft - Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

// import gcewing.sg.ic2.*; //[IC2]

public class SGCraftClient extends BaseModClient<SGCraft> {

    public SGCraftClient(SGCraft mod) {
        super(mod);
        //debugSound = true;
        //debugModelRegistration = true;
    }
    
    @Override
    protected void registerScreens() {
        //System.out.printf("SGCraft: ProxyClient.registerScreens\n");
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
        addEntityRenderer(EntityStargateIris.class, IrisRenderer.class);
    }

}
