// ------------------------------------------------------------------------------------------------
//
// SG Craft - IC2 Integration Module
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import net.minecraft.item.ItemStack;

import gcewing.sg.BaseSubsystem;
import gcewing.sg.SGCraft;
import gcewing.sg.SGCraftClient;
import ic2.api.item.IC2Items;

public class IC2Integration extends BaseSubsystem<SGCraft, SGCraftClient> {

    public static ItemStack getIC2Item(String name) { // [IC2]
        return IC2Items.getItem(name);
    }

    @Override
    public void registerBlocks() {
        SGCraft.ic2PowerUnit = mod.newBlock("ic2PowerUnit", IC2PowerBlock.class, IC2PowerItem.class);
    }

    @Override
    public void registerRecipes() {
        ItemStack rubber = getIC2Item("rubber");
        ItemStack copperPlate = getIC2Item("platecopper");
        ItemStack machine = getIC2Item("machine");
        ItemStack wire = getIC2Item("copperCableItem");
        ItemStack circuit = getIC2Item("electronicCircuit");
        mod.newRecipe(SGCraft.ic2Capacitor, 1, "ppp", "rrr", "ppp", 'p', copperPlate, 'r', rubber);
        mod.newRecipe(
                SGCraft.ic2PowerUnit,
                1,
                "cwc",
                "wMw",
                "cec",
                'c',
                SGCraft.ic2Capacitor,
                'w',
                wire,
                'M',
                machine,
                'e',
                circuit);
    }

}
