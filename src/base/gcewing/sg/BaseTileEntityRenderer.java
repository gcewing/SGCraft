//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Tile Entity Renderer
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.tileentity.*;

import gcewing.sg. BaseModClient.IRenderTarget;

public abstract class BaseTileEntityRenderer extends TileEntitySpecialRenderer {

    protected static BaseGLRenderTarget target = new BaseGLRenderTarget();

    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        BaseTileEntity bte = (BaseTileEntity)te;
        Trans3 t = bte.localToGlobalTransformation(Vector3.blockCenter(x, y, z));
        target.start(true);
        render(bte, partialTicks, destroyStage, t, target);
        target.finish();
    }
    
    public void render(BaseTileEntity te, float dt, int destroyStage,
        Trans3 t, IRenderTarget target) {}

}
