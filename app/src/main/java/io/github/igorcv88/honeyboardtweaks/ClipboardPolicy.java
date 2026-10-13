package io.github.igorcv88.honeyboardtweaks;

import java.util.ArrayList;
import java.util.List;

/** Pure policy. The adapter deletes through Samsung's store, including its media cleanup. */
final class ClipboardPolicy {
    record Entry(long id, long timestamp, int userId, int origin, boolean pinned) {}

    static int validCapacity(int requested) {
        return switch (requested) {
            case 40, 50, 100, 150, 200, 500 -> requested;
            default -> 100;
        };
    }
    static List<Long> victims(List<Entry> entries, Entry incoming, int capacity,
                              boolean keepRemoteHistory) {
        List<Entry> sameUser = new ArrayList<>();
        List<Long> result = new ArrayList<>();
        for (Entry e : entries) {
            if (e.userId != incoming.userId) continue;
            // Origin 1/2 normally retains only the latest remote item. Never remove pins.
            if (!keepRemoteHistory && !e.pinned && incoming.origin != 0
                    && incoming.origin == e.origin) result.add(e.id);
            else sameUser.add(e);
        }
        sameUser.sort(java.util.Comparator.comparingLong(Entry::timestamp));
        int excess = Math.max(0, sameUser.size() + 1 - validCapacity(capacity));
        for (Entry e : sameUser) {
            if (excess == 0) break;
            if (!e.pinned) { result.add(e.id); excess--; }
        }
        return result;
    }
}
