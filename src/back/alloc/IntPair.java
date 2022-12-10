package back.alloc;

public class IntPair implements Comparable<IntPair> {
    private final int lower;
    private final int upper;

    public IntPair(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public int getLower() {
        return lower;
    }

    public int getUpper() {
        return upper;
    }

    @Override
    public int compareTo(IntPair o) {
        if (this.lower != o.lower) {
            return this.lower - o.lower;
        } else {
            return this.upper - o.upper;
        }
    }
}
