//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;
import cpw.mods.fml.relauncher.*;

public class SGBaseBlock extends Base4WayCtrBlock<SGBaseTE> implements ISGBlock {

    static boolean debugMerge = false;
    static int explosionRadius = 10;
    static boolean fieryExplosion = true;
    static boolean smokyExplosion = true;

    static final int mergedBit = 0x8;
    
    static int southSide[] = {3, 5, 2, 4};
    static int unitX[] = {1, 0, -1, 0};
    static int unitZ[] = {0, -1, 0, 1};
    
    static int pattern[][] = {
        {2, 1, 2, 1, 2},
        {1, 0, 0, 0, 1},
        {2, 0, 0, 0, 2},
        {1, 0, 0, 0, 1},
        {2, 1, 0, 1, 2},
    };

    IIcon topAndBottomTexture; // = 0x00;
    IIcon frontTexture; // = 0x01;
    IIcon sideTexture; // = 0x02;
    
    public static void configure(BaseConfiguration config) {
        explosionRadius = config.getInteger("stargate", "explosionRadius", explosionRadius);
        fieryExplosion = config.getBoolean("stargate", "explosionFlame", fieryExplosion);
        smokyExplosion = config.getBoolean("stargate", "explosionSmoke", smokyExplosion);
    }
    
    public SGBaseBlock() {
        super(Material.rock /*SGRingBlock.ringMaterial*/, SGBaseTE.class);
        setHardness(1.5F);
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public SGBaseTE getBaseTE(IBlockAccess world, int x, int y, int z) {
        return getTileEntity(world, x, y, z);
    }

    @Override
    protected String getRendererClassName() {
        return "SGBaseBlockRenderer";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        topAndBottomTexture = getIcon(reg, "stargateBlock");
        frontTexture = getIcon(reg, "stargateBase_front");
        sideTexture = getIcon(reg, "stargateRing");
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return true;
    }

    public boolean isMerged(IBlockAccess world, int x, int y, int z) {
        SGBaseTE te = getTileEntity(world, x, y, z);
        return te != null && te.isMerged;
    }
    
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGBaseBlock.onBlockAdded: at (%d,%d,%d)\n", x, y, z);
        checkForMerge(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        int data = Math.round((180 - player.rotationYaw) / 90) & 3;
        world.setBlockMetadataWithNotify(x, y, z, data, 0x3);
        if (!world.isRemote) {
            if (debugMerge)
                System.out.printf("SGBaseBlock.onBlockPlacedBy: yaw = %.1f data = %d\n", player.rotationYaw, data);
            checkForMerge(world, x, y, z);
        }
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
        int side, float cx, float cy, float cz)
    {
        String Side = world.isRemote ? "Client" : "Server";
        SGBaseTE te = getTileEntity(world, x, y, z);
        //System.out.printf("SGBaseBlock.onBlockActivated: %s: Tile entity = %s\n", Side, te);
        if (te != null) {
            if (debugMerge)
                System.out.printf("SGBaseBlock.onBlockActivated: %s: isMerged = %s\n", Side, te.isMerged);
            //if (!world.isRemote)
            //	te.dumpChunkLoadingState("SGBaseBlock.onBlockActivated");
            if (te.isMerged) {
                SGCraft.mod.openGui(player, SGGui.SGBase, world, x, y, z);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean getWeakChanges(IBlockAccess world, int x, int y, int z) {
        return true;
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        SGBaseTE te = getTileEntity(world, x, y, z);
        if (te != null)
            te.onNeighborBlockChange();
    }
    
    @Override
    public IIcon getIcon(int side, int data) {
        if (side <= 1)
            return topAndBottomTexture;
        else if (side == 3) // south
            return frontTexture;
        else
            return sideTexture;
    }
    
//	int getRotation(World world, int x, int y, int z) {
//		return world.getBlockMetadata(x, y, z) & rotationMask;
//	}
    
    void checkForMerge(World world, int x, int y, int z) {
        if (debugMerge)
            System.out.printf("SGBaseBlock.checkForMerge at (%d,%d,%d)\n", x, y, z);
        if (!isMerged(world, x, y, z)) {
            int rot = getRotation(world, x, y, z);
            int dx = unitX[rot];
            int dz = unitZ[rot];
            if (debugMerge)
                System.out.printf("SGBaseBlock: rot = %d, dx = %d, dz = %d\n", rot, dx, dz);
            for (int i = -2; i <= 2; i++)
                for (int j = 0; j <= 4; j++) 
                    if (!(i == 0 && j == 0)) {
                        int xr = x + i * dx;
                        int yr = y + j;
                        int zr = z + i * dz;
                        int type = getRingBlockType(world, xr, yr, zr);
                        int pat = pattern[4 - j][2 + i];
                        if (pat != 0 && type != pat) {
                            if (debugMerge)
                                System.out.printf("SGBaseBlock: world %d != pattern %d at (%d,%d,%d)\n", type, pattern[j][2 + i], xr, yr, zr);
                            return;
                        }
                    }
            if (debugMerge)
                System.out.printf("SGBaseBlock: Merging\n");
            SGBaseTE te = getTileEntity(world, x, y, z);
            te.setMerged(true);
            world.markBlockForUpdate(x, y, z);
            for (int i = -2; i <= 2; i++)
                for (int j = 0; j <= 4; j++) 
                    if (!(i == 0 && j == 0)) {
                        int xr = x + i * dx;
                        int yr = y + j;
                        int zr = z + i * dz;
                        Block block = world.getBlock(xr, yr, zr);
                        if (block instanceof SGRingBlock)
                            ((SGRingBlock)block).mergeWith(world, xr, yr, zr, x, y, z);
                        //else if (block == null)
                        //	world.setBlock(xr, yr, zr, SGCraft.sgPortalBlock.blockID, 0, 0x3);
                    }
            te.checkForLink();
        }
    }
    
    int getRingBlockType(World world, int xr, int yr, int zr) {
        Block id = world.getBlock(xr, yr, zr);
        if (id == Blocks.air)
            return 0;
        if (id == SGCraft.sgRingBlock)
            if (!SGCraft.sgRingBlock.isMerged(world, xr, yr, zr)) {
                int data = world.getBlockMetadata(xr, yr, zr);
                switch (data & SGRingBlock.subBlockMask) {
                    case 0: return 1;
                    case 1: return 2;
                }
            }
        return -1;
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, Block id, int data) {
        unmerge(world, x, y, z);
        dropUpgrades(world, x, y, z);
        super.breakBlock(world, x, y, z, id, data);
    }
    
    void dropUpgrades(World world, int x, int y, int z) {
        SGBaseTE te = getTileEntity(world, x, y, z);
        if (te != null) {
            if (te.hasChevronUpgrade)
                scatterNearby(world, x, y, z, new ItemStack(SGCraft.sgChevronUpgrade));
            if (te.hasIrisUpgrade)
                scatterNearby(world, x, y, z, new ItemStack(SGCraft.sgIrisUpgrade));
        }
    }
    
    public void unmerge(World world, int x, int y, int z) {
        SGBaseTE te = getTileEntity(world, x, y, z);
        boolean goBang = false;
        if (te != null /*&& te.isMerged*/) {
            if (te.isMerged && te.state == SGState.Connected) {
                te.state = SGState.Idle;
                goBang = true;
            }
            te.disconnect();
            te.unlinkFromController();
            te.setMerged(false);
            world.markBlockForUpdate(x, y, z);
            unmergeRing(world, x, y, z);
        }
        if (goBang && explosionRadius > 0)
            explode(world, x + 0.5, y + 2.5, z + 0.5, explosionRadius);
    }
    
//	static DamageSource explodingStargateDamage = new ExplodingStargateDamage();
    
    void explode(World world, double x, double y, double z, double s) {
//		DamageSource oldDamage = DamageSource.explosion;
//		DamageSource.explosion = explodingStargateDamage;
//		try {
                world.newExplosion(null, x, y, z, (float)s, fieryExplosion, smokyExplosion);
//		}
//		finally {
//			DamageSource.explosion = oldDamage;
//		}
//		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x - s, y - s, z - s, x + s, y + s, z + s);
//		List<EntityLiving> ents = world.getEntitiesWithinAABB(EntityLiving.class, box);
//		for (EntityLiving ent : ents) {
//			double dx = ent.posX - x, dy = ent.posY - y, dz = ent.posZ - z;
//			double rsq = Math.max(1.0, dx * dx + dy * dy + dz * dz);
//			int damage = (int)(1000 / rsq);
//			System.out.printf("SGBaseBlock.explode: damaging %s by %s\n", ent, damage);
//			ent.attackEntityFrom(DamageSource.explosion, damage);
//		}
    }
    
    void unmergeRing(World world, int x, int y, int z) {
        for (int i = -2; i <= 2; i++)
            for (int j = 0; j <= 4; j++)
                for (int k = -2; k <= 2; k++)
                    unmergeRingBlock(world, x, y, z, x + i, y + j, z + k);
    }
    
    void unmergeRingBlock(World world, int x, int y, int z, int xr, int yr, int zr) {
        //System.out.printf("SGBaseBlock.unmergeRingBlock at (%d,%d,%d)\n", xr, yr, zr);
        Block block = world.getBlock(xr, yr, zr);
        if (debugMerge)
            System.out.printf("SGBaseBlock.unmergeRingBlock: found %s at (%d,%d,%d)\n",
                block, xr, yr, zr);
        if (block instanceof SGRingBlock) {
            //System.out.printf("SGBaseBlock: unmerging ring block\n");
            ((SGRingBlock)block).unmergeFrom(world, xr, yr, zr, x, y, z);
        }
        else if (block instanceof SGPortalBlock)
            world.setBlock(xr, yr, zr, Blocks.air, 0, 0x3);
    }
    
    @Override
    public boolean canProvidePower() {
        return true;
    }
    
    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int dir) {
        return isProvidingWeakPower(world, x, y, z, dir);
    }
    
    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int dir) {
        SGBaseTE te = getTileEntity(world, x, y, z);
        return (te != null && te.state != SGState.Idle) ? 15 : 0;
    }
    
}

//------------------------------------------------------------------------------------------------

//class ExplodingStargateDamage extends DamageSource {
//
//	public ExplodingStargateDamage() {
//		super("sgExplosion");
//	}
//	
//	public String getDeathMessage(EntityPlayer player) {
//		return player.username + " was killed by an exploding stargate";
//	}
//	
//}
