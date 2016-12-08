//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base tile entity
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;
import java.lang.reflect.Method;
import org.apache.logging.log4j.*;
import io.netty.channel.*;

import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.*;
import net.minecraft.server.*;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.ChunkProviderServer;

import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;
import net.minecraftforge.common.network.*;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.relauncher.Side;

import gcewing.sg.SGAddressing.AddressingError;
import gcewing.sg.oc.OCWirelessEndpoint; //[OC]
import static gcewing.sg.BaseBlockUtils.*;
import static gcewing.sg.BaseUtils.*;
import static gcewing.sg.Utils.*;

public class SGBaseTE extends BaseTileInventory implements ITickable {

    static boolean debugState = false;
    static boolean debugEnergyUse = false;
    static boolean debugConnect = false;
    static boolean debugTransientDamage = false;
    static boolean debugTeleport = false;

    static SoundEvent sound(String name) {
        return new SoundEvent(new ResourceLocation(name));
    }
    
    static SoundEvent
        abortSound,
        openSound,
        closeSound,
        irisOpenSound,
        irisCloseSound,
        irisHitSound,
        diallingSound;
    
    public static void registerSounds(SGCraft mod) {
        abortSound = mod.newSound("sg_abort");
        openSound = mod.newSound("sg_open");
        closeSound = mod.newSound("sg_close");
        irisOpenSound = mod.newSound("iris_open");
        irisCloseSound = mod.newSound("iris_close");
        irisHitSound = mod.newSound("iris_hit");
        diallingSound = mod.newSound("sg_dial7");
    }

    public final static String symbolChars = SGAddressing.symbolChars;
    public final static int numRingSymbols = SGAddressing.numSymbols;
    public final static double ringSymbolAngle = 360.0 / numRingSymbols;
    public final static double irisZPosition = 0.1;
    public final static double irisThickness = 0.2; //0.1;
    public final static DamageSource irisDamageSource = new DamageSource("sgcraft:iris");
    public final static float irisDamageAmount = 1000000;
    
    final static int diallingTime = 40; // ticks
    final static int interDiallingTime = 10; // ticks
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
    static float chevronAngles[][] = {
    //     0    1    2    <-- Base camouflage level
        { 45f, 45f, 40f }, // 7 chevrons
        { 36f, 33f, 30f }  // 9 chevrons
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
    public OCWirelessEndpoint ocWirelessEndpoint; //[OC]

    SGLocation connectedLocation;
    boolean isInitiator;
    int timeout;
    double energyInBuffer;
    double distanceFactor; // all energy use is multiplied by this
    boolean redstoneInput;
    boolean loaded;
    public String homeAddress;
    public String addressError;
    
//  public static final int firstFuelSlot = 0;
//  public static final int numFuelSlots = 4;
//  public static final int firstUpgradeSlot = 4;
//  public static final int numUpgradeSlots = 0;

    IInventory inventory = new InventoryBasic("Stargate", false, numInventorySlots);

    double ehGrid[][][];
    
    public static void configure(BaseConfiguration cfg) {
        energyPerFuelItem = cfg.getDouble("stargate", "energyPerFuelItem", energyPerFuelItem);
        gateOpeningsPerFuelItem = cfg.getInteger("stargate", "gateOpeningsPerFuelItem", gateOpeningsPerFuelItem);
        minutesOpenPerFuelItem = cfg.getInteger("stargate", "minutesOpenPerFuelItem", minutesOpenPerFuelItem);
        secondsToStayOpen = cfg.getInteger("stargate", "secondsToStayOpen", secondsToStayOpen);
        oneWayTravel = cfg.getBoolean("stargate", "oneWayTravel", oneWayTravel);
        closeFromEitherEnd = cfg.getBoolean("stargate", "closeFromEitherEnd", closeFromEitherEnd);
        //energyPerFuelItem = minutesOpenPerFuelItem * 60 * 20;
        //maxEnergyBuffer = 2 * energyPerFuelItem;
        maxEnergyBuffer = cfg.getDouble("stargate", "maxEnergyBuffer", maxEnergyBuffer);
        energyToOpen = energyPerFuelItem / gateOpeningsPerFuelItem;
        energyUsePerTick = energyPerFuelItem / (minutesOpenPerFuelItem * 60 * 20);
        distanceFactorMultiplier = cfg.getDouble("stargate", "distanceFactorMultiplier", distanceFactorMultiplier);
        interDimensionMultiplier = cfg.getDouble("stargate", "interDimensionMultiplier", interDimensionMultiplier);
        if (debugEnergyUse) {
            System.out.printf("SGBaseTE: energyPerFuelItem = %s\n", energyPerFuelItem);
            System.out.printf("SGBaseTE: energyToOpen = %s\n", energyToOpen);
            System.out.printf("SGBaseTE: energyUsePerTick = %s\n", energyUsePerTick);
        }
        ticksToStayOpen = 20 * secondsToStayOpen;
        chunkLoadingRange = cfg.getInteger("options", "chunkLoadingRange", chunkLoadingRange);
        //if (chunkLoadingRange < 0)
        //  chunkLoadingRange = 0;
        transparency = cfg.getBoolean("stargate", "transparency", transparency);
        logStargateEvents = cfg.getBoolean("options", "logStargateEvents", logStargateEvents);
        preserveInventory = cfg.getBoolean("iris", "preserveInventory", preserveInventory);
        soundVolume = (float)cfg.getDouble("stargate", "soundVolume", soundVolume);
        variableChevronPositions = cfg.getBoolean("stargate", "variableChevronPositions", variableChevronPositions);
    }
    
    public static SGBaseTE get(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SGBaseTE)
            return (SGBaseTE)te;
        else if (te instanceof SGRingTE)
            return ((SGRingTE)te).getBaseTE();
        else
            return null;
    }
    
    @Override
    public String toString() {
        return String.format("SGBaseTE(%s,%s)", pos, worldObj.provider.getDimension());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return new AxisAlignedBB(x - 2, y, z - 2, x + 3, y + 5, z + 3);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 32768.0;
    }

    @Override
    public void onAddedToWorld() {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGBaseTE.onAddedToWorld\n");
        updateChunkLoadingStatus();
    }
    
    void updateChunkLoadingStatus() {
        if (state != SGState.Idle) {
            int n = chunkLoadingRange;
            if (n >= 0)
                SGCraft.chunkManager.setForcedChunkRange(this, -n, -n, n, n);
        }
        else
            SGCraft.chunkManager.clearForcedChunkRange(this);
    }

    public static SGBaseTE at(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SGBaseTE)
            return (SGBaseTE)te;
        else
            return null;
    }
    
    public static SGBaseTE at(SGLocation loc) {
        if (loc != null) {
            World world = SGAddressing.getWorld(loc.dimension);
            if (world != null)
                return SGBaseTE.at(world, loc.pos);
        }
        return null;
    }
    
    public static SGBaseTE at(IBlockAccess world, NBTTagCompound nbt) {
        BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        return SGBaseTE.at(world, pos);
    }
    
    void setMerged(boolean state) {
        if (isMerged != state) {
            isMerged = state;
            markBlockChanged();
            if (logStargateEvents) {
                String address = tryToGetHomeAddress();
                if (address != null) {
                    Logger log = LogManager.getLogger();
                    String action = isMerged ? "ADDED" : "REMOVED";
                    String name = getWorld().getWorldInfo().getWorldName();
                    log.info(String.format("STARGATE %s %s %s %s",
                        action, name, pos, address));
                }
            }
            updateIrisEntity();
        }
    }
    
    String tryToGetHomeAddress() {
        try {
            return getHomeAddress();
        }
        catch (SGAddressing.AddressingError e) {
            return null;
        }
    }

    public int dimension() {
        if (worldObj != null)
            return worldObj.provider.getDimension();
        else
            return -999;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
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
        if (nbt.hasKey("energyInBuffer"))
            energyInBuffer = nbt.getDouble("energyInBuffer");
        else
            energyInBuffer = nbt.getInteger("fuelBuffer");
        distanceFactor = nbt.getDouble("distanceFactor");
        hasIrisUpgrade = nbt.getBoolean("hasIrisUpgrade");
        irisState = IrisState.valueOf(nbt.getInteger("irisState"));
        irisPhase = nbt.getInteger("irisPhase");
        redstoneInput = nbt.getBoolean("redstoneInput");
        homeAddress = getStringOrNull(nbt, "address");
        addressError = nbt.getString("addressError");
    }
    
    protected String getStringOrNull(NBTTagCompound nbt, String name) {
        if (nbt.hasKey(name))
            return nbt.getString(name);
        else
            return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isMerged", isMerged);
        nbt.setInteger("state", state.ordinal());
        nbt.setDouble("targetRingAngle", targetRingAngle);
        nbt.setInteger("numEngagedChevrons", numEngagedChevrons);
        //nbt.setString("homeAddress", homeAddress);
        nbt.setString("dialledAddress", dialledAddress);
        nbt.setBoolean("isLinkedToController", isLinkedToController);
        nbt.setInteger("linkedX", linkedPos.getX());
        nbt.setInteger("linkedY", linkedPos.getY());
        nbt.setInteger("linkedZ", linkedPos.getZ());
        nbt.setBoolean("hasChevronUpgrade", hasChevronUpgrade);
        if (connectedLocation != null)
            nbt.setTag("connectedLocation", connectedLocation.toNBT());
        nbt.setBoolean("isInitiator", isInitiator);
        nbt.setInteger("timeout", timeout);
        nbt.setDouble("energyInBuffer", energyInBuffer);
        nbt.setDouble("distanceFactor", distanceFactor);
        nbt.setBoolean("hasIrisUpgrade", hasIrisUpgrade);
        nbt.setInteger("irisState", irisState.ordinal());
        nbt.setInteger("irisPhase", irisPhase);
        nbt.setBoolean("redstoneInput", redstoneInput);
        if (homeAddress != null)
            nbt.setString("address", homeAddress);
        if (addressError != null)
            nbt.setString("addressError", addressError);
        return nbt;
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
    
    public EnumActionResult applyChevronUpgrade(ItemStack stack, EntityPlayer player) {
        if (!getWorld().isRemote && !hasChevronUpgrade && stack.stackSize > 0) {
            //System.out.printf("SGBaseTE.applyChevronUpgrade: Installing chevron upgrade\n");
            hasChevronUpgrade = true;
            stack.stackSize -= 1;
            markChanged();
        }
        return EnumActionResult.SUCCESS;
    }
    
    public EnumActionResult applyIrisUpgrade(ItemStack stack, EntityPlayer player) {
        if (!getWorld().isRemote && !hasIrisUpgrade && stack.stackSize > 0) {
            //System.out.printf("SGBaseTE.applyIrisUpgrade: Installing iris upgrade\n");
            hasIrisUpgrade = true;
            stack.stackSize -= 1;
            markChanged();
            updateIrisEntity();
        }
        return EnumActionResult.SUCCESS;
    }
    
    public int getNumChevrons() {
        //if (upgradePresent(SGCraft.sgChevronUpgrade))
        if (hasChevronUpgrade)
            return 9;
        else
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
        else
            return defaultChevronAngle;
    }
    
//  boolean upgradePresent(Item item) {
//      for (int i = firstUpgradeSlot; i < firstUpgradeSlot + numUpgradeSlots; i++)
//          if (getItemInSlot(i) == item)
//              return true;
//      return false;
//  }
    
    Item getItemInSlot(int slot) {
        ItemStack stack = getStackInSlot(slot);
        return stack != null ? stack.getItem() : null;
    }

    public String getHomeAddress() throws SGAddressing.AddressingError {
        return SGAddressing.addressForLocation(new SGLocation(this));
    }
    
    public SGBaseBlock getBlock() {
        return (SGBaseBlock)getBlockType();
    }
    
    public double interpolatedRingAngle(double t) {
        return Utils.interpolateAngle(lastRingAngle, ringAngle, t);
    }
    
    @Override
    public void update() {
        if (worldObj.isRemote)
            clientUpdate();
        else {
            serverUpdate();
            checkForEntitiesInPortal();
        }
        irisUpdate();
    }
    
    @Override
    public void invalidate() {
        super.invalidate();
        if (!worldObj.isRemote && ocWirelessEndpoint != null)  //[OC]
            ocWirelessEndpoint.remove();
    }
    
    String side() {
        return worldObj.isRemote ? "Client" : "Server";
    }
    
    void enterState(SGState newState, int newTimeout) {
        if (debugState)
            System.out.printf("SGBaseTE: at %s in dimension %s entering state %s with timeout %s\n", 
                pos, worldObj.provider.getDimension(), newState, newTimeout);
        SGState oldState = state;
        state = newState;
        timeout = newTimeout;
        markChanged();
        if ((oldState == SGState.Idle) != (newState == SGState.Idle)) {
            updateChunkLoadingStatus();
            worldObj.notifyNeighborsOfStateChange(pos, getBlockType());
        }
        String oldDesc = sgStateDescription(oldState);
        String newDesc = sgStateDescription(newState);
        if (!oldDesc.equals(newDesc))
            postEvent("sgStargateStateChange", newDesc, oldDesc);
    }
    
    public boolean isConnected() {
        return state == SGState.Transient || state == SGState.Connected || state == SGState.Disconnecting;
    }
    
    DHDTE getLinkedControllerTE() {
        //System.out.printf("SGBaseTE.getLinkedControllerTE: isLinkedToController = %s, linkedPos = %s\n",
        //    isLinkedToController, linkedPos);
        if (isLinkedToController) {
            TileEntity cte = worldObj.getTileEntity(linkedPos);
            if (cte instanceof DHDTE)
                return (DHDTE)cte;
        }
        return null;
    }
    
    void checkForLink() {
        int rangeXY = max(DHDTE.linkRangeX, DHDTE.linkRangeY);
        int rangeZ = DHDTE.linkRangeZ;
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGBaseTE.checkForLink: in range +/-(%d,%d,%d) of %s\n",
                rangeXY, rangeZ, rangeXY, pos);
        for (int i = -rangeXY; i <= rangeXY; i++)
            for (int j = -rangeZ; j <= rangeZ; j++)
                for (int k = -rangeXY; k <= rangeXY; k++) {
                    TileEntity te = worldObj.getTileEntity(pos.add(i, j, k));
                    if (te instanceof DHDTE)
                        ((DHDTE)te).checkForLink();
                }
    }

    public void unlinkFromController() {
        if (isLinkedToController) {
            DHDTE cte = getLinkedControllerTE();
            if (cte != null)
                cte.clearLinkToStargate();
            clearLinkToController();
        }
    }
    
    public void clearLinkToController() {
        if (SGBaseBlock.debugMerge)
            System.out.printf("SGBaseTE: Unlinking stargate at %d from controller\n", pos);
        isLinkedToController = false;
        markDirty();
    }

    //------------------------------------   Server   --------------------------------------------
    
    public void connectOrDisconnect(String address, EntityPlayer player) {
        if (debugConnect)
            System.out.printf("SGBaseTE: %s: connectOrDisconnect('%s') in state %s by %s\n",
                side(), address, state, player);
        if (address.length() > 0)
            connect(address, player);
        else
            attemptToDisconnect(player);
    }
    
    public String attemptToDisconnect(EntityPlayer player) {        
        boolean canDisconnect = disconnectionAllowed();
        SGBaseTE dte = getConnectedStargateTE();
        boolean validConnection =
            (dte != null) && !dte.isInvalid() && (dte.getConnectedStargateTE() == this);
        if (canDisconnect || !validConnection) {
            if (state != SGState.Disconnecting)
                disconnect();
                return null;
        }
        else
            return operationFailure(player, "Connection initiated from other end");
    }
    
    public boolean disconnectionAllowed() {
        return isInitiator || closeFromEitherEnd;
    }
    
    String connect(String address, EntityPlayer player) {
        SGBaseTE dte;
        if (state != SGState.Idle)
            return diallingFailure(player, "Stargate is busy");
        String homeAddress = findHomeAddress();
        if (homeAddress.equals(""))
            return diallingFailure(player, "Coordinates of dialling stargate are out of range");
        try {
            dte = SGAddressing.findAddressedStargate(address, worldObj);
        }
        catch (SGAddressing.AddressingError e) {
            return diallingFailure(player, e.getMessage());
        }
        if (dte == null || !dte.isMerged)
            return diallingFailure(player, "No stargate at address " + address);
        if (getWorld() == dte.getWorld()) {
            address = SGAddressing.localAddress(address);
            homeAddress = SGAddressing.localAddress(homeAddress);
        }
        if (address.length() > getNumChevrons())
            return diallingFailure(player, "Not enough chevrons to dial " + address);
        if (dte == this)
            return diallingFailure(player, "Stargate cannot connect to itself");
        if (debugConnect)
            System.out.printf("SGBaseTE.connect: to %s in dimension %d with state %s\n",
                dte.getPos(), dte.getWorld().provider.getDimension(),
                dte.state);
        if (dte.getNumChevrons() < homeAddress.length())
            return diallingFailure(player, "Destination stargate has insufficient chevrons");
        if (dte.state != SGState.Idle)
            return diallingFailure(player, "Stargate at address " + address + " is busy");
        distanceFactor = distanceFactorForCoordDifference(this, dte);
        if (debugEnergyUse)
            System.out.printf("SGBaseTE: distanceFactor = %s\n", distanceFactor);
        if (!energyIsAvailable(energyToOpen * distanceFactor))
            return diallingFailure(player, "Stargate has insufficient energy");
        startDiallingStargate(address, dte, true);
        dte.startDiallingStargate(homeAddress, this, false);
        return null;
    }
    
    public static double distanceFactorForCoordDifference(TileEntity te1, TileEntity te2) {
        double dx = te1.getPos().getX() - te2.getPos().getX();
        double dz = te1.getPos().getZ() - te2.getPos().getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        if (debugEnergyUse)
            System.out.printf("SGBaseTE: Connection distance = %s\n", d);
        double ld = Math.log(0.05 * d + 1);
        double lm = Math.log(0.05 * 16 * SGAddressing.coordRange);
        double lr = ld / lm;
        double f = 1 + 14 * distanceFactorMultiplier * lr * lr;
        if (te1.getWorld() != te2.getWorld())
            f *= interDimensionMultiplier;
        return f;
    }
    
    public void playSGSoundEffect(SoundEvent se, float volume, float pitch) {
        playSoundEffect(se, volume * soundVolume, pitch);
    }
    
    String diallingFailure(EntityPlayer player, String mess) {
        if (player != null) {
            if (state == SGState.Idle)
                playSGSoundEffect(abortSound, 1.0F, 1.0F);
        }
        return operationFailure(player, mess);
    }
    
    String operationFailure(EntityPlayer player, String mess) {
        if (player != null)
            sendChatMessage(player, mess);
        return mess;
    }
    
    static void sendChatMessage(EntityPlayer player, String mess) {
        player.addChatMessage(new TextComponentTranslation(mess));
    }
    
    String findHomeAddress() {
        String homeAddress;
        try {
            return getHomeAddress();
        }
        catch (SGAddressing.AddressingError e) {
            System.out.printf("SGBaseTE.findHomeAddress: %s\n", e);
            return "";
        }
    }
    
    public void disconnect() {
        if (debugConnect)
            System.out.printf("SGBaseTE: %s: disconnect()\n", side());
        SGBaseTE dte = SGBaseTE.at(connectedLocation);
        if (dte != null)
            dte.clearConnection();
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
                //sendClientEvent(SGEvent.StartDisconnecting, 0);
                playSGSoundEffect(closeSound, 1.0F, 1.0F);
            }
            else {
                if (state != SGState.Idle && state != SGState.Disconnecting)
                    playSGSoundEffect(abortSound, 1.0F, 1.0F);
                enterState(SGState.Idle, 0);
                //sendClientEvent(SGEvent.FinishDisconnecting, 0);
            }
        }
    }
    
    void startDiallingStargate(String address, SGBaseTE dte, boolean initiator) {
        //System.out.printf("SGBaseTE.startDiallingStargate %s, initiator = %s\n",
        //  dte, initiator);
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
            }
            catch (SGAddressing.AddressingError e) {
                homeAddress = null;
                addressError = e.getMessage();
            }
            if (SGCraft.ocIntegration != null) //[OC]
                SGCraft.ocIntegration.onSGBaseTEAdded(this);
        }
        if (isMerged) {
            if (debugState && state != SGState.Connected && timeout > 0) {
                int dimension = worldObj.provider.getDimension();
                System.out.printf(
                    "SGBaseTE.serverUpdate at %d in dimension %d: state %s, timeout %s\n",
                    pos, dimension, state, timeout);
            }
            tickEnergyUsage();
            if (timeout > 0) {
                if (state == SGState.Transient && !irisIsClosed())
                    performTransientDamage();
                --timeout;
            }
            else switch(state) {
                case Idle:
                    if (undialledDigitsRemaining())
                        startDiallingNextSymbol();
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
                    if (isInitiator && ticksToStayOpen > 0)
                        disconnect();
                    break;
                case Disconnecting:
                    enterState(SGState.Idle, 0);
                    break;
            }
        }
    }
    
    void tickEnergyUsage() {
        if (state == SGState.Connected && isInitiator)
            if (!useEnergy(energyUsePerTick * distanceFactor))
                disconnect();
    }
    
    double availableEnergy() {
        List<ISGEnergySource> sources = findEnergySources();
        return energyInBuffer + energyAvailableFrom(sources);
    }

    boolean energyIsAvailable(double amount) {
        double energy = availableEnergy();
        if (debugEnergyUse)
            System.out.printf("SGBaseTE.energyIsAvailable: need %s, have %s\n", amount, energy);
        return energy >= amount;
    }
    
    boolean useEnergy(double amount) {
        if (debugEnergyUse)
            System.out.printf("SGBaseTE.useEnergy: %s; buffered: %s\n", amount, energyInBuffer);
        if (amount <= energyInBuffer) {
            energyInBuffer -= amount;
            return true;
        }
        List<ISGEnergySource> sources = findEnergySources();
        double energyAvailable = energyInBuffer + energyAvailableFrom(sources);
        if (debugEnergyUse)
            System.out.printf("SGBaseTE.useEnergy: %s available\n", energyAvailable);
        if (amount > energyAvailable) {
            System.out.printf("SGBaseTE: Not enough energy available\n");
            return false;
        }
        double desiredEnergy = max(amount, maxEnergyBuffer);
        double targetEnergy = min(desiredEnergy, energyAvailable);
        double energyRequired = targetEnergy - energyInBuffer;
        if (debugEnergyUse)
            System.out.printf("SGBaseTE.useEnergy: another %s required\n", energyRequired);
        double energyOnHand = energyInBuffer + drawEnergyFrom(sources, energyRequired);
        if (debugEnergyUse)
            System.out.printf("SGBaseTE.useEnergy: %s now on hand, need %s\n", energyOnHand, amount);
        if (amount - 0.0001 > energyOnHand) {
            System.out.printf("SGBaseTE: Energy sources only delivered %s of promised %s\n",
                energyOnHand - energyInBuffer, energyAvailable);
            return false;
        }
        setEnergyInBuffer(energyOnHand - amount);
        if (debugEnergyUse)
            System.out.printf("SGBaseTE.useEnergy: %s left over in buffer\n", energyInBuffer);
        return true;
    }
    
    List<ISGEnergySource> findEnergySources() {
//         System.out.printf("SGBaseTe.findEnergySources: for %s\n", getPos());
        List<ISGEnergySource> result = new ArrayList<ISGEnergySource>();
        Trans3 t = localToGlobalTransformation();
        for (int i = -2; i <= 2; i++) {
            BlockPos bp = t.p(i, -1, 0).blockPos();
            TileEntity nte = worldObj.getTileEntity(bp);
//             System.out.printf("SGBaseTE.findEnergySources: %s at %s\n", nte, bp);
            if (nte instanceof ISGEnergySource)
                result.add((ISGEnergySource)nte);
        }
        DHDTE te = getLinkedControllerTE();
        if (te != null)
            result.add(te);
        return result;
    }
    
    double energyAvailableFrom(List<ISGEnergySource> sources) {
        double energy = 0;
        for (ISGEnergySource source : sources) {
            double e = source.availableEnergy();
            if (debugEnergyUse)
                System.out.printf("SGBaseTe.energyAvailableFrom: %s can supply %s\n", source, e);
            energy += e;
        }
        return energy;
    }
    
    double drawEnergyFrom(List<ISGEnergySource> sources, double amount) {
        double total = 0;
        for (ISGEnergySource source : sources) {
            if (total >= amount)
                break;
            double e = source.drawEnergy(amount - total);
            if (debugEnergyUse)
                System.out.printf("SGBaseTe.drawEnergyFrom: %s supplied %s\n", source, e);
            total += e;
        }
        if (total < amount)
            System.out.printf("SGCraft: Warning: Energy sources did not deliver promised energy " +
                "(%s requested, %s delivered)\n", amount, total);
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
        AxisAlignedBB box = new AxisAlignedBB(q0.x, q0.y, q0.z, q1.x, q1.y, q1.z);
        if (debugTransientDamage) {
            System.out.printf("SGBaseTE.performTransientDamage: players in world:\n");
            for (Entity ent : (List<Entity>)worldObj.loadedEntityList)
                if (ent instanceof EntityPlayer)
                    System.out.printf("--- %s\n", ent);
            System.out.printf("SGBaseTE.performTransientDamage: box = %s\n", box);
        }
        List<EntityLivingBase> ents = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase ent : ents) {
            Vector3 ep = new Vector3(ent.posX, ent.posY, ent.posZ);
            Vector3 gp = t.p(0, 2, 0.5);
            double dist = ep.distance(gp);
            if (debugTransientDamage)
                System.out.printf("SGBaseTE.performTransientDamage: found %s\n", ent);
            if (dist > 1.0)
                dist = 1.0;
            int damage = (int)Math.ceil(dist * transientDamageRate);
            if (debugTransientDamage)
                System.out.printf("SGBaseTE.performTransientDamage: distance = %s, damage = %s\n",
                    dist, damage);
            ent.attackEntityFrom(transientDamage, damage);
        }
    }
    
    boolean undialledDigitsRemaining() {
        int n = numEngagedChevrons;
        return n < dialledAddress.length();
    }
    
    void startDiallingNextSymbol() {
        if (debugState)
            System.out.printf("SGBaseTE.startDiallingNextSymbol: %s of %s\n",
                numEngagedChevrons, dialledAddress);
        startDiallingSymbol(dialledAddress.charAt(numEngagedChevrons));
    }
    
    void startDiallingSymbol(char c) {
        int i = SGAddressing.charToSymbol(c);
        if (debugState)
            System.out.printf("SGBaseTE.startDiallingSymbol: %s\n", i);
        if (i >= 0 && i < numRingSymbols) {
            startDiallingToAngle(i * ringSymbolAngle);
            playSGSoundEffect(diallingSound, 1.0F, 1.0F);
        }
        else {
            System.out.printf("SGCraft: Stargate jammed trying to dial symbol %s\n", c);
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
        if (undialledDigitsRemaining())
            enterState(SGState.InterDialling, interDiallingTime);
        else
            finishDiallingAddress();
    }
    
    void finishDiallingAddress() {
        //System.out.printf("SGBaseTE: Connecting to '%s'\n", dialledAddress);
        if (!isInitiator || useEnergy(energyToOpen * distanceFactor)) {
            enterState(SGState.Transient, transientDuration);
            playSGSoundEffect(openSound, 1.0F, 1.0F);
        }
        else
            disconnect();
    }
    
    boolean canTravelFromThisEnd() {
        return isInitiator || !oneWayTravel;
    }
    
    static String repr(Entity entity) {
        if (entity != null) {
            String s = String.format("%s#%s", entity.getClass().getSimpleName(), entity.getEntityId());
            if (entity.isDead)
                s += "(dead)";
            return s;
        }
        else
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
        if (state == SGState.Connected) {
            for (TrackedEntity trk : trackedEntities)
                entityInPortal(trk.entity, trk.lastPos);
            trackedEntities.clear();
            Vector3 p0 = new Vector3(-1.5, 0.5, -3.5);
            Vector3 p1 = new Vector3(1.5, 3.5, 3.5);
            Trans3 t = localToGlobalTransformation();
            AxisAlignedBB box = t.box(p0, p1);
            //System.out.printf("SGBaseTE.checkForEntitiesInPortal: %s\n", box);
            List<Entity> ents = (List<Entity>)worldObj.getEntitiesWithinAABB(Entity.class, box);
            for (Entity entity : ents) {
                if (entity instanceof EntityFishHook)
                    continue;
                if (!entity.isDead && entity.getRidingEntity() == null) {
                    //if (!(entity instanceof EntityPlayer))
                    //  System.out.printf("SGBaseTE.checkForEntitiesInPortal: Tracking %s\n", repr(entity));
                    trackedEntities.add(new TrackedEntity(entity));
                }
            }
        }
        else
            trackedEntities.clear();
    }

    public void entityInPortal(Entity entity, Vector3 prevPos) {
        if (!entity.isDead && state == SGState.Connected && canTravelFromThisEnd()) {
            Trans3 t = localToGlobalTransformation();
            double vx = entity.posX - prevPos.x;
            double vy = entity.posY - prevPos.y;
            double vz = entity.posZ - prevPos.z;
            Vector3 p1 = t.ip(entity.posX, entity.posY, entity.posZ);
            Vector3 p0 = t.ip(2 * prevPos.x - entity.posX, 2 * prevPos.y - entity.posY, 2 * prevPos.z - entity.posZ);
            //if (!(entity instanceof EntityPlayer))
            //  System.out.printf("SGBaseTE.entityInPortal: z0 = %.3f z1 = %.3f\n", p0.z, p1.z);
            double z0 = 0.0;
            if (p0.z >= z0 && p1.z < z0 && p1.z > z0 - 5.0) {
                //System.out.printf("SGBaseTE.entityInPortal: %s passed through event horizon of stargate at (%d,%d,%d) in %s\n",
                //  repr(entity), xCoord, yCoord, zCoord, worldObj);
                entity.motionX = vx;
                entity.motionY = vy;
                entity.motionZ = vz;
                //System.out.printf("SGBaseTE.entityInPortal: %s pos (%.2f, %.2f, %.2f) prev (%.2f, %.2f, %.2f) motion (%.2f, %.2f, %.2f)\n",
                //  repr(entity),
                //  entity.posX, entity.posY, entity.posZ,
                //  prevPos.x, prevPos.y, prevPos.z,
                //  entity.motionX, entity.motionY, entity.motionZ);
                SGBaseTE dte = getConnectedStargateTE();
                if (dte != null) {
                    Trans3 dt = dte.localToGlobalTransformation();
                    while (entity.getRidingEntity() != null)
                        entity = entity.getRidingEntity();
                    teleportEntityAndRiders(entity, t, dt, connectedLocation.dimension, dte.irisIsClosed());
                }
            }
        }
    }
    
    Entity teleportEntityAndRiders(Entity entity, Trans3 t1, Trans3 t2, int dimension, boolean destBlocked) {
        if (debugTeleport)
            System.out.printf("SGBaseTE.teleportEntityAndRiders: destBlocked = %s\n", destBlocked);
        //Entity rider = entity.riddenByEntity;
        List<Entity> riders = entity.getPassengers();
        for (int i = 0; i < riders.size(); i++) {
            Entity rider = riders.get(i);
            //System.out.printf("SGBaseTE.teleportEntityAndRider: Unmounting %s from %s\n",
            //  repr(rider), repr(entity));
            rider.dismountRidingEntity();
            rider = teleportEntityAndRiders(rider, t1, t2, dimension, destBlocked);
            riders.set(i, rider);
        }
        unleashEntity(entity);
        entity = teleportEntity(entity, t1, t2, dimension, destBlocked);
        if (entity != null && !entity.isDead) {
            for (Entity rider : riders) {
                if (rider != null && !rider.isDead) {
                    //System.out.printf("SGBaseTE.teleportEntityAndRider: Mounting %s on %s\n",
                    //  repr(rider), repr(entity));
                    rider.startRiding(entity, true);
                }
            }
        }
        return entity;
    }
    
    // Break any leash connections to or from the given entity. That happens anyway
    // when the entity is teleported, but without this it drops an extra leash item.
    protected static void unleashEntity(Entity entity) {
        if (entity instanceof EntityLiving)
            ((EntityLiving)entity).clearLeashed(true, false);
        for (EntityLiving entity2 : entitiesWithinLeashRange(entity))
            if (entity2.getLeashed() && entity2.getLeashedToEntity() == entity)
                entity2.clearLeashed(true, false);
    }
    
    protected static List<EntityLiving> entitiesWithinLeashRange(Entity entity) {
        AxisAlignedBB box = new AxisAlignedBB(
            entity.posX - 7.0D, entity.posY - 7.0D, entity.posZ - 7.0D,
            entity.posX + 7.0D, entity.posY + 7.0D, entity.posZ + 7.0D);
        return entity.worldObj.getEntitiesWithinAABB(EntityLiving.class, box);
    }

    static Entity teleportEntity(Entity entity, Trans3 t1, Trans3 t2, int dimension, boolean destBlocked) {
        Entity newEntity = null;
        if (debugTeleport) {
            System.out.printf("SGBaseTE.teleportEntity: %s (in dimension %d)  to dimension %d\n",
              repr(entity), entity.dimension, dimension);
            System.out.printf("SGBaseTE.teleportEntity: pos (%.2f, %.2f, %.2f) prev (%.2f, %.2f, %.2f) last (%.2f, %.2f, %.2f) pitch %.2f yaw %.2f\n",
              entity.posX, entity.posY, entity.posZ,
              entity.prevPosX, entity.prevPosY, entity.prevPosZ,
              entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ,
              entity.rotationPitch, entity.rotationYaw);
        }
        Vector3 p = t1.ip(entity.posX, entity.posY, entity.posZ); // local position
        Vector3 v = t1.iv(entity.motionX, entity.motionY, entity.motionZ); // local velocity
        Vector3 r = t1.iv(yawVector(entity)); // local facing
        Vector3 q = t2.p(-p.x, p.y, -p.z); // new global position
        Vector3 u = t2.v(-v.x, v.y, -v.z); // new global velocity
        Vector3 s = t2.v(r.mul(-1)); // new global facing
        if (debugTeleport)
            System.out.printf("SGBaseTE.teleportEntity: Facing old %s new %s\n", r, s);
        double a = yawAngle(s, entity); // new global yaw angle
        if (debugTeleport)
            System.out.printf("SGBaseTE.teleportEntity: new yaw %.2f\n", a);
        if (!destBlocked) {
            if (entity.dimension == dimension)
                newEntity = teleportWithinDimension(entity, q, u, a, destBlocked);
            else {
                newEntity = teleportToOtherDimension(entity, q, u, a, dimension, destBlocked);
                if (newEntity != null)
                    newEntity.dimension = dimension;
                //else
                //  System.out.printf("SGBaseTE.teleportEntity: teleportToOtherDimension returned null for %s\n",
                //      entity);
            }
            //if (entity != newEntity)
            //  System.out.printf("SGBaseTE.teleportEntity: %s is now %s\n", repr(entity), repr(newEntity));
        }
        else {
            terminateEntityByIrisImpact(entity);
            playIrisHitSound(worldForDimension(dimension), q, entity);  
        }
        return newEntity;
    }
    
    static void terminateEntityByIrisImpact(Entity entity) {
        if (entity instanceof EntityPlayer)
            terminatePlayerByIrisImpact((EntityPlayer)entity);
        else
            entity.setDead();
    }
    
    static void terminatePlayerByIrisImpact(EntityPlayer player) {
        if (player.capabilities.isCreativeMode)
            sendChatMessage(player, "Destination blocked by iris");
        else {
            if (!(preserveInventory || player.worldObj.getGameRules().getBoolean("keepInventory")))
                player.inventory.clear();
            player.attackEntityFrom(irisDamageSource, irisDamageAmount);
        }
    }

    static WorldServer worldForDimension(int dimension) {
        return SGAddressing.getWorld(dimension);
    }

    static void playIrisHitSound(World world, Vector3 pos, Entity entity) {
        double volume = min(entity.width * entity.height, 1.0);
        double pitch = 2.0 - volume;
        if (debugTeleport)
            System.out.printf("SGBaseTE.playIrisHitSound: at (%.3f,%.3f,%.3f) volume %.3f pitch %.3f\n",
                pos.x, pos.y, pos.z, volume, pitch);
        world.playSound(pos.x, pos.y, pos.z, irisHitSound, SoundCategory.NEUTRAL,
            (float)volume, (float)pitch, false);
    }
    
    static Entity teleportWithinDimension(Entity entity, Vector3 p, Vector3 v, double a, boolean destBlocked) {
        if (entity instanceof EntityPlayerMP)
            return teleportPlayerWithinDimension((EntityPlayerMP)entity, p, v, a);
        else
            return teleportEntityToWorld(entity, p, v, a, (WorldServer)entity.worldObj, destBlocked);
    }
    
    static Entity teleportPlayerWithinDimension(EntityPlayerMP entity, Vector3 p, Vector3 v, double a) {
        entity.rotationYaw = (float)a;
        entity.setPositionAndUpdate(p.x, p.y, p.z);
        entity.worldObj.updateEntityWithOptionalForce(entity, false);
        return entity;
    }

    static Entity teleportToOtherDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension, boolean destBlocked) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)entity;
            Vector3 q = p.add(yawVector(a));
            transferPlayerToDimension(player, dimension, q, a);
            return player;
        }
        else
            return teleportEntityToDimension(entity, p, v, a, dimension, destBlocked);
    }
    
    static void sendDimensionRegister(EntityPlayerMP player, int dimensionID) {
        DimensionType providerID = DimensionManager.getProviderType(dimensionID);
        ForgeMessage msg = new ForgeMessage.DimensionRegisterMessage(dimensionID, providerID.toString());
        FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
    
    static void transferPlayerToDimension(EntityPlayerMP player, int newDimension, Vector3 p, double a) {
        //System.out.printf("SGBaseTE.transferPlayerToDimension: %s to dimension %d\n", repr(player), newDimension);
        MinecraftServer server = BaseUtils.getMinecraftServer();
        PlayerList scm = server.getPlayerList();
        int oldDimension = player.dimension;
        player.dimension = newDimension;
        WorldServer oldWorld = server.worldServerForDimension(oldDimension);
        WorldServer newWorld = server.worldServerForDimension(newDimension);
        //System.out.printf("SGBaseTE.transferPlayerToDimension: %s with %s\n", newWorld, newWorld.getEntityTracker());
        // <<< Fix for MCPC+
        // -- Is this still necessary now that we are calling firePlayerChangedDimensionEvent?
        // -- Yes, apparently it is.
        sendDimensionRegister(player, newDimension);
        // >>>
        player.closeScreen();
        player.connection.sendPacket(new SPacketRespawn(player.dimension,
            player.worldObj.getDifficulty(), newWorld.getWorldInfo().getTerrainType(),
            player.interactionManager.getGameType()));
//         if (SGCraft.mystcraftIntegration != null) //[MYST]
//             SGCraft.mystcraftIntegration.sendAgeData(newWorld, player);
        oldWorld.removeEntityDangerously(player); // Removes player right now instead of waiting for next tick
        player.isDead = false;
        player.setLocationAndAngles(p.x, p.y, p.z, (float)a, player.rotationPitch);
        newWorld.spawnEntityInWorld(player);
        player.setWorld(newWorld);
        scm.preparePlayer(player, oldWorld);
        player.connection.setPlayerLocation(p.x, p.y, p.z, (float)a, player.rotationPitch);
        player.interactionManager.setWorld(newWorld);
        scm.updateTimeAndWeatherForPlayer(player, newWorld);
        scm.syncPlayerInventory(player);
        Iterator var6 = player.getActivePotionEffects().iterator();
        while (var6.hasNext()) {
            PotionEffect effect = (PotionEffect)var6.next();
            player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), effect));
        }
        player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldDimension, newDimension);
        //System.out.printf("SGBaseTE.transferPlayerToDimension: Transferred %s\n", repr(player));
    }   
    
    static Entity teleportEntityToDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension, boolean destBlocked) {
        //System.out.printf("SGBaseTE.teleportEntityToDimension: %s to dimension %d\n", repr(entity), dimension);
        MinecraftServer server = BaseUtils.getMinecraftServer();
        WorldServer world = server.worldServerForDimension(dimension);
        return teleportEntityToWorld(entity, p, v, a, world, destBlocked);
    }
    
    static Entity teleportEntityToWorld(Entity oldEntity, Vector3 p, Vector3 v, double a, WorldServer newWorld, boolean destBlocked) {
        if (debugTeleport)
            System.out.printf("SGBaseTE.teleportEntityToWorld: %s to %s, destBlocked = %s\n",
                repr(oldEntity), newWorld, destBlocked);
        WorldServer oldWorld = (WorldServer)oldEntity.worldObj;
        NBTTagCompound nbt = new NBTTagCompound();
        oldEntity.writeToNBT(nbt);
        extractEntityFromWorld(oldWorld, oldEntity);
        if (destBlocked) {
            if (!(oldEntity instanceof EntityLivingBase))
                return null;
        }
        Entity newEntity = instantiateEntityFromNBT(oldEntity.getClass(), nbt, newWorld);
        if (newEntity != null) {
            if (oldEntity instanceof EntityLiving)
                copyMoreEntityData((EntityLiving)oldEntity, (EntityLiving)newEntity);
            setVelocity(newEntity, v);
            newEntity.setLocationAndAngles(p.x, p.y, p.z, (float)a, oldEntity.rotationPitch);
            checkChunk(newWorld, newEntity);
            //System.out.printf("SGBaseTE.teleportEntityToWorld: Spawning %s in %s\n", repr(newEntity), newWorld);
            newEntity.forceSpawn = true; // Force spawn packet to be sent as soon as possible
            newWorld.spawnEntityInWorld(newEntity);
            newEntity.setWorld(newWorld);
            if (debugTeleport)
                System.out.printf(
                  "SGBaseTE.teleportEntityToWorld: Spawned %s pos (%.2f, %.2f, %.2f) vel (%.2f, %.2f, %.2f) pitch %.2f (%.2f) yaw %.2f (%.2f)\n",
                  repr(newEntity),
                  newEntity.posX, newEntity.posY, newEntity.posZ,
                  newEntity.motionX, newEntity.motionY, newEntity.motionZ,
                  newEntity.rotationPitch, newEntity.prevRotationPitch,
                  newEntity.rotationYaw, newEntity.prevRotationYaw);
        }
        oldWorld.resetUpdateEntityTick();
        if (oldWorld != newWorld)
            newWorld.resetUpdateEntityTick();
        return newEntity;
    }
    
    static Entity instantiateEntityFromNBT(Class cls, NBTTagCompound nbt, WorldServer world) {
        try {
            Entity entity = (Entity)cls.getConstructor(World.class).newInstance(world);
            entity.readFromNBT(nbt);
            return entity;
        }
        catch (Exception e) {
            System.out.printf("SGCraft: SGBaseTE.instantiateEntityFromNBT: Could not instantiate %s: %s\n",
                cls, e);
            e.printStackTrace();
            return null;
        }
    }

    static void copyMoreEntityData(EntityLiving oldEntity, EntityLiving newEntity) {
        float s = oldEntity.getAIMoveSpeed();
        if (s != 0)
            newEntity.setAIMoveSpeed(s);
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
        if (entity.addedToChunk && ((ChunkProviderServer)world.getChunkProvider()).chunkExists(i, j))
            world.getChunkFromChunkCoords(i, j).removeEntity(entity);
        world.loadedEntityList.remove(entity);
        //BaseReflectionUtils.call(world, onEntityRemoved, entity);
        world.onEntityRemoved(entity);
    }
    
    static void checkChunk(World world, Entity entity) {
        int cx = MathHelper.floor_double(entity.posX / 16.0D);
        int cy = MathHelper.floor_double(entity.posZ / 16.0D);
        Chunk chunk = world.getChunkFromChunkCoords(cx, cy);
    }
    
    protected static int yawSign(Entity entity) {
        if (entity instanceof EntityArrow)
            return -1;
        else
            return 1;
    }
    
    static Vector3 yawVector(Entity entity) {
        return yawVector(yawSign(entity) * entity.rotationYaw);
    }
    
    static Vector3 yawVector(double yaw) {
        double a = Math.toRadians(yaw);
        Vector3 v = new Vector3(-Math.sin(a), 0, Math.cos(a));
        //System.out.printf("SGBaseTE.yawVector: %.2f --> (%.3f, %.3f)\n", yaw, v.x, v.z);
        return v;
    }
    
    static double yawAngle(Vector3 v, Entity entity) {
        double a = Math.atan2(-v.x, v.z);
        double d = Math.toDegrees(a);
        //System.out.printf("SGBaseTE.yawAngle: (%.3f, %.3f) --> %.2f\n", v.x, v.z, d);
        return yawSign(entity) * d;
    }
    
    public SGBaseTE getConnectedStargateTE() {
        if (isConnected() && connectedLocation != null)
            return connectedLocation.getStargateTE();
        else
            return null;
        }
    
    //------------------------------------   Client   --------------------------------------------

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        //System.out.printf("SGBaseTE.onDataPacket: with state %s numEngagedChevrons %s\n",
        //  SGState.valueOf(pkt.customParam1.getInteger("state")),
        //  pkt.customParam1.getInteger("numEngagedChevrons"));
        SGState oldState = state;
        super.onDataPacket(net, pkt);
        if (isMerged && state != oldState) {
            switch (state) {
                case Transient:
                    initiateOpeningTransient();
                    break;
                case Disconnecting:
                    initiateClosingTransient();
                    break;
            }
        }
    }
    
    void clientUpdate() {
        lastRingAngle = ringAngle;
        switch (state) {
            case Dialling:
                //System.out.printf("SGBaseTe: Relaxing angle %s towards %s at rate %s\n",
                //  ringAngle, targetRingAngle, diallingRelaxationRate);
                //setRingAngle(Utils.relaxAngle(ringAngle, targetRingAngle, diallingRelaxationRate));
                updateRingAngle();
                //System.out.printf("SGBaseTe: Ring angle now %s\n", ringAngle);
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
        }
        else
            setRingAngle(targetRingAngle);
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
        double v[][] = getEventHorizonGrid()[1];
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int j = 0; j <= n+1; j++) {
            v[j][0] = openingTransientIntensity;
            v[j][1] = v[j][0] + openingTransientRandomness * random.nextGaussian();
        }
    }
    
    void initiateClosingTransient() {
        double v[][] = getEventHorizonGrid()[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++)
                v[j][i] += closingTransientRandomness * random.nextGaussian();
    }
    
    void applyRandomImpulse() {
        double v[][] = getEventHorizonGrid()[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        int i = random.nextInt(m - 1) + 1;
        int j = random.nextInt(n) + 1;
        v[j][i] += 0.05 * random.nextGaussian();
    }
    
    void updateEventHorizon() {
        double grid[][][] = getEventHorizonGrid();
        double u[][] = grid[0];
        double v[][] = grid[1];
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        double dt = 1.0;
        double asq = 0.03;
        double d = 0.95;
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++) {
                double du_dr = 0.5 * (u[j][i+1] - u[j][i-1]);
                double d2u_drsq = u[j][i+1] - 2 * u[j][i] + u[j][i-1];
                double d2u_dthsq = u[j+1][i] - 2 * u[j][i] + u[j-1][i];
                v[j][i] = d * v[j][i] + (asq * dt) * (d2u_drsq + du_dr / i + d2u_dthsq / (i * i));
        }
        for (int i = 1; i < m; i++)
            for (int j = 1; j <= n; j++)
                u[j][i] += v[j][i] * dt;
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
        //dumpGrid("u", u);
        //dumpGrid("v", v);
    }
    
    void dumpGrid(String label, double g[][]) {
        System.out.printf("SGBaseTE: %s:\n", label);
        int m = SGBaseTERenderer.ehGridRadialSize;
        int n = SGBaseTERenderer.ehGridPolarSize;
        for (int j = 0; j <= n+1; j++) {
            for (int i = 0; i <= m; i++)
                System.out.printf(" %6.3f", g[j][i]);
            System.out.printf("\n");
        }
    }
    
    @Override
    protected IInventory getInventory() {
        return inventory;
    }

    public boolean irisIsClosed() {
        //System.out.printf("SGBaseTE.irisIsClosed: irisPhase = %s\n", irisPhase);
        return hasIrisUpgrade && irisPhase <= maxIrisPhase / 2;
    }
    
    public double getIrisAperture(double t) {
        return (lastIrisPhase * (1 - t) + irisPhase * t) / maxIrisPhase;
    }
    
    void irisUpdate() {
        lastIrisPhase = irisPhase;
        switch (irisState) {
            case Opening:
                if (irisPhase < maxIrisPhase)
                    ++irisPhase;
                else
                    enterIrisState(IrisState.Open);
                break;
            case Closing:
                if (irisPhase > 0)
                    --irisPhase;
                else
                    enterIrisState(IrisState.Closed);
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
                        playSGSoundEffect(irisOpenSound, 1.0F, 1.0F);
                        break;
                    case Closing:
                        playSGSoundEffect(irisCloseSound, 1.0F, 1.0F);
                        break;
                }
            }
            if (!oldDesc.equals(newDesc))
                //postEvent("sgIrisStateChange", "oldState", oldDesc, "newState", newDesc);
                postEvent("sgIrisStateChange", newDesc, oldDesc);
        }
    }
    
    public void openIris() {
        if (isMerged && hasIrisUpgrade && irisState != IrisState.Open)
            enterIrisState(IrisState.Opening);
    }
    
    public void closeIris() {
        if (isMerged && hasIrisUpgrade && irisState != IrisState.Closed)
            enterIrisState(IrisState.Closing);
    }
    
    public void onNeighborBlockChange() {
        if (!worldObj.isRemote) {
            boolean newInput = BaseBlockUtils.blockIsGettingExternallyPowered(worldObj, pos);
            if (redstoneInput != newInput) {
                redstoneInput = newInput;
                markDirty();
                if (redstoneInput)
                    closeIris();
                else
                    openIris();
            }
        }
    }
    
    void updateIrisEntity() {
        if (!worldObj.isRemote) {
            if (isMerged && hasIrisUpgrade) {
                if (!hasIrisEntity()) {
                    IrisEntity ent = new IrisEntity(this);
                    worldObj.spawnEntityInWorld(ent);
                    //System.out.printf("SGBaseTE.updateIrisEntity: Spawned %s with bounds %s\n", ent,
                    //    ent.getEntityBoundingBox());
                }
            }
            else {
                //System.out.printf("SGBaseTE.updateIrisEntity: Removing iris entities\n");
                for (IrisEntity ent : findIrisEntities()) {
                    //System.out.printf("SGBaseTE.updateIrisEntity: Removing %s\n", ent);
                    worldObj.removeEntity(ent);
                }
            }
        }
    }
    
    boolean hasIrisEntity() {
        return findIrisEntities().size() != 0;
    }
    
    List<IrisEntity> findIrisEntities() {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        AxisAlignedBB box = new AxisAlignedBB(
            x, y, z, x + 1, y + 2, z + 1);
        //System.out.printf("SGBaseTE.findIrisEntities: in %s\n", box);
        return (List<IrisEntity>)worldObj.getEntitiesWithinAABB(IrisEntity.class, box);
    }
    
    ItemStack getCamouflageStack(BlockPos cpos) {
        //System.out.printf("SGBaseTE.getCamouflageStack: for %s from base at %s\n", cpos, pos);
        Trans3 t = localToGlobalTransformation();
        Vector3 p = t.ip(Vector3.blockCenter(cpos));
        //System.out.printf("SGBaseTE.getCamouflageStack: p = %s\n", p);
        if (p.y == 0) {
            int i = 2 + p.roundX();
            //System.out.printf("SGBaseTE.getCamouflageStack: i = %s\n", i);
            if (i >= 0 && i < 5)
                return getStackInSlot(firstCamouflageSlot + i);
        }
        return null;
    }
    
    boolean isCamouflageSlot(int slot) {
        return slot >= firstCamouflageSlot && slot < firstCamouflageSlot + numCamouflageSlots;
    }
    
    @Override
    protected void onInventoryChanged(int slot) {
        //System.out.printf("SGBaseTE.onInventoryChanged: %s\n", slot);
        super.onInventoryChanged(slot);
        if (isCamouflageSlot(slot)) {
            //System.out.printf("SGBaseTE.onInventoryChanged: Camouflage slot changed\n");
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    BaseBlockUtils.markBlockForUpdate(worldObj, pos.add(dx, 0, dz));
        }
    }
    
    public int numItemsInSlot(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null)
            return stack.stackSize;
        else
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
                if (block instanceof BlockSlab)
                    return 1;
                int meta = item.getMetadata(stack);
                IBlockState state = block.getStateFromMeta(meta);
                if (block.isFullCube(state))
                    return 2;
            }
        }
        return 0;
    }

    static int rdx[] = {1, 0, -1, 0};
    static int rdz[] = {0, -1, 0, 1};
    
    // Find locations of tile entities that could connect to the stargate ring.
    // TODO: Cache this
    public Collection<BlockRef> adjacentTiles() {
        Collection<BlockRef> result = new ArrayList<BlockRef>();
        Trans3 t = localToGlobalTransformation();
        for (int i = -2; i <= 2; i++) {
            BlockPos bp = t.p(i, -1, 0).blockPos();
            TileEntity te = getWorldTileEntity(worldObj, bp);
            if (te != null)
                result.add(new BlockRef(te));
        }
        return result;
    }
    
//------------------------------------ Computer interface ----------------------------------

    public void forwardNetworkPacket(Object packet) {
        SGBaseTE dte = getConnectedStargateTE();
        if (dte != null)
            dte.rebroadcastNetworkPacket(packet);
    }
    
    void rebroadcastNetworkPacket(Object packet) {
        for (BlockRef ref : adjacentTiles()) {
            TileEntity te = ref.getTileEntity();
            if (te instanceof SGInterfaceTE)
                ((SGInterfaceTE)te).rebroadcastNetworkPacket(packet);
        }
    }
    
    public String sendMessage(Object[] args) {
        SGBaseTE dte = getConnectedStargateTE();
        if (dte != null) {
            dte.postEvent("sgMessageReceived", args);
            return null;
        }
        else
            return "Stargate not connected";
    }
    
    void postEvent(String name, Object... args) {
        //System.out.printf("SGBaseTE.postEvent: %s from (%s,%s,%s)\n", name,
        //  xCoord, yCoord, zCoord);
        for (BlockRef b : adjacentTiles()) {
            TileEntity te = b.getTileEntity();
            if (te instanceof IComputerInterface) {
                //System.out.printf("SGBaseTE.postEvent: to TE at (%s,%s,%s)\n",
                //  b.xCoord, b.yCoord, b.zCoord);
                ((IComputerInterface)te).postEvent(this, name, args);
            }
        }
    }
    
    public String sgStateDescription() {
        return sgStateDescription(state);
    }
    
    static String sgStateDescription(SGState state) {
        switch (state) {
            case Idle: return "Idle";
            case Dialling:
            case InterDialling: return "Dialling";
            case Transient: return "Opening";
            case Connected: return "Connected";
            case Disconnecting: return "Closing";
            default: return "Unknown";
        }
    }
    
    public String irisStateDescription() {
        return irisStateDescription(irisState);
    }
    
    static String irisStateDescription(IrisState state) {
        return state.toString();
    }
    
    public static SGBaseTE getBaseTE(SGInterfaceTE ite) {
        return SGBaseTE.get(ite.getWorld(), ite.getPos().add(0, 1, 0));
    }
    
}

//------------------------------------------------------------------------------------------------

class BlockRef {

    public IBlockAccess worldObj;
    BlockPos pos;
    
    public BlockRef(TileEntity te) {
        this(te.getWorld(), te.getPos());
    }
    
    public BlockRef(IBlockAccess world, BlockPos pos) {
        worldObj = world;
        this.pos = pos;
    }
    
    public TileEntity getTileEntity() {
        return worldObj.getTileEntity(pos);
    }
    
}
