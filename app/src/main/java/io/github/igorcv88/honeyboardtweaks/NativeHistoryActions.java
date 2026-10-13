package io.github.igorcv88.honeyboardtweaks;

import java.lang.reflect.Method;

/** DZIF's own Keys Cafe gesture actions, including engine clear and native eligibility checks. */
final class NativeHistoryActions implements HistoryDispatcher.NativeRoute {
    private final ClassLoader loader;
    NativeHistoryActions(ClassLoader loader) { this.loader = loader; }
    @Override public HistoryDispatcher.Prepared prepare(EditAction action) throws Throwable {
        if (action != EditAction.UNDO && action != EditAction.REDO)
            throw new IllegalArgumentException("Not a history action");
        // Resolve/construct only when clicked, after Honeyboard's dependency container is ready.
        Class<?> type = Class.forName(action == EditAction.UNDO ? "fa0.l" : "fa0.j", false, loader);
        Object gesture = type.getDeclaredConstructor().newInstance();
        boolean blocked = (boolean) Reflect.method(type, "i").invoke(gesture);
        Method execute = Reflect.method(type, "a", Object.class);
        return new HistoryDispatcher.Prepared(blocked, () -> execute.invoke(gesture, new Object[]{null}));
    }
}
