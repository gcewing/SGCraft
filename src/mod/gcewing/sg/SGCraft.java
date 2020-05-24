//------------------------------------------------------------------------------------------------
//
//   SG Craft - Main Class
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.structure.*;

import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.world.*;
import net.minecraftforge.event.terraingen.*;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.gameevent.*;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.registry.*;

// import ic2.api.item.*; //[IC2]
// import dan200.computercraft.api.*; //[CC]
import gcewing.sg.ic2.*; //[IC2]
import gcewing.sg.rf.*; //[RF]
import gcewing.sg.cc.*; //[CC]
import gcewing.sg.oc.*; //[OC]

import static gcewing.sg.BaseUtils.*;

@Mod(modid = Info.modID, name = Info.modName, version = Info.versionNumber,
    acceptableRemoteVersions = Info.versionBounds)

public class SGCraft extends BaseMod<SGCraftClient> {

    public static final Material machineMaterial = new Material(MapColor.ironColor);

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
    public static int tokraVillagerID;
    
    public static BaseSubsystem ic2Integration; //[IC2]
    public static BaseSubsystem rfIntegration; //[RF]
    public static BaseSubsystem txIntegration; //[TX]
    public static BaseSubsystem ccIntegration; //[CC]
    public static OCIntegration ocIntegration; //[OC]
    public static MystcraftIntegration mystcraftIntegration; //[MYST]

    public SGCraft() {
        mod = this;
//         assetKey = "gcewing_sg";
        blockDomain = itemDomain = "gcewing_sg";
        creativeTab = new CreativeTabs("gcewing_sg:sgcraft") {
            public Item getTabIconItem() {
                return Item.getItemFromBlock(sgBaseBlock);
            }
        };
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        FMLCommonHandler.instance().bus().register(this);
        ic2Integration = integrateWithMod("IC2", "gcewing.sg.ic2.IC2Integration"); //[IC2]
        rfIntegration = integrateWithMod("CoFHCore", "gcewing.sg.rf.RFIntegration"); //[RF]
        txIntegration = integrateWithMod("ThermalExpansion", "gcewing.sg.TXIntegration"); //[TX]
        ccIntegration = integrateWithMod("ComputerCraft", "gcewing.sg.cc.CCIntegration"); //[CC]
        ocIntegration = (OCIntegration)integrateWithMod("OpenComputers", "gcewing.sg.oc.OCIntegration"); //[OC]
        mystcraftIntegration = (MystcraftIntegration)integrateWithMod("Mystcraft", "gcewing.sg.MystcraftIntegration"); //[MYST]
        super.preInit(e);
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        super.init(e);
        System.out.printf("SGCraft.init\n");
        configure();
        channel = new SGChannel(Info.modID);
        chunkManager = new BaseTEChunkManager(this);
        //chunkManager.debug = true;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @Override   
    protected SGCraftClient initClient() {
        return new SGCraftClient(this);
    }

    void configure() {
        DHDTE.configure(config);
        NaquadahOreWorldGen.configure(config);
        SGBaseBlock.configure(config);
        SGBaseTE.configure(config);
        FeatureGeneration.configure(config);
        addOresToExistingWorlds = config.getBoolean("options", "addOresToExistingWorlds", false);
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
        if (isModLoaded("IC2") || (isModLoaded("CoFHCore") && !isModLoaded("ThermalExpansion"))) {
            ic2Capacitor = newItem("ic2Capacitor");
        }
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
        ItemStack chiselledSandstone = new ItemStack(Blocks.sandstone, 1, 1);
        ItemStack smoothSandstone = new ItemStack(Blocks.sandstone, 1, 2);
        ItemStack sgChevronBlock = new ItemStack(sgRingBlock, 1, 1);
        ItemStack blueDye = new ItemStack(Items.dye, 1, 4);
        ItemStack orangeDye = new ItemStack(Items.dye, 1, 14);
        if (config.getBoolean("options", "allowCraftingNaquadah", false))
            newShapelessRecipe(naquadah, 1, Items.coal, Items.slime_ball, Items.blaze_powder);
        newRecipe(sgRingBlock, 1, "CCC", "NNN", "SSS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone);
        newRecipe(sgChevronBlock, "CgC", "NpN", "SrS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'g', Items.glowstone_dust, 'r', Items.redstone, 'p', Items.ender_pearl);
        newRecipe(sgBaseBlock, 1, "CrC", "NeN", "ScS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'r', Items.redstone, 'e', Items.ender_eye, 'c', sgCoreCrystal);
        newRecipe(sgControllerBlock, 1, "bbb", "OpO", "OcO",
            'b', Blocks.stone_button, 'O', Blocks.obsidian, 'p', Items.ender_pearl,
            'r', Items.redstone, 'c', sgControllerCrystal);
        newShapelessRecipe(naquadahIngot, 1, "naquadah", Items.iron_ingot);
        newRecipe(naquadahBlock, 1, "NNN", "NNN", "NNN", 'N', "ingotNaquadahAlloy");
        newRecipe(sgChevronUpgrade, 1, "g g", "pNp", "r r",
            'N', "ingotNaquadahAlloy",
            'g', Items.glowstone_dust, 'r', Items.redstone, 'p', Items.ender_pearl);
        newRecipe(naquadahIngot, 9, "B", 'B', naquadahBlock);
        newRecipe(sgIrisBlade, 1, " ii", "ic ", "i  ",
            'i', Items.iron_ingot, 'c', new ItemStack(Items.coal, 1, 1));
        newRecipe(sgIrisUpgrade, 1, "bbb", "brb", "bbb",
            'b', sgIrisBlade, 'r', Items.redstone);
        if (config.getBoolean("options", "allowCraftingCrystals", false)) {
            newRecipe(sgCoreCrystal, 1, "bbr", "rdb", "brb",
                'b', blueDye, 'r', Items.redstone, 'd', Items.diamond);
            newRecipe(sgControllerCrystal, 1, "roo", "odr", "oor",
                'o', orangeDye, 'r', Items.redstone, 'd', Items.diamond);
        }
        if (!isModLoaded("ThermalExpansion") && isModLoaded("CoFHCore")) {
            if (!isModLoaded("IC2"))
                registerFallbackCapacitorRecipe();
            registerFallbackPowerBlockRecipe();
        }
    }
    
    protected void registerFallbackCapacitorRecipe() {
        newRecipe(ic2Capacitor, 1, "iii", "ppp", "iii",
            'i', Items.iron_ingot, 'p', Items.paper);
    }
    
    protected void registerFallbackPowerBlockRecipe() {
        newRecipe(rfPowerUnit, 1, "cgc", "gIg", "crc",
            'c', ic2Capacitor, 'g', Items.gold_ingot,
            'I', Blocks.iron_block, 'r', Items.redstone);
    }
    
    @Override
    protected void registerContainers() {
        //System.out.printf("SGCraft.registerContainers\n");
        addContainer(SGGui.SGBase, SGBaseContainer.class);
        addContainer(SGGui.DHDFuel, DHDFuelContainer.class);
        addContainer(SGGui.PowerUnit, PowerContainer.class);
    }

    //@Override
    //protected void registerRandomItems() {
        //String[] categories = {ChestGenHooks.MINESHAFT_CORRIDOR,
            //ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST,
            //ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.VILLAGE_BLACKSMITH};
        //addRandomChestItem(new ItemStack(sgBaseBlock), 1, 1, 2, categories);
        //addRandomChestItem(new ItemStack(sgControllerBlock), 1, 1, 1, categories);
        //addRandomChestItem(new ItemStack(sgRingBlock, 1, 0), 1, 3, 8, categories);
        //addRandomChestItem(new ItemStack(sgRingBlock, 1, 1), 1, 3, 7, categories);
        //addRandomChestItem(new ItemStack(sgCoreCrystal), 1, 1, 2, categories);
        //addRandomChestItem(new ItemStack(sgControllerCrystal), 1, 1, 1, categories);
        //addRandomChestItem(new ItemStack(sgChevronUpgrade), 1, 1, 1, categories);
        //addRandomChestItem(new ItemStack(sgIrisBlade), 1, 1, 1, categories);
    //}
    
    @Override
    protected void registerWorldGenerators() {
        if (config.getBoolean("options", "enableNaquadahOre", true)) {
            System.out.printf("SGCraft: Registering NaquadahOreWorldGen\n");
            naquadahOreGenerator = new NaquadahOreWorldGen();
            GameRegistry.registerWorldGenerator(naquadahOreGenerator, 0);
        }
        registerStructureComponent(FeatureUnderDesertPyramid.class,
            "SGCraft:FeatureUnderDesertPyramid");
    }
    
    @Override //[VILL]
    protected void registerVillagers() {
        tokraVillagerID = addVillager("tokra", resourceLocation("textures/skins/tokra.png"));
        addTradeHandler(tokraVillagerID, new SGTradeHandler());
    }
    
    @Override
    protected void registerEntities() {
        addEntity(IrisEntity.class, "Stargate Iris", SGEntity.Iris, 1000000, false);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load e) {
        Chunk chunk = e.getChunk();
        //System.out.printf("SGCraft.onChunkLoad: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
        SGChunkData.onChunkLoad(e);
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save e) {
        Chunk chunk = e.getChunk();
        //System.out.printf("SGCraft.onChunkSave: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
        SGChunkData.onChunkSave(e);
    }
    
    @SubscribeEvent
    public void onInitMapGen(InitMapGenEvent e) {
        FeatureGeneration.onInitMapGen(e);
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        //System.out.printf("SGCraft.onServerTick\n");
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
        if (!getChunkWorld(chunk).isRemote) {
            //System.out.printf("SGCraft.onChunkUnload: (%d, %d)\n", chunk.xPosition, chunk.zPosition);
            for (Object obj : getChunkTileEntityMap(chunk).values()) {
                if (obj instanceof SGBaseTE) {
                    SGBaseTE te = (SGBaseTE)obj;
                    //System.out.printf("SGCraft.onChunkUnload: Disconnecting stargate at (%s, %s, %s)\n",
                    //  te.xCoord, te.yCoord, te.zCoord);
                    te.disconnect();
                }
            }
        }
    }

//  @SubscribeEvent
//  void onWorldLoad(WorldEvent.Load e) {
//      System.out.printf("SGCraft: World loaded: %s\n", e.world);
//  }
//  
//  @SubscribeEvent
//  void onWorldUnload(WorldEvent.Unload e) {
//      System.out.printf("SGCraft: World unloaded: %s\n", e.world);
//  }

}
