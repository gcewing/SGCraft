//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

public class BaseBlock extends BaseContainerBlock<TileEntity> {

	public BaseBlock(Material material) {
		super(material, null);
	}
	
	/*
	 *   Test whether block is receiving a redstone signal from a source
	 *   other than itself. For blocks that can both send and receive in
	 *   any direction.
	 */
	public static boolean isGettingExternallyPowered(World world, int x, int y, int z) {
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (isPoweringSide(world, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side))
					return true;
		}
		return false;
	}
	
	static boolean isPoweringSide(World world, int x, int y, int z, ForgeDirection side) {
		Block block = world.getBlock(x, y, z);
		if (block.isProvidingWeakPower(world, x, y, z, side.ordinal()) > 0)
			return true;
		if (block.shouldCheckWeakPower(world, x, y, z, side.ordinal())) {
			for (ForgeDirection side2 : ForgeDirection.VALID_DIRECTIONS)
				if (side2 != side.getOpposite())
					if (world.isBlockProvidingPowerTo(
							x + side2.offsetX, y + side2.offsetY, z + side2.offsetZ, side2.ordinal()) > 0)
						return true;
		}
		return false;
	}
}
