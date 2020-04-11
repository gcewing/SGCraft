package gcewing.sg;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import net.minecraft.util.EnumFacing;

import java.util.Iterator;
import java.util.Random;

public enum Plane implements Predicate<EnumFacing>, Iterable<EnumFacing> {
    HORIZONTAL,
    VERTICAL;

    private final int[] axes = {1, 1, 0, 0, 0, 0};

    /**
     * All EnumFacing values for this Plane
     */
    public EnumFacing[] facings() {
        switch (this) {
            case HORIZONTAL:
                return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
//                 return new EnumFacing[] {EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST};
            case VERTICAL:
                return new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
            default:
                throw new Error("Someone's been tampering with the universe!");
        }
    }

    /**
     * Choose a random Facing from this Plane using the given Random
     */
    public EnumFacing random(Random rand) {
        EnumFacing[] aenumfacing = this.facings();
        return aenumfacing[rand.nextInt(aenumfacing.length)];
    }

    public boolean apply(EnumFacing dir) {
        return dir != null && axes[dir.ordinal()] == this.ordinal();
    }

    public Iterator<EnumFacing> iterator() {
        return Iterators.forArray(this.facings());
    }
}
