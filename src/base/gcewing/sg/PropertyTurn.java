package gcewing.sg;

import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.Collection;

public class PropertyTurn extends PropertyEnum<EnumFacing> {

    protected static final EnumFacing[] values = {
            EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.EAST
    };
    protected static final Collection valueList = Arrays.asList(values);

    public PropertyTurn(String name) {
        super(name, EnumFacing.class, valueList);
    }

}
