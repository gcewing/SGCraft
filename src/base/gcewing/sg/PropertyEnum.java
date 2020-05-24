package gcewing.sg;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
// import net.minecraft.util.IStringSerializable;

public class PropertyEnum<T extends Enum> extends PropertyHelper<T>
{
    private final ImmutableSet<T> allowedValues;
    private final Map<String, T> nameToValue = Maps.<String, T>newHashMap();

    protected PropertyEnum(String name, Class<T> valueClass, Collection<T> allowedValues)
    {
        super(name, valueClass);
        this.allowedValues = ImmutableSet.copyOf(allowedValues);

        for (T t : allowedValues)
        {
            //String s = ((IStringSerializable)t).getName();
            String s = getName(t);

            if (this.nameToValue.containsKey(s))
            {
                throw new IllegalArgumentException("Multiple values have the same name \'" + s + "\'");
            }

            this.nameToValue.put(s, t);
        }
    }

    public Collection<T> getAllowedValues()
    {
        return this.allowedValues;
    }

    /**
     * Get the name for the given value.
     */
    public String getName(T value)
    {
        //return ((IStringSerializable)value).getName();
        return value.toString().toLowerCase();
    }

    public static <T extends Enum<T>> PropertyEnum<T> create(String name, Class<T> clazz)
    {
        /**
         * Create a new PropertyEnum with all Enum constants of the given class that match the given Predicate.
         */
        return create(name, clazz, Predicates.<T>alwaysTrue());
    }

    public static <T extends Enum<T>> PropertyEnum<T> create(String name, Class<T> clazz, Predicate<T> filter)
    {
        /**
         * Create a new PropertyEnum with the specified values
         */
        return create(name, clazz, Collections2.<T>filter(Lists.newArrayList(clazz.getEnumConstants()), filter));
    }

    public static <T extends Enum<T>> PropertyEnum<T> create(String name, Class<T> clazz, T... values)
    {
        /**
         * Create a new PropertyEnum with the specified values
         */
        return create(name, clazz, Lists.newArrayList(values));
    }

    public static <T extends Enum<T>> PropertyEnum<T> create(String name, Class<T> clazz, Collection<T> values)
    {
        return new PropertyEnum(name, clazz, values);
    }
}