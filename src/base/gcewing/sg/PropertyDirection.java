package gcewing.sg;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;

import java.util.Collection;
import java.util.function.Predicate;

public class PropertyDirection extends PropertyEnum<EnumFacing> {
    protected PropertyDirection(String name, Collection<EnumFacing> values) {
        super(name, EnumFacing.class, values);
    }

    /**
     * Create a new PropertyDirection with the given name
     */
    public static PropertyDirection create(String name) {
        /**
         * Create a new PropertyDirection with all directions that match the given Predicate
         */
        return create(name, t -> true);
    }

    /**
     * Create a new PropertyDirection with all directions that match the given Predicate
     */
    public static PropertyDirection create(String name, Predicate<EnumFacing> filter) {
        /**
         * Create a new PropertyDirection for the given direction values
         */
        return create(name, Collections2.filter(Lists.newArrayList(EnumFacing.values()), filter::test));
    }

    /**
     * Create a new PropertyDirection for the given direction values
     */
    public static PropertyDirection create(String name, Collection<EnumFacing> values) {
        return new PropertyDirection(name, values);
    }
}