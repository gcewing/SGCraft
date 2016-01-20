//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Client Proxy
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.net.*;
import java.util.*;
import java.lang.reflect.*;

import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.audio.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraftforge.client.event.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.src.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;

import net.minecraftforge.common.*;
import net.minecraftforge.client.*;
import cpw.mods.fml.client.*;
import cpw.mods.fml.client.registry.*;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.registry.*;

import gcewing.sg.BaseMod.IBlock;
import gcewing.sg.BaseMod.VSBinding;

public class BaseModClient<MOD extends BaseMod<? extends BaseModClient>> implements IGuiHandler {

	static class IDBinding<T> {
		public int id;
		public T object;
	}
	
	static class BRBinding extends IDBinding<ISimpleBlockRenderingHandler> {}
	
	static Map<String, BRBinding>
		blockRenderers = new HashMap<String, BRBinding>();

	MOD base;
	boolean debugSound = false;

	Map<Integer, Class<? extends GuiScreen>> screenClasses =
		new HashMap<Integer, Class<? extends GuiScreen>>();

	public BaseModClient(MOD mod) {
		base = mod;
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}
	
	public void preInit(FMLPreInitializationEvent e) {
	}
	
	public void init(FMLInitializationEvent e) {
	}
	
	public void postInit(FMLPostInitializationEvent e) {
		registerScreens();
		registerRenderers();
		registerOther();
		for (BaseIntegration i : base.integrations) {
			i.registerScreens();
			i.registerRenderers();
			//i.registerSounds();
			i.registerOtherClient();
		}
		registerImplicitBlockRenderers();
		registerSavedVillagerSkins();
	}
	
	void registerImplicitBlockRenderers() {
		for (IBlock block : base.registeredBlocks) {
			String name = block.getQualifiedRendererClassName();
			if (name != null) {
				BRBinding b = getBlockRendererForName(name);
				if (b != null) {
					//System.out.printf("BaseModClient: Binding renderer id %s to %s\n", b.id, block);
					block.setRenderType(b.id);
				}
			}
		}
	}
	
	void registerSavedVillagerSkins() {
		VillagerRegistry reg = VillagerRegistry.instance();
		for (VSBinding b : base.registeredVillagers)
			reg.registerVillagerSkin(b.id, b.object);
	}
	
	BRBinding getBlockRendererForName(String name) {
		//System.out.printf("BaseModClient: Getting block renderer class %s\n", name);
		BRBinding b = blockRenderers.get(name);
		if (b == null) {
			//System.out.printf("BaseModClient: Loading block renderer class %s\n", name);
			Class cls = null;
			ISimpleBlockRenderingHandler h;
			int i = name.lastIndexOf(".");
			String initPkgName = name.substring(0, i);
			String pkgName = initPkgName;
			String clsName = name.substring(i + 1);
			while (cls == null) {
				try {
					cls = Class.forName(pkgName + "." + clsName);
				}
				catch (ClassNotFoundException e) {
					//throw new RuntimeException(String.format("Block renderer class %s not found", name));
					i = pkgName.lastIndexOf(".");
					if (i < 0)
						throw new RuntimeException(String.format(
							"Block renderer class %s not found in %s or containing packages", clsName, initPkgName));
					pkgName = pkgName.substring(0, i);
				}
			}
			try {
				h = (ISimpleBlockRenderingHandler)cls.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			b = new BRBinding();
			b.id = RenderingRegistry.getNextAvailableRenderId();
			b.object = h;
			RenderingRegistry.registerBlockHandler(b.id, h);
			blockRenderers.put(name, b);
		}
		return b;
	}
	
//	boolean classIsPresent(String name) {
//		try {
//			Class.forName(name);
//			return true;
//		}
//		catch (ClassNotFoundException e) {
//			return false;
//		}
//	}
	
	String qualifyName(String name) {
		return getClass().getPackage().getName() + "." + name;
	}
	
	void registerOther() {}
	
	//-------------- Screen registration --------------------------------------------------------
	
	void registerScreens() {
		//
		//  Make calls to addScreen() here.
		//
		//  Screen classes registered using these methods must implement one of:
		//
		//  (1) A static method create(EntityPlayer, World, int x, int y, int z)
		//  (2) A constructor MyScreen(EntityPlayer, World, int x, int y, int z)
		//  (3) A constructor MyScreen(MyContainer) where MyContainer is the
		//      corresponding registered container class
		//
		//System.out.printf("%s: BaseModClient.registerScreens\n", this);
	}
	
	public void addScreen(Enum id, Class<? extends GuiScreen> cls) {
		addScreen(id.ordinal(), cls);
	}

	public void addScreen(int id, Class<? extends GuiScreen> cls) {
		screenClasses.put(id, cls);
	}
	
	//-------------- Renderer registration --------------------------------------------------------
	
	void registerRenderers() {
		// Make calls to addBlockRenderer(), addItemRenderer() and addTileEntityRenderer() here
	}

	void addBlockRenderer(IBlock block, ISimpleBlockRenderingHandler renderer) {
		addBlockRenderer(renderer, block);
	}
	
	void addBlockRenderer(ISimpleBlockRenderingHandler renderer, IBlock... blocks) {
		int renderID = RenderingRegistry.getNextAvailableRenderId();
		for (IBlock block : blocks) {
			System.out.printf("BaseModClient: Registering %s with id %s for %s\n", renderer, renderID, block);
			block.setRenderType(renderID);
			RenderingRegistry.registerBlockHandler(renderID, renderer);
		}
	}
	
	void addItemRenderer(Item item, IItemRenderer renderer) {
		MinecraftForgeClient.registerItemRenderer(item, renderer);
	}
	
	void addItemRenderer(Block block, IItemRenderer renderer) {
		MinecraftForgeClient.registerItemRenderer(block.getItemDropped(0, null, 0), renderer);
	}
	
	void addTileEntityRenderer(Class <? extends TileEntity> teClass, TileEntitySpecialRenderer renderer) {
		ClientRegistry.bindTileEntitySpecialRenderer(teClass, renderer);
	}
	
	void addEntityRenderer(Class<? extends Entity> entityClass, Render renderer) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, renderer);
	}
	
	//-------------- Client-side guis ------------------------------------------------
	
	public static void openClientGui(GuiScreen gui) {
		FMLClientHandler.instance().getClient().displayGuiScreen(gui);
	}
	
	//-------------- Rendering --------------------------------------------------------
	
	public ResourceLocation textureLocation(String path) {
		return base.resourceLocation("textures/" + path);
	}
	
	public void bindTexture(String path) {
		bindTexture(textureLocation(path));
	}
	
	public static void bindTexture(ResourceLocation rsrc) {
		TextureManager tm = Minecraft.getMinecraft().getTextureManager();
		tm.bindTexture(rsrc);
	}
	
	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre e) {
		int type = e.map.getTextureType();
		System.out.printf("BaseModClient.onTextureStitchPre: for texture type %s\n", type);
		switch (type) {
			case 0:
				registerBlockIcons(e.map);
				break;
			case 1:
				registerItemIcons(e.map);
				break;
		}
	}
	
	void registerBlockIcons(IIconRegister reg) {
	}
	
	void registerItemIcons(IIconRegister reg) {
	}
	
	//-------------- Internal --------------------------------------------------------
	
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
		return base.getServerGuiElement(id, player, world, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		int param = id >> 16;
		id = id & 0xffff;
		Object result = null;
		if (base.debugGui)
			System.out.printf("BaseModClient.getClientGuiElement: for id %s\n", id);
		Class scrnCls = screenClasses.get(id);
		if (scrnCls != null) {
			if (base.debugGui)
				System.out.printf("BaseModClient.getClientGuiElement: Instantiating %s\n", scrnCls);
			// If there is a container class registered for this gui and the screen class has
			// a constructor taking it, instantiate the screen automatically.
			Class contCls = base.containerClasses.get(id);
			if (contCls != null) {
				try {
					if (base.debugGui)
						System.out.printf("BaseModClient.getClientGuiElement: Looking for constructor taking %s\n", contCls);
					Constructor ctor = scrnCls.getConstructor(contCls);
					if (base.debugGui)
						System.out.printf("BaseModClient.getClientGuiElement: Instantiating container\n");
					Object cont = base.createGuiElement(contCls, player, world, x, y, z, param);
					if (cont != null) {
						if (base.debugGui)
							System.out.printf("BaseModClient.getClientGuiElement: Instantiating screen with container\n");
						try {
							result = ctor.newInstance(cont);
						}
						catch (Exception e) {
							//throw new RuntimeException(e);
							base.reportExceptionCause(e);
							return null;
						}
					}
				}
				catch (NoSuchMethodException e) {
				}
			}
			// Otherwise, contruct screen from player, world, x, y, z.
			if (result == null)
				result = base.createGuiElement(scrnCls, player, world, x, y, z, param);
		}
		else {
			result = getGuiScreen(id, player, world, x, y, z, param);
		}
		base.setModOf(result);
		if (base.debugGui)
			System.out.printf("BaseModClient.getClientGuiElement: returning %s\n", result);
		return result;
	}
	
	GuiScreen getGuiScreen(int id, EntityPlayer player, World world, int x, int y, int z, int param) {
		//  Called when screen id not found in registry
		System.out.printf("%s: BaseModClient.getGuiScreen: No GuiScreen class found for gui id %d\n", 
			this, id);
		return null;
	}

}
