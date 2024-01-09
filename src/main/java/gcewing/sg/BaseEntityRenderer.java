// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 Version B - Entity Renderer
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public abstract class BaseEntityRenderer<T extends Entity> extends Render {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float dt) {
        renderEntity((T) entity, x, y, z, yaw, dt);
    }

    public abstract void renderEntity(T entity, double x, double y, double z, float yaw, float dt);

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return getTexture((T) entity);
    }

    protected ResourceLocation getTexture(T entity) {
        return null;
    }

}
