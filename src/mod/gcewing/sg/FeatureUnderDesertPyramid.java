//------------------------------------------------------------------------------------------------
//
//   SG Craft - Generate stargate under desert pyramid
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class FeatureUnderDesertPyramid extends StructureComponent {

    StructureComponent base;

    @Override
    protected void readStructureFromNBT(NBTTagCompound compound, TemplateManager templateManager) {}

    @Override
    protected void writeStructureToNBT(NBTTagCompound compound) {}

    public FeatureUnderDesertPyramid() {
        //System.out.printf("SGCraft: FeatureUnderDesertPyramid instantiated with no arguments\n");
    }
    
    public FeatureUnderDesertPyramid(StructureComponent base) {
        super(0);
        if (FeatureGeneration.debugStructures)
            System.out.println("SGCraft: Instantiating FeatureUnderDesertPyramid");
        this.base = base;
        StructureBoundingBox baseBox = base.getBoundingBox();
        BlockPos boxCenter = new BlockPos(baseBox.minX + (baseBox.maxX - baseBox.minX + 1) / 2, baseBox.minY + (baseBox.maxY - baseBox.minY + 1) / 2, baseBox.minZ + (baseBox.maxZ - baseBox.minZ + 1) / 2);
        int cx = boxCenter.getX();
        int cz = boxCenter.getZ();
        int bottom = baseBox.minY - 7;
        boundingBox = new StructureBoundingBox(cx - 5, bottom, cz - 5, cx + 5, bottom + 7, cz + 8);
        setCoordBaseMode(EnumFacing.SOUTH);
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox clip) {
        return rand.nextInt(100) >= FeatureGeneration.structureAugmentationChance || addAugmentationParts(world, rand, clip);
    }
    
    protected boolean addAugmentationParts(World world, Random rand, StructureBoundingBox clip) {
        if (FeatureGeneration.debugStructures)
            System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts in %s clipped to %s\n", getBoundingBox(), clip);
        if (base == null) {
            System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts: no base\n");
            return false;
        }
        StructureBoundingBox box = getBoundingBox();
        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState sandstone = Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH);
        IBlockState orange = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
        IBlockState stairs = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
        IBlockState ladder = Blocks.LADDER.getDefaultState();
        IBlockState dhd = SGCraft.sgControllerBlock.getDefaultState().withProperty(BaseOrientation.Orient4WaysByState.FACING, EnumFacing.NORTH);
        IBlockState sgBase = SGCraft.sgBaseBlock.getDefaultState().withProperty(BaseOrientation.Orient4WaysByState.FACING, EnumFacing.NORTH);
        IBlockState[] sgRings = new IBlockState[2];
        sgRings[0] = SGCraft.sgRingBlock.getDefaultState();
        sgRings[1] = sgRings[0].withProperty(SGRingBlock.VARIANT, 1);
        //System.out.printf("SGCraft: FeatureUnderDesertPyramid.addComponentParts: " +
        //  "Filling (%d,%d,%d)-(%d,%d,%d)\n", box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        // Main chamber
        fillWithBlocks(world, clip, 0, 0, 0, 10, 7, 10, sandstone, air, false);
        // Stairwell
        fillWithBlocks(world, clip, 4, 0, 11, 13, 7, 13, sandstone, air, false);
        // Stairwell entrance
        fillWithAir(world, clip, 12, 7, 12, 12, 9, 12);
        // Stairwell exit
        fillWithAir(world, clip, 5, 1, 10, 5, 2, 11);
        // Stairs
        setBlockState(world, sandstone, 12, 4, 12, clip);
        for (int i = 0; i < 4; i++)
            setBlockState(world, stairs, 8+i, 1+i, 12, clip);
        for (int i = 0; i < 3; i++)
            setBlockState(world, ladder, 12, 5+i, 12, clip);
        // Wall decorations
        fillWithBlocks(world, clip, 0, 3, 0, 10, 3, 10, orange, air, true);
        fillWithBlocks(world, clip, 3, 4, 10, 7, 4, 10, orange, air, true);
        // Floor decorations
        fillWithBlocks(world, clip, 3, 0, 4, 3, 0, 6, orange, air, true);
        fillWithBlocks(world, clip, 7, 0, 4, 7, 0, 6, orange, air, true);
        fillWithBlocks(world, clip, 4, 0, 3, 6, 0, 3, orange, air, true);
        fillWithBlocks(world, clip, 4, 0, 7, 6, 0, 7, orange, air, true);
        setBlockState(world, orange, 5, 0, 5, clip);
        // Door frame
        fillWithBlocks(world, clip, 4, 1, 10, 6, 3, 10, sandstone, air, true);
        // Stargate
        for (int i = -2; i <= 2; i++)
            for (int j = 0; j <= 4; j++) {
                IBlockState id;
//                 int data;
                if (i == 0 && j == 0) {
                    id = sgBase;
//                     data = sgBaseNorth;
                }
                else if (i == -2 || i == 2 || j == 0 || j == 4) {
                    id = sgRings[(i + j + 1) & 1];
//                     data = (i + j + 1) & 1;
                }
                else {
                    id = air;
//                     data = 0;
                }
                setBlockState(world, id, 5+i, 1+j, 2, clip);
            }
        int baseX = box.minX + 5, baseY = box.minY + 1, baseZ = box.minZ + 2;
        SGBaseTE te = (SGBaseTE)world.getTileEntity(new BlockPos(baseX, baseY, baseZ));
        if (te != null)
            te.hasChevronUpgrade = true;
//      else
//          System.out.printf("FeatureUnderDesertPyramid.addComponentParts: No tile entity at (%d,%d,%d)\n",
//              baseX, baseY, baseZ);
        // Controller
        setBlockState(world, dhd, 5, 1, 7, clip);
        return true;
    }

//     @Override
//     protected void setBlockState(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn) {
//         System.out.printf("SGCraft: FeatureUnderDesertPyramid.setBlockState: %s at (%s, %s, %s)\n",
//             blockstateIn, this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
//         super.setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn);
//     }

}
