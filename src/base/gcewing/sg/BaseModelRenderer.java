//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - Render block using model + textures
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

// import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.*;

import gcewing.sg.BaseMod.*;
import gcewing.sg.BaseModClient.*;

public class BaseModelRenderer implements ICustomRenderer {

    public static boolean debugRenderModel = false;

    protected IModel model;
    protected ITexture[] textures;
    protected Vector3 origin;
    
    private static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 0);

    public BaseModelRenderer(IModel model, ITexture... textures) {
        this(model, Vector3.zero, textures);
    }

    public BaseModelRenderer(IModel model, Vector3 origin, ITexture... textures) {
        this.model = model;
        this.textures = textures;
        this.origin = origin;
    }

    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
        EnumWorldBlockLayer layer, Trans3 t)
    {
        IBlock block = (IBlock)state.getBlock();
        Trans3 t2 = t.t(block.localToGlobalTransformation(world, pos, state, Vector3.zero)).translate(origin);
        model.render(t2, target, textures);
    }
    
    public void renderItemStack(ItemStack stack, IRenderTarget target) {
        if (debugRenderModel) {
            System.out.printf("BaseModelRenderer.renderItemStack: %s\n", stack);
            System.out.printf("BaseModelRenderer.renderItemStack: model = %s\n", model);
            for (int i = 0; i < textures.length; i++)
                System.out.printf("BaseModelRenderer.renderItemStack: textures[%s] = %s\n", i, textures[i]);
        }
        model.render(itemTrans.translate(origin), target, textures);
    }

}

