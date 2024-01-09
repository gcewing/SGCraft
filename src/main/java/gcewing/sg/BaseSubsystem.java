// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 Version B - Mod Subsystem
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.item.Item;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class BaseSubsystem<MOD extends BaseMod, CLIENT extends BaseModClient> {

    public MOD mod;
    public CLIENT client;

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

    protected void registerBlockRenderers() {}

    protected void registerItemRenderers() {}

    protected void registerEntityRenderers() {}

    protected void registerTileEntityRenderers() {}

    protected void registerModelLocations() {}

    protected void registerOtherClient() {}

    public Item searchForItem(String... names) {
        Item result = null;
        for (String name : names) {
            result = findItem(name);
            if (result != null) return result;
        }
        SGCraft.log.debug(
                String.format("%s: Unable to find an item with any of the following names:", getClass().getName()));
        for (String name : names) SGCraft.log.debug(String.format(" %s", name));
        return null;
    }

    public static Item findItem(String name) {
        String[] parts = BaseStringUtils.split(":", name);
        return GameRegistry.findItem(parts[0], parts[1]);
    }

}
