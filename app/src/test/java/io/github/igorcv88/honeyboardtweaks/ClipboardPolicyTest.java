package io.github.igorcv88.honeyboardtweaks;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ClipboardPolicyTest {
    private ClipboardPolicy.Entry e(long id, long timestamp, int user, int origin, boolean pin) {
        return new ClipboardPolicy.Entry(id, timestamp, user, origin, pin);
    }
    @Test public void allRequestedLimitsRetainExactlyCapacity() {
        for (int capacity : new int[]{50,100,150,200,500}) {
            List<ClipboardPolicy.Entry> entries = new ArrayList<>();
            for (int i = 0; i < capacity; i++) entries.add(e(i, i, 0, 0, false));
            List<Long> victims = ClipboardPolicy.victims(entries, e(capacity, capacity, 0, 0, false), capacity, true);
            assertEquals(List.of(0L), victims);
            assertEquals(capacity, entries.size() - victims.size() + 1);
        }
    }
    @Test public void belowLimitDoesNotDelete() {
        assertTrue(ClipboardPolicy.victims(List.of(e(1,0,0,0,false)),
                e(2,1,0,0,false),50,true).isEmpty());
    }
    @Test public void pinnedItemsSurviveEvenWhenOverLimit() {
        List<ClipboardPolicy.Entry> entries = new ArrayList<>();
        for (int i = 0; i < 51; i++) entries.add(e(i,i,0,0,true));
        assertTrue(ClipboardPolicy.victims(entries,e(52,52,0,0,false),50,true).isEmpty());
    }
    @Test public void oldestUnpinnedIsRemovedAndPinCountsTowardCapacity() {
        List<ClipboardPolicy.Entry> entries = new ArrayList<>();
        for (int i = 0; i < 50; i++) entries.add(e(i,i,0,0,i==0));
        assertEquals(List.of(1L),ClipboardPolicy.victims(entries,e(51,51,0,0,false),50,true));
    }
    @Test public void otherUserIsUntouched() {
        List<ClipboardPolicy.Entry> entries = new ArrayList<>();
        for (int i = 0; i < 100; i++) entries.add(e(i,i,10,1,false));
        assertTrue(ClipboardPolicy.victims(entries,e(101,101,0,1,false),50,false).isEmpty());
    }
    @Test public void retainingRemoteHistoryDoesNotReplaceLastRemoteCopy() {
        assertTrue(ClipboardPolicy.victims(List.of(e(1,0,0,1,false),e(2,1,0,2,false)),
                e(3,2,0,1,false),50,true).isEmpty());
    }
    @Test public void nativeRemoteReplacementPreservesPinsAndOtherOrigin() {
        assertEquals(List.of(1L),ClipboardPolicy.victims(
                List.of(e(1,0,0,1,false),e(2,1,0,1,true),e(3,2,0,2,false)),
                e(4,3,0,1,false),50,false));
    }
    @Test public void oldTimestampsAreNotAnExpiryCriterion() {
        assertTrue(ClipboardPolicy.victims(List.of(e(1,0,0,0,false)),
                e(2,System.currentTimeMillis(),0,0,false),500,true).isEmpty());
    }
    @Test public void loweringCapacityRemovesEnoughItemsBeforeInsert() {
        List<ClipboardPolicy.Entry> entries = new ArrayList<>();
        for(int i=0;i<500;i++) entries.add(e(i,i,0,0,false));
        List<Long> victims = ClipboardPolicy.victims(entries,e(500,500,0,0,false),50,true);
        assertEquals(451,victims.size()); assertEquals(Long.valueOf(450),victims.get(450));
    }
    @Test public void orderingUsesTimestampRatherThanInputListOrder() {
        List<ClipboardPolicy.Entry> entries = new ArrayList<>();
        for(int i=49;i>=0;i--) entries.add(e(i,i,0,0,false));
        assertEquals(List.of(0L),ClipboardPolicy.victims(entries,e(50,50,0,0,false),50,true));
    }
    @Test public void invalidConfigurationFallsBackTo100() {
        assertEquals(100,ClipboardPolicy.validCapacity(-1));
        assertEquals(100,ClipboardPolicy.validCapacity(Integer.MAX_VALUE));
    }
}
