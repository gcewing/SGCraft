//------------------------------------------------------------------------------------------------
//
//   SG Craft - Main Class
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.oc.OCIntegration;
import gcewing.sg.rf.RFIntegration;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import static net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

// import dan200.computercraft.api.*; //[CC]

@Mod(modid = Info.modID, name = Info.modName, version = Info.versionNumber,
    acceptableRemoteVersions = Info.versionBounds, dependencies = "after:opencomputers;after:ic2;after:computercraft")

public class SGCraft extends BaseMod<SGCraftClient> {

    public static final Material machineMaterial = new Material(MapColor.IRON);

    public static SGCraft mod;

    public static SGChannel channel;
    public static BaseTEChunkManager chunkManager;
    
    public static SGBaseBlock sgBaseBlock;
    public static SGRingBlock sgRingBlock;
    public static DHDBlock sgControllerBlock;
    //public static SGPortalBlock sgPortalBlock;
    public static Block naquadahBlock, naquadahOre;
    
    public static Item naquadah, naquadahIngot, sgCoreCrystal, sgControllerCrystal, sgChevronUpgrade,
        sgIrisUpgrade, sgIrisBlade;
    
    public static Block ic2PowerUnit;
    public static Item ic2Capacitor;
    public static Block rfPowerUnit;
    
    public static boolean addOresToExistingWorlds;
    public static NaquadahOreWorldGen naquadahOreGenerator;
//     public static int tokraVillagerID;
    
    public static BaseSubsystem ic2Integration; //[IC2]
    public static IIntegration ccIntegration; //[CC]
    public static OCIntegration ocIntegration; //[OC]
    public static RFIntegration rfIntegration; //[RF]
//     public static MystcraftIntegration mystcraftIntegration; //[MYST]

    // Villager Profession for Generators
    public static VillagerProfession tokraProfession;

    // Block Harvests
    public static boolean canHarvestDHD = false;
    public static boolean canHarvestSGBaseBlock = false;
    public static boolean canHarvestSGRingBlock = false;

    // IC2 Options
    public static int Ic2SafeInput = 2048;
    public static int Ic2MaxEnergyBuffer = 1000000;
    public static double Ic2euPerSGEnergyUnit = 20.0;
    public static int Ic2PowerTETier = 3;

    // Redstone Flux Options
    public static int RfMaxEnergyBuffer = 4000000;
    public static double RfPerSGEnergyUnit = 80.0;

    //Client Options
    public static boolean useHDEventHorizionTexture = true;
    public static boolean saveAddressToClipboard = false;
    public static boolean displayGuiPowerDebug = true;

    //World data fixes
    public static boolean forceDHDCfgUpdate = false;
    public static boolean forceIC2CfgUpdate = false;
    public static boolean forceRFCfgUpdate = false;

    public SGCraft() {
        mod = this;
        creativeTab = new CreativeTabs("sgcraft:sgcraft") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(Item.getItemFromBlock(sgBaseBlock));
            }
        };
    }
    
    @Mod.EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        FMLCommonHandler.instance().bus().register(this);
        rfIntegration = (RFIntegration) integrateWithMod("redstoneflux", "gcewing.sg.rf.RFIntegration"); //[RF]
        ic2Integration = integrateWithMod("ic2", "gcewing.sg.ic2.IC2Integration"); //[IC2]
        ccIntegration = (IIntegration) integrateWithMod("computercraft", "gcewing.sg.cc.CCIntegration"); //[CC]
        ocIntegration = (OCIntegration)integrateWithMod("opencomputers", "gcewing.sg.oc.OCIntegration"); //[OC]
//         mystcraftIntegration = (MystcraftIntegration)integrateWithMod("Mystcraft", "gcewing.sg.MystcraftIntegration"); //[MYST]
        super.preInit(e);
    }
    
    @Mod.EventHandler
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        System.out.printf("SGCraft.init\n");
        configure();
        channel = new SGChannel(Info.modID);
        chunkManager = new BaseTEChunkManager(this);
    }

    @Mod.EventHandler
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @Override   
    protected SGCraftClient initClient() {
        return new SGCraftClient(this);
    }

    @Override
    void configure() {
        DHDTE.configure(config);
        NaquadahOreWorldGen.configure(config);
        SGBaseBlock.configure(config);
        SGBaseTE.configure(config);
        FeatureGeneration.configure(config);
        // Server-Side Options
        addOresToExistingWorlds = config.getBoolean("options", "addOresToExistingWorlds", false);
        // Client-Side Options
        useHDEventHorizionTexture = config.getBoolean("client", "useHDEventHorizonTexture", useHDEventHorizionTexture);
        saveAddressToClipboard = config.getBoolean("client", "saveAddressToClipboard", saveAddressToClipboard);
    }       

    @Override
    protected void registerOther() {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @Override
    protected void registerBlocks() {
        sgRingBlock = newBlock("stargateRing", SGRingBlock.class, SGRingItem.class);
        sgBaseBlock = newBlock("stargateBase", SGBaseBlock.class);
        sgControllerBlock = newBlock("stargateController", DHDBlock.class);
        //sgPortalBlock = newBlock("stargatePortal", SGPortalBlock.class);
        naquadahBlock = newBlock("naquadahBlock", NaquadahBlock.class);
        naquadahOre = newBlock("naquadahOre", NaquadahOreBlock.class);
        this.setOptions();
    }
    
    @Override
    protected void registerItems() {
        naquadah = newItem("naquadah"); //, "Naquadah");
        naquadahIngot = newItem("naquadahIngot"); //, "Naquadah Alloy Ingot");
        sgCoreCrystal = newItem("sgCoreCrystal"); //, "Stargate Core Crystal");
        sgControllerCrystal = newItem("sgControllerCrystal"); //, "Stargate Controller Crystal");
        sgChevronUpgrade = addItem(new SGChevronUpgradeItem(), "sgChevronUpgrade");
        sgIrisUpgrade = addItem(new SGIrisUpgradeItem(), "sgIrisUpgrade");
        sgIrisBlade = newItem("sgIrisBlade");
        if (isModLoaded("ic2") || !isModLoaded("thermalexpansion")) {
            ic2Capacitor = newItem("ic2Capacitor");
        }
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound) {
        playSound(source, sound, SoundCategory.AMBIENT);
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound, SoundCategory category) {
        SoundHandler soundHandler = getSoundHandler();
        soundHandler.playSound(new Sound(source, sound, category));
    }

    @SideOnly(Side.CLIENT)
    private static SoundHandler getSoundHandler() {
        return Minecraft.getMinecraft().getSoundHandler();
    }
    
    public static boolean isValidStargateUpgrade(Item item) {
        return item == sgChevronUpgrade || item == sgIrisUpgrade;
    }
    
    @Override
    protected void registerOres() {
        addOre("oreNaquadah", naquadahOre);
        addOre("naquadah", naquadah);
        addOre("ingotNaquadahAlloy", naquadahIngot);
    }

    @Override
    protected void registerRecipes() {
        ItemStack chiselledSandstone = new ItemStack(Blocks.SANDSTONE, 1, 1);
        ItemStack smoothSandstone = new ItemStack(Blocks.SANDSTONE, 1, 2);
        ItemStack sgChevronBlock = new ItemStack(sgRingBlock, 1, 1);
        ItemStack blueDye = new ItemStack(Items.DYE, 1, 4);
        ItemStack orangeDye = new ItemStack(Items.DYE, 1, 14);

        if (config.getBoolean("recipes", "naquadah", false)) {
            newShapelessRecipe("naquada", naquadah, 1, Ingredient.fromItems(Items.COAL, Items.SLIME_BALL, Items.BLAZE_POWDER));
        }

        if (config.getBoolean("recipes", "naquadahIngot", true)) {
            newShapelessRecipe("naquadahingot", naquadahIngot, 1, Ingredient.fromItem(Items.IRON_INGOT), Ingredient.fromItem(naquadah));
        }

        if (config.getBoolean("recipes", "naquadahIngotFromBlock", true)) {
            newRecipe("naquadahingot_from_block", naquadahIngot, 9, "B", 'B', naquadahBlock);
        }

        if (config.getBoolean("recipes", "naquadahBlock", true)) {
            newRecipe("naquadahblock", naquadahBlock, 1, "NNN", "NNN", "NNN", 'N', "ingotNaquadahAlloy");
        }

        if (config.getBoolean("recipes", "sgRingBlock", true)) {
            newRecipe("sgringblock", sgRingBlock, 1, "CCC", "NNN", "SSS", 'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone);
        }

        if (config.getBoolean("recipes", "sgChevronBlock", true)) {
            newRecipe("sgcheveronblock", sgChevronBlock, "CgC", "NpN", "SrS", 'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone, 'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        }

        if (config.getBoolean("recipes", "sgBaseBlock", true)) {
            newRecipe("sgbaseblock", sgBaseBlock, 1, "CrC", "NeN", "ScS", 'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone, 'r', Items.REDSTONE, 'e', Items.ENDER_EYE, 'c', sgCoreCrystal);
        }

        if (config.getBoolean("recipes", "sgControllerBlock", true)) {
            newRecipe("sgcontrollerblock", sgControllerBlock, 1, "bbb", "OpO", "OcO", 'b', Blocks.STONE_BUTTON, 'O', Blocks.OBSIDIAN, 'p', Items.ENDER_PEARL, 'c', sgControllerCrystal);
        }

        if (config.getBoolean("recipes", "sgChevronUpgradeItem", true)) {
            newRecipe("sgchevronupgrade", sgChevronUpgrade, 1, "g g", "pNp", "r r", 'N', "ingotNaquadahAlloy", 'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        }

        if (config.getBoolean("recipes", "sgIrisBladeItem", true)) {
            newRecipe("sgirisblade", sgIrisBlade, 1, " ii", "ic ", "i  ", 'i', Items.IRON_INGOT, 'c', new ItemStack(Items.COAL, 1, 1));
        }

        if (config.getBoolean("recipes", "sgIrisUpgradeItem", true)) {
            newRecipe("sgirisupgrade", sgIrisUpgrade, 1, "bbb", "brb", "bbb", 'b', sgIrisBlade, 'r', Items.REDSTONE);
        }

        if (config.getBoolean("recipes", "sgCoreCrystalItem", false)) {
            newRecipe("sgcorecrystal", sgCoreCrystal, 1, "bbr", "rdb", "brb", 'b', blueDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
        }

        if (config.getBoolean("recipes", "sgControllerCrystalItem", false)) {
            newRecipe("sgcontrollercrystal", sgControllerCrystal, 1, "roo", "odr", "oor", 'o', orangeDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
        }

        if (!isModLoaded("ic2"))
            addGenericCapacitorRecipe();
    }

    protected void addGenericCapacitorRecipe() {
        if (config.getBoolean("recipes", "genericCapacitorItem", true)) {
            newRecipe("ic2capacitor", ic2Capacitor, 1, "iii", "ppp", "iii", 'i', "ingotIron", 'p', "paper");
        }
    }

    @Override
    protected void registerContainers() {
        addContainer(SGGui.SGBase, SGBaseContainer.class);
        addContainer(SGGui.DHDFuel, DHDFuelContainer.class);
        addContainer(SGGui.PowerUnit, PowerContainer.class);
    }
   
    @Override
    protected void registerWorldGenerators() {
        if (config.getBoolean("options", "enableNaquadahOre", true)) {
            System.out.printf("SGCraft: Registering NaquadahOreWorldGen\n");
            naquadahOreGenerator = new NaquadahOreWorldGen();
            GameRegistry.registerWorldGenerator(naquadahOreGenerator, 0);
        }
        MapGenStructureIO.registerStructureComponent(FeatureUnderDesertPyramid.class, "SGCraft:FeatureUnderDesertPyramid");
    }
    
    @Override //[VILL]
    protected void registerVillagers() {
        tokraProfession = new VillagerProfession("sgcraft:tokra", "sgcraft:textures/skins/tokra.png","sgcraft:textures/skins/tokra.png");
        // Update: Needs new skin for Zombie mode?
        VillagerCareer tokraCareer = new VillagerCareer(tokraProfession, "sgcraft:tokra");
        tokraCareer.addTrade(1, new SGTradeHandler());
        ForgeRegistries.VILLAGER_PROFESSIONS.register(tokraProfession);
    }

    @Override
    protected void registerEntities() {
        addEntity(EntityStargateIris.class, "stargate_iris", SGEntity.Iris, 1000000, false);
    }
    
    @Override
    protected void registerSounds() {
        SGBaseTE.registerSounds(this);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load e) {
        Chunk chunk = e.getChunk();
        SGChunkData.onChunkLoad(e);
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save e) {
        Chunk chunk = e.getChunk();
        SGChunkData.onChunkSave(e);
    }
    
    @SubscribeEvent
    public void onInitMapGen(InitMapGenEvent e) {
        FeatureGeneration.onInitMapGen(e);
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        switch (e.phase) {
            case START: {
                for (BaseSubsystem om : subsystems)
                    if (om instanceof IIntegration)
                        ((IIntegration)om).onServerTick();
                break;
            }
        }
    }
    
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload e) {
        Chunk chunk = e.getChunk();
        if (!chunk.getWorld().isRemote) {
            for (Object obj : chunk.getTileEntityMap().values()) {
                if (obj instanceof SGBaseTE) {
                    SGBaseTE te = (SGBaseTE)obj;
                    te.disconnect();
                }
            }
        }
    }

    private void setOptions() {
        // Block Harvests
        canHarvestDHD = config.getBoolean("block-harvest", "dhdBlock", canHarvestDHD);
        canHarvestSGBaseBlock  = config.getBoolean("block-harvest", "sgBaseBlock", canHarvestSGBaseBlock);
        canHarvestSGRingBlock  = config.getBoolean("block-harvest", "sgRingBlock", canHarvestSGRingBlock);

        // IC2
        Ic2SafeInput  = config.getInteger("ic2", "safeInputRate", Ic2SafeInput);
        Ic2MaxEnergyBuffer = config.getInteger("ic2", "energyBufferSize", Ic2MaxEnergyBuffer);
        Ic2euPerSGEnergyUnit = config.getDouble("ic2", "euPerSGEnergyUnit", Ic2euPerSGEnergyUnit);
        Ic2PowerTETier = config.getInteger("ic2", "PowerTETier", Ic2PowerTETier);

        // Redstone Flux
        RfMaxEnergyBuffer = config.getInteger("rf", "energyBufferSize", RfMaxEnergyBuffer);
        RfPerSGEnergyUnit = config.getDouble("rf", "rfPerSGEnergyUnit", RfPerSGEnergyUnit);

        // World Update / Fixes
        forceDHDCfgUpdate = config.getBoolean("dhd", "force-update", forceDHDCfgUpdate);
        forceIC2CfgUpdate = config.getBoolean("ic2", "force-update", forceIC2CfgUpdate);
        forceRFCfgUpdate = config.getBoolean("rf", "force-update", forceRFCfgUpdate);
    }
}
