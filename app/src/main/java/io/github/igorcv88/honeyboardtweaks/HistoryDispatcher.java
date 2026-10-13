package io.github.igorcv88.honeyboardtweaks;

/** A native dispatch may partially execute: never replay it through another route. */
final class HistoryDispatcher {
    interface Operation { void run() throws Throwable; }
    record Prepared(boolean blocked, Operation operation) {}
    interface NativeRoute { Prepared prepare(EditAction action) throws Throwable; }
    interface ShortcutRoute { boolean send(EditAction action) throws Throwable; }
    enum Outcome { NATIVE_SUBMITTED, NATIVE_BLOCKED, SHORTCUT_SUBMITTED, REJECTED, ERROR }

    static Outcome dispatch(EditAction action, NativeRoute nativeRoute, ShortcutRoute shortcuts,
                            boolean forceShortcuts) {
        if (!forceShortcuts) {
            Prepared prepared = null;
            try { prepared = nativeRoute.prepare(action); }
            catch (Throwable unavailable) { /* No native command was executed. */ }
            if (prepared != null) {
                if (prepared.blocked()) return Outcome.NATIVE_BLOCKED;
                try { prepared.operation().run(); return Outcome.NATIVE_SUBMITTED; }
                catch (Throwable partialFailure) { return Outcome.ERROR; }
            }
        }
        try { return shortcuts.send(action) ? Outcome.SHORTCUT_SUBMITTED : Outcome.REJECTED; }
        catch (Throwable failure) { return Outcome.ERROR; }
    }
}
