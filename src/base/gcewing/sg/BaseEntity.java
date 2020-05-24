//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.7 Version B - 1.8 Entity compatibility
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public abstract class BaseEntity extends Entity {

    public BaseEntity(World world) {
        super(world);
    }

    public void setBoundingBox(AxisAlignedBB box) {
        boundingBox.setBounds(box.minX, box.minY, box.minZ,
            box.maxX, box.maxY, box.maxZ);
    }
    
    public AxisAlignedBB getEntityBoundingBox() {
        return boundingBox;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return getCollisionBoundingBox();
    }

    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

}
