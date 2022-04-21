//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Generic Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.BaseMod.ITextureConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import gcewing.sg.BaseMod.IBlock;

public class BaseModClient<MOD extends BaseMod<? extends BaseModClient>> implements IGuiHandler {

    MOD base;
    boolean debugSound = false;

    Map<Integer, Class<? extends GuiScreen>> screenClasses =
        new HashMap<Integer, Class<? extends GuiScreen>>();

    public BaseModClient(MOD mod) {
        base = mod;
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }
    
    public void preInit(FMLPreInitializationEvent e) {
        //System.out.printf("BaseModClient.preInit\n");
        registerSavedVillagerSkins();
        for (BaseSubsystem sub : base.subsystems) {
            sub.registerBlockRenderers();
            sub.registerItemRenderers();
        }
        if (renderingManager == null && renderingManagerRequired())
            getRenderingManager();
        if (renderingManager != null)
            renderingManager.preInit();
   }
    
    public void init(FMLInitializationEvent e) {
        //System.out.printf("BaseModClient.init\n");
    }
    
    public void postInit(FMLPostInitializationEvent e) {
        //System.out.printf("BaseModClient.postInit\n");
        for (BaseSubsystem sub : base.subsystems) {
            sub.registerModelLocations();
            sub.registerTileEntityRenderers();
            sub.registerEntityRenderers();
            sub.registerScreens();
            sub.registerOtherClient();
        }
        if (renderingManager != null)
            renderingManager.postInit();
    }
    
    void registerSavedVillagerSkins() {
//         VillagerRegistry reg = VillagerRegistry.instance();
//         for (VSBinding b : base.registeredVillagers)
//             reg.registerVillagerSkin(b.id, b.object);
    }
        
//  String qualifyName(String name) {
//      return getClass().getPackage().getName() + "." + name;
//  }
    
    void registerOther() {}
    
    //-------------- Screen registration --------------------------------------------------------
    
    void registerScreens() {
        //
        //  Make calls to addScreen() here.
        //
        //  Screen classes registered using these methods must implement one of:
        //
        //  (1) A static method create(EntityPlayer, World, int x, int y, int z)
        //  (2) A constructor MyScreen(EntityPlayer, World, int x, int y, int z)
        //  (3) A constructor MyScreen(MyContainer) where MyContainer is the
        //      corresponding registered container class
        //
        //System.out.printf("%s: BaseModClient.registerScreens\n", this);
    }
    
    public void addScreen(Enum id, Class<? extends GuiScreen> cls) {
        addScreen(id.ordinal(), cls);
    }

    public void addScreen(int id, Class<? extends GuiScreen> cls) {
        screenClasses.put(id, cls);
    }
    
    //-------------- Renderer registration --------------------------------------------------------
    
//     void registerRenderers() {
//         // Make calls to addBlockRenderer(), addItemRenderer() and addTileEntityRenderer() here
//     }
    
    protected void registerBlockRenderers() {}
    protected void registerItemRenderers() {}
    protected void registerEntityRenderers() {}
    protected void registerTileEntityRenderers() {}

    public void addTileEntityRenderer(Class <? extends TileEntity> teClass, TileEntitySpecialRenderer renderer) {
        ClientRegistry.bindTileEntitySpecialRenderer(teClass, renderer);
    }
    
    public void addEntityRenderer(Class<? extends Entity> entityClass, Render renderer) {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, renderer);
    }
    
    public void addEntityRenderer(Class<? extends Entity> entityClass, Class<? extends Render> rendererClass) {
        Object renderer;
        try {
            //Constructor ctor = rendererClass.getConstructor(RenderManager.class);
            //renderer = ctor.newInstance(Minecraft.getMinecraft().getRenderManager());
            renderer = rendererClass.newInstance();
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        addEntityRenderer(entityClass, (Render)renderer);
    }
    
    //-------------- Client-side guis ------------------------------------------------
    
    public static void openClientGui(GuiScreen gui) {
        FMLClientHandler.instance().getClient().displayGuiScreen(gui);
    }
    
    //-------------- Rendering --------------------------------------------------------
    
    public ResourceLocation textureLocation(String path) {
        return base.resourceLocation("textures/" + path);
    }
    
    public void bindTexture(String path) {
        bindTexture(textureLocation(path));
    }
    
    public static void bindTexture(ResourceLocation rsrc) {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();
        tm.bindTexture(rsrc);
    }
    
    //-------------- GUI - Internal --------------------------------------------------------
    
    /**
     * Returns a Container to be displayed to the user. 
     * On the client side, this needs to return a instance of GuiScreen
     * On the server side, this needs to return a instance of Container
     *
     * @param ID The Gui ID Number
     * @param player The player viewing the Gui
     * @param world The current world
     * @param pos Position in world
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */
    
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return base.getServerGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return getClientGuiElement(id, player, world, new BlockPos(x, y, z));
    }

    public Object getClientGuiElement(int id, EntityPlayer player, World world, BlockPos pos) {
        int param = id >> 16;
        id = id & 0xffff;
        Object result = null;
        if (base.debugGui)
            System.out.printf("BaseModClient.getClientGuiElement: for id %s\n", id);
        Class scrnCls = screenClasses.get(id);
        if (scrnCls != null) {
            if (base.debugGui)
                System.out.printf("BaseModClient.getClientGuiElement: Instantiating %s\n", scrnCls);
            // If there is a container class registered for this gui and the screen class has
            // a constructor taking it, instantiate the screen automatically.
            Class contCls = base.containerClasses.get(id);
            if (contCls != null) {
                try {
                    if (base.debugGui)
                        System.out.printf("BaseModClient.getClientGuiElement: Looking for constructor taking %s\n", contCls);
                    Constructor ctor = scrnCls.getConstructor(contCls);
                    if (base.debugGui)
                        System.out.printf("BaseModClient.getClientGuiElement: Instantiating container\n");
                    Object cont = base.createGuiElement(contCls, player, world, pos, param);
                    if (cont != null) {
                        if (base.debugGui)
                            System.out.printf("BaseModClient.getClientGuiElement: Instantiating screen with container\n");
                        try {
                            result = ctor.newInstance(cont);
                        }
                        catch (Exception e) {
                            //throw new RuntimeException(e);
                            base.reportExceptionCause(e);
                            return null;
                        }
                    }
                }
                catch (NoSuchMethodException e) {
                }
            }
            // Otherwise, contruct screen from player, world, pos.
            if (result == null)
                result = base.createGuiElement(scrnCls, player, world, pos, param);
        }
        else {
            result = getGuiScreen(id, player, world, pos, param);
        }
        base.setModOf(result);
        if (base.debugGui)
            System.out.printf("BaseModClient.getClientGuiElement: returning %s\n", result);
        return result;
    }
    
    GuiScreen getGuiScreen(int id, EntityPlayer player, World world, BlockPos pos, int param) {
        //  Called when screen id not found in registry
        System.out.printf("%s: BaseModClient.getGuiScreen: No GuiScreen class found for gui id %d\n", 
            this, id);
        return null;
    }

    //======================================= Custom Rendering =======================================
    
    public interface IRenderingManager {
        void preInit();
        void postInit();
        void addBlockRenderer(Block block, ICustomRenderer renderer);
        void addItemRenderer(Item item, ICustomRenderer renderer);
        IModel getModel(String name);
    }
    
    public interface ICustomRenderer {
        void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
            BlockRenderLayer layer, Trans3 t);
        void renderItemStack(ItemStack stack, IRenderTarget target, Trans3 t);
    }
    
    public interface ITexture {
        ResourceLocation location();
        int tintIndex();
        double red();
        double green();
        double blue();
        double interpolateU(double u);
        double interpolateV(double v);
        boolean isEmissive();
        boolean isProjected();
        boolean isSolid();
        ITexture tinted(int index);
        ITexture colored(double red, double green, double blue);
        ITexture projected();
        ITexture emissive();
        ITiledTexture tiled(int numRows, int numCols);
    }
    
    public interface ITiledTexture extends ITexture {
        ITexture tile(int row, int col);
    }

    public interface IRenderTarget {
        boolean isRenderingBreakEffects();
        void setTexture(ITexture texture);
        void setColor(double r, double g, double b, double a);
        void setNormal(Vector3 n);
        void beginTriangle();
        void beginQuad();
        void addVertex(Vector3 p, double u, double v);
        void addProjectedVertex(Vector3 p, EnumFacing face);
        void endFace();
    }
    
    public interface IModel {
        AxisAlignedBB getBounds();
        void addBoxesToList(Trans3 t, List list);
        void render(Trans3 t, IRenderTarget renderer, ITexture... textures);
    }
    
    //-------------- Renderering  --------------------------------------------------------

    public void addBlockRenderer(Block block, ICustomRenderer renderer) {
        getRenderingManager().addBlockRenderer(block, renderer);
    }
    
    public void addItemRenderer(Item item, ICustomRenderer renderer) {
        getRenderingManager().addItemRenderer(item, renderer);
    }
    
    public IModel getModel(String name) {
        return getRenderingManager().getModel(name);
    }

    //--------------- Model Locations ----------------------------------------------------
    
    public ModelResourceLocation modelResourceLocation(String path, String variant) {
        return new ModelResourceLocation(base.resourceLocation(path), variant);
    }
    
    public void registerModelLocations() {
    }
    
    //--------------- Rendering - Internal ----------------------------------------------

    protected IRenderingManager renderingManager;
    
    protected Class lookForClass(String name) {
        try {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected String[] renderingManagerClasses = {
        "gcewing.sg.BaseAORenderingManager",
        "gcewing.sg.BaseRenderingManager"
    };

    protected IRenderingManager getRenderingManager() {
        if (renderingManager != null)
            return renderingManager;
        for (String name : renderingManagerClasses) {
            Class cls = lookForClass(name);
            if (cls != null) {
                try {
                    Constructor con = cls.getConstructor(BaseModClient.class);
                    renderingManager = (IRenderingManager)con.newInstance(this);
                    return renderingManager;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }   
        }
        throw new RuntimeException("No rendering manager found in package gcewing.sg");
    }

    protected boolean renderingManagerRequired() {
        for (Block block : base.registeredBlocks)
            if (objectNeedsCustomRendering(block))
                return true;
        for (Item item : base.registeredItems)
            if (objectNeedsCustomRendering(item))
                return true;
        return false;
    }
    
    protected boolean objectNeedsCustomRendering(Object obj) {
        return obj instanceof ITextureConsumer && ((ITextureConsumer)obj).getTextureNames() != null;
    }

}
