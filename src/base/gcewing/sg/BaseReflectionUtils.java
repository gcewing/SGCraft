//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Reflection Utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.lang.reflect.*;

public class BaseReflectionUtils {

    public static Field getFieldDef(Class cls, String unobfName, String obfName) {
        try {
            Field field;
            try {
                field = cls.getDeclaredField(unobfName);
            }
            catch (NoSuchFieldException e) {
                field = cls.getDeclaredField(obfName);
            }
            field.setAccessible(true);
            return field;
        }
        catch (Exception e) {
            throw new RuntimeException(
                String.format("Cannot find field %s or %s of %s", unobfName, obfName, cls.getName()),
                e);
        }
    }
    
    public static Object getField(Object obj, String unobfName, String obfName) {
        Field field = getFieldDef(obj.getClass(), unobfName, obfName);
        return getField(obj, field);
    }
        
    public static Object getField(Object obj, Field field) {
        try {
            return field.get(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setField(Object obj, String unobfName, String obfName, Object value) {
        Field field = getFieldDef(obj.getClass(), unobfName, obfName);
        setField(obj, field, value);
    }
    
    public static void setField(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
