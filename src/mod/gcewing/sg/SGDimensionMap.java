//------------------------------------------------------------------------------------------------
//
//   SG Craft - Dimension map
//
//------------------------------------------------------------------------------------------------
package gcewing.sg;

import java.util.*;
import com.google.common.primitives.Ints;

import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;

//import net.minecraftforge.common.*;

public class SGDimensionMap extends WorldSavedData {

    public static boolean debugDimensionMap = false;

    protected List<Integer> indexToDimension = new ArrayList<>();
    protected Map<Integer, Integer> dimensionToIndex = new HashMap<>();
    
    public SGDimensionMap(String name) {
        super(name);
    }
    
    public static SGDimensionMap get() {
        World world = BaseUtils.getWorldForDimension(0);
        return BaseUtils.getWorldData(world, SGDimensionMap.class, "sgcraft-dimension_map");
    }
    
    public static Integer dimensionForIndex(int index) {
        return get().getDimensionForIndex(index);
    }

    protected Integer getDimensionForIndex(int index) {
        Integer dimension = null;
        if (index >= 0 && index < indexToDimension.size())
            dimension = indexToDimension.get(index);
        if (debugDimensionMap)
            System.out.printf("SGDimensionMap: Found index %s -> dimension %s\n", index, dimension);
        return dimension;
    }
    
    public static Integer indexForDimension(int dimension) {
        return get().getIndexForDimension(dimension);
    }

    protected Integer getIndexForDimension(int dimension) {
        if (!dimensionToIndex.containsKey(dimension)) {
            int index = indexToDimension.size();
            if (debugDimensionMap)
                System.out.printf("SGDimensionMap: Adding dimension %s -> index %s\n", dimension, index);
            indexToDimension.add(dimension);
            dimensionToIndex.put(dimension, index);
            markDirty();
            return index;
        }
        else {
            int index = dimensionToIndex.get(dimension);
            if (debugDimensionMap)
                System.out.printf("SGDimensionMap: Found dimension %s -> index %s\n", dimension, index);
            return index;
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (debugDimensionMap)
            System.out.printf("SGDimensionMap: Reading from nbt\n");
        int[] a = nbt.getIntArray("dimensions");
        for (int i = 0; i < a.length; i++) {
            indexToDimension.add(a[i]);
            dimensionToIndex.put(a[i], i);
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        if (debugDimensionMap)
            System.out.printf("SGDimensionMap: Writing to nbt\n");
        int[] a = Ints.toArray(indexToDimension);
        nbt.setIntArray("dimensions", a);
        return nbt;
    }

}
