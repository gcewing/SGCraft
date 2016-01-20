//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Mod
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import net.minecraft.block.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import net.minecraftforge.common.*;
import net.minecraftforge.common.config.*;
import net.minecraftforge.client.*;
import net.minecraftforge.oredict.*;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.common.registry.VillagerRegistry.*;
import cpw.mods.fml.relauncher.*;

public class BaseMod<CLIENT extends BaseModClient<? extends BaseMod>>
	extends BaseIntegration implements IGuiHandler
{

	interface IBlock {
		public void setRenderType(int id);
		public String getQualifiedRendererClassName();
	}
	
	interface ITileEntity {
		public void onAddedToWorld();
	}
	
	interface ISetMod {
		public void setMod(BaseMod mod);
	}
	
	public void setModOf(Object obj) {
		if (obj instanceof ISetMod)
			((ISetMod)obj).setMod(this);
	}
	
	static class IDBinding<T> {
		public int id;
		public T object;
	}
	
	public String modID;
	public BaseConfiguration config;
	public String modPackage;
	public String assetKey;
	public String resourceDir; // path to resources directory with leading and trailing slashes
	//public String textureFile; // path to default texture file with leading slash
	public URL resourceURL; // URL to the resources directory
	//public BaseMod base;
	public CLIENT client;
	public IGuiHandler proxy;
	public boolean serverSide, clientSide;
	public CreativeTabs creativeTab; // = CreativeTabs.tabMisc;
	public boolean debugGui;

	File cfgFile;
	List<IBlock> registeredBlocks = new ArrayList<IBlock>();
	List<BaseIntegration> integrations = new ArrayList<BaseIntegration>();

	public String resourcePath(String fileName) {
		return resourceDir + fileName;
	}

	public BaseMod() {
		Class modClass = getClass();
		modPackage = modClass.getPackage().getName();
		assetKey = modPackage.replace(".", "_");
		modID = getModID(modClass);
		String resourceRelDir = "assets/" + assetKey + "/";
		resourceDir = "/" + resourceRelDir;
		resourceURL = getClass().getClassLoader().getResource(resourceRelDir);
		integrations.add(this);
		creativeTab = CreativeTabs.tabMisc;
	}
	
	static String getModID(Class cls) {
		Annotation ann = cls.getAnnotation(Mod.class);
		if (ann instanceof Mod)
			return ((Mod)ann).modid();
		else {
			System.out.printf("BaseMod: Mod annotation not found\n");
			return "<unknown>";
		}
	}
	
	public static boolean isModLoaded(String modid) {
		return Loader.isModLoaded(modid);
	}

	public void preInit(FMLPreInitializationEvent e) {
		serverSide = e.getSide().isServer();
		clientSide = e.getSide().isClient();
		if (clientSide) {
			client = initClient();
			proxy = client;
		}
		cfgFile = e.getSuggestedConfigurationFile();
		loadConfig();
		configure();
		if (client != null)
			client.preInit(e);
		for (BaseIntegration i : integrations) {
			if (i != this)
				i.preInit(e);
			i.configure(config);
			i.registerBlocks();
			i.registerTileEntities();
			i.registerItems();
			i.registerOres();
		}
	}
	
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		if (client != null)
			client.init(e);
		for (BaseIntegration i : integrations)
			if (i != this)
				i.init(e);
	}
	
	public void postInit(FMLPostInitializationEvent e) {
		for (BaseIntegration i : integrations) {
			if (i != this)
				i.postInit(e);
			i.registerRecipes();
			i.registerRandomItems();
			i.registerWorldGenerators();
			i.registerContainers();
			i.registerEntities();
			i.registerVillagers();
			i.registerOther();
		}
		if (client != null)
			client.postInit(e);
		if (proxy == null)
			proxy = this;
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		saveConfig();
	}

	void loadConfig() {
		config = new BaseConfiguration(cfgFile);
	}

	void saveConfig() {
		if (config.extended)
			config.save();
	}

	String qualifiedName(String name) {
		return modPackage + "." + name;
	}
	
	//-------------------- Configuration ---------------------------------------------------------
	
	void configure() {
	}
	
	//----------------- Client Proxy -------------------------------------------------------------
	
	CLIENT initClient() {
		return (CLIENT)(new BaseModClient(this));
	}

	//--------------- Third-party mod integration ------------------------------------------------

	public BaseIntegration integrateWith(String modId, String className) {
		BaseIntegration om = null;
		if (isModLoaded(modId)) {
			om = newIntegration(className);
			integrations.add(om);
		}
		return om;
	}
	
	BaseIntegration newIntegration(String className) {
		try {
			return (BaseIntegration)Class.forName(className).newInstance();
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	//--------------- Item registration ----------------------------------------------------------
	
	public Item newItem(String name) {
		return newItem(name, Item.class);
	}

	public <ITEM extends Item> ITEM newItem(String name, Class<ITEM> cls) {
		ITEM item;
		try {
			Constructor<ITEM> ctor = cls.getConstructor();
			item = ctor.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return addItem(item, name);
	}

	public <ITEM extends Item> ITEM addItem(ITEM item, String name) {
		String qualName = assetKey + ":" + name;
		item.setUnlocalizedName(qualName);
		item.setTextureName(qualName);
		GameRegistry.registerItem(item, name);
		System.out.printf("BaseMod.addItem: Registered %s as %s\n", item, name);
		if (creativeTab != null) {
			System.out.printf("BaseMod.addItem: Setting creativeTab to %s\n", creativeTab);
			item.setCreativeTab(creativeTab);
		}
		return item;
	}
	
	//--------------- Block registration ----------------------------------------------------------

	public Block newBlock(String name) {
		return newBlock(name, Block.class);
	}
	
	public <BLOCK extends Block> BLOCK newBlock(String name, Class<BLOCK> cls) {
		return newBlock(name, cls, ItemBlock.class);
	}
	
	public <BLOCK extends Block> BLOCK newBlock(String name, Class<BLOCK> cls, Class itemClass) {
		BLOCK block;
		try {
			Constructor<BLOCK> ctor = cls.getConstructor();
			block = ctor.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return addBlock(block, name, itemClass);
	}
	
	public <BLOCK extends Block> BLOCK addBlock(BLOCK block, String name) {
		return addBlock(block, name, ItemBlock.class);
	}

	public <BLOCK extends Block> BLOCK addBlock(BLOCK block, String name, Class itemClass) {
		String qualName = assetKey + ":" + name;
		block.setBlockName(qualName);
		block.setBlockTextureName(qualName);
		System.out.printf("BaseMod.addBlock: name '%s' qualName '%s' %s\n", name, qualName, block);
		GameRegistry.registerBlock(block, itemClass, name);
		if (creativeTab != null) {
			System.out.printf("BaseMod.addBlock: Setting creativeTab to %s\n", creativeTab);
			block.setCreativeTab(creativeTab);
		}
		if (block instanceof IBlock)
			registeredBlocks.add((IBlock)block);
		return block;
	}
	
	//--------------- Ore registration ----------------------------------------------------------

	public void addOre(String name, Block block) {
		OreDictionary.registerOre(name, new ItemStack(block));
	}
	
	public void addOre(String name, Item item) {
		OreDictionary.registerOre(name, item);
	}
	
	public static boolean blockMatchesOre(Block block, String name) {
		return stackMatchesOre(new ItemStack(block), name);
	}

	public static boolean itemMatchesOre(Item item, String name) {
		return stackMatchesOre(new ItemStack(item), name);
	}

	public static boolean stackMatchesOre(ItemStack stack, String name) {
		int id = OreDictionary.getOreID(stack);
		return id == OreDictionary.getOreID(name);
	}

	//--------------- Recipe construction ----------------------------------------------------------

	public void newRecipe(Item product, int qty, Object... params) {
		newRecipe(new ItemStack(product, qty), params);
	}
	
	public void newRecipe(Block product, int qty, Object... params) {
		newRecipe(new ItemStack(product, qty), params);
	}

	public void newRecipe(ItemStack product, Object... params) {
		GameRegistry.addRecipe(new ShapedOreRecipe(product, params));
	}

	public void newShapelessRecipe(Block product, int qty, Object... params) {
		newShapelessRecipe(new ItemStack(product, qty), params);
	}
	
	public void newShapelessRecipe(Item product, int qty, Object... params) {
		newShapelessRecipe(new ItemStack(product, qty), params);
	}
	
	public void newShapelessRecipe(ItemStack product, Object... params) {
		GameRegistry.addRecipe(new ShapelessOreRecipe(product, params));
	}

	public void newSmeltingRecipe(Item product, int qty, Item input) {
		newSmeltingRecipe(product, qty, input, 0);
	}

	public void newSmeltingRecipe(Item product, int qty, Item input, int xp) {
		GameRegistry.addSmelting(input, new ItemStack(product, qty), xp);
	}
	
	public void newSmeltingRecipe(Item product, int qty, Block input) {
		newSmeltingRecipe(product, qty, input, 0);
	}

	public void newSmeltingRecipe(Item product, int qty, Block input, int xp) {
		GameRegistry.addSmelting(input, new ItemStack(product, qty), xp);
	}
	
	//--------------- Dungeon loot ----------------------------------------------------------

	public void addRandomChestItem(ItemStack stack, int minQty, int maxQty, int weight, String... category) {
		WeightedRandomChestContent item = new WeightedRandomChestContent(stack, minQty, maxQty, weight);
		for (int i = 0; i < category.length; i++)
			ChestGenHooks.addItem(category[i], item);
	}

	//--------------- Entity registration ----------------------------------------------------------

	public void addEntity(Class<? extends Entity> cls, String name, Enum id) {
		addEntity(cls, name, id.ordinal());
	}
	
	public void addEntity(Class<? extends Entity> cls, String name, int id) {
		addEntity(cls, name, id, 1, true);
	}

	public void addEntity(Class<? extends Entity> cls, String name, Enum id,
		int updateFrequency, boolean sendVelocityUpdates)
	{
		addEntity(cls, name, id.ordinal(), updateFrequency, sendVelocityUpdates);
	}
	
	public void addEntity(Class<? extends Entity> cls, String name, int id,
		int updateFrequency, boolean sendVelocityUpdates)
	{
		System.out.printf("%s: BaseMod.addEntity: %s, \"%s\", %s\n",
			getClass().getSimpleName(), cls.getSimpleName(), name, id);
		EntityRegistry.registerModEntity(cls, name, id, /*base*/this, 256, updateFrequency, sendVelocityUpdates);
	}

	//--------------- Villager registration -------------------------------------------------
	
	static class VSBinding extends IDBinding<ResourceLocation> {};
	
	public List<VSBinding> registeredVillagers = new ArrayList<VSBinding>();
	
	int addVillager(String name, ResourceLocation skin) {
		int id = config.getVillager(name);
		VSBinding b = new VSBinding();
		b.id = id;
		b.object = skin;
		registeredVillagers.add(b);
		return id;
	}
	
	void addTradeHandler(int villagerID, IVillageTradeHandler handler) {
		VillagerRegistry.instance().registerVillageTradeHandler(villagerID, handler);
	}

	//--------------- Method stubs ----------------------------------------------------------

	// Moved to BaseIntegration

//	void registerBlocks() {}
//	void registerItems() {}
//	void registerOres() {}
//	void registerRecipes() {}
//	void registerTileEntities() {}
//	void registerRandomItems() {}
//	void registerWorldGenerators() {}
//	void registerEntities() {}
//	void registerVillagers() {}
//	void registerOther() {}
	
	//--------------- Resources ----------------------------------------------------------
	
	public ResourceLocation resourceLocation(String path) {
		return new ResourceLocation(assetKey, path);
	}
	
	public String soundName(String name) {
		return assetKey + ":" + name;
	}
	
	public ResourceLocation textureLocation(String path) {
		return resourceLocation("textures/" + path);
	}

//	public ResourceLocation blockTextureLocation(String path) {
//		return resourceLocation("textures/blocks/" + path);
//	}
//
//	public ResourceLocation itemTextureLocation(String path) {
//		return resourceLocation("textures/items/" + path);
//	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IIconRegister reg, String name) {
		return reg.registerIcon(assetKey + ":" + name);
	}

	public Set<String> listResources(String subdir) {
		try {
			Set<String>result = new HashSet<String>();
			if (resourceURL != null) {
				String protocol = resourceURL.getProtocol();
				if (protocol.equals("jar")) {
					String resPath = resourceURL.getPath();
					int pling = resPath.lastIndexOf("!");
					URL jarURL = new URL(resPath.substring(0, pling));
					String resDirInJar = resPath.substring(pling + 2);
					String prefix = resDirInJar + subdir + "/";
					//System.out.printf("BaseMod.listResources: looking for names starting with %s\n", prefix);
					JarFile jar = new JarFile(new File(jarURL.toURI()));
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						String name = entries.nextElement().getName();
						if (name.startsWith(prefix) && !name.endsWith("/") && !name.contains("/.")) {
							//System.out.printf("BaseMod.listResources: name = %s\n", name);
							result.add(name.substring(prefix.length()));
						}
					}
				}
				else
					throw new RuntimeException("Resource URL protocol " + protocol + " not supported");
			}
			return result;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//--------------- GUIs - Registration ------------------------------------------------

	protected void registerContainers() {
	//  Make calls to addContainer() here.
	//
	//  Container classes registered using these methods must implement either:
	//
	//  (1) A static method create(EntityPlayer player, World world, int x, int y, int z [,int param])
	//  (2) A constructor MyContainer(EntityPlayer player, World world, int x, int y, int z [, int param])
	}
	
	public void addContainer(Enum id, Class<? extends Container> cls) {
		addContainer(id.ordinal(), cls);
	}

	public void addContainer(int id, Class<? extends Container> cls) {
		containerClasses.put(id, cls);
	}
	
	//--------------- GUIs  - Invoking -------------------------------------------------

	public void openGui(EntityPlayer player, Enum id, TileEntity te) {
		openGui(player, id, te, 0);
	}

	public void openGui(EntityPlayer player, Enum id, TileEntity te, int param) {
		openGui(player, id.ordinal(), te, param);
	}

	public void openGui(EntityPlayer player, int id, TileEntity te) {
		openGui(player, id, te, 0);
	}

	public void openGui(EntityPlayer player, int id, TileEntity te, int param) {
		openGui(player, id, te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, param);
	}

	public void openGui(EntityPlayer player, Enum id, World world, int x, int y, int z) {
		openGui(player, id, world, x, y, z, 0);
	}

	public void openGui(EntityPlayer player, Enum id, World world, int x, int y, int z, int param) {
		openGui(player, id.ordinal(), world, x, y, z, param);
	}

	public void openGui(EntityPlayer player, int id, World world, int x, int y, int z, int param) {
		openGui(player, id | (param << 16), world, x, y, z);
	}
	
	public void openGui(EntityPlayer player, int id, World world, int x, int y, int z) {
		if (debugGui)
			System.out.printf("BaseMod.openGui: for %s with id 0x%x in %s at (%s, %s, %s)\n",
				this, id, world, x, y, z);
		player.openGui(this, id, world, x, y, z);
	}
	
	//--------------- GUIs  - Internal -------------------------------------------------

	Map<Integer, Class<? extends Container>> containerClasses =
		new HashMap<Integer, Class<? extends Container>>();

	/**
	 * Returns a Container to be displayed to the user. 
	 * On the client side, this needs to return a instance of GuiScreen
	 * On the server side, this needs to return a instance of Container
	 *
	 * @param ID The Gui ID Number
	 * @param player The player viewing the Gui
	 * @param world The current world
	 * @param x X Position
	 * @param y Y Position
	 * @param z Z Position
	 * @return A GuiScreen/Container to be displayed to the user, null if none.
	 */

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (debugGui)
			System.out.printf("BaseMod.getServerGuiElement: for id 0x%x\n", id);
		int param = id >> 16;
		id = id & 0xffff;
		Class cls = containerClasses.get(id);
		Object result;
		if (cls != null)
			result = createGuiElement(cls, player, world, x, y, z, param);
		else
			result = getGuiContainer(id, player, world, x, y, z, param);
		if (debugGui)
			System.out.printf("BaseMod.getServerGuiElement: Returning %s\n", result);
		setModOf(result);
		return result;
	}
	
	Container getGuiContainer(int id, EntityPlayer player, World world, int x, int y, int z, int param) {
		//  Called when container id not found in registry
		if (debugGui)
			System.out.printf("%s: BaseMod.getGuiContainer: No Container class found for gui id %d\n", 
				this, id);
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
	
	Object createGuiElement(Class cls, EntityPlayer player, World world, int x, int y, int z, int param) {
		try {
			if (debugGui)
				System.out.printf("BaseMod.createGuiElement: Looking for create method on %s for %s in %s\n",
					cls, player, world);
			Method m = getMethod(cls, "create",
				EntityPlayer.class, World.class, int.class, int.class, int.class, int.class);
			if (m != null)
				return m.invoke(null, player, world, x, y, z, param);
			m = getMethod(cls, "create", EntityPlayer.class, World.class, int.class, int.class, int.class);
			if (m != null)
				return m.invoke(null, player, world, x, y, z);
			if (debugGui)
				System.out.printf("BaseMod.createGuiElement: Looking for constructor on %s\n", cls);
			Constructor c = getConstructor(cls,
				EntityPlayer.class, World.class, int.class, int.class, int.class, int.class);
			if (c != null)
				return c.newInstance(player, world, x, y, z, param);
			c = getConstructor(cls, EntityPlayer.class, World.class, int.class, int.class, int.class);
			if (c != null)
				return c.newInstance(player, world, x, y, z);
			throw new RuntimeException(String.format("%s: No suitable gui element constructor found for %s\n",
				modID, cls));
		}
		catch (Exception e) {
			reportExceptionCause(e);
			return null;
		}
	}
	
	Method getMethod(Class cls, String name, Class... argTypes) {
		try {
			return cls.getMethod(name, argTypes);
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	Constructor getConstructor(Class cls, Class... argTypes) {
		try {
			return cls.getConstructor(argTypes);
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	public static void reportExceptionCause(Exception e) {
		Throwable cause = e.getCause();
		System.out.printf("BaseMod.createGuiElement: %s: %s\n", e, cause);
		if (cause != null)
			cause.printStackTrace();
		else
			e.printStackTrace();
	}
}
