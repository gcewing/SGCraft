//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Generic Block with optional Tile Entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
// import net.minecraft.block.properties.*;
// import net.minecraft.block.state.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.GameRules;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;

import gcewing.sg.BaseMod.ModelSpec;
import static gcewing.sg.BaseUtils.*;
import static gcewing.sg.BaseBlockUtils.*;

public class BaseBlock<TE extends TileEntity>
    extends BlockContainer implements BaseMod.IBlock
{

    public static boolean debugState = false;
    
    protected static Random RANDOM = new Random();
    private static TileEntity tileEntityHarvested;
    
    public Class getDefaultItemClass() {
        return BaseItemBlock.class;
    }

    // --------------------------- Orientation -------------------------------

    public interface IOrientationHandler {
    
        void defineProperties(BaseBlock block);
        IBlockState onBlockPlaced(Block block, World world, BlockPos pos, EnumFacing side, 
            float hitX, float hitY, float hitZ, IBlockState baseState, EntityLivingBase placer);
        Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin);
    }
    
    public static class Orient1Way implements IOrientationHandler {
    
        public void defineProperties(BaseBlock block) {
        }
        
        public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, EnumFacing side, 
            float hitX, float hitY, float hitZ, IBlockState baseState, EntityLivingBase placer)
        {
            return baseState;
        }
        
        public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
            return new Trans3(origin);
        }
    
    }
    
    public static IOrientationHandler orient1Way = new Orient1Way();
    
    // --------------------------- Members -------------------------------

    protected MapColor mapColor;
    protected final BlockState blockState;
    protected IBlockState defaultBlockState;
    protected IProperty[] properties;
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise
    protected int renderID;
    protected Class<? extends TileEntity> tileEntityClass = null;
    protected IOrientationHandler orientationHandler = orient1Way;
    protected String[] textureNames;
    protected ModelSpec modelSpec;


    // --------------------------- Constructors -------------------------------
    
    public BaseBlock(Material material) {
        this(material, null, null, null);
    }

    public BaseBlock(Material material, IOrientationHandler orient) {
        this(material, orient, null, null);
    }

    public BaseBlock(Material material, Class<TE> teClass) {
        this(material, null, teClass, null);
    }

    public BaseBlock(Material material, IOrientationHandler orient, Class<TE> teClass) {
        this(material, orient, teClass, null);
    }

    public BaseBlock(Material material, Class<TE> teClass, String teID) {
        this(material, null, teClass, teID);
    }

    public BaseBlock(Material material, IOrientationHandler orient, Class<TE> teClass, String teID) {
        super(material);
        if (orient == null)
            orient = orient1Way;
        this.orientationHandler = orient;
        tileEntityClass = teClass;
        if (teClass != null) {
            if (teID == null)
                teID = teClass.getName();
            try {
                GameRegistry.registerTileEntity(teClass, teID);
            }
            catch (IllegalArgumentException e) {
                // Ignore redundant registration
            }
        }
        blockState = createBlockState();
        defaultBlockState = blockState.getBaseState();
    }

    // --------------------------- States -------------------------------
    
    public IOrientationHandler getOrientationHandler() {
        return orientationHandler;
    }
    
    protected void defineProperties() {
        properties = new IProperty[4];
        propertyValues = new Object[4][];
        getOrientationHandler().defineProperties(this);
    }
    
    protected void addProperty(IProperty property) {
        if (debugState)
            System.out.printf("BaseBlock.addProperty: %s to %s\n", property, getClass().getName());
        if (numProperties < 4) {
            int i = numProperties++;
            properties[i] = property;
            Object[] values = BaseUtils.arrayOf(property.getAllowedValues());
            propertyValues[i] = values;
        }
        else
            throw new IllegalStateException("Block " + getClass().getName() +
                " has too many properties");
        if (debugState)
            System.out.printf("BaseBlock.addProperty: %s now has %s properties\n",
                getClass().getName(), numProperties);
    }
    
//     @Override
    protected BlockState createBlockState() {
        if (debugState)
            System.out.printf("BaseBlock.createBlockState: Defining properties\n");
        defineProperties();
        if (debugState)
            dumpProperties();
        checkProperties();
        IProperty[] props = Arrays.copyOf(properties, numProperties);
        if (debugState)
            System.out.printf("BaseBlock.createBlockState: Creating BlockState with %s properties\n", props.length);
        return new BlockState(this, props);
    }
    
    private void dumpProperties() {
        System.out.printf("BaseBlock: Properties of %s:\n", getClass().getName());
        for (int i = 0; i < numProperties; i++) {
            System.out.printf("%s: %s\n", i, properties[i]);
            Object[] values = propertyValues[i];
            for (int j = 0; j < values.length; j++)
                System.out.printf("   %s: %s\n", j, values[j]);
        }
    }

    protected void checkProperties() {
        int n = 1;
        for (int i = 0; i < numProperties; i++)
            n *= propertyValues[i].length;
        if (n > 16)
            throw new IllegalStateException(String.format(
                "Block %s has %s combinations of property values (16 allowed)",
                getClass().getName(), n));
    }
    
    public BlockState getBlockState() {
        return this.blockState;
    }

    public final IBlockState getDefaultState() {
        return this.defaultBlockState;
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state;
    }

//     @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        for (int i = numProperties - 1; i >= 0; i--) {
            Object value = state.getValue(properties[i]);
            Object[] values = propertyValues[i];
            int k = values.length - 1;
            while (k > 0 && !values[k].equals(value))
                --k;
            if (debugState)
                System.out.printf("BaseBlock.getMetaFromState: property %s value %s --> %s of %s\n",
                    i, value, k, values.length);
            meta = meta * values.length + k;
        }
        if (debugState)
            System.out.printf("BaseBlock.getMetaFromState: %s --> %s\n", state, meta);
        return meta & 15; // To be on the safe side
    }
    
//     @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        int m = meta;
        for (int i = numProperties - 1; i >= 0; i--) {
            Object[] values = propertyValues[i];
            int n = values.length;
            int k = m % n;
            m /= n;
            state = state.withProperty(properties[i], (Comparable)values[k]);
        }
        if (debugState)
            System.out.printf("BaseBlock.getStateFromMeta: %s --> %s\n", meta, state);
        return state;
    }

//     @Override
//     public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
//      return new BaseBlockState(state, world, pos);
//     }

    // -------------------------- Subtypes ------------------------------
    
    public int getNumSubtypes() {
        return 1;
    }
    
    // -------------------------- Rendering -----------------------------
    
    public void setModelAndTextures(String modelName, String... textureNames) {
        this.textureNames = textureNames;
        this.modelSpec = new ModelSpec(modelName, textureNames);
    }
    
    public void setModelAndTextures(String modelName, Vector3 origin, String... textureNames) {
        this.textureNames = textureNames;
        this.modelSpec = new ModelSpec(modelName, origin, textureNames);
    }
    
    @Override
    public String[] getTextureNames() {
        return textureNames;
    }
    
    @Override
    public ModelSpec getModelSpec(IBlockState state) {
        return modelSpec;
    }

    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return getBlockLayer() == layer;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.SOLID;
    }

    @Override
    public int getRenderType() {
        return renderID;
    }
    
    @Override
    public void setRenderType(int id) {
        renderID = id;
    }
    
    public String getQualifiedRendererClassName() {
        String name = getRendererClassName();
        if (name != null)
            name = getClass().getPackage().getName() + "." + name;
        return name;
    }
    
    protected String getRendererClassName() {
        return null;
    }
        
    public Trans3 localToGlobalRotation(IBlockAccess world, BlockPos pos) {
        return localToGlobalRotation(world, pos, getWorldBlockState(world, pos));
    }
    
    public Trans3 localToGlobalRotation(IBlockAccess world, BlockPos pos, IBlockState state) {
        return localToGlobalTransformation(world, pos, state, Vector3.zero);
    }
    
    public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos) {
        return localToGlobalTransformation(world, pos, getWorldBlockState(world, pos));
    }
    
    public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state) {
        return localToGlobalTransformation(world, pos, state, Vector3.blockCenter(pos));
    }
    
    public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
        IOrientationHandler oh = getOrientationHandler();
        return oh.localToGlobalTransformation(world, pos, state, origin);
    }
    
    // -------------------------- Tile Entity -----------------------------
    
    @Override
    public boolean hasTileEntity(int meta) {
        return hasTileEntity(getStateFromMeta(meta));
    }
    
    public boolean hasTileEntity(IBlockState state) {
        return tileEntityClass != null;
    }
    
    public TE getTileEntity(IBlockAccess world, BlockPos pos) {
        if (hasTileEntity())
            return (TE)world.getTileEntity(pos.x, pos.y, pos.z);
        else
            return null;
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (tileEntityClass != null) {
            try {
                return tileEntityClass.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else
            return null;
    }
    
    // -------------------------------------------------------------------

    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing side, 
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return getOrientationHandler().onBlockPlaced(this, world, pos, side,
            hitX, hitY, hitZ, getStateFromMeta(meta), placer);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (hasTileEntity(meta)) {
            TileEntity te = getTileEntity(world, pos);
            if (te instanceof BaseMod.ITileEntity)
                ((BaseMod.ITileEntity)te).onAddedToWorld();
        }
        onBlockAdded(world, pos, getStateFromMeta(meta));
    }
    
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
    }
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = getWorldBlockState(world, pos);
        onBlockPlacedBy(world, pos, state, entity, stack);
    }
    
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
    }        

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        BlockPos pos = new BlockPos(x, y, z);
        breakBlock(world, pos, getStateFromMeta(meta));
        if (hasTileEntity(meta)) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IInventory)
                InventoryHelper.dropInventoryItems(world, pos, (IInventory)te);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
    
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return canHarvestBlock(getStateFromMeta(meta), player);
    }
    
    public boolean canHarvestBlock(IBlockState state, EntityPlayer player) {
        return super.canHarvestBlock(player, getMetaFromState(state));
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        int meta = world.getBlockMetadata(x, y, z);
        IBlockState state = getStateFromMeta(meta);
        return onBlockActivated(world, new BlockPos(x, y, z), state, player, facings[side], hitX, hitY, hitZ);
    }
    
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
        EnumFacing side, float cx, float cy, float cz)
    {
        return false;
    }
    
    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (side != ForgeDirection.UNKNOWN)
            return isSideSolid(world, new BlockPos(x, y, z), facings[side.ordinal()]);
        else
            return super.isSideSolid(world, x, y, z, side);
    }
    
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return super.isSideSolid(world, pos.x, pos.y, pos.z, ForgeDirection.VALID_DIRECTIONS[side.ordinal()]);
    }
    
    @Override
    public boolean getWeakChanges(IBlockAccess world, int x, int y, int z) {
        return getWeakChanges(world, new BlockPos(x, y, z));
    }
    
    public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
        return super.getWeakChanges(world, pos.x, pos.y, pos.z);
    }

    @Override    
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        IBlockState state = getStateFromMeta(meta);
        onNeighborBlockChange(world, new BlockPos(x, y, z), state, block);
    }
    
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        IBlockState state = getStateFromMeta(meta);
        return getStrongPower(world, new BlockPos(x, y, z), state, facings[side]);
    }

    public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return 0;
    }
    
    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        IBlockState state = getStateFromMeta(meta);
        return getWeakPower(world, new BlockPos(x, y, z), state, facings[side]);
    }

    public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return 0;
    }
    
    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return shouldCheckWeakPower(world, new BlockPos(x, y, z), facings[side]);
    }
    
    public boolean shouldCheckWeakPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return super.shouldCheckWeakPower(world, pos.x, pos.y, pos.z, side.ordinal());
    }

    public void spawnAsEntity(World world, BlockPos pos, ItemStack stack) {
        dropBlockAsItem(world, pos.x, pos.y, pos.z, stack);
    }

    @Override
    public int damageDropped(int meta) {
        return damageDropped(getStateFromMeta(meta));
    }
    
    public int damageDropped(IBlockState state) {
        return 0;
    }
    
    @Override
    public MapColor getMapColor(int meta) {
        if (mapColor != null)
            return mapColor;
        else
            return super.getMapColor(meta);
    }
    
    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return getItemDropped(getStateFromMeta(meta), random, fortune);
    }
    
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return super.getItemDropped(getMetaFromState(state), random, fortune);
    }

    @Override    
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        IBlockState state = getStateFromMeta(meta);
        return getDrops(world, new BlockPos(x, y, z), state, fortune);
    }
    
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int meta = getMetaFromState(state);
        return super.getDrops((World)world, pos.x, pos.y, pos.z, meta, fortune);
    }
    
    @Override
    public boolean renderAsNormalBlock() {
        return isFullCube();
    }
    
    public boolean isFullCube() {
        return super.renderAsNormalBlock();
    }
    
    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        return collisionRayTrace(world, new BlockPos(x, y, z), start, end);
    }
    
    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {
        return super.collisionRayTrace(world, pos.x, pos.y, pos.z, start, end);
    }
    
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, new BlockPos(x, y, z));
    }
    
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        super.setBlockBoundsBasedOnState(world, pos.x, pos.y, pos.z);
    }
    
    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB clip,
        List result, Entity entity)
    {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = getWorldBlockState(world, pos);
        addCollisionBoxesToList(world, pos, state, clip, result, entity);
    }

    
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state,  AxisAlignedBB clip,
        List result, Entity entity)
    {
        super.addCollisionBoxesToList(world, pos.x, pos.y, pos.z, clip, result, entity);
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        tileEntityHarvested = world.getTileEntity(x, y, z);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        harvestBlock(world, player, new BlockPos(x, y, z), getStateFromMeta(meta), tileEntityHarvested);
    }
    
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        super.harvestBlock(world, player, pos.x, pos.y, pos.z, getMetaFromState(state));
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return getPickBlock(target, world, new BlockPos(x, y, z));
    }
    
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
        return super.getPickBlock(target, world, pos.x, pos.y, pos.z);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
        return getPlayerRelativeBlockHardness(player, world, new BlockPos(x, y, z));
    }
    
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, BlockPos pos) {
        return super.getPlayerRelativeBlockHardness(player, world, pos.x, pos.y, pos.z);
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        return getBlockHardness(world, new BlockPos(x, y, z));
    }
    
    public float getBlockHardness(World world, BlockPos pos) {
        return super.getBlockHardness(world, pos.x, pos.y, pos.z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer er) {
        BlockPos pos = new BlockPos(target.blockX, target.blockY, target.blockZ);
        IBlockState state = getParticleState(world, pos);
        Block block = state.getBlock();
        int meta = getMetaFromBlockState(state);
        EntityDiggingFX fx;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 0.1F;
        double d0 = i + RANDOM.nextDouble() * (getBlockBoundsMaxX() - getBlockBoundsMinX() - (f * 2.0F)) + f + getBlockBoundsMinX();
        double d1 = j + RANDOM.nextDouble() * (getBlockBoundsMaxY() - getBlockBoundsMinY() - (f * 2.0F)) + f + getBlockBoundsMinY();
        double d2 = k + RANDOM.nextDouble() * (getBlockBoundsMaxZ() - getBlockBoundsMinZ() - (f * 2.0F)) + f + getBlockBoundsMinZ();
        switch (target.sideHit) {
            case 0: d1 = j + getBlockBoundsMinY() - f; break;
            case 1: d1 = j + getBlockBoundsMaxY() + f; break;
            case 2: d2 = k + getBlockBoundsMinZ() - f; break;
            case 3: d2 = k + getBlockBoundsMaxZ() + f; break;
            case 4: d0 = i + getBlockBoundsMinX() - f; break;
            case 5: d0 = i + getBlockBoundsMaxX() + f; break;
        }
        fx = new EntityDiggingFX(world, d0, d1, d2, 0, 0, 0, block, meta);
        er.addEffect(fx.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer er) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = getParticleState(world, pos);
        Block block = state.getBlock();
        meta = getMetaFromBlockState(state);
        EntityDiggingFX fx;
        byte b0 = 4;
        for (int i = 0; i < b0; ++i) {
            for (int j = 0; j < b0; ++j) {
                for (int k = 0; k < b0; ++k) {
                    double d0 = pos.getX() + (i + 0.5D) / b0;
                    double d1 = pos.getY() + (j + 0.5D) / b0;
                    double d2 = pos.getZ() + (k + 0.5D) / b0;
                    fx = new EntityDiggingFX(world, d0, d1, d2,
                        d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D,
                        block, meta);
                    er.addEffect(fx);
                }
            }
        }
        return true;
    }
    
    public IBlockState getParticleState(IBlockAccess world, BlockPos pos) {
        return getWorldBlockState(world, pos);
    }
    
    // This needs to return the MAXIMUM pass number that the block renders in.
    @Override
    public int getRenderBlockPass() {
        if (canRenderInLayer(EnumWorldBlockLayer.TRANSLUCENT))
            return 1;
        else
            return 0;
    }
    
    @Override
    public boolean canRenderInPass(int pass) {
        for (EnumWorldBlockLayer layer : BaseModClient.passLayers[pass + 1])
            if (canRenderInLayer(layer))
                return true;
        return false;
    }
}
