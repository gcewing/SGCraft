// ------------------------------------------------------------------------------------------------
//
// Greg's Mod Base for 1.7 Version B - Extended ItemBlock
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseBlockUtils.getBlockStateFromMeta;
import static gcewing.sg.BaseBlockUtils.getMetaFromBlockState;
import static gcewing.sg.BaseUtils.facings;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import gcewing.sg.BaseMod.IItem;
import gcewing.sg.BaseMod.ModelSpec;

public class BaseItemBlock extends ItemBlock implements IItem {

    public BaseItemBlock(Block block) {
        super(block);
    }

    public String[] getTextureNames() {
        return null;
    }

    public ModelSpec getModelSpec(ItemStack stack) {
        return null;
    }

    public int getNumSubtypes() {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        Block block = world.getBlock(x, y, z);
        if (!block.isReplaceable(world, x, y, z)) {
            if (side == 0) --y;
            if (side == 1) ++y;
            if (side == 2) --z;
            if (side == 3) ++z;
            if (side == 4) --x;
            if (side == 5) ++x;
        }
        if (stack.stackSize == 0) return false;
        else if (!player.canPlayerEdit(x, y, z, side, stack)) return false;
        else if (y == 255 && this.field_150939_a.getMaterial().isSolid()) return false;
        else if (world.canPlaceEntityOnSide(this.field_150939_a, x, y, z, false, side, player, stack)) {
            int i1 = this.getMetadata(stack.getItemDamage());
            BaseBlock baseBlock = (BaseBlock) this.field_150939_a;
            IBlockState state = baseBlock
                    .onBlockPlaced(world, new BlockPos(x, y, z), facings[side], hitX, hitY, hitZ, i1, player);
            int j1 = baseBlock.getMetaFromState(state);
            if (placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, j1)) {
                world.playSoundEffect(
                        (double) ((float) x + 0.5F),
                        (double) ((float) y + 0.5F),
                        (double) ((float) z + 0.5F),
                        this.field_150939_a.stepSound.func_150496_b(),
                        (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F,
                        this.field_150939_a.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, int meta) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState newState = getBlockStateFromMeta(field_150939_a, meta);
        return placeBlockAt(stack, player, world, pos, facings[side], hitX, hitY, hitZ, newState);
    }

    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        int meta = getMetaFromBlockState(newState);
        return super.placeBlockAt(stack, player, world, pos.x, pos.y, pos.z, face.ordinal(), hitX, hitY, hitZ, meta);
    }

}
