//------------------------------------------------------------------------------------------------
//
//   SG Craft - Iris Renderer
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraft.util.*;

public class IrisRenderer extends Render<IrisEntity> {

    public IrisRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(IrisEntity entity, double d0, double d1, double d2, float f, float f1) {
    }
    
    @Override
    protected ResourceLocation getEntityTexture(IrisEntity e) {
        return null;
    }
    
}
