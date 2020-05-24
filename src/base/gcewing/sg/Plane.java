package gcewing.sg;

import java.util.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import net.minecraft.util.EnumFacing;

public enum Plane implements Predicate<EnumFacing>, Iterable<EnumFacing> {
    HORIZONTAL,
    VERTICAL;

    /**
     * All EnumFacing values for this Plane
     */
    public EnumFacing[] facings()
    {
        switch (this)
        {
            case HORIZONTAL:
                return new EnumFacing[] {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
//                 return new EnumFacing[] {EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST};
            case VERTICAL:
                return new EnumFacing[] {EnumFacing.UP, EnumFacing.DOWN};
            default:
                throw new Error("Someone\'s been tampering with the universe!");
        }
    }
    
    private int[] axes = {1, 1, 0, 0, 0, 0};

    /**
     * Choose a random Facing from this Plane using the given Random
     */
    public EnumFacing random(Random rand)
    {
        EnumFacing[] aenumfacing = this.facings();
        return aenumfacing[rand.nextInt(aenumfacing.length)];
    }

    public boolean apply(EnumFacing dir)
    {
        return dir != null && axes[dir.ordinal()] == this.ordinal();
    }

    public Iterator<EnumFacing> iterator()
    {
        return Iterators.<EnumFacing>forArray(this.facings());
    }
}
