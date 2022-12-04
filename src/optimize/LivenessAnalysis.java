package optimize;

import mid.MidCode;
import mid.frame.FuncFrame;

import java.util.Iterator;

public class LivenessAnalysis {
    public static void run(MidCode midCode) {
        Iterator<FuncFrame> funcFrames = midCode.funcIter();
        while (funcFrames.hasNext()) {
            FuncFrame funcFrame = funcFrames.next();
            runFunc(funcFrame);
        }
    }

    private static void runFunc(FuncFrame funcFrame) {

    }
}
