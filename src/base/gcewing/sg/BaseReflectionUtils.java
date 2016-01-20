//------------------------------------------------------------------------------------------------
//
//	 Mod Base - Reflection utilities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BaseReflectionUtils {

	static Field modifiers;
	static {
		try {
			modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static Method getMethod(Class cls, String name, String srg_name, String signature, Class... paramTypes) {
		Method result = null;
		try {
			result = cls.getDeclaredMethod(name, paramTypes);
		}
		catch (NoSuchMethodException e1) {
			try {
				result = cls.getDeclaredMethod(srg_name, paramTypes);
			}
			catch (NoSuchMethodException e2) {
				throw new RuntimeException(String.format(
					"Can't find obfuscated method: %s.%s (%s %s)",
					cls.getName(), name, srg_name, signature));
			}
		}
		result.setAccessible(true);
		return result;
	}
	
	public static Object call(Object obj, Method method, Object... args) {
		try {
			return method.invoke(obj, args);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Field getField(Class cls, String name, String srg_name) {
		Field result = null;
		try {
			result = cls.getDeclaredField(name);
		}
		catch (NoSuchFieldException e1) {
			try {
				result = cls.getDeclaredField(srg_name);
			}
			catch (NoSuchFieldException e2) {
				throw new RuntimeException(String.format(
					"Can't find obfuscated field: %s.%s (%s)",
					cls.getName(), name, srg_name));
			}
		}
		result.setAccessible(true);
		return result;
	}
	
	public static Object getFieldOf(Object obj, String name, String srg_name) {
		Field field = getField(obj.getClass(), name, srg_name);
		try {
			return field.get(obj);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Field getFinalField(Class cls, String name, String srg_name) {
		Field result = getField(cls, name, srg_name);
		try {
			modifiers.setInt(result, result.getModifiers() & ~Modifier.FINAL);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public static Object get(Object obj, Field field) {
		try {
			return field.get(obj);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void set(Object obj, Field field, Object value) {
		try {
			field.set(obj, value);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
