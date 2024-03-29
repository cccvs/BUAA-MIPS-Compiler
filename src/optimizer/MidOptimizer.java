package optimizer;

import back.alloc.RegAllocator;
import mid.MidCode;
import mid.frame.FuncFrame;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MidOptimizer {
    private final MidCode midCode;
    private final List<FuncFrame> funcList = new ArrayList<>();
    private final List<RegAllocator> allocatorList = new ArrayList<>();
    private static final int turn = 10;
    public static final boolean HACK_DIV = true;
    public static final boolean HACK_ALLOC = true;


    public MidOptimizer(MidCode midCode) {
        this.midCode = midCode;
        Iterator<FuncFrame> funcIterator = midCode.funcIter();
        while (funcIterator.hasNext()) {
            funcList.add(funcIterator.next());
        }
        funcList.add(midCode.getMainFunc());
    }

    public void run() {
        for (FuncFrame funcFrame : funcList) {
            for (int i = 0; i < turn; i++) {
                new RegAllocator(funcFrame);
            }
            RegAllocator regAllocator = new RegAllocator(funcFrame);
            regAllocator.walkIntervals();
            allocatorList.add(regAllocator);
        }
    }

    public void outputRegInfo(PrintStream ps) {
        for (RegAllocator allocator : allocatorList) {
            allocator.outputInterval(ps);
        }
    }
}
