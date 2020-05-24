//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import static java.lang.Math.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
//import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;

import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.relauncher.*;

public class BaseUtils {

    public static EnumFacing[] facings = EnumFacing.values();
    public static EnumFacing[] horizontalFacings = {
        EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST}; 
    
    public static int clampIndex(int x, int n) {
        return max(0, min(x, n - 1));
    }
    
    public static int ifloor(double x) {
        return (int)Math.floor(x);
    }
    
    public static int iround(double x) {
        return (int)Math.round(x);
    }
    
    public static int iceil(double x) {
        return (int)Math.ceil(x);
    }
    
    public static Object[] arrayOf(Collection c) {
        int n = c.size();
        Object[] result = new Object[n];
        int i = 0;
        for (Object item : c)
            result[i++] = item;
        return result;
    }
    
    public static Class classForName(String name) {
        try {
            return Class.forName(name);
        }
         catch (Exception e) {
            throw new RuntimeException(e);
        }
    }        
    
    public static Field getFieldDef(Class cls, String unobfName, String obfName) {
        try {
            Field field;
            try {
                field = cls.getDeclaredField(unobfName);
            }
            catch (NoSuchFieldException e) {
                field = cls.getDeclaredField(obfName);
            }
            field.setAccessible(true);
            return field;
        }
        catch (Exception e) {
            throw new RuntimeException(
                String.format("Cannot find field %s or %s of %s", unobfName, obfName, cls.getName()),
                e);
        }
    }
    
    public static Object getField(Object obj, String unobfName, String obfName) {
        Field field = getFieldDef(obj.getClass(), unobfName, obfName);
        return getField(obj, field);
    }
        
    public static Object getField(Object obj, Field field) {
        try {
            return field.get(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static int getIntField(Object obj, Field field) {
        try {
            return field.getInt(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setField(Object obj, String unobfName, String obfName, Object value) {
        Field field = getFieldDef(obj.getClass(), unobfName, obfName);
        setField(obj, field, value);
    }
    
    public static void setField(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setIntField(Object obj, Field field, int value) {
        try {
            field.setInt(obj, value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethodDef(Class cls, String unobfName, String obfName, Class... params) {
        try {
            Method meth;
            try {
                meth = cls.getDeclaredMethod(unobfName, params);
            }
            catch (NoSuchMethodException e) {
                meth = cls.getDeclaredMethod(obfName, params);
            }
            meth.setAccessible(true);
            return meth;
        }
         catch (Exception e) {
            throw new RuntimeException(
                String.format("Cannot find method %s or %s of %s", unobfName, obfName, cls.getName()),
                e);
        }
    }
    
    public static Object invokeMethod(Object target, Method meth, Object... args) {
        try {
            return meth.invoke(target, args);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static int packedColor(double red, double green, double blue) {
        return ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | (int)(blue * 255);
    }
    
    public static int turnToFace(EnumFacing local, EnumFacing global) {
        return (turnToFaceEast(local) - turnToFaceEast(global)) & 3;
    }
    
    public static int turnToFaceEast(EnumFacing f) {
        switch (f) {
            case SOUTH: return 1;
            case WEST: return 2;
            case NORTH: return 3;
            default: return 0;
        }
    }
    
    public static ItemStack blockStackWithTileEntity(Block block, int size, BaseTileEntity te) {
        ItemStack stack = new ItemStack(block, size);
        if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToItemStackNBT(tag);
            stack.setTagCompound(tag);
        }
        return stack;
    }
    public static BlockPos readBlockPos(DataInput data) {
        try {
            int x = data.readInt();
            int y = data.readInt();
            int z = data.readInt();
            return new BlockPos(x, y, z);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void writeBlockPos(DataOutput data, BlockPos pos) {
        try {
            data.writeInt(pos.getX());
            data.writeInt(pos.getY());
            data.writeInt(pos.getZ());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
//     public static int getMetaFromState(IBlockState state) {
//         return ((BaseBlock)state.getBlock()).getMetaFromState(state);
//     }
    
    public static int getWorldDimensionId(World world) {
        return world.provider.dimensionId;
    }
    
    public static EnumDifficulty getWorldDifficulty(World world) {
        return world.difficultySetting;
    }
    
    public static World getChunkWorld(Chunk chunk) {
        return chunk.worldObj;
    }
    
    public static Map getChunkTileEntityMap(Chunk chunk) {
        return chunk.chunkTileEntityMap;
    }
    
    public static AxisAlignedBB newAxisAlignedBB(double x0, double y0, double z0,
        double x1, double y1, double z1)
    {
        return AxisAlignedBB.getBoundingBox(x0, y0, z0, x1, y1, z1);
    }
    
    public static boolean getGameRuleBoolean(GameRules gr, String name) {
        return gr.getGameRuleBooleanValue(name);
    }
    
    public static void scmPreparePlayer(ServerConfigurationManager scm, EntityPlayerMP player, WorldServer world) {
        scm.func_72375_a(player, world);
    }
    
    public static void setBoundingBoxOfEntity(Entity entity, AxisAlignedBB box) {
        entity.boundingBox.setBounds(box.minX, box.minY, box.minZ,
            box.maxX, box.maxY, box.maxZ);
    }
    
    public static String getEntityName(Entity entity) {
        return entity.getCommandSenderName();
    }
    
    public static MapStorage getPerWorldStorage(World world) {
        return world.perWorldStorage;
    }
    
    public static EnumFacing oppositeFacing(EnumFacing dir) {
        return facings[dir.ordinal() ^ 1];
    }
    
    public static boolean facingAxesEqual(EnumFacing facing1, EnumFacing facing2) {
        return (facing1.ordinal() & 6) == (facing2.ordinal() & 6);
    }
    
    public static int getStackMetadata(ItemStack stack) {
        return stack.getItem().getMetadata(stack.getItemDamage());
    }
    
    public static MovingObjectPosition newMovingObjectPosition(Vec3 hitVec, int sideHit, BlockPos pos) {
        return new MovingObjectPosition(pos.x, pos.y, pos.z, sideHit, hitVec, true);
    }
    
    public static AxisAlignedBB boxUnion(AxisAlignedBB box1, AxisAlignedBB box2) {
        return box1.func_111270_a(box2);
    }

    public static AxisAlignedBB boxIntersection(AxisAlignedBB box1, AxisAlignedBB box2) {
        double minX = max(box1.minX, box2.minX);
        double minY = max(box1.minY, box2.minY);
        double minZ = max(box1.minZ, box2.minZ);
        double maxX = min(box1.maxX, box2.maxX);
        double maxY = min(box1.maxY, box2.maxY);
        double maxZ = min(box1.maxZ, box2.maxZ);
        if (minX < maxX && minY < maxY && minZ < maxZ)
            return newAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        else
            return null;
    }

    public static MinecraftServer getMinecraftServer() {
        return MinecraftServer.getServer();
    }

    public static WorldServer getWorldForDimension(int id) {
        return getMinecraftServer().worldServerForDimension(id);
    }
    
    public static <T extends WorldSavedData> T getWorldData(World world, Class<T> cls, String name) {
        MapStorage storage = world.perWorldStorage;
        T result = (T)storage.loadData(cls, name);
        if (result == null) {
            try {
                result = cls.getConstructor(String.class).newInstance(name);
                storage.setData(name, result);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
    
    public static String translateToLocal(String s) {
        if (!StatCollector.canTranslate(s))
            s = s + ".name";
        return StatCollector.translateToLocal(s);
    }
    
    public static void addChatMessage(EntityPlayer player, String text) {
        player.addChatComponentMessage(new ChatComponentText(text));
    }
    
    @SideOnly(Side.CLIENT)
    public static void addClientChatMessage(String text) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(text));
    }
    
}
