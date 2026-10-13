package io.github.igorcv88.honeyboardtweaks;

import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class HistoryDispatcherTest {
    @Test public void nativeFailureAfterMutationMustNotReplayShortcut() {
        AtomicInteger operations = new AtomicInteger(), shortcuts = new AtomicInteger();
        var outcome = HistoryDispatcher.dispatch(EditAction.UNDO, action ->
                new HistoryDispatcher.Prepared(false, () -> {
                    operations.incrementAndGet(); throw new IllegalStateException();
                }), action -> { shortcuts.incrementAndGet(); return true; }, false);
        assertEquals(HistoryDispatcher.Outcome.ERROR, outcome);
        assertEquals(1, operations.get()); assertEquals(0, shortcuts.get());
    }
    @Test public void successfulNativeUndoIsNeverFollowedByAnotherUndo() {
        AtomicInteger operations = new AtomicInteger(), shortcuts = new AtomicInteger();
        var outcome = HistoryDispatcher.dispatch(EditAction.UNDO,
                action -> new HistoryDispatcher.Prepared(false, operations::incrementAndGet),
                action -> { shortcuts.incrementAndGet(); return true; }, false);
        assertEquals(HistoryDispatcher.Outcome.NATIVE_SUBMITTED, outcome);
        assertEquals(1, operations.get()); assertEquals(0, shortcuts.get());
    }
    @Test public void nativeEligibilityBlockDoesNotBypassSamsungPolicy() {
        var outcome = HistoryDispatcher.dispatch(EditAction.REDO,
                action -> new HistoryDispatcher.Prepared(true, () -> fail()),
                action -> { fail(); return true; }, false);
        assertEquals(HistoryDispatcher.Outcome.NATIVE_BLOCKED, outcome);
    }
    @Test public void unavailableNativeSymbolsUseOneShortcutRoute() {
        AtomicInteger shortcuts = new AtomicInteger();
        var outcome = HistoryDispatcher.dispatch(EditAction.UNDO,
                action -> { throw new NoSuchMethodException(); },
                action -> { shortcuts.incrementAndGet(); return true; }, false);
        assertEquals(HistoryDispatcher.Outcome.SHORTCUT_SUBMITTED, outcome);
        assertEquals(1, shortcuts.get());
    }
    @Test public void explicitShortcutModeDoesNotConstructNativeObjects() {
        var outcome = HistoryDispatcher.dispatch(EditAction.REDO,
                action -> { fail(); return null; }, action -> true, true);
        assertEquals(HistoryDispatcher.Outcome.SHORTCUT_SUBMITTED, outcome);
    }
    @Test public void rejectedShortcutIsNotReportedAsNativeSuccess() {
        assertEquals(HistoryDispatcher.Outcome.REJECTED,
                HistoryDispatcher.dispatch(EditAction.REDO, action -> null, action -> false, true));
    }
}
