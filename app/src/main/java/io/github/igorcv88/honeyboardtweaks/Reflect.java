package io.github.igorcv88.honeyboardtweaks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class Reflect {
    static Method method(Class<?> type, String name, Class<?>... params) throws ReflectiveOperationException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(name, params); m.setAccessible(true); return m;
            } catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(type.getName() + "#" + name);
    }
    static Object fieldOfType(Object owner, Class<?> type) throws ReflectiveOperationException {
        for (Field f : owner.getClass().getDeclaredFields()) {
            if (f.getType() == type) { f.setAccessible(true); return f.get(owner); }
        }
        throw new NoSuchFieldException(type.getName());
    }
    static void field(Object owner, String name, Object value) throws ReflectiveOperationException {
        Field f = owner.getClass().getDeclaredField(name); f.setAccessible(true); f.set(owner, value);
    }
    static Object call(Object owner, String name) throws ReflectiveOperationException {
        return method(owner.getClass(), name).invoke(owner);
    }
}
