//------------------------------------------------------------------------------------------------
//
//   SG Craft - Dimension map
//
//------------------------------------------------------------------------------------------------
package gcewing.sg;

import com.google.common.primitives.Ints;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return BaseUtils.getWorldData(world, SGDimensionMap.class, "sgcraft:dimension_map");
    }
    
    public static Integer dimensionForIndex(int index) {
        return get().getDimensionForIndex(index);
    }

    protected Integer getDimensionForIndex(int index) {
        Integer dimension = null;
        if (index >= 0 && index < indexToDimension.size())
            dimension = indexToDimension.get(index);
        if (debugDimensionMap) SGCraft.log.debug(String.format("SGDimensionMap: Found index %s -> dimension %s", index, dimension));
        return dimension;
    }
    
    public static Integer indexForDimension(int dimension) {
        return get().getIndexForDimension(dimension);
    }

    protected Integer getIndexForDimension(int dimension) {
        int index;
        if (!dimensionToIndex.containsKey(dimension)) {
            index = indexToDimension.size();
            if (debugDimensionMap) SGCraft.log.debug(String.format("SGDimensionMap: Adding dimension %s -> index %s", dimension, index));
            indexToDimension.add(dimension);
            dimensionToIndex.put(dimension, index);
            markDirty();
        }
        else {
            index = dimensionToIndex.get(dimension);
            if (debugDimensionMap) SGCraft.log.debug(String.format("SGDimensionMap: Found dimension %s -> index %s", dimension, index));
        }
        return index;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (debugDimensionMap) SGCraft.log.debug("SGDimensionMap: Reading from nbt");
        int[] a = nbt.getIntArray("dimensions");
        for (int i = 0; i < a.length; i++) {
            indexToDimension.add(a[i]);
            dimensionToIndex.put(a[i], i);
        }
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (debugDimensionMap) SGCraft.log.debug("SGDimensionMap: Writing to nbt");
        int[] a = Ints.toArray(indexToDimension);
        nbt.setIntArray("dimensions", a);
    }

}
