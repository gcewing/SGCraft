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

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.*;
import static net.minecraftforge.fml.common.registry.VillagerRegistry.*;

// import dan200.computercraft.api.*; //[CC]
// import gcewing.sg.cc.*; //[CC]
import gcewing.sg.oc.*; //[OC]

@Mod(modid = Info.modID, name = Info.modName, version = Info.versionNumber,
    acceptableRemoteVersions = Info.versionBounds)

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
    
    public static boolean rfAvailable;
    
    public static BaseSubsystem ic2Integration; //[IC2]
//     public static IIntegration ccIntegration; //[CC]
    public static OCIntegration ocIntegration; //[OC]
//     public static MystcraftIntegration mystcraftIntegration; //[MYST]

    public SGCraft() {
        mod = this;
        creativeTab = new CreativeTabs("sgcraft:sgcraft") {
            public Item getTabIconItem() {
                return Item.getItemFromBlock(sgBaseBlock);
            }
        };
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        FMLCommonHandler.instance().bus().register(this);
        rfAvailable = classAvailable("cofh.api.energy.IEnergyConnection");
        if (rfAvailable)
            loadSubsystem("gcewing.sg.rf.RFIntegration"); //[RF]
        ic2Integration = integrateWithMod("IC2", "gcewing.sg.ic2.IC2Integration"); //[IC2]
//         ccIntegration = (CCIntegration)integrateWithMod("ComputerCraft", "gcewing.sg.cc.CCIntegration"); //[CC]
        ocIntegration = (OCIntegration)integrateWithMod("OpenComputers", "gcewing.sg.oc.OCIntegration"); //[OC]
//         mystcraftIntegration = (MystcraftIntegration)integrateWithMod("Mystcraft", "gcewing.sg.MystcraftIntegration"); //[MYST]
        super.preInit(e);
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        super.init(e);
        System.out.printf("SGCraft.init\n");
        configure();
        channel = new SGChannel(Info.modID);
        chunkManager = new BaseTEChunkManager(this);
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
        if (isModLoaded("IC2") || (rfAvailable && !isModLoaded("ThermalExpansion"))) {
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
        ItemStack chiselledSandstone = new ItemStack(Blocks.SANDSTONE, 1, 1);
        ItemStack smoothSandstone = new ItemStack(Blocks.SANDSTONE, 1, 2);
        ItemStack sgChevronBlock = new ItemStack(sgRingBlock, 1, 1);
        ItemStack blueDye = new ItemStack(Items.DYE, 1, 4);
        ItemStack orangeDye = new ItemStack(Items.DYE, 1, 14);
        if (config.getBoolean("options", "allowCraftingNaquadah", false))
            newShapelessRecipe(naquadah, 1, Items.COAL, Items.SLIME_BALL, Items.BLAZE_POWDER);
        newRecipe(sgRingBlock, 1, "CCC", "NNN", "SSS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone);
        newRecipe(sgChevronBlock, "CgC", "NpN", "SrS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        newRecipe(sgBaseBlock, 1, "CrC", "NeN", "ScS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'r', Items.REDSTONE, 'e', Items.ENDER_EYE, 'c', sgCoreCrystal);
        newRecipe(sgControllerBlock, 1, "bbb", "OpO", "OcO",
            'b', Blocks.STONE_BUTTON, 'O', Blocks.OBSIDIAN, 'p', Items.ENDER_PEARL,
            'r', Items.REDSTONE, 'c', sgControllerCrystal);
        newShapelessRecipe(naquadahIngot, 1, "naquadah", Items.IRON_INGOT);
        newRecipe(naquadahBlock, 1, "NNN", "NNN", "NNN", 'N', "ingotNaquadahAlloy");
        newRecipe(sgChevronUpgrade, 1, "g g", "pNp", "r r",
            'N', "ingotNaquadahAlloy",
            'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        newRecipe(naquadahIngot, 9, "B", 'B', naquadahBlock);
        newRecipe(sgIrisBlade, 1, " ii", "ic ", "i  ",
            'i', Items.IRON_INGOT, 'c', new ItemStack(Items.COAL, 1, 1));
        newRecipe(sgIrisUpgrade, 1, "bbb", "brb", "bbb",
            'b', sgIrisBlade, 'r', Items.REDSTONE);
        if (config.getBoolean("options", "allowCraftingCrystals", false)) {
            newRecipe(sgCoreCrystal, 1, "bbr", "rdb", "brb",
                'b', blueDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
            newRecipe(sgControllerCrystal, 1, "roo", "odr", "oor",
                'o', orangeDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
        }
        if (rfAvailable && !isModLoaded("IC2"))
            addGenericCapacitorRecipe();
    }
    
    protected void addGenericCapacitorRecipe() {
        newRecipe(ic2Capacitor, 1, "iii", "ppp", "iii",
            'i', "ingotIron", 'p', "paper");
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
        MapGenStructureIO.registerStructureComponent(FeatureUnderDesertPyramid.class,
            "SGCraft:FeatureUnderDesertPyramid");
    }
    
    @Override //[VILL]
    protected void registerVillagers() {
        VillagerProfession tokraProfession = new VillagerProfession("sgcraft:tokra", "sgcraft:textures/skins/tokra.png");
        VillagerCareer tokraCareer = new VillagerCareer(tokraProfession, "sgcraft:tokra");
        tokraCareer.addTrade(1, new SGTradeHandler());
        VillagerRegistry.instance().register(tokraProfession);
    }

    @Override
    protected void registerEntities() {
        addEntity(IrisEntity.class, "Stargate Iris", SGEntity.Iris, 1000000, false);
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

}
