package back.alloc;

import mid.code.*;
import mid.operand.MidVar;
import mid.operand.Symbol;

import java.io.PrintStream;
import java.util.*;

public class BasicBlock {
    private static int idCount = 0;

    // basic information
    private final List<SerialIns> insList = new ArrayList<>();
    private final int blockId;

    // optimize
    private final List<BasicBlock> preBlocks = new ArrayList<>();
    private final List<BasicBlock> sucBlocks = new ArrayList<>();
    private final Set<MidVar> liveDef = new HashSet<>();
    private final Set<MidVar> liveUse = new HashSet<>();
    private final Set<MidVar> liveIn = new HashSet<>();
    private final Set<MidVar> liveOut = new HashSet<>();

    public BasicBlock() {
        blockId = ++idCount;
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
            // 标记死代码, 由于是基本指令, 只需查看左值是否活跃 TODO: 可能出bug
            if (basicIns instanceof BinaryOp || basicIns instanceof UnaryOp
                    || basicIns instanceof GetInt || basicIns instanceof Offset) {
                MidVar midVar = basicIns.leftSet().iterator().next();
                if (!liveSet.contains(midVar) && !(midVar instanceof Symbol && ((Symbol) midVar).isGlobal())) {
                    basicIns.setDead();
                }
            }
            // remove def var
            liveSet.removeAll(serialIns.leftSet());
        }
    }

    public void output(PrintStream ps) {
        ps.println("block: " + this);
        StringBuilder sb = new StringBuilder();
        sb.append("pre: ");
        for (BasicBlock preBlock : preBlocks) {
            sb.append(preBlock).append(" ");
        }
        ps.println(sb);

        sb = new StringBuilder();
        sb.append("suc: ");
        for (BasicBlock sucBlock : sucBlocks) {
            sb.append(sucBlock).append(" ");
        }
        ps.println(sb);

        sb = new StringBuilder();
        sb.append("Def: ");
        for (MidVar var : liveDef) {
            sb.append(var).append(" ");
        }
        ps.println(sb);

        sb = new StringBuilder();
        sb.append("Use: ");
        for (MidVar var : liveUse) {
            sb.append(var).append(" ");
        }
        ps.println(sb);

        sb = new StringBuilder();
        sb.append("In: ");
        for (MidVar var : liveIn) {
            sb.append(var).append(" ");
        }
        ps.println(sb);

        sb = new StringBuilder();
        sb.append("Out: ");
        for (MidVar var : liveOut) {
            sb.append(var).append(" ");
        }
        ps.println(sb);
        ps.println();
    }

    @Override
    public String toString() {
        return "block" + blockId;
    }
}
