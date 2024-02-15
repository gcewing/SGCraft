// ------------------------------------------------------------------------------------------------
//
// SG Craft - Stargate base tile entity
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseBlockUtils.getTileEntityPos;
import static gcewing.sg.BaseBlockUtils.getTileEntityWorld;
import static gcewing.sg.BaseBlockUtils.getWorldTileEntity;
import static gcewing.sg.BaseBlockUtils.markWorldBlockForUpdate;
import static gcewing.sg.BaseBlockUtils.notifyWorldNeighborsOfStateChange;
import static gcewing.sg.BaseUtils.getGameRuleBoolean;
import static gcewing.sg.BaseUtils.getWorldDifficulty;
import static gcewing.sg.BaseUtils.getWorldDimensionId;
import static gcewing.sg.BaseUtils.newAxisAlignedBB;
import static gcewing.sg.BaseUtils.scmPreparePlayer;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.network.ForgeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import gcewing.sg.oc.OCWirelessEndpoint;
import io.netty.channel.ChannelFutureListener;

public class SGBaseTE extends BaseTileInventory {

    static boolean debugState = false;
    static boolean debugEnergyUse = false;
    static boolean debugConnect = false;
    static boolean debugTransientDamage = false;
    static boolean debugTeleport = false;

    public final static String symbolChars = SGAddressing.symbolChars;
    public final static int numRingSymbols = SGAddressing.numSymbols;
    public final static double ringSymbolAngle = 360.0 / numRingSymbols;
    public final static double irisZPosition = 0.1;
    public final static double irisThickness = 0.2; // 0.1;
    public final static DamageSource irisDamageSource = new DamageSource("sgcraft:iris");
    public final static float irisDamageAmount = 1000000;

    final static int diallingTime = 40; // ticks
    final static int interDiallingTime = 10; // ticks
    final static String diallingSound = "sgcraft:sg_dial7";
    final static int transientDuration = 20; // ticks
    final static int disconnectTime = 30; // ticks

    final static double openingTransientIntensity = 1.3;
    final static double openingTransientRandomness = 0.25;
    final static double closingTransientRandomness = 0.25;
    final static double transientDamageRate = 50;

    final static int maxIrisPhase = 60; // 3 seconds

    final static int firstCamouflageSlot = 0;
    final static int numCamouflageSlots = 5;
    final static int numInventorySlots = numCamouflageSlots;

    static float defaultChevronAngle = 40f;
    static float[][] chevronAngles = {
            // 0 1 2 <-- Base camouflage level
            { 45f, 45f, 40f }, // 7 chevrons
            { 36f, 33f, 30f } // 9 chevrons
    };

    // Configuration options
    static double maxEnergyBuffer = 1000;
    static double energyPerFuelItem = 96000;
    static double distanceFactorMultiplier = 1.0;
    static double interDimensionMultiplier = 4.0;
    static int gateOpeningsPerFuelItem = 24;
    static int minutesOpenPerFuelItem = 80;
    static int secondsToStayOpen = 5 * 60;
    static boolean oneWayTravel = false;
    static boolean closeFromEitherEnd = true;
    static int chunkLoadingRange = 1;
    static boolean logStargateEvents = false;
    static boolean preserveInventory = false;
    static float soundVolume = 1.0F;
    static boolean variableChevronPositions = true;

    public static double energyToOpen;
    static double energyUsePerTick;
    static int ticksToStayOpen;
    public static boolean transparency = true;

    static Random random = new Random();
    static DamageSource transientDamage = new DamageSource("sgcraft:transient");

    public boolean isMerged;
    public SGState state = SGState.Idle;
    public double ringAngle, lastRingAngle, targetRingAngle; // degrees
    public int numEngagedChevrons;
    public String dialledAddress = "";
    public boolean isLinkedToController;
    public BlockPos linkedPos = new BlockPos(0, 0, 0);
    public boolean hasChevronUpgrade;
    public boolean hasIrisUpgrade;
    public IrisState irisState = IrisState.Open;
    public int irisPhase = maxIrisPhase; // 0 = fully closed, maxIrisPhase = fully open
    public int lastIrisPhase = maxIrisPhase;
    public OCWirelessEndpoint ocWirelessEndpoint; // [OC]

    SGLocation connectedLocation;
    boolean isInitiator;
    int timeout;
    double energyInBuffer;
    double distanceFactor; // all energy use is multiplied by this
    boolean redstoneInput;
    boolean loaded;
    public String homeAddress;
    public String addressError;

    IInventory inventory = new InventoryBasic("Stargate", false, numInventorySlots);

    double[][][] ehGrid;

    public static void configure(BaseConfiguration cfg) {
        energyPerFuelItem = cfg.getDouble("stargate", "energyPerFuelItem", energyPerFuelItem);
        gateOpeningsPerFuelItem = cfg.getInteger("stargate", "gateOpeningsPerFuelItem", gateOpeningsPerFuelItem);
        minutesOpenPerFuelItem = cfg.getInteger("stargate", "minutesOpenPerFuelItem", minutesOpenPerFuelItem);
        secondsToStayOpen = cfg.getInteger("stargate", "secondsToStayOpen", secondsToStayOpen);
        oneWayTravel = cfg.getBoolean("stargate", "oneWayTravel", oneWayTravel);
        closeFromEitherEnd = cfg.getBoolean("stargate", "closeFromEitherEnd", closeFromEitherEnd);
        maxEnergyBuffer = cfg.getDouble("stargate", "maxEnergyBuffer", maxEnergyBuffer);
        energyToOpen = energyPerFuelItem / gateOpeningsPerFuelItem;
        energyUsePerTick = energyPerFuelItem / (minutesOpenPerFuelItem * 60 * 20);
        distanceFactorMultiplier = cfg.getDouble("stargate", "distanceFactorMultiplier", distanceFactorMultiplier);
        interDimensionMultiplier = cfg.getDouble("stargate", "interDimensionMultiplier", interDimensionMultiplier);
        if (debugEnergyUse) {
            SGCraft.log.debug(String.format("SGBaseTE: energyPerFuelItem = %s", energyPerFuelItem));
            SGCraft.log.debug(String.format("SGBaseTE: energyToOpen = %s", energyToOpen));
            SGCraft.log.debug(String.format("SGBaseTE: energyUsePerTick = %s", energyUsePerTick));
        }
        ticksToStayOpen = 20 * secondsToStayOpen;
        chunkLoadingRange = cfg.getInteger("options", "chunkLoadingRange", chunkLoadingRange);
        transparency = cfg.getBoolean("stargate", "transparency", transparency);
        logStargateEvents = cfg.getBoolean("options", "logStargateEvents", logStargateEvents);
        preserveInventory = cfg.getBoolean("iris", "preserveInventory", preserveInventory);
        soundVolume = (float) cfg.getDouble("stargate", "soundVolume", soundVolume);
        variableChevronPositions = cfg.getBoolean("stargate", "variableChevronPositions", variableChevronPositions);
    }

    public static SGBaseTE get(IBlockAccess world, BlockPos pos) {
        TileEntity te = getWorldTileEntity(world, pos);
        if (te instanceof SGBaseTE) return (SGBaseTE) te;
        else if (te instanceof SGRingTE) return ((SGRingTE) te).getBaseTE();
        return null;
    }

    @Override
    public String toString() {
        return String.format("SGBaseTE(pos=%s,dim=%s)", getPos(), getWorldDimensionId(worldObj));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int x = getX(), y = getY(), z = getZ();
        return newAxisAlignedBB(x - 2, y, z - 2, x + 3, y + 5, z + 3);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 32768.0;
    }

    @Override
    public void onAddedToWorld() {
        if (SGBaseBlock.debugMerge) SGCraft.log.debug("SGBaseTE.onAddedToWorld");
        updateChunkLoadingStatus();
    }

    void updateChunkLoadingStatus() {
        if (state != SGState.Idle) {
            int n = chunkLoadingRange;
            if (n >= 0) SGCraft.chunkManager.setForcedChunkRange(this, -n, -n, n, n);
        } else SGCraft.chunkManager.clearForcedChunkRange(this);
    }

    public static SGBaseTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = getWorldTileEntity(world, pos);
        if (te instanceof SGBaseTE) return (SGBaseTE) te;
        return null;
    }

    public static SGBaseTE at(SGLocation loc) {
        if (loc != null) {
            World world = SGAddressing.getWorld(loc.dimension);
            if (world != null) return SGBaseTE.at(world, loc.pos);
        }
        return null;
    }

    public static SGBaseTE at(IBlockAccess world, NBTTagCompound nbt) {
        BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        return SGBaseTE.at(world, pos);
    }

    void setMerged(boolean state) {
        if (isMerged == state) {
            return;
        }

        isMerged = state;
        markBlockChanged();
        if (logStargateEvents) {
            String address = tryToGetHomeAddress();
            if (address != null) {
                Logger log = LogManager.getLogger();
                String action = isMerged ? "ADDED" : "REMOVED";
                String name = worldObj.getWorldInfo().getWorldName();
                log.info(String.format("STARGATE %s %s %s %s", action, name, getPos(), address));
            }
        }
        updateIrisEntity();
    }

    String tryToGetHomeAddress() {
        try {
            return getHomeAddress();
        } catch (SGAddressing.AddressingError e) {
            return null;
        }
    }

    public int dimension() {
        if (worldObj != null) return getWorldDimensionId(worldObj);
        return -999;
    }

    @Override
    public void readContentsFromNBT(NBTTagCompound nbt) {
        super.readContentsFromNBT(nbt);
        isMerged = nbt.getBoolean("isMerged");
        state = SGState.valueOf(nbt.getInteger("state"));
        targetRingAngle = nbt.getDouble("targetRingAngle");
        numEngagedChevrons = nbt.getInteger("numEngagedChevrons");
        dialledAddress = nbt.getString("dialledAddress");
        isLinkedToController = nbt.getBoolean("isLinkedToController");
        int x = nbt.getInteger("linkedX");
        int y = nbt.getInteger("linkedY");
        int z = nbt.getInteger("linkedZ");
        linkedPos = new BlockPos(x, y, z);
        hasChevronUpgrade = nbt.getBoolean("hasChevronUpgrade");
        if (nbt.hasKey("connectedLocation"))
            connectedLocation = new SGLocation(nbt.getCompoundTag("connectedLocation"));
        isInitiator = nbt.getBoolean("isInitiator");
        timeout = nbt.getInteger("timeout");
        if (nbt.hasKey("energyInBuffer")) energyInBuffer = nbt.getDouble("energyInBuffer");
        else energyInBuffer = nbt.getInteger("fuelBuffer");
        distanceFactor = nbt.getDouble("distanceFactor");
        hasIrisUpgrade = nbt.getBoolean("hasIrisUpgrade");
        irisState = IrisState.valueOf(nbt.getInteger("irisState"));
        irisPhase = nbt.getInteger("irisPhase");
        redstoneInput = nbt.getBoolean("redstoneInput");
        homeAddress = getStringOrNull(nbt, "address");
        addressError = nbt.getString("addressError");
    }

    protected String getStringOrNull(NBTTagCompound nbt, String name) {
        if (nbt.hasKey(name)) return nbt.getString(name);
        return null;
    }

    @Override
    public void writeContentsToNBT(NBTTagCompound nbt) {
        super.writeContentsToNBT(nbt);
        nbt.setBoolean("isMerged", isMerged);
        nbt.setInteger("state", state.ordinal());
        nbt.setDouble("targetRingAngle", targetRingAngle);
        nbt.setInteger("numEngagedChevrons", numEngagedChevrons);
        // nbt.setString("homeAddress", homeAddress);
        nbt.setString("dialledAddress", dialledAddress);
        nbt.setBoolean("isLinkedToController", isLinkedToController);
        nbt.setInteger("linkedX", linkedPos.getX());
        nbt.setInteger("linkedY", linkedPos.getY());
        nbt.setInteger("linkedZ", linkedPos.getZ());
        nbt.setBoolean("hasChevronUpgrade", hasChevronUpgrade);
        if (connectedLocation != null) nbt.setTag("connectedLocation", connectedLocation.toNBT());
        nbt.setBoolean("isInitiator", isInitiator);
        nbt.setInteger("timeout", timeout);
        nbt.setDouble("energyInBuffer", energyInBuffer);
        nbt.setDouble("distanceFactor", distanceFactor);
        nbt.setBoolean("hasIrisUpgrade", hasIrisUpgrade);
        nbt.setInteger("irisState", irisState.ordinal());
        nbt.setInteger("irisPhase", irisPhase);
        nbt.setBoolean("redstoneInput", redstoneInput);
        if (homeAddress != null) nbt.setString("address", homeAddress);
        if (addressError != null) nbt.setString("addressError", addressError);
    }

    public boolean isActive() {
        return state != SGState.Idle && state != SGState.Disconnecting;
    }

    static boolean isValidSymbolChar(String c) {
        return SGAddressing.isValidSymbolChar(c);
    }

    static char symbolToChar(int i) {
        return SGAddressing.symbolToChar(i);
    }

    static int charToSymbol(char c) {
        return SGAddressing.charToSymbol(c);
    }

    static int charToSymbol(String c) {
        return SGAddressing.charToSymbol(c);
    }

    public boolean applyChevronUpgrade(ItemStack stack, EntityPlayer player) {
        if (!worldObj.isRemote && !hasChevronUpgrade && stack.stackSize > 0) {
            hasChevronUpgrade = true;
            stack.stackSize -= 1;
            markChanged();
        }
        return true;
    }

    public boolean applyIrisUpgrade(ItemStack stack, EntityPlayer player) {
        if (!worldObj.isRemote && !hasIrisUpgrade && stack.stackSize > 0) {
            hasIrisUpgrade = true;
            stack.stackSize -= 1;
            markChanged();
            updateIrisEntity();
        }
        return true;
    }

    public int getNumChevrons() {
        if (hasChevronUpgrade) return 9;
        return 7;
    }

    public boolean chevronIsEngaged(int i) {
        return i < numEngagedChevrons;
    }

    public float angleBetweenChevrons() {
        if (variableChevronPositions) {
            int c9 = getNumChevrons() > 7 ? 1 : 0;
            int bc = baseCornerCamouflage();
            return chevronAngles[c9][bc];
        }
        return defaultChevronAngle;
    }

    Item getItemInSlot(int slot) {
        ItemStack stack = getStackInSlot(slot);
        return stack != null ? stack.getItem() : null;
    }

    public String getHomeAddress() throws SGAddressing.AddressingError {
        return SGAddressing.addressForLocation(new SGLocation(this));
    }

    public SGBaseBlock getBlock() {
        return (SGBaseBlock) getBlockType();
    }

    public double interpolatedRingAngle(double t) {
        return Utils.interpolateAngle(lastRingAngle, ringAngle, t);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        tick();
    }

    public void tick() {
        if (worldObj.isRemote) clientUpdate();
        else {
            serverUpdate();
            checkForEntitiesInPortal();
        }
        irisUpdate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!worldObj.isRemote && ocWirelessEndpoint != null) // [OC]
            ocWirelessEndpoint.remove();
    }

    String side() {
        return worldObj.isRemote ? "Client" : "Server";
    }

    void enterState(SGState newState, int newTimeout) {
        if (debugState) SGCraft.log.debug(
                String.format(
                        "SGBaseTE: at %s in dimension %s entering state %s with timeout %s",
                        getPos(),
                        getWorldDimensionId(worldObj),
                        newState,
                        newTimeout));
        SGState oldState = state;
        state = newState;
        timeout = newTimeout;
        markChanged();
        if ((oldState == SGState.Idle) != (newState == SGState.Idle)) {
            updateChunkLoadingStatus();
            notifyWorldNeighborsOfStateChange(worldObj, getPos(), getBlockType());
        }
        String oldDesc = sgStateDescription(oldState);
        String newDesc = sgStateDescription(newState);
        if (!oldDesc.equals(newDesc)) postEvent("sgStargateStateChange", newDesc, oldDesc);
    }

    public boolean isConnected() {
        return state == SGState.Transient || state == SGState.Connected || state == SGState.Disconnecting;
    }

    DHDTE getLinkedControllerTE() {
        if (isLinkedToController) {
            TileEntity cte = getWorldTileEntity(worldObj, linkedPos);
            if (cte instanceof DHDTE) return (DHDTE) cte;
        }
        return null;
    }

    void checkForLink() {
        int rangeXY = max(DHDTE.linkRangeX, DHDTE.linkRangeY);
        int rangeZ = DHDTE.linkRangeZ;
        if (SGBaseBlock.debugMerge) SGCraft.log.debug(
                String.format(
                        "SGBaseTE.checkForLink: in range +/-(%d,%d,%d) of %s",
                        rangeXY,
                        rangeZ,
                        rangeXY,
                        getPos()));
        for (int i = -rangeXY; i <= rangeXY; i++)
            for (int j = -rangeZ; j <= rangeZ; j++) for (int k = -rangeXY; k <= rangeXY; k++) {
                TileEntity te = getWorldTileEntity(worldObj, getPos().add(i, j, k));
                if (te instanceof DHDTE) ((DHDTE) te).checkForLink();
            }
    }

    public void unlinkFromController() {
        if (isLinkedToController) {
            DHDTE cte = getLinkedControllerTE();
            if (cte != null) cte.clearLinkToStargate();
            clearLinkToController();
        }
    }

    public void clearLinkToController() {
        if (SGBaseBlock.debugMerge)
            SGCraft.log.debug(String.format("SGBaseTE: Unlinking stargate at %s from controller", getPos()));
        isLinkedToController = false;
        markDirty();
    }

    public void connectOrDisconnect(String address, EntityPlayer player) {
        if (debugConnect) SGCraft.log.debug(
                String.format(
                        "SGBaseTE: %s: connectOrDisconnect('%s') in state %s by %s",
                        side(),
                        address,
                        state,
                        player));
        if (address.length() > 0) connect(address, player);
        else attemptToDisconnect(player);
    }

    public String attemptToDisconnect(EntityPlayer player) {
        boolean canDisconnect = disconnectionAllowed();
        SGBaseTE dte = getConnectedStargateTE();
        boolean validConnection = (dte != null) && !dte.isInvalid() && (dte.getConnectedStargateTE() == this);
        if (canDisconnect || !validConnection) {
            if (state != SGState.Disconnecting) disconnect();
            return null;
        }
        return operationFailure(player, "Connection initiated from other end");
    }

    public boolean disconnectionAllowed() {
        return isInitiator || closeFromEitherEnd;
    }

    String connect(String address, EntityPlayer player) {
        SGBaseTE dte;
        if (state != SGState.Idle) return diallingFailure(player, "Stargate is busy");
        String homeAddress = findHomeAddress();
        if (homeAddress.equals("")) return diallingFailure(player, "Coordinates of dialling stargate are out of range");
        try {
            dte = SGAddressing.findAddressedStargate(address, worldObj);
        } catch (SGAddressing.AddressingError e) {
            return diallingFailure(player, e.getMessage());
        }
        if (dte == null || !dte.isMerged) return diallingFailure(player, "No stargate at address " + address);
        if (worldObj == getTileEntityWorld(dte)) {
            address = SGAddressing.localAddress(address);
            homeAddress = SGAddressing.localAddress(homeAddress);
        }
        if (address.length() > getNumChevrons())
            return diallingFailure(player, "Not enough chevrons to dial " + address);
        if (dte == this) return diallingFailure(player, "Stargate cannot connect to itself");
        if (debugConnect) SGCraft.log.debug(
                String.format(
                        "SGBaseTE.connect: to %s in dimension %d with state %s",
                        dte.getPos(),
                        getWorldDimensionId(getTileEntityWorld(dte)),
                        dte.state));
        if (dte.getNumChevrons() < homeAddress.length())
            return diallingFailure(player, "Destination stargate has insufficient chevrons");
        if (dte.state != SGState.Idle) return diallingFailure(player, "Stargate at address " + address + " is busy");
        distanceFactor = distanceFactorForCoordDifference(this, dte);
        if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTE: distanceFactor = %s", distanceFactor));
        if (!energyIsAvailable(energyToOpen * distanceFactor))
            return diallingFailure(player, "Stargate has insufficient energy");
        startDiallingStargate(address, dte, true);
        dte.startDiallingStargate(homeAddress, this, false);
        return null;
    }

    public static double distanceFactorForCoordDifference(TileEntity te1, TileEntity te2) {
        BlockPos pos1 = getTileEntityPos(te1);
        BlockPos pos2 = getTileEntityPos(te2);
        double dx = pos1.getX() - pos2.getX();
        double dz = pos1.getZ() - pos2.getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTE: Connection distance = %s", d));
        double ld = Math.log(0.05 * d + 1);
        double lm = Math.log(0.05 * 16 * SGAddressing.coordRange);
        double lr = ld / lm;
        double f = 1 + 14 * distanceFactorMultiplier * lr * lr;
        if (getTileEntityWorld(te1) != getTileEntityWorld(te2)) f *= interDimensionMultiplier;
        return f;
    }

    public void playSGSoundEffect(String name, float volume, float pitch) {
        playSoundEffect(name, volume * soundVolume, pitch);
    }

    String diallingFailure(EntityPlayer player, String mess) {
        if (player != null) {
            if (state == SGState.Idle) playSGSoundEffect("sgcraft:sg_abort", 1.0F, 1.0F);
        }
        return operationFailure(player, mess);
    }

    String operationFailure(EntityPlayer player, String mess) {
        if (player != null) sendChatMessage(player, mess);
        return mess;
    }

    static void sendChatMessage(EntityPlayer player, String mess) {
        player.addChatMessage(new ChatComponentText(mess));
    }

    String findHomeAddress() {
        String homeAddress;
        try {
            return getHomeAddress();
        } catch (SGAddressing.AddressingError e) {
            SGCraft.log.error(String.format("SGBaseTE.findHomeAddress: %s", e));
            return "";
        }
    }

    public void disconnect() {
        if (debugConnect) SGCraft.log.debug(String.format("SGBaseTE: %s: disconnect()", side()));
        SGBaseTE dte = SGBaseTE.at(connectedLocation);
        if (dte != null) dte.clearConnection();
        clearConnection();
    }

    public void clearConnection() {
        if (state != SGState.Idle || connectedLocation != null) {
            dialledAddress = "";
            connectedLocation = null;
            isInitiator = false;
            numEngagedChevrons = 0;
            markChanged();
            if (state == SGState.Connected) {
                enterState(SGState.Disconnecting, disconnectTime);
                playSGSoundEffect("sgcraft:sg_close", 1.0F, 1.0F);
            } else if (state != SGState.Idle && state != SGState.Disconnecting) {
                playSGSoundEffect("sgcraft:sg_abort", 1.0F, 1.0F);
                enterState(SGState.Idle, 0);
            }
        }
    }

    void startDiallingStargate(String address, SGBaseTE dte, boolean initiator) {
        dialledAddress = address;
        connectedLocation = new SGLocation(dte);
        isInitiator = initiator;
        markDirty();
        startDiallingNextSymbol();
        postEvent(initiator ? "sgDialOut" : "sgDialIn", address);
    }

    void serverUpdate() {
        if (!loaded) {
            loaded = true;
            try {
                homeAddress = getHomeAddress();
                addressError = "";
            } catch (SGAddressing.AddressingError e) {
                homeAddress = null;
                addressError = e.getMessage();
            }
            if (SGCraft.ocIntegration != null) // [OC]
                SGCraft.ocIntegration.onSGBaseTEAdded(this);
        }
        if (!isMerged) {
            return;
        }

        if (debugState && state != SGState.Connected && timeout > 0) {
            int dimension = getWorldDimensionId(worldObj);
            SGCraft.log.debug(
                    String.format(
                            "SGBaseTE.serverUpdate at %s in dimension %d: state %s, timeout %s",
                            getPos(),
                            dimension,
                            state,
                            timeout));
        }
        tickEnergyUsage();
        if (timeout > 0) {
            if (state == SGState.Transient && !irisIsClosed()) performTransientDamage();
            --timeout;
        } else {
            switch (state) {
                case Idle:
                    if (undialledDigitsRemaining()) startDiallingNextSymbol();
                    break;
                case Dialling:
                    finishDiallingSymbol();
                    break;
                case InterDialling:
                    startDiallingNextSymbol();
                    break;
                case Transient:
                    enterState(SGState.Connected, isInitiator ? ticksToStayOpen : 0);
                    break;
                case Connected:
                    if (isInitiator && ticksToStayOpen > 0) disconnect();
                    break;
                case Disconnecting:
                    enterState(SGState.Idle, 0);
                    break;
            }
        }

    }

    void tickEnergyUsage() {
        if (state == SGState.Connected && isInitiator) if (!useEnergy(energyUsePerTick * distanceFactor)) disconnect();
    }

    double availableEnergy() {
        List<ISGEnergySource> sources = findEnergySources();
        return energyInBuffer + energyAvailableFrom(sources);
    }

    boolean energyIsAvailable(double amount) {
        double energy = availableEnergy();
        if (debugEnergyUse)
            SGCraft.log.debug(String.format("SGBaseTE.energyIsAvailable: need %s, have %s", amount, energy));
        return energy >= amount;
    }

    boolean useEnergy(double amount) {
        if (debugEnergyUse)
            SGCraft.log.debug(String.format("SGBaseTE.useEnergy: %s; buffered: %s", amount, energyInBuffer));
        if (amount <= energyInBuffer) {
            energyInBuffer -= amount;
            return true;
        }
        List<ISGEnergySource> sources = findEnergySources();
        double energyAvailable = energyInBuffer + energyAvailableFrom(sources);
        if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTE.useEnergy: %s available", energyAvailable));
        if (amount > energyAvailable) {
            SGCraft.log.debug("SGBaseTE: Not enough energy available");
            return false;
        }
        double desiredEnergy = max(amount, maxEnergyBuffer);
        double targetEnergy = min(desiredEnergy, energyAvailable);
        double energyRequired = targetEnergy - energyInBuffer;
        if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTE.useEnergy: another %s required", energyRequired));
        double energyOnHand = energyInBuffer + drawEnergyFrom(sources, energyRequired);
        if (debugEnergyUse)
            SGCraft.log.debug(String.format("SGBaseTE.useEnergy: %s now on hand, need %s", energyOnHand, amount));
        if (amount - 0.0001 > energyOnHand) {
            SGCraft.log.debug(
                    String.format(
                            "SGBaseTE: Energy sources only delivered %s of promised %s",
                            energyOnHand - energyInBuffer,
                            energyAvailable));
            return false;
        }
        setEnergyInBuffer(energyOnHand - amount);
        if (debugEnergyUse)
            SGCraft.log.debug(String.format("SGBaseTE.useEnergy: %s left over in buffer", energyInBuffer));
        return true;
    }

    List<ISGEnergySource> findEnergySources() {
        if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTe.findEnergySources: for %s", getPos()));
        List<ISGEnergySource> result = new ArrayList<ISGEnergySource>();
        Trans3 t = localToGlobalTransformation();
        for (int i = -2; i <= 2; i++) {
            BlockPos bp = t.p(i, -1, 0).blockPos();
            if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTE.findEnergySources: Checking %s", bp));
            TileEntity nte = getWorldTileEntity(worldObj, bp);
            if (nte instanceof ISGEnergySource) result.add((ISGEnergySource) nte);
        }
        DHDTE te = getLinkedControllerTE();
        if (te != null) result.add(te);
        return result;
    }

    double energyAvailableFrom(List<ISGEnergySource> sources) {
        double energy = 0;
        for (ISGEnergySource source : sources) {
            double e = source.availableEnergy();
            if (debugEnergyUse)
                SGCraft.log.debug(String.format("SGBaseTe.energyAvailableFrom: %s can supply %s", source, e));
            energy += e;
        }
        return energy;
    }

    double drawEnergyFrom(List<ISGEnergySource> sources, double amount) {
        double total = 0;
        for (ISGEnergySource source : sources) {
            if (total >= amount) break;
            double e = source.drawEnergy(amount - total);
            if (debugEnergyUse) SGCraft.log.debug(String.format("SGBaseTe.drawEnergyFrom: %s supplied %s", source, e));
            total += e;
        }
        if (total < amount) SGCraft.log.info(
                String.format(
                        "SGCraft: Warning: Energy sources did not deliver promised energy (%s requested, %s delivered)",
                        amount,
                        total));
        return total;
    }

    void setEnergyInBuffer(double amount) {
        if (energyInBuffer != amount) {
            energyInBuffer = amount;
            markDirty();
        }
    }

    void performTransientDamage() {
        Trans3 t = localToGlobalTransformation();
        Vector3 p0 = t.p(-1.5, 0.5, 0.5);
        Vector3 p1 = t.p(1.5, 3.5, 5.5);
        Vector3 q0 = p0.min(p1);
        Vector3 q1 = p0.max(p1);
        AxisAlignedBB box = newAxisAlignedBB(q0.x, q0.y, q0.z, q1.x, q1.y, q1.z);
        if (debugTransientDamage) {
            SGCraft.log.debug("SGBaseTE.performTransientDamage: players in world:");
            for (Entity ent : (List<Entity>) worldObj.loadedEntityList)
                if (ent instanceof EntityPlayer) SGCraft.log.debug(String.format("--- %s", ent));
            SGCraft.log.debug(String.format("SGBaseTE.performTransientDamage: box = %s", box));
        }
        List<EntityLivingBase> ents = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase ent : ents) {
            Vector3 ep = new Vector3(ent.posX, ent.posY, ent.posZ);
            Vector3 gp = t.p(0, 2, 0.5);
            double dist = ep.distance(gp);
            if (debugTransientDamage)
                SGCraft.log.debug(String.format("SGBaseTE.performTransientDamage: found %s", ent));
            if (dist > 1.0) dist = 1.0;
            int damage = (int) Math.ceil(dist * transientDamageRate);
            if (debugTransientDamage) SGCraft.log
                    .debug(String.format("SGBaseTE.performTransientDamage: distance = %s, damage = %s", dist, damage));
            ent.attackEntityFrom(transientDamage, damage);
        }
    }

    boolean undialledDigitsRemaining() {
        int n = numEngagedChevrons;
        return n < dialledAddress.length();
    }

    void startDiallingNextSymbol() {
        if (debugState) SGCraft.log
                .debug(String.format("SGBaseTE.startDiallingNextSymbol: %s of %s", numEngagedChevrons, dialledAddress));
        startDiallingSymbol(dialledAddress.charAt(numEngagedChevrons));
    }

    void startDiallingSymbol(char c) {
        int i = SGAddressing.charToSymbol(c);
        if (debugState) SGCraft.log.debug(String.format("SGBaseTE.startDiallingSymbol: %s", i));
        if (i >= 0 && i < numRingSymbols) {
            startDiallingToAngle(i * ringSymbolAngle);
            playSGSoundEffect(diallingSound, 1.0F, 1.0F);
        } else {
            SGCraft.log.info(String.format("SGCraft: Stargate jammed trying to dial symbol %s", c));
            dialledAddress = "";
            enterState(SGState.Idle, 0);
        }
    }

    void startDiallingToAngle(double a) {
        targetRingAngle = Utils.normaliseAngle(a);
        enterState(SGState.Dialling, diallingTime);
    }

    void finishDiallingSymbol() {
        ++numEngagedChevrons;
        String symbol = dialledAddress.substring(numEngagedChevrons - 1, numEngagedChevrons);
        postEvent("sgChevronEngaged", numEngagedChevrons, symbol);
        if (undialledDigitsRemaining()) enterState(SGState.InterDialling, interDiallingTime);
        else finishDiallingAddress();
    }

    void finishDiallingAddress() {
        if (!isInitiator || useEnergy(energyToOpen * distanceFactor)) {
            enterState(SGState.Transient, transientDuration);
            playSGSoundEffect("sgcraft:sg_open", 1.0F, 1.0F);
        } else disconnect();
    }

    boolean canTravelFromThisEnd() {
        return isInitiator || !oneWayTravel;
    }

    static String repr(Entity entity) {
        if (entity != null) {
            String s = String.format("%s#%s", entity.getClass().getSimpleName(), entity.getEntityId());
            if (entity.isDead) s += "(dead)";
            return s;
        }
        return "null";
    }

    class TrackedEntity {

        public Entity entity;
        public Vector3 lastPos;

        public TrackedEntity(Entity entity) {
            this.entity = entity;
            this.lastPos = new Vector3(entity.posX, entity.posY, entity.posZ);
        }

    }

    List<TrackedEntity> trackedEntities = new ArrayList<TrackedEntity>();

    void checkForEntitiesInPortal() {
        if (state != SGState.Connected) {
            trackedEntities.clear();
            return;
        }

        for (TrackedEntity trk : trackedEntities) entityInPortal(trk.entity, trk.lastPos);
        trackedEntities.clear();
        Vector3 p0 = new Vector3(-1.5, 0.5, -3.5);
        Vector3 p1 = new Vector3(1.5, 3.5, 3.5);
        Trans3 t = localToGlobalTransformation();
        AxisAlignedBB box = t.box(p0, p1);
        List<Entity> ents = (List<Entity>) worldObj.getEntitiesWithinAABB(Entity.class, box);
        for (Entity entity : ents) {
            if (entity instanceof EntityFishHook) continue;
            if (!entity.isDead && entity.ridingEntity == null) {
                trackedEntities.add(new TrackedEntity(entity));
            }
        }

    }

    public void entityInPortal(Entity entity, Vector3 prevPos) {
        if (entity.isDead || state != SGState.Connected || !canTravelFromThisEnd()) {
            return;
        }

        Trans3 t = localToGlobalTransformation();
        double vx = entity.posX - prevPos.x;
        double vy = entity.posY - prevPos.y;
        double vz = entity.posZ - prevPos.z;
        Vector3 p1 = t.ip(entity.posX, entity.posY, entity.posZ);
        Vector3 p0 = t.ip(2 * prevPos.x - entity.posX, 2 * prevPos.y - entity.posY, 2 * prevPos.z - entity.posZ);
        double z0 = 0.0;
        if (p0.z < z0 || p1.z >= z0 || p1.z <= z0 - 5.0) {
            return;
        }

        entity.motionX = vx;
        entity.motionY = vy;
        entity.motionZ = vz;
        SGBaseTE dte = getConnectedStargateTE();
        if (dte == null) {
            return;
        }

        Trans3 dt = dte.localToGlobalTransformation();
        while (entity.ridingEntity != null) entity = entity.ridingEntity;
        teleportEntityAndRider(entity, t, dt, connectedLocation.dimension, dte.irisIsClosed());
    }

    Entity teleportEntityAndRider(Entity entity, Trans3 t1, Trans3 t2, int dimension, boolean destBlocked) {
        if (debugTeleport)
            SGCraft.log.debug(String.format("SGBaseTE.teleportEntityAndRider: destBlocked = %s", destBlocked));
        Entity rider = entity.riddenByEntity;
        if (rider != null) {
            rider.mountEntity(null);
            rider = teleportEntityAndRider(rider, t1, t2, dimension, destBlocked);
        }
        unleashEntity(entity);
        entity = teleportEntity(entity, t1, t2, dimension, destBlocked);
        if (entity != null && !entity.isDead && rider != null && !rider.isDead) {
            rider.mountEntity(entity);
        }
        return entity;
    }

    // Break any leash connections to or from the given entity. That happens anyway
    // when the entity is teleported, but without this it drops an extra leash item.
    protected static void unleashEntity(Entity entity) {
        if (entity instanceof EntityLiving) ((EntityLiving) entity).clearLeashed(true, false);
        for (EntityLiving entity2 : entitiesWithinLeashRange(entity))
            if (entity2.getLeashed() && entity2.getLeashedToEntity() == entity) entity2.clearLeashed(true, false);
    }

    protected static List<EntityLiving> entitiesWithinLeashRange(Entity entity) {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                entity.posX - 7.0D,
                entity.posY - 7.0D,
                entity.posZ - 7.0D,
                entity.posX + 7.0D,
                entity.posY + 7.0D,
                entity.posZ + 7.0D);
        return entity.worldObj.getEntitiesWithinAABB(EntityLiving.class, box);
    }

    static Entity teleportEntity(Entity entity, Trans3 t1, Trans3 t2, int dimension, boolean destBlocked) {
        Entity newEntity = null;
        if (debugTeleport) {
            SGCraft.log.debug(
                    String.format(
                            "SGBaseTE.teleportEntity: %s (in dimension %d) to dimension %d",
                            repr(entity),
                            entity.dimension,
                            dimension));
            SGCraft.log.debug(
                    String.format(
                            "SGBaseTE.teleportEntity: pos (%.2f, %.2f, %.2f) prev (%.2f, %.2f, %.2f) last (%.2f, %.2f, %.2f)",
                            entity.posX,
                            entity.posY,
                            entity.posZ,
                            entity.prevPosX,
                            entity.prevPosY,
                            entity.prevPosZ,
                            entity.lastTickPosX,
                            entity.lastTickPosY,
                            entity.lastTickPosZ));
        }
        Vector3 p = t1.ip(entity.posX, entity.posY, entity.posZ); // local position
        Vector3 v = t1.iv(entity.motionX, entity.motionY, entity.motionZ); // local velocity
        Vector3 r = t1.iv(yawVector(entity)); // local facing
        Vector3 q = t2.p(-p.x, p.y, -p.z); // new global position
        Vector3 u = t2.v(-v.x, v.y, -v.z); // new global velocity
        Vector3 s = t2.v(r.mul(-1)); // new global facing
        if (debugTeleport) SGCraft.log.debug(String.format("SGBaseTE.teleportEntity: Facing old %s new %s", r, s));
        double a = yawAngle(s, entity); // new global yaw angle
        if (debugTeleport) SGCraft.log.debug(String.format("SGBaseTE.teleportEntity: new yaw %.2f", a));
        if (!destBlocked) {
            if (entity.dimension == dimension) newEntity = teleportWithinDimension(entity, q, u, a, destBlocked);
            else {
                newEntity = teleportToOtherDimension(entity, q, u, a, dimension, destBlocked);
                if (newEntity != null) newEntity.dimension = dimension;
            }
        } else {
            terminateEntityByIrisImpact(entity);
            playIrisHitSound(worldForDimension(dimension), q, entity);
        }
        return newEntity;
    }

    static void terminateEntityByIrisImpact(Entity entity) {
        if (entity instanceof EntityPlayer) terminatePlayerByIrisImpact((EntityPlayer) entity);
        else entity.setDead();
    }

    static void terminatePlayerByIrisImpact(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) sendChatMessage(player, "Destination blocked by iris");
        else {
            if (!(preserveInventory || getGameRuleBoolean(player.worldObj.getGameRules(), "keepInventory")))
                BaseInventoryUtils.clearInventory(player.inventory);
            player.attackEntityFrom(irisDamageSource, irisDamageAmount);
        }
    }

    static WorldServer worldForDimension(int dimension) {
        MinecraftServer server = MinecraftServer.getServer();
        return server.worldServerForDimension(dimension);
    }

    static void playIrisHitSound(World world, Vector3 pos, Entity entity) {
        double volume = min(entity.width * entity.height, 1.0);
        double pitch = 2.0 - volume;
        if (debugTeleport) SGCraft.log.debug(
                String.format(
                        "SGBaseTE.playIrisHitSound: at (%.3f,%.3f,%.3f) volume %.3f pitch %.3f",
                        pos.x,
                        pos.y,
                        pos.z,
                        volume,
                        pitch));
        world.playSoundEffect(pos.x, pos.y, pos.z, "sgcraft:iris_hit", (float) volume, (float) pitch);
    }

    static Entity teleportWithinDimension(Entity entity, Vector3 p, Vector3 v, double a, boolean destBlocked) {
        if (entity instanceof EntityPlayerMP) return teleportPlayerWithinDimension((EntityPlayerMP) entity, p, v, a);
        else return teleportEntityToWorld(entity, p, v, a, (WorldServer) entity.worldObj, destBlocked);
    }

    static Entity teleportPlayerWithinDimension(EntityPlayerMP entity, Vector3 p, Vector3 v, double a) {
        entity.rotationYaw = (float) a;
        entity.setPositionAndUpdate(p.x, p.y, p.z);
        entity.worldObj.updateEntityWithOptionalForce(entity, false);
        return entity;
    }

    static Entity teleportToOtherDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension,
            boolean destBlocked) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            Vector3 q = p.add(yawVector(a));
            transferPlayerToDimension(player, dimension, q, a);
            return player;
        }
        return teleportEntityToDimension(entity, p, v, a, dimension, destBlocked);
    }

    static void sendDimensionRegister(EntityPlayerMP player, int dimensionID) {
        int providerID = DimensionManager.getProviderType(dimensionID);
        ForgeMessage msg = new ForgeMessage.DimensionRegisterMessage(dimensionID, providerID);
        FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    static void transferPlayerToDimension(EntityPlayerMP player, int newDimension, Vector3 p, double a) {
        MinecraftServer server = MinecraftServer.getServer();
        ServerConfigurationManager scm = server.getConfigurationManager();
        int oldDimension = player.dimension;
        player.dimension = newDimension;
        WorldServer oldWorld = server.worldServerForDimension(oldDimension);
        WorldServer newWorld = server.worldServerForDimension(newDimension);
        sendDimensionRegister(player, newDimension);
        player.closeScreen();
        player.playerNetServerHandler.sendPacket(
                new S07PacketRespawn(
                        player.dimension,
                        getWorldDifficulty(player.worldObj),
                        newWorld.getWorldInfo().getTerrainType(),
                        player.theItemInWorldManager.getGameType()));
        if (SGCraft.mystcraftIntegration != null) // [MYST]
            SGCraft.mystcraftIntegration.sendAgeData(newWorld, player);
        oldWorld.removePlayerEntityDangerously(player); // Removes player right now instead of waiting for next tick
        player.isDead = false;
        player.setLocationAndAngles(p.x, p.y, p.z, (float) a, player.rotationPitch);
        newWorld.spawnEntityInWorld(player);
        player.setWorld(newWorld);
        scmPreparePlayer(scm, player, oldWorld);
        player.playerNetServerHandler.setPlayerLocation(p.x, p.y, p.z, (float) a, player.rotationPitch);
        player.theItemInWorldManager.setWorld(newWorld);
        scm.updateTimeAndWeatherForPlayer(player, newWorld);
        scm.syncPlayerInventory(player);
        Iterator var6 = player.getActivePotionEffects().iterator();
        while (var6.hasNext()) {
            PotionEffect effect = (PotionEffect) var6.next();
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), effect));
        }
        player.playerNetServerHandler.sendPacket(
                new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldDimension, newDimension);
    }

    static Entity teleportEntityToDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension,
            boolean destBlocked) {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer world = server.worldServerForDimension(dimension);
        return teleportEntityToWorld(entity, p, v, a, world, destBlocked);
    }

    static Entity teleportEntityToWorld(Entity oldEntity, Vector3 p, Vector3 v, double a, WorldServer newWorld,
            boolean destBlocked) {
        if (debugTeleport) SGCraft.log.debug(
                String.format(
                        "SGBaseTE.teleportEntityToWorld: %s to %s, destBlocked = %s",
                        repr(oldEntity),
                        newWorld,
                        destBlocked));
        WorldServer oldWorld = (WorldServer) oldEntity.worldObj;
        NBTTagCompound nbt = new NBTTagCompound();
        oldEntity.writeToNBT(nbt);
        extractEntityFromWorld(oldWorld, oldEntity);
        if (destBlocked) {
            if (!(oldEntity instanceof EntityLivingBase)) return null;
        }
        Entity newEntity = instantiateEntityFromNBT(oldEntity.getClass(), nbt, newWorld);
        if (newEntity != null) {
            if (oldEntity instanceof EntityLiving)
                copyMoreEntityData((EntityLiving) oldEntity, (EntityLiving) newEntity);
            setVelocity(newEntity, v);
            newEntity.setLocationAndAngles(p.x, p.y, p.z, (float) a, oldEntity.rotationPitch);
            checkChunk(newWorld, newEntity);
            newEntity.forceSpawn = true; // Force spawn packet to be sent as soon as possible
            newWorld.spawnEntityInWorld(newEntity);
            newEntity.setWorld(newWorld);
            if (debugTeleport) SGCraft.log.debug(
                    String.format(
                            "SGBaseTE.teleportEntityToWorld: Spawned %s pos (%.2f, %.2f, %.2f) vel (%.2f, %.2f, %.2f) pitch %.2f (%.2f) yaw %.2f (%.2f)",
                            repr(newEntity),
                            newEntity.posX,
                            newEntity.posY,
                            newEntity.posZ,
                            newEntity.motionX,
                            newEntity.motionY,
                            newEntity.motionZ,
                            newEntity.rotationPitch,
                            newEntity.prevRotationPitch,
                            newEntity.rotationYaw,
                            newEntity.prevRotationYaw));
        }
        oldWorld.resetUpdateEntityTick();
        if (oldWorld != newWorld) newWorld.resetUpdateEntityTick();
        return newEntity;
    }

    static Entity instantiateEntityFromNBT(Class cls, NBTTagCompound nbt, WorldServer world) {
        try {
            Entity entity = (Entity) cls.getConstructor(World.class).newInstance(world);
            entity.readFromNBT(nbt);
            return entity;
        } catch (Exception e) {
            SGCraft.log.error(
                    String.format("SGCraft: SGBaseTE.instantiateEntityFromNBT: Could not instantiate %s: %s", cls, e));
            e.printStackTrace();
            return null;
        }
    }

    static void copyMoreEntityData(EntityLiving oldEntity, EntityLiving newEntity) {
        float s = oldEntity.getAIMoveSpeed();
        if (s != 0) newEntity.setAIMoveSpeed(s);
    }

    static void setVelocity(Entity entity, Vector3 v) {
        entity.motionX = v.x;
        entity.motionY = v.y;
        entity.motionZ = v.z;
    }

    static void extractEntityFromWorld(World world, Entity entity) {
        // Immediately remove entity from world without calling setDead(), which has
        // undesirable side effects on some entities.
        if (entity instanceof EntityPlayer) {
            world.playerEntities.remove(entity);
            world.updateAllPlayersSleepingFlag();
        }
        int i = entity.chunkCoordX;
        int j = entity.chunkCoordZ;
        if (entity.addedToChunk && world.getChunkProvider().chunkExists(i, j))
            world.getChunkFromChunkCoords(i, j).removeEntity(entity);
        world.loadedEntityList.remove(entity);
        world.onEntityRemoved(entity);
    }

    static void checkChunk(World world, Entity entity) {
        int cx = MathHelper.floor_double(entity.posX / 16.0D);
        int cy = MathHelper.floor_double(entity.posZ / 16.0D);
        Chunk chunk = world.getChunkFromChunkCoords(cx, cy);
    }

    protected static int yawSign(Entity entity) {
        if (entity instanceof EntityArrow) return -1;
        return 1;
    }

    static Vector3 yawVector(Entity entity) {
        return yawVector(yawSign(entity) * entity.rotationYaw);
    }

    static Vector3 yawVector(double yaw) {
        double a = Math.toRadians(yaw);
        return new Vector3(-Math.sin(a), 0, Math.cos(a));
    }

    static double yawAngle(Vector3 v, Entity entity) {
        double a = Math.atan2(-v.x, v.z);
        double d = Math.toDegrees(a);
        return yawSign(entity) * d;
    }

    public SGBaseTE getConnectedStargateTE() {
        if (isConnected() && connectedLocation != null) return connectedLocation.getStargateTE();
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        SGState oldState = state;
        super.onDataPacket(net, pkt);
        if (!isMerged || state == oldState) {
            return;
        }

        switch (state) {
            case Transient:
                initiateOpeningTransient();
                break;
            case Disconnecting:
                initiateClosingTransient();
                break;
        }
    }

    void clientUpdate() {
        lastRingAngle = ringAngle;
        switch (state) {
            case Dialling:
                updateRingAngle();
                break;
            case Transient:
            case Connected:
            case Disconnecting:
                applyRandomImpulse();
                updateEventHorizon();
                break;
        }
    }

    void setRingAngle(double a) {
        ringAngle = a;
    }

    void updateRingAngle() {
        if (timeout > 0) {
            double da = Utils.diffAngle(ringAngle, targetRingAngle) / timeout;
            setRingAngle(Utils.addAngle(ringAngle, da));
            --timeout;
        } else setRingAngle(targetRingAngle);
    }

    public double[][][] getEventHorizonGrid() {
        if (ehGrid == null) {
            int m = SGBaseTERenderer.ehGridRadialSize;
            int n = SGBaseTERenderer.ehGridPolarSize;
            ehGrid = new double[2][n + 2][m + 1];
            for (int i = 0; i < 2; i++) {
                ehGrid[i][0] = ehGrid[i][n];
                ehGrid[i][n + 1] = ehGrid[i][1];
            }
        }
        return ehGrid;
    }

    void initiateOpeningTransient() {
        double[][] v = getEventHorizonGrid()[1];
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int j = 0; j <= n + 1; j++) {
            v[j][0] = openingTransientIntensity;
            v[j][1] = v[j][0] + openingTransientRandomness * random.nextGaussian();
        }
    }

    void initiateClosingTransient() {
        double[][] v = getEventHorizonGrid()[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++) v[j][i] += closingTransientRandomness * random.nextGaussian();
    }

    void applyRandomImpulse() {
        double[][] v = getEventHorizonGrid()[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        int i = random.nextInt(m - 1) + 1;
        int j = random.nextInt(n) + 1;
        v[j][i] += 0.05 * random.nextGaussian();
    }

    void updateEventHorizon() {
        double[][][] grid = getEventHorizonGrid();
        double[][] u = grid[0];
        double[][] v = grid[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        double dt = 1.0;
        double asq = 0.03;
        double d = 0.95;
        for (int i = 1; i < m; i++) for (int j = 1; j <= n; j++) {
            double du_dr = 0.5 * (u[j][i + 1] - u[j][i - 1]);
            double d2u_drsq = u[j][i + 1] - 2 * u[j][i] + u[j][i - 1];
            double d2u_dthsq = u[j + 1][i] - 2 * u[j][i] + u[j - 1][i];
            v[j][i] = d * v[j][i] + (asq * dt) * (d2u_drsq + du_dr / i + d2u_dthsq / (i * i));
        }
        for (int i = 1; i < m; i++) for (int j = 1; j <= n; j++) u[j][i] += v[j][i] * dt;
        double u0 = 0, v0 = 0;
        for (int j = 1; j <= n; j++) {
            u0 += u[j][1];
            v0 += v[j][1];
        }
        u0 /= n;
        v0 /= n;
        for (int j = 1; j <= n; j++) {
            u[j][0] = u0;
            v[j][0] = v0;
        }
    }

    @Override
    protected IInventory getInventory() {
        return inventory;
    }

    public boolean irisIsClosed() {
        return hasIrisUpgrade && irisPhase <= maxIrisPhase / 2;
    }

    public double getIrisAperture(double t) {
        return (lastIrisPhase * (1 - t) + irisPhase * t) / maxIrisPhase;
    }

    void irisUpdate() {
        lastIrisPhase = irisPhase;
        switch (irisState) {
            case Opening:
                if (irisPhase < maxIrisPhase) ++irisPhase;
                else enterIrisState(IrisState.Open);
                break;
            case Closing:
                if (irisPhase > 0) --irisPhase;
                else enterIrisState(IrisState.Closed);
                break;
        }
    }

    void enterIrisState(IrisState newState) {
        if (irisState != newState) {
            String oldDesc = irisStateDescription(irisState);
            String newDesc = irisStateDescription(newState);
            irisState = newState;
            markChanged();
            if (!worldObj.isRemote) {
                switch (newState) {
                    case Opening:
                        playSGSoundEffect("sgcraft:iris_open", 1.0F, 1.0F);
                        break;
                    case Closing:
                        playSGSoundEffect("sgcraft:iris_close", 1.0F, 1.0F);
                        break;
                }
            }
            if (!oldDesc.equals(newDesc)) postEvent("sgIrisStateChange", newDesc, oldDesc);
        }
    }

    public void openIris() {
        if (isMerged && hasIrisUpgrade && irisState != IrisState.Open) enterIrisState(IrisState.Opening);
    }

    public void closeIris() {
        if (isMerged && hasIrisUpgrade && irisState != IrisState.Closed) enterIrisState(IrisState.Closing);
    }

    public void onNeighborBlockChange() {
        if (!worldObj.isRemote) {
            boolean newInput = BaseBlockUtils.blockIsGettingExternallyPowered(worldObj, getPos());
            if (redstoneInput != newInput) {
                redstoneInput = newInput;
                markDirty();
                if (redstoneInput) closeIris();
                else openIris();
            }
        }
    }

    void updateIrisEntity() {
        if (worldObj.isRemote) {
            return;
        }

        if (isMerged && hasIrisUpgrade) {
            if (!hasIrisEntity()) {
                IrisEntity ent = new IrisEntity(this);
                worldObj.spawnEntityInWorld(ent);
            }
            return;
        }
        for (IrisEntity ent : findIrisEntities()) {
            worldObj.removeEntity(ent);
        }
    }

    boolean hasIrisEntity() {
        return findIrisEntities().size() != 0;
    }

    List<IrisEntity> findIrisEntities() {
        int x = getX(), y = getY(), z = getZ();
        AxisAlignedBB box = newAxisAlignedBB(x, y, z, x + 1, y + 2, z + 1);
        return (List<IrisEntity>) worldObj.getEntitiesWithinAABB(IrisEntity.class, box);
    }

    ItemStack getCamouflageStack(BlockPos cpos) {
        Trans3 t = localToGlobalTransformation();
        Vector3 p = t.ip(Vector3.blockCenter(cpos));
        if (p.y == 0) {
            int i = 2 + p.roundX();
            if (i >= 0 && i < 5) return getStackInSlot(firstCamouflageSlot + i);
        }
        return null;
    }

    boolean isCamouflageSlot(int slot) {
        return slot >= firstCamouflageSlot && slot < firstCamouflageSlot + numCamouflageSlots;
    }

    @Override
    protected void onInventoryChanged(int slot) {
        super.onInventoryChanged(slot);
        if (isCamouflageSlot(slot)) {
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) markWorldBlockForUpdate(worldObj, getPos().add(dx, 0, dz));
        }
    }

    public int numItemsInSlot(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) return stack.stackSize;
        return 0;
    }

    protected int baseCornerCamouflage() {
        return max(baseCamouflageAt(0), baseCamouflageAt(4));
    }

    protected int baseCamouflageAt(int i) {
        ItemStack stack = getStackInSlot(i);
        if (stack != null) {
            Item item = stack.getItem();
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block != null) {
                if (block instanceof BlockSlab) return 1;
                if (block.isBlockNormalCube()) return 2;
            }
        }
        return 0;
    }

    // Find locations of tile entities that could connect to the stargate ring.
    // TODO: Cache this
    public Collection<BlockRef> adjacentTiles() {
        Collection<BlockRef> result = new ArrayList<BlockRef>();
        Trans3 t = localToGlobalTransformation();
        for (int i = -2; i <= 2; i++) {
            BlockPos bp = t.p(i, -1, 0).blockPos();
            TileEntity te = getWorldTileEntity(worldObj, bp);
            if (te != null) result.add(new BlockRef(te));
        }
        return result;
    }

    public void forwardNetworkPacket(Object packet) {
        SGBaseTE dte = getConnectedStargateTE();
        if (dte != null) dte.rebroadcastNetworkPacket(packet);
    }

    void rebroadcastNetworkPacket(Object packet) {
        for (BlockRef ref : adjacentTiles()) {
            TileEntity te = ref.getTileEntity();
            if (te instanceof SGInterfaceTE) ((SGInterfaceTE) te).rebroadcastNetworkPacket(packet);
        }
    }

    public String sendMessage(Object[] args) {
        SGBaseTE dte = getConnectedStargateTE();
        if (dte != null) {
            dte.postEvent("sgMessageReceived", args);
            return null;
        }
        return "Stargate not connected";
    }

    void postEvent(String name, Object... args) {
        for (BlockRef b : adjacentTiles()) {
            TileEntity te = b.getTileEntity();
            if (te instanceof IComputerInterface) ((IComputerInterface) te).postEvent(this, name, args);
        }
    }

    public String sgStateDescription() {
        return sgStateDescription(state);
    }

    static String sgStateDescription(SGState state) {
        switch (state) {
            case Idle:
                return "Idle";
            case Dialling:
            case InterDialling:
                return "Dialling";
            case Transient:
                return "Opening";
            case Connected:
                return "Connected";
            case Disconnecting:
                return "Closing";
            default:
                return "Unknown";
        }
    }

    public String irisStateDescription() {
        return irisStateDescription(irisState);
    }

    static String irisStateDescription(IrisState state) {
        return state.toString();
    }

    public static SGBaseTE getBaseTE(SGInterfaceTE ite) {
        return SGBaseTE.get(getTileEntityWorld(ite), getTileEntityPos(ite).add(0, 1, 0));
    }

}

class BlockRef {

    public IBlockAccess worldObj;
    BlockPos pos;

    public BlockRef(TileEntity te) {
        this(getTileEntityWorld(te), getTileEntityPos(te));
    }

    public BlockRef(IBlockAccess world, BlockPos pos) {
        worldObj = world;
        this.pos = pos;
    }

    public TileEntity getTileEntity() {
        return getWorldTileEntity(worldObj, pos);
    }

}
