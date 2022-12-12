package back.alloc;

import mid.code.BasicIns;
import mid.code.Call;
import mid.code.Return;
import mid.operand.MidVar;

import java.util.*;

public class BasicBlock {

    // basic information
    private final List<SerialIns> insList = new ArrayList<>();

    // optimize
    private final List<BasicBlock> preBlocks = new ArrayList<>();
    private final List<BasicBlock> sucBlocks = new ArrayList<>();
    private final Set<MidVar> liveDef = new HashSet<>();
    private final Set<MidVar> liveUse = new HashSet<>();
    private final Set<MidVar> liveIn = new HashSet<>();
    private final Set<MidVar> liveOut = new HashSet<>();

    public BasicBlock() {

    }

    public void append(SerialIns ins) {
        insList.add(ins);
    }

    public BasicIns getLastIns() {
        return insList.isEmpty() ? null : insList.get(insList.size() - 1).getIns();
    }

    public boolean isEmpty() {
        return insList.isEmpty();
    }

    // ops
    public void clearReturnFollows() {
        for (int i = 0; i < insList.size(); i++) {
            if (insList.get(i).getIns() instanceof Return) {
                if (insList.size() > i + 1) {
                    insList.subList(i + 1, insList.size()).clear();
                    return;
                }
            }
        }
    }

    public void linkNext(BasicBlock next) {
        sucBlocks.add(next);
        next.preBlocks.add(this);
    }

    public void buildDefUse() {
        liveDef.clear();
        liveUse.clear();
        for (SerialIns serialIns : insList) {
            Set<MidVar> leftSet = serialIns.leftSet();
            Set<MidVar> rightSet = serialIns.rightSet();
            for (MidVar midVar : rightSet) {
                if (!liveDef.contains(midVar)) {
                    liveUse.add(midVar);
                }
            }
            for (MidVar midVar : leftSet) {
                if (!liveUse.contains(midVar)) {
                    liveDef.add(midVar);
                }
            }
        }
    }

    public boolean updateLiveness() {
        int preInSize = liveIn.size();
        int preOutSize = liveOut.size();
        for (BasicBlock subBlock : sucBlocks) {
            liveOut.addAll(subBlock.liveIn);
        }
        liveIn.clear();
        liveIn.addAll(liveOut);
        liveIn.removeAll(liveDef);
        liveIn.addAll(liveUse);
        return liveOut.size() > preOutSize || liveIn.size() > preInSize;
    }

    public void buildIntervals(Map<MidVar, LiveInterval> varToInterval) {
        int blockUpper = insList.get(insList.size() - 1).getPos() + 1;
        int blockLower = insList.get(0).getPos();
        Set<MidVar> liveSet = new HashSet<>(liveOut);
        for (int i = insList.size() - 1; i >= 0; --i) {
            // update user var
            SerialIns serialIns = insList.get(i);
            int pos = serialIns.getPos();
            liveSet.addAll(serialIns.rightSet());
            // 记录函数调用时当前的活跃变量，函数调用时用于保存寄存器
            BasicIns basicIns = serialIns.getIns();
            if (basicIns instanceof Call) {
                ((Call) basicIns).addLiveVars(liveSet);
            }
            // append intervals
            for (MidVar midVar : liveSet) {
                if (!varToInterval.containsKey(midVar)) {
                    varToInterval.put(midVar, new LiveInterval(midVar));
                }
                LiveInterval interval = varToInterval.get(midVar);
                interval.addPair(pos, pos + 1);
            }
            // remove def var
            liveSet.removeAll(serialIns.leftSet());
        }
    }
}
