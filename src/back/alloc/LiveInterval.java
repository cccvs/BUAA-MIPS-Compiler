package back.alloc;

import mid.operand.MidVar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class LiveInterval implements Comparable<LiveInterval> {
    private final List<IntPair> pairs;
    private final MidVar midVar;

    public LiveInterval(MidVar midVar) {
        this.pairs = new LinkedList<>();
        this.midVar = midVar;
    }

    public MidVar getMidVar() {
        return midVar;
    }

    // 从大到小的顺序添加区间，每次加在区间开头
    public void addPair(int lower, int upper) {
        if (pairs.isEmpty() || upper + 1 < pairs.get(0).getLower()) {
            pairs.add(0, new IntPair(lower, upper));
        } else {
            int newUpper = pairs.get(0).getUpper();
            pairs.remove(0);
            pairs.add(0, new IntPair(lower, newUpper));
        }
    }

    public boolean intersect(LiveInterval other) {
        // cal merge list
        List<IntPair> mergeList = new ArrayList<>(pairs);
        mergeList.addAll(other.pairs);
        mergeList.sort(Comparator.naturalOrder());
        // intersect
        for (int i = 0; i < mergeList.size(); i++) {
            if (i > 0 && mergeList.get(i - 1).getUpper() >= mergeList.get(i).getLower()) {
                return true;
            }
        }
        return false;
    }

    public int lower() {
        assert !pairs.isEmpty();
        return pairs.get(0).getLower();
    }

    public int upper() {
        assert !pairs.isEmpty();
        return pairs.get(pairs.size() - 1).getLower();
    }

    @Override
    public int compareTo(LiveInterval o) {
        if (this.lower() != o.lower()) {
            return this.lower() - o.lower();
        } else {
            return this.upper() - o.upper();
        }
    }
}
