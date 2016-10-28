//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Generic Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.Thread;

import static org.lwjgl.opengl.GL11.*;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.audio.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import net.minecraftforge.common.*;
import net.minecraftforge.client.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;

import net.minecraftforge.fml.client.*;
import net.minecraftforge.fml.client.registry.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.registry.*;

//import gcewing.sg.BaseMod.IBlock;
import gcewing.sg.BaseMod.*;
import static gcewing.sg.BaseUtils.setField;

public class BaseModClient<MOD extends BaseMod<? extends BaseModClient>> implements IGuiHandler {

//  static class IDBinding<T> {
//      public int id;
//      public T object;
//  }
//  
//  static class BRBinding extends IDBinding<ISimpleBlockRenderingHandler> {}
//  
//  static Map<String, BRBinding>
//      blockRenderers = new HashMap<String, BRBinding>();

    public boolean debugModelRegistration = false;

    MOD base;
    boolean customRenderingRequired;
    boolean debugSound = false;

    Map<Integer, Class<? extends GuiScreen>> screenClasses =
        new HashMap<Integer, Class<? extends GuiScreen>>();

    public BaseModClient(MOD mod) {
        base = mod;
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }
    
    public void preInit(FMLPreInitializationEvent e) {
        System.out.printf("BaseModClient.preInit\n");
        registerSavedVillagerSkins();
//         registerDummyStateMappers();
        for (BaseSubsystem sub : base.subsystems) {
            sub.registerBlockRenderers();
            sub.registerItemRenderers();
        }
        registerDefaultRenderers();
        registerDefaultModelLocations();
   }
    
    public void init(FMLInitializationEvent e) {
        System.out.printf("BaseModClient.init\n");
    }
    
    public void postInit(FMLPostInitializationEvent e) {
        System.out.printf("BaseModClient.postInit\n");
        for (BaseSubsystem sub : base.subsystems) {
            sub.registerModelLocations();
            sub.registerTileEntityRenderers();
            sub.registerEntityRenderers();
            sub.registerScreens();
            sub.registerOtherClient();
        }
        if (customRenderingRequired)
            enableCustomRendering();
    }
    
    void registerSavedVillagerSkins() {
        VillagerRegistry reg = VillagerRegistry.instance();
        for (VSBinding b : base.registeredVillagers)
            reg.registerVillagerSkin(b.id, b.object);
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
    
    protected void registerDefaultRenderers() {
        for (Block block : base.registeredBlocks) {
            if (block instanceof IBlock) {
                if (!blockRenderers.containsKey(block)) {
                    String name = ((IBlock)block).getQualifiedRendererClassName();
                    if (name != null) {
                        try {
                            Class cls = Class.forName(name);
                            addBlockRenderer(block, (ICustomRenderer)cls.newInstance());
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
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
    
    public interface ICustomRenderer {
        void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
            EnumWorldBlockLayer layer, Trans3 t);
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
    
    protected Map<Block, ICustomRenderer> blockRenderers = new HashMap<Block, ICustomRenderer>();
    protected Map<Item, ICustomRenderer> itemRenderers = new HashMap<Item, ICustomRenderer>();
    protected Map<IBlockState, ICustomRenderer> stateRendererCache = new HashMap<IBlockState, ICustomRenderer>();
    protected Map<ResourceLocation, ITexture> textureCache = new HashMap<ResourceLocation, ITexture>();
    
    //-------------- Renderer registration -------------------------------

    public void addBlockRenderer(Block block, ICustomRenderer renderer) {
        blockRenderers.put(block, renderer);
        customRenderingRequired = true;
        Item item = Item.getItemFromBlock(block);
        if (item != null)
            addItemRenderer(item, renderer);
    }
    
    public void addItemRenderer(Item item, ICustomRenderer renderer) {
        itemRenderers.put(item, renderer);
    }
    
    //--------------- Model Locations ----------------------------------------------------
    
    public ModelResourceLocation modelResourceLocation(String path, String variant) {
        return new ModelResourceLocation(base.resourceLocation(path), variant);
    }
    
    public void registerModelLocations() {
    }
    
//     protected void registerDummyStateMappers() {
//         for (Block block : base.registeredBlocks) {
//             if (blockNeedsCustomRendering(block)) {
//                 System.out.printf("BaseModClient: registering dummy state mapper for  %s\n", block.getUnlocalizedName());
//                 ModelLoader.setCustomStateMapper(block, dummyStateMapper);
//                 Item item = Item.getItemFromBlock(block);
//                 if (item != null) {
//                     System.out.printf("BaseModClient: registering empty variant list for %s\n", item.getUnlocalizedName());
//                     ModelBakery.registerItemVariants(item);
//                 }
//             }
//         }
//     }

    protected void registerDefaultModelLocations() {
        //CustomBlockRenderDispatch blockDisp = getCustomBlockRenderDispatch();
        CustomItemRenderDispatch itemDisp = getCustomItemRenderDispatch();
        for (Block block : base.registeredBlocks) {
            Item item = Item.getItemFromBlock(block);
            if (blockNeedsCustomRendering(block)) {
                //registerRenderDispatcherForBlock(blockReg, block, blockDisp);
                registerSmartModelsForBlock(block);
                if (item != null)
                    //registerRenderDispatcherForItem(itemReg, item, itemDisp);
                    registerRenderDispatcherForItem(item, itemDisp);
            }
            else
                registerInventoryLocationForItem(item, block.getUnlocalizedName());
        }
        for (Item item : base.registeredItems) {
            if (itemNeedsCustomRendering(item))
                registerRenderDispatcherForItem(item, itemDisp);
            else
                registerInventoryLocationForItem(item, item.getUnlocalizedName());
        }
    }
    
//     private void registerRenderDispatcherForBlock(BlockModelShapes reg, Block block, CustomBlockRenderDispatch disp) {
//      if (debugModelRegistration)
//          System.out.printf("BaseMod: Registering model location %s for %s\n", disp.modelLocation, block);
//      reg.registerBlockWithStateMapper(block, customBlockStateMapper);
//     }

    protected Map<ModelResourceLocation, IBakedModel> smartModels = new HashMap<>();

    protected void registerSmartModelsForBlock(Block block) {
        //reg.registerBlockWithStateMapper(block, customBlockStateMapper);
        ModelLoader.setCustomStateMapper(block, customBlockStateMapper);
        for (IBlockState state : block.getBlockState().getValidStates()) {
            ModelResourceLocation location = customBlockStateMapper.getModelResourceLocation(state);
            IBakedModel model = new BlockParticleModel(state);
            if (debugModelRegistration)
                System.out.printf("BaseModClient.registerSmartModelsForBlock: Squirreling %s --> %s\n", location, model);
            smartModels.put(location, model);
        }
    }
    
    protected class BlockParticleModel implements IBakedModel {

        protected IBlockState state;
        
        public BlockParticleModel(IBlockState state) {
            this.state = state;
        }
        
        public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {return null;}
        public List<BakedQuad> getGeneralQuads() {return null;}
        public boolean isAmbientOcclusion() {return false;}
        public boolean isGui3d() {return false;}
        public boolean isBuiltInRenderer() {return false;}
        public ItemCameraTransforms getItemCameraTransforms() {return null;}
        
        public TextureAtlasSprite getParticleTexture() {
            Block block = state.getBlock();
            if (block instanceof IBlock) {
                String[] textures = ((IBlock)block).getTextureNames();
                if (textures != null && textures.length > 0)
                    return getIcon(0, textures[0]);
            }
            return null;
        }
    
    }

    protected boolean blockNeedsCustomRendering(Block block) {
        return blockRenderers.containsKey(block) || specifiesTextures(block);
    }
    
    protected boolean itemNeedsCustomRendering(Item item) {
        return itemRenderers.containsKey(item) || specifiesTextures(item);
    }
    
    protected boolean specifiesTextures(Object obj) {
        return obj instanceof ITextureConsumer && ((ITextureConsumer)obj).getTextureNames() != null;
    }
    
    protected void registerRenderDispatcherForItem(Item item, CustomItemRenderDispatch disp) {
        registerModelLocationForSubtypes(item, disp.modelLocation);
    }

    protected void registerInventoryLocationForItem(Item item, String extdName) {
        String name = extdName.substring(5); // strip "item." or "tile."
        registerModelLocationForSubtypes(item, new ModelResourceLocation(name, "inventory"));
    }
    
    protected void registerModelLocationForSubtypes(Item item, ModelResourceLocation location) {
        int numVariants = 1;
        if (item.getHasSubtypes())
            numVariants = getNumItemSubtypes(item);
        if (debugModelRegistration)
            System.out.printf("BaseModClient: Registering model location %s for %d subtypes of %s\n",
                location, numVariants, item.getUnlocalizedName());
        for (int i = 0; i < numVariants; i++)
            ModelLoader.setCustomModelResourceLocation(item, i, location);
    }

    private CustomBlockStateMapper customBlockStateMapper = new CustomBlockStateMapper();

    protected static class CustomBlockStateMapper extends DefaultStateMapper {
        public ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return super.getModelResourceLocation(state);
        }
    };

    private int getNumBlockSubtypes(Block block) {
        if (block instanceof IBlock)
            return ((IBlock)block).getNumSubtypes();
        else
            return 1;
    }
    
    private int getNumItemSubtypes(Item item) {
        if (item instanceof IItem)
            return ((IItem)item).getNumSubtypes();
        else if (item instanceof ItemBlock)
            return getNumBlockSubtypes(Block.getBlockFromItem(item));
        else
            return 1;
    }

    //------------------------------------------------------------------------------------------------

    protected ICustomRenderer getCustomRenderer(IBlockAccess world, BlockPos pos, IBlockState state) {
        //System.out.printf("BaseModClient.getCustomRenderer: %s\n", state);
        Block block = state.getBlock();
        ICustomRenderer rend = blockRenderers.get(block);
        if (rend == null && block instanceof IBlock /*&& block.getRenderType() == -1*/) {
            IBlockState astate = block.getActualState(state, world, pos);
            rend = getCustomRendererForState(astate);
        }
        return rend;
    }
    
    protected ICustomRenderer getCustomRendererForSpec(int textureType, ModelSpec spec) {
//         System.out.printf("BaseModClient.getCustomRendererForSpec: %s\n", spec.modelName);
//         for (int i = 0; i < spec.textureNames.length; i++)
//           System.out.printf(" %s", spec.textureNames[i]);
//         System.out.printf("\n");
        IModel model = getModel(spec.modelName);
        ITexture[] textures = new ITexture[spec.textureNames.length];
        for (int i = 0; i < textures.length; i++)
            textures[i] = getTexture(textureType, spec.textureNames[i]);
//         System.out.printf("BaseModClient.getCustomRendererForSpec: model = %s\n", model);
//         for (int i = 0; i < spec.textureNames.length; i++)
//           System.out.printf("BaseModClient.getCustomRendererForSpec: texture[%s] = %s\n",
//               i, textures[i]);
        return new BaseModelRenderer(model, spec.origin, textures);
    }
    
    protected ICustomRenderer getCustomRendererForState(IBlockState astate) {
        ICustomRenderer rend = stateRendererCache.get(astate);
        if (rend == null) {
//             System.out.printf("BaseModClient.getCustomRendererForState: %s\n", astate);
            Block block = astate.getBlock();
            if (block instanceof IBlock) {
                ModelSpec spec = ((IBlock)block).getModelSpec(astate);
                if (spec != null) {
                    rend = getCustomRendererForSpec(0, spec);
                    stateRendererCache.put(astate, rend);
                }
            }
        }
        return rend;
    }
    
    public void renderBlockUsingModelSpec(IBlockAccess world, BlockPos pos, IBlockState state,
        IRenderTarget target, EnumWorldBlockLayer layer, Trans3 t)
    {
        ICustomRenderer rend = getCustomRendererForState(state);
        if (rend != null)
            rend.renderBlock(world, pos, state, target, layer, t);
    }
    
    // Call this from renderItemStack of an ICustomRenderer to fall back to model spec
    public void renderItemStackUsingModelSpec(ItemStack stack, IRenderTarget target, Trans3 t) {
        IBlockState state = BaseBlockUtils.getBlockStateFromItemStack(stack);
        IBlock block = (IBlock)state.getBlock();
        ModelSpec spec = block.getModelSpec(state);
        ICustomRenderer rend = getCustomRendererForSpec(0, spec);
        rend.renderItemStack(stack, target, t);
    }

    public IModel getModel(String name) {
        return base.getModel(name);
    }
    
    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    
    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return base.resourceLocation(texturePrefixes[type] + name);
    }

    public ITexture getTexture(int type, String name) {
        // Cache is keyed by resource locaton without "textures/"
        ResourceLocation loc = textureResourceLocation(type, name);
        return textureCache.get(loc);
    }
    
    public TextureAtlasSprite getIcon(int type, String name) {
        return ((BaseTexture.Sprite)getTexture(type, name)).icon;
    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre e) {
        //System.out.printf("BaseModClient.onTextureStitchEventPre: %s\n", e.map);
        textureCache.clear();
        for (Block block : base.registeredBlocks) {
            //System.out.printf("BaseModClient.onTextureStitchEvent: Block %s\n", block.getUnlocalizedName());
            registerSprites(0, e.map, block);
        }
        for (Item item : base.registeredItems)
            registerSprites(1, e.map, item);
    }
    
    protected void registerSprites(int textureType, TextureMap reg, Object obj) {
        if (debugModelRegistration)
            System.out.printf("BaseModClient.registerSprites: for %s\n", obj);
        if (obj instanceof ITextureConsumer) {
            String names[] = ((ITextureConsumer)obj).getTextureNames();
            if (debugModelRegistration)
                System.out.printf("BaseModClient.registerSprites: texture names = %s\n", (Object)names);
            if (names != null) {
                customRenderingRequired = true;
                for (String name : names) {
                    ResourceLocation loc = textureResourceLocation(textureType, name);
                    if (textureCache.get(loc) == null) {
                        TextureAtlasSprite icon = reg.registerSprite(loc);
                        ITexture texture = BaseTexture.fromSprite(icon);
                        textureCache.put(loc, texture);
                    }
                }
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------

    protected class CustomBlockRendererDispatcher extends BlockRendererDispatcher {
    
        protected BlockRendererDispatcher base;
    
        public CustomBlockRendererDispatcher(BlockRendererDispatcher base) {
            super(null, null);
            this.base = base;
        }
        
        @Override public BlockModelShapes getBlockModelShapes()
            {return base.getBlockModelShapes();}
        @Override public BlockModelRenderer getBlockModelRenderer()
            {return base.getBlockModelRenderer();}
        @Override public IBakedModel getModelFromBlockState(IBlockState state, IBlockAccess world, BlockPos pos)
            {return base.getModelFromBlockState(state, world, pos);}
        @Override public void renderBlockBrightness(IBlockState state, float brightness)
            {base.renderBlockBrightness(state, brightness);}
        @Override public boolean isRenderTypeChest(Block block, int i)
            {return base.isRenderTypeChest(block, i);}

        @Override
        public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite icon, IBlockAccess world) {
            ICustomRenderer rend = getCustomRenderer(world, pos, state);
            if (rend != null) {
                BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos, icon);
                Trans3 t = Trans3.blockCenter;
                Block block = state.getBlock();
                for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.values())
                    if (block.canRenderInLayer(layer))
                        rend.renderBlock(world, pos, state, target, layer, t);
                IBakedModel model = target.getBakedModel();
                WorldRenderer tess = Tessellator.getInstance().getWorldRenderer();
                getBlockModelRenderer().renderModel(world, model, state, pos, tess);
            }
            else
                base.renderBlockDamage(state, pos, icon, world);
        }

//      @Override
//      public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite damageIcon, IBlockAccess world) {
//          ICustomRenderer rend = getCustomRenderer(world, pos, state);
//          if (rend != null) {
//              BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos);
//              Block block = state.getBlock();
//              Trans3 t = Trans3.blockCenter;
//              for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.values())
//                  if (block.canRenderInLayer(layer))
//                      rend.renderBlock(world, pos, state, target, layer, t);
//              TextureAtlasSprite particle = getBlockModelShapes().getTexture(getBlockParticleState(state, world, pos));
//              IBakedModel model = target.getBakedModel(particle);
//              IBakedModel damageModel = (new SimpleBakedModel.Builder(model, damageIcon)).makeBakedModel();
//              WorldRenderer tess = Tessellator.getInstance().getWorldRenderer();
//              getBlockModelRenderer().renderModel(world, damageModel, state, pos, tess);
//          }
//          else
//              base.renderBlockDamage(state, pos, damageIcon, world);
//      }

//      @Override
//      public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite damageIcon, IBlockAccess world) {
//          base.renderBlockDamage(state, pos, damageIcon, world);
//      }

        @Override
        public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess world, WorldRenderer tess) {
            ICustomRenderer rend = getCustomRenderer(world, pos, state);
            if (rend != null)
                return customRenderBlockToWorld(world, pos, state, tess, null, rend);
            else
                return base.renderBlock(state, pos, world, tess);
        }
        
    }
    
    protected boolean customRenderBlockToWorld(IBlockAccess world, BlockPos pos, IBlockState state, WorldRenderer tess,
        TextureAtlasSprite icon, ICustomRenderer rend)
    {
        //System.out.printf("BaseModClient.customRenderBlock: %s\n", state);
        BaseWorldRenderTarget target = new BaseWorldRenderTarget(world, pos, tess, icon);
        EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
        rend.renderBlock(world, pos, state, target, layer, Trans3.blockCenter(pos));
        return target.end();
    }
    
    protected IBakedModel customRenderBlockToBakedModel(IBlockAccess world, BlockPos pos, IBlockState state,
        ICustomRenderer rend)
    {
        BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos);
        Trans3 t = Trans3.blockCenter;
        EnumWorldBlockLayer layer = MinecraftForgeClient.getRenderLayer();
        BlockModelShapes shapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        TextureAtlasSprite particle = shapes.getTexture(getBlockParticleState(state, world, pos));
        rend.renderBlock(world, pos, state, target, layer, t);
        return target.getBakedModel(particle);
    }

    public IBlockState getBlockParticleState(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof IBlock)
            return ((IBlock)block).getParticleState(world, pos);
        else
            return block.getActualState(state, world, pos);
    }
    
    public boolean renderAlternateBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target)
    {
        Block block = state.getBlock();
        if (!block.hasTileEntity(state)) {
            try {
                BlockRendererDispatcher disp = getCustomBlockRendererDispatcher();
                WorldRenderer tess = ((BaseWorldRenderTarget)target).getWorldRenderer();
                return disp.renderBlock(state, pos, world, tess);
            }
            catch (Exception e) {
                // Some blocks are averse to being abused this way. Try to avoid crashing in that case.
                return false;
            }
        }
        return false;
    }

    //------------------------------------------------------------------------------------------------

    protected class CustomRenderDispatch implements IBakedModel {
    
        public ModelResourceLocation modelLocation;
        
        public void install(ModelBakeEvent event) {
            if (debugModelRegistration)
                System.out.printf("BaseModClient: Installing %s at %s\n", this, modelLocation);
            event.modelRegistry.putObject(modelLocation, this);
        }
    
        // ----- IBakedModel -----
        
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {return null;}
    public List<BakedQuad> getGeneralQuads() {return null;}
    public boolean isAmbientOcclusion() {return false;}
    public boolean isGui3d() {return false;}
    public boolean isBuiltInRenderer() {return false;}
    public TextureAtlasSprite getParticleTexture() {return null;}
    public ItemCameraTransforms getItemCameraTransforms() {return null;}
        
    }
    
    //------------------------------------------------------------------------------------------------
    
//      protected class CustomBlockRenderDispatch extends CustomRenderDispatch implements ISmartBlockModel {
//  
//          private List<BakedQuad> emptyQuads = new ArrayList<BakedQuad>();
//          private List<List<BakedQuad>> emptyFaceQuads = new ArrayList<List<BakedQuad>>();
//          private IBakedModel emptyBakedModel = new SimpleBakedModel(emptyQuads, emptyFaceQuads, false, false, null, null);
//  
//          public CustomBlockRenderDispatch() {
//              modelLocation = modelResourceLocation("__custblock__", "");
//          }
//      
//          // ----- ISmartBlockModel -----
//  
//          public IBakedModel handleBlockState(IBlockState state) {
//              //System.out.printf("CustomBlockRenderDispatch.handleBlockState: %s\n", state);
//              if (state instanceof BaseBlockState) {
//                  BaseBlockState bstate = (BaseBlockState)state;
//                  ICustomRenderer rend = getCustomRenderer(bstate.world, bstate.pos, state);
//                  if (rend == null)
//                      throw new RuntimeException(String.format("Could not find custom renderer for %s", state));
//                  return customRenderBlockToBakedModel(bstate.world, bstate.pos, state, rend);
//              }
//              else
//                  throw new RuntimeException(String.format(
//                      "BaseModClient: Block with custom renderer did not return a BaseBlockState from getExtendedState(): %s",
//                      state));
//          }
//      
//      }
    
    //------------------------------------------------------------------------------------------------
    
    protected static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);

    protected class CustomItemRenderDispatch extends CustomRenderDispatch implements ISmartItemModel {
    
        public CustomItemRenderDispatch() {
            modelLocation = modelResourceLocation("__custitem__", "");
        }
        
        // ----- ISmartItemModel -----
        
        public IBakedModel handleItemState(ItemStack stack) {
            //System.out.printf("BaseModClient.CustomItemRenderDispatch.handleItemState: %s\n", stack);
            Item item = stack.getItem();
            ICustomRenderer rend = itemRenderers.get(item);
            if (rend == null && item instanceof IItem) {
                ModelSpec spec = ((IItem)item).getModelSpec(stack);
                if (spec != null)
                    rend = getCustomRendererForSpec(1, spec);
            }
            if (rend == null) {
                Block block = Block.getBlockFromItem(item);
                if (block != null)
                    rend = getCustomRendererForState(block.getDefaultState());
            }
            if (rend != null) {
//                 System.out.printf("CustomItemRenderDispatch.handleItemState: %s: Rendering with %s\n",
//                     stack, rend);
                GlStateManager.shadeModel(GL_SMOOTH);
                BaseBakedRenderTarget target = new BaseBakedRenderTarget();
                rend.renderItemStack(stack, target, itemTrans);
                return target.getBakedModel();
            }
            else
                return null;
        }
        
    }

    //------------------------------------------------------------------------------------------------

    protected CustomBlockRendererDispatcher customBlockRendererDispatcher;
//     protected CustomBlockRenderDispatch customBlockRenderDispatch;
    protected CustomItemRenderDispatch customItemRenderDispatch;
    
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        if (debugModelRegistration)
            System.out.printf("BaseModClient.ModelBakeEvent\n");
        //getCustomBlockRenderDispatch().install(event);
        getCustomItemRenderDispatch().install(event);
        for (Map.Entry<ModelResourceLocation, IBakedModel> e : smartModels.entrySet()) {
            if (debugModelRegistration)
                System.out.printf("BaseModClient.onModelBakeEvent: Installing %s --> %s\n", e.getKey(), e.getValue());
            event.modelRegistry.putObject(e.getKey(), e.getValue());
        }
    }
    
    protected CustomBlockRendererDispatcher getCustomBlockRendererDispatcher() {
        if (customBlockRendererDispatcher == null) {
            Minecraft mc = Minecraft.getMinecraft();
            customBlockRendererDispatcher = new CustomBlockRendererDispatcher(mc.getBlockRendererDispatcher());
            setField(mc, "blockRenderDispatcher", "field_175618_aM", customBlockRendererDispatcher);
        }
        return customBlockRendererDispatcher;
    }
    
//      protected CustomBlockRenderDispatch getCustomBlockRenderDispatch() {
//          if (customBlockRenderDispatch == null)
//              customBlockRenderDispatch = new CustomBlockRenderDispatch();
//          return customBlockRenderDispatch;
//      }

    protected CustomItemRenderDispatch getCustomItemRenderDispatch() {
        if (customItemRenderDispatch == null)
            customItemRenderDispatch = new CustomItemRenderDispatch();
        return customItemRenderDispatch;
    }

    public void enableCustomRendering() {
        getCustomBlockRendererDispatcher();
    }
    
    protected static class DummyStateMapper implements IStateMapper {
        private static Map<IBlockState, ModelResourceLocation> emptyMap =
            new HashMap<>();
        public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
            return emptyMap;
        }
    }
    
    protected static IStateMapper dummyStateMapper = new DummyStateMapper();

}
