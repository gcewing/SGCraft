package gcewing.sg;

import java.util.Arrays;
import java.util.Collection;

import net.minecraft.util.EnumFacing;

public class PropertyTurn extends PropertyEnum<EnumFacing> {

    protected static EnumFacing[] values = { EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST };
    protected static Collection valueList = Arrays.asList(values);

    public PropertyTurn(String name) {
        super(name, EnumFacing.class, valueList);
    }

}
