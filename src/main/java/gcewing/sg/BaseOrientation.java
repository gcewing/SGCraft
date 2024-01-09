// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 Version B - Block orientation handlers
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseUtils.horizontalFacings;
import static gcewing.sg.BaseUtils.iround;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import gcewing.sg.BaseBlock.IOrientationHandler;

public class BaseOrientation {

    public static boolean debugPlacement = false;
    public static boolean debugOrientation = false;

    public static IOrientationHandler orient4WaysByState = new Orient4WaysByState();
    public static IOrientationHandler orient24WaysByTE = new Orient24WaysByTE();

    // ------------------------------------------------------------------------------------------------

    public static class Orient4WaysByState implements IOrientationHandler {

        // public static IProperty FACING = PropertyDirection.create("facing", Plane.HORIZONTAL);
        public static IProperty FACING = new PropertyTurn("facing");

        public void defineProperties(BaseBlock block) {
            block.addProperty(FACING);
        }

        public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, EnumFacing side, float hitX,
                float hitY, float hitZ, IBlockState baseState, EntityLivingBase placer) {
            EnumFacing dir = getHorizontalFacing(placer);
            if (debugPlacement) SGCraft.log
                    .debug(String.format("BaseOrientation.Orient4WaysByState: Placing block with FACING = %s", dir));
            return baseState.withProperty(FACING, dir);
        }

        protected EnumFacing getHorizontalFacing(Entity entity) {
            return horizontalFacings[iround(entity.rotationYaw / 90.0) & 3];
        }

        public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
            EnumFacing f = (EnumFacing) state.getValue(FACING);
            if (debugOrientation) SGCraft.log.debug(
                    String.format(
                            "BaseOrientation.Orient4WaysByState.localToGlobalTransformation: for %s: facing = %s",
                            state,
                            f));
            int i;
            switch (f) {
                case NORTH:
                    i = 0;
                    break;
                case WEST:
                    i = 1;
                    break;
                case SOUTH:
                    i = 2;
                    break;
                case EAST:
                    i = 3;
                    break;
                default:
                    i = 0;
            }
            return new Trans3(origin).turn(i);
        }

    }

    // ------------------------------------------------------------------------------------------------

    public static class Orient24WaysByTE extends BaseBlock.Orient1Way {

        public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
            TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
            if (te instanceof BaseTileEntity) {
                BaseTileEntity bte = (BaseTileEntity) te;
                return Trans3.sideTurn(origin, bte.side, bte.turn);
            } else return super.localToGlobalTransformation(world, pos, state, origin);
        }

    }

}
