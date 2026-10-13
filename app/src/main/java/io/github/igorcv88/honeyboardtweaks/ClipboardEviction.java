package io.github.igorcv88.honeyboardtweaks;

import java.util.List;

/** Native deletion removes files as well as rows, so failure cannot be rolled back with SQL alone. */
final class ClipboardEviction {
    interface Remove { void remove(long id) throws Throwable; }
    record Report(int planned, int completed, String failureType) {}
    static Report run(List<Long> victims, Remove operation) {
        int completed = 0;
        for (long id : victims) {
            try { operation.remove(id); completed++; }
            catch (Throwable error) {
                if (error instanceof java.lang.reflect.InvocationTargetException && error.getCause() != null)
                    error = error.getCause();
                return new Report(victims.size(), completed, error.getClass().getSimpleName());
            }
        }
        return new Report(victims.size(), completed, null);
    }
}
