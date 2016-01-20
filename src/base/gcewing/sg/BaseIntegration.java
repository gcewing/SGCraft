//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Third Party Mod Integration
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.item.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.*;

public class BaseIntegration {

	public void preInit(FMLPreInitializationEvent e) {}
	public void init(FMLInitializationEvent e) {}
	public void postInit(FMLPostInitializationEvent e) {}
	
	public void configure(BaseConfiguration config) {}

	protected void registerBlocks() {}
	protected void registerItems() {}
	protected void registerOres() {}
	protected void registerRecipes() {}
	protected void registerTileEntities() {}
	protected void registerRandomItems() {}
	protected void registerWorldGenerators() {}
	protected void registerContainers() {}
	protected void registerEntities() {}
	protected void registerVillagers() {}
	protected void registerOther() {}
	
	protected void registerScreens() {}
	protected void registerRenderers() {}
	protected void registerOtherClient() {}
	
	public Item searchForItem(String... names) {
		Item result = null;
		for (String name : names) {
			result = findItem(name);
			if (result != null)
				return result;
		}
		System.out.printf("%s: Unable to find an item with any of the following names:",
			getClass().getName());
		for (String name : names)
			System.out.printf(" %s", name);
		System.out.printf("\n");
		return null;
	}
	
	public static Item findItem(String name) {
		String[] parts = BaseUtils.split(":", name);
		return GameRegistry.findItem(parts[0], parts[1]);
	}
	
}
