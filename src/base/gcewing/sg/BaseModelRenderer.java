//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Render block using model + textures
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

import gcewing.sg.BaseMod.*;
import gcewing.sg.BaseModClient.*;

public class BaseModelRenderer implements ICustomRenderer {

    protected IModel model;
    protected ITexture[] textures;
    protected Vector3 origin;
    
//     private static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);

    public BaseModelRenderer(IModel model, ITexture... textures) {
        this(model, Vector3.zero, textures);
    }

    public BaseModelRenderer(IModel model, Vector3 origin, ITexture... textures) {
        this.model = model;
        this.textures = textures;
        this.origin = origin;
    }

    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
        BlockRenderLayer layer, Trans3 t)
    {
        IBlock block = (IBlock)state.getBlock();
        Trans3 t2 = t.t(block.localToGlobalTransformation(world, pos, state, Vector3.zero)).translate(origin);
        model.render(t2, target, textures);
    }
    
    public void renderItemStack(ItemStack stack, IRenderTarget target, Trans3 t) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            if (block instanceof IBlock)
                t = t.t(((IBlock)block).itemTransformation());
        }
        model.render(t.translate(origin), target, textures);
    }

}

