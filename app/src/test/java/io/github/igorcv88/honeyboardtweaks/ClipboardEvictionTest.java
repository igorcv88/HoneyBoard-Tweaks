package io.github.igorcv88.honeyboardtweaks;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ClipboardEvictionTest {
    @Test public void partialFailureStopsWithoutRetryingAnyVictim() {
        List<Long> attempted = new ArrayList<>();
        var report = ClipboardEviction.run(List.of(1L, 2L, 3L), id -> {
            attempted.add(id); if (id == 2) throw new IllegalStateException("secret clipboard content");
        });
        assertEquals(List.of(1L, 2L), attempted);
        assertEquals(3, report.planned()); assertEquals(1, report.completed());
        assertEquals("IllegalStateException", report.failureType());
    }
    @Test public void firstFailureIsNotCountedAsCompleted() {
        var report = ClipboardEviction.run(List.of(1L), id -> { throw new IllegalStateException(); });
        assertEquals(0, report.completed()); assertNotNull(report.failureType());
    }
    @Test public void successfulEvictionReportsAllCompletions() {
        var report = ClipboardEviction.run(List.of(1L, 2L), id -> {});
        assertEquals(2, report.completed()); assertNull(report.failureType());
    }
    @Test public void reflectionFailureReportsCauseTypeWithoutPayload() {
        var report = ClipboardEviction.run(List.of(1L), id -> {
            throw new java.lang.reflect.InvocationTargetException(
                    new IllegalStateException("clipboard payload"));
        });
        assertEquals("IllegalStateException", report.failureType());
    }
}
