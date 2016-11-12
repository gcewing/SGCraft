//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Rendering Manager
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.net.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import net.minecraftforge.client.event.*;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.FMLCommonHandler;

import gcewing.sg.BaseMod.*;
import gcewing.sg.BaseModClient.*;
import static gcewing.sg.BaseReflectionUtils.setField;

public class BaseRenderingManager<MOD extends BaseMod<? extends BaseModClient>> implements IRenderingManager {

    public boolean debugRenderingManager = false;
    public boolean debugModelRegistration = false;
    
    protected BaseModClient<MOD> client;
    protected Map<Block, ICustomRenderer> blockRenderers = new HashMap<Block, ICustomRenderer>();
    protected Map<Item, ICustomRenderer> itemRenderers = new HashMap<Item, ICustomRenderer>();
    protected Map<IBlockState, ICustomRenderer> stateRendererCache = new HashMap<IBlockState, ICustomRenderer>();
    protected Map<ResourceLocation, ITexture> textureCache = new HashMap<ResourceLocation, ITexture>();
    protected boolean customRenderingRequired;    
    
    public BaseRenderingManager(BaseModClient client) {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager: Creating\n");
        this.client = client;
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    public void preInit() {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager.preInit\n");
        registerDefaultRenderers();
        registerDefaultModelLocations();
//         registerDummyStateMappers();
    }
    
    public void postInit() {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager.postInit: customRenderingRequired = %s\n", customRenderingRequired);
        if (customRenderingRequired)
            enableCustomRendering();
    }
    
    protected void registerDefaultRenderers() {
        for (Block block : client.base.registeredBlocks) {
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
    
    //-------------- Internal --------------------------------------------

//     protected void registerDummyStateMappers() {
//         for (Block block : client.base.registeredBlocks) {
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
        for (Block block : client.base.registeredBlocks) {
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
        for (Item item : client.base.registeredItems) {
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
        
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {return null;}
        public boolean isAmbientOcclusion() {return false;}
        public boolean isGui3d() {return false;}
        public boolean isBuiltInRenderer() {return false;}
        public ItemCameraTransforms getItemCameraTransforms() {return null;}
        public ItemOverrideList getOverrides() {return null;}
        
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
        int numVariants = getNumItemSubtypes(item);
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
    
    public static void renderBlockUsingModelSpec(BaseModClient client,
        IBlockAccess world, BlockPos pos, IBlockState state,
        IRenderTarget target, BlockRenderLayer layer, Trans3 t)
    {
        ((BaseRenderingManager)client.renderingManager).renderBlockUsingModelSpec(
            world, pos, state, target, layer, t);
    }

    public void renderBlockUsingModelSpec(IBlockAccess world, BlockPos pos, IBlockState state,
        IRenderTarget target, BlockRenderLayer layer, Trans3 t)
    {
        ICustomRenderer rend = getCustomRendererForState(state);
        if (rend != null)
            rend.renderBlock(world, pos, state, target, layer, t);
    }
    
    // Call this from renderItemStack of an ICustomRenderer to fall back to model spec
    public static void renderItemStackUsingModelSpec(BaseModClient client,
        ItemStack stack, IRenderTarget target, Trans3 t)
    {
        ((BaseRenderingManager)client.renderingManager).renderItemStackUsingModelSpec(
            stack, target, t);
    }

    public void renderItemStackUsingModelSpec(ItemStack stack, IRenderTarget target, Trans3 t) {
        IBlockState state = BaseBlockUtils.getBlockStateFromItemStack(stack);
        IBlock block = (IBlock)state.getBlock();
        ModelSpec spec = block.getModelSpec(state);
        ICustomRenderer rend = getCustomRendererForSpec(0, spec);
        rend.renderItemStack(stack, target, t);
    }

    public IModel getModel(String name) {
        return client.base.getModel(name);
    }
    
    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    
    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return client.base.resourceLocation(texturePrefixes[type] + name);
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
        for (Block block : client.base.registeredBlocks) {
            //System.out.printf("BaseModClient.onTextureStitchEvent: Block %s\n", block.getUnlocalizedName());
            registerSprites(0, e.getMap(), block);
        }
        for (Item item : client.base.registeredItems)
            registerSprites(1, e.getMap(), item);
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
        @Override public IBakedModel getModelForState(IBlockState state)
            {return base.getModelForState(state);}
        @Override public void renderBlockBrightness(IBlockState state, float brightness)
            {base.renderBlockBrightness(state, brightness);}

        @Override
        public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite icon, IBlockAccess world) {
            ICustomRenderer rend = getCustomRenderer(world, pos, state);
            if (rend != null) {
                BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos, icon);
                Trans3 t = Trans3.blockCenter;
                Block block = state.getBlock();
                for (BlockRenderLayer layer : BlockRenderLayer.values())
                    if (block.canRenderInLayer(layer))
                        rend.renderBlock(world, pos, state, target, layer, t);
                IBakedModel model = target.getBakedModel();
                VertexBuffer tess = Tessellator.getInstance().getBuffer();
                getBlockModelRenderer().renderModel(world, model, state, pos, tess, false); //TODO chould checkSides be false?
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
        public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess world, VertexBuffer tess) {
            ICustomRenderer rend = getCustomRenderer(world, pos, state);
            if (rend != null)
                return customRenderBlockToWorld(world, pos, state, tess, null, rend);
            else
                return base.renderBlock(state, pos, world, tess);
        }
        
    }
    
    protected boolean customRenderBlockToWorld(IBlockAccess world, BlockPos pos, IBlockState state, VertexBuffer tess,
        TextureAtlasSprite icon, ICustomRenderer rend)
    {
        //System.out.printf("BaseModClient.customRenderBlock: %s\n", state);
        BaseWorldRenderTarget target = new BaseWorldRenderTarget(world, pos, tess, icon);
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        rend.renderBlock(world, pos, state, target, layer, Trans3.blockCenter(pos));
        return target.end();
    }
    
    protected IBakedModel customRenderBlockToBakedModel(IBlockAccess world, BlockPos pos, IBlockState state,
        ICustomRenderer rend)
    {
        BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos);
        Trans3 t = Trans3.blockCenter;
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
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
    
    public static boolean renderAlternateBlock(BaseModClient client,
        IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target)
    {
        return ((BaseRenderingManager)client.renderingManager).renderAlternateBlock(
            world, pos, state, target);
    }

    public boolean renderAlternateBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target)
    {
        Block block = state.getBlock();
        if (!block.hasTileEntity(state)) {
            try {
                BlockRendererDispatcher disp = getCustomBlockRendererDispatcher();
                VertexBuffer tess = ((BaseWorldRenderTarget)target).getWorldRenderer();
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

    protected abstract class CustomRenderDispatch implements IBakedModel {
    
        public ModelResourceLocation modelLocation;
        
        public void install(ModelBakeEvent event) {
            if (debugModelRegistration)
                System.out.printf("BaseModClient: Installing %s at %s\n", this, modelLocation);
            event.getModelRegistry().putObject(modelLocation, this);
        }
    
        // ----- IBakedModel -----
        
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {return null;}
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

    protected class CustomItemRenderDispatch extends CustomRenderDispatch {
    
        public CustomItemRenderDispatch() {
            modelLocation = client.modelResourceLocation("__custitem__", "");
        }
        
        private class CustomItemRenderOverrideList extends ItemOverrideList {
        
            public CustomItemRenderOverrideList() {
                super(ImmutableList.<ItemOverride>of());
            }
            
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
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
                    GlStateManager.shadeModel(GL_SMOOTH);
                    BaseBakedRenderTarget target = new BaseBakedRenderTarget();
                    rend.renderItemStack(stack, target, itemTrans);
                    return target.getBakedModel();
                }
                else
                    return null;
            }

        }
        
        @Override
        public ItemOverrideList getOverrides() {
            return new CustomItemRenderOverrideList();
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
            event.getModelRegistry().putObject(e.getKey(), e.getValue());
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
    
//     protected static class DummyStateMapper implements IStateMapper {
//         private static Map<IBlockState, ModelResourceLocation> emptyMap =
//             new HashMap<>();
//         public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
//             return emptyMap;
//         }
//     }
//     
//     protected static IStateMapper dummyStateMapper = new DummyStateMapper();
    
}
