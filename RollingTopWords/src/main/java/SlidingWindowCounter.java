import java.io.Serializable;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ivensli on 2017/1/5.
 */
public final class SlidingWindowCounter<T> implements Serializable {

    private static final long serialVersionUID = -2645063988768785810L;

    private int slotNum;
    private int currentSlot;
    private int nextSlot;

    private final Map<T, long[]> counter = new HashMap<T, long[]>();

    public SlidingWindowCounter(int slotNum) {
        if (slotNum < 1) {
            throw new IllegalArgumentException("Slot num need to be at least 1!");
        }
        this.slotNum = slotNum;
        currentSlot = 0;
        nextSlot = (currentSlot + 1) % slotNum;
    }

    private Map<T, Long> getCounts() {
        Map<T, Long> result = new HashMap<T, Long>();
        for (T obj : counter.keySet()) {
            result.put(obj, getObjSum(obj));
        }
        return result;
    }

    private long getObjSum(T obj) {
        long ret = 0;
        long[] curs = counter.get(obj);
        for (long i : curs) {
            ret += i;
        }
        return ret;
    }

    private void wipeZero() {
        Set<T> objToRemove = new HashSet<T>();
        Map<T, Long> counts = getCounts();
        for (T obj : counts.keySet()) {
            if (0 == getObjSum(obj)) {
                objToRemove.add(obj);
            }
        }

        for (T obj : objToRemove) {
            counter.remove(obj);
        }
    }

    private void wipeSlot(int slot) {
        for (T obj : counter.keySet()) {
            long[] counts = counter.get(obj);
            counts[slot] = 0;
        }
    }

    public void incrementCount(T obj) {
        long[] counts = counter.get(obj);
        if (counts == null) {
            counts = new long[this.slotNum];
            counter.put(obj, counts);
        }
        counts[currentSlot]++;
    }

    public Map<T, Long> getCountsThenAdvanceWindow() {
        Map<T, Long> counts = getCounts();
        wipeZero();
        wipeSlot(nextSlot);
        currentSlot = nextSlot;
        nextSlot = (currentSlot + 1) % slotNum;
        return counts;
    }
}
