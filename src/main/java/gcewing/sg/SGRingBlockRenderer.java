//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block renderer
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.BaseModClient.ICustomRenderer;
import gcewing.sg.BaseModClient.IRenderTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import static gcewing.sg.BaseBlockUtils.blockCanRenderInLayer;

public class SGRingBlockRenderer implements ICustomRenderer {

    public SGRingBlockRenderer() {
    }

    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
        EnumWorldBlockLayer layer, Trans3 t)
    {
        ISGBlock ringBlock = (ISGBlock)state.getBlock();
        if (target.isRenderingBreakEffects() || (layer == EnumWorldBlockLayer.SOLID && !ringBlock.isMerged(world, pos)))
            SGCraft.mod.client.renderBlockUsingModelSpec(world, pos, state, target, layer, t);
        else {
            SGBaseTE te = ringBlock.getBaseTE(world, pos);
            if (te != null) {
                ItemStack stack = te.getCamouflageStack(pos);
                if (stack != null) {
                    Item item = stack.getItem();
                    if (item instanceof ItemBlock) {
                        IBlockState camoState = BaseBlockUtils.getBlockStateFromItemStack(stack);
                        if (blockCanRenderInLayer(camoState.getBlock(), layer)) {
                            BaseRenderingUtils.renderAlternateBlock(SGCraft.mod, world, pos, camoState, target);
                        }
                    }
                }
            }
        }
    }
    
    public void renderItemStack(ItemStack stack, IRenderTarget target, Trans3 t) {
        if (BaseModClient.debugRenderItem) SGCraft.log.debug(String.format("SGRingBlockRenderer.renderItemStack: %s", stack));
        SGCraft.mod.client.renderItemStackUsingModelSpec(stack, target, t);
    }

}
