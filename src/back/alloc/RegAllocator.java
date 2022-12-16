package back.alloc;

import mid.code.BasicIns;
import mid.code.Branch;
import mid.code.Jump;
import mid.code.Return;
import mid.frame.FuncFrame;
import mid.frame.MidLabel;
import mid.operand.MidVar;
import mid.operand.Operand;
import mid.operand.Symbol;

import java.io.PrintStream;
import java.util.*;


public class RegAllocator {
    private final HashSet<Integer> allocatableRegs = new HashSet<>
            (Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25));

    // mid code
    private final FuncFrame funcFrame;
    private final List<BasicBlock> blockList = new LinkedList<>();
    private final List<LiveInterval> intervalList = new ArrayList<>();
    private final Map<String, BasicBlock> labelToBlock = new HashMap<>();
    private final Map<BasicIns, Integer> posMap = new HashMap<>();

    // regs
    private final Set<LiveInterval> liveIntervalSet = new HashSet<>();
    private final Map<Integer, Integer> liveRegs = new HashMap<>();     // key: 寄存器编号, value: 现存次数
    private final Set<Integer> freeRegs = new LinkedHashSet<>(allocatableRegs);

    // const spread
    private final Map<MidVar, Set<BasicIns>> varToDef = new HashMap<>();
    private final Map<BasicIns, Integer> defToVal = new HashMap<>();

    private BasicBlock curBlock;

    public RegAllocator(FuncFrame funcFrame) {
        this.funcFrame = funcFrame;
        buildBasicFrame(funcFrame);
        allocRegs();
        constBroadcast();
        removeDeadIns();
    }

    // 1.1
    private void readFuncInfo(FuncFrame funcFrame) {
        int cnt = 0;
        List<BasicIns> insList = funcFrame.insList();
        for (BasicIns basicIns : insList) {
            posMap.put(basicIns, cnt * 2);
            // begin or label
            if (basicIns instanceof MidLabel || cnt == 0) {
                updateBlock();
                if (basicIns instanceof MidLabel) {
                    labelToBlock.put(((MidLabel) basicIns).getLabel(), curBlock);
                }
            }
            // append
            curBlock.append(basicIns);
            // jump follow or branch follow
            if (basicIns instanceof Jump || basicIns instanceof Branch) {
                updateBlock();
            }
            cnt += 1;
        }
    }

    private void updateBlock() {
        if (curBlock == null || !curBlock.isEmpty()) {
            curBlock = new BasicBlock(this);
            blockList.add(curBlock);
        }
    }

    // 1.2
    private void cleanBlocks() {
        for (int i = 0; i < blockList.size(); ) {
            if (blockList.get(i).isEmpty()) {
                blockList.remove(i);
            } else {
                blockList.get(i).clearReturnFollows();
                ++i;
            }
        }
    }

    // 1.3
    private void buildFlowGraph() {
        for (int i = 0; i < blockList.size(); i++) {
            BasicBlock block = blockList.get(i);
            BasicIns lastIns = block.getLastIns();
            if (lastIns instanceof Jump) {
                block.linkNext(labelToBlock.get(((Jump) lastIns).getTargetLabel().getLabel()));
            } else if (lastIns instanceof Branch) {
                block.linkNext(labelToBlock.get(((Branch) lastIns).getLabelTrue().getLabel()));
                block.linkNext(labelToBlock.get(((Branch) lastIns).getLabelFalse().getLabel()));
            } else if (!(lastIns instanceof Return)) {
                if (i + 1 < blockList.size()) {
                    block.linkNext(blockList.get(i + 1));
                }
            }
        }
    }

    // 1
    private void buildBasicFrame(FuncFrame funcFrame) {
        readFuncInfo(funcFrame);
        cleanBlocks();
        buildFlowGraph();
    }

    // 2
    private void constBroadcast() {
        buildDefMap();
        buildGenKill();
        arrivalAnalysis();
        walkIntervals();
        broadcast();
    }

    // 2.1
    private void buildDefMap() {
        for (BasicIns basicIns : funcFrame.insList()) {
            if (!basicIns.leftSet().isEmpty()) {
                MidVar midVar = basicIns.leftSet().iterator().next();
                if (varToDef.containsKey(midVar)) {
                    varToDef.get(midVar).add(basicIns);
                } else {
                    Set<BasicIns> insSet = new HashSet<>();
                    insSet.add(basicIns);
                    varToDef.put(midVar, insSet);
                }
            }
        }
    }

    // 2.2
    private void buildGenKill() {
        for (BasicBlock basicBlock : blockList) {
            basicBlock.buildGenKill();
        }
    }

    // 2.3
    private void arrivalAnalysis() {
        boolean change = true;
        while (change) {
            change = false;
            for (BasicBlock basicBlock : blockList) {
                change = change || basicBlock.updateArrival();
            }
        }
    }

    // 2.4
    private void broadcast() {
        boolean change = true;
        while (change) {
            change = false;
            for (int i = blockList.size() - 1; i >= 0; --i) {
                change = change || blockList.get(i).broadcast();
            }
        }
    }

    // 3
    private void allocRegs() {
        serializeIns();
        buildDefUse();
        livenessAnalysis();
        buildIntervals();
    }

    // 3.1
    private void serializeIns() {
        int cnt = 0;
        List<BasicIns> insList = funcFrame.insList();
        for (BasicIns basicIns : insList) {
            posMap.put(basicIns, cnt * 2);
            cnt += 1;
        }
    }

    // 3.2
    private void buildDefUse() {
        for (BasicBlock basicBlock : blockList) {
            basicBlock.buildDefUse();
        }
    }

    // 3.3
    private void livenessAnalysis() {
        boolean change = true;
        while (change) {
            change = false;
            for (int i = blockList.size() - 1; i >= 0; --i) {
                change = change || blockList.get(i).updateLiveness();
            }
        }
    }

    // 3.4
    private void buildIntervals() {
        HashMap<MidVar, LiveInterval> varToInterval = new HashMap<>();
        for (int i = blockList.size() - 1; i >= 0; --i) {
            blockList.get(i).buildIntervals(varToInterval);
        }
        intervalList.addAll(varToInterval.values());
        intervalList.sort(Comparator.naturalOrder());
    }

    // 3.5
    public void walkIntervals() {
        for (LiveInterval newInterval : intervalList) {
            // 全局变量不作分配
            if (newInterval.getMidVar() instanceof Symbol) {
                Symbol symbol = (Symbol) newInterval.getMidVar();
                if (symbol.isGlobal()) {
                    continue;
                }
            }
            // 遍历
            int curPos = newInterval.lower();
            Set<LiveInterval> liveIntervalMeta = new HashSet<>(liveIntervalSet);
            for (LiveInterval live : liveIntervalMeta) {
                if (curPos > live.upper()) {
                    removeInterval(live);
                }
            }
            pushInterval(newInterval);
        }
    }

    private Integer allocRegForInterval(LiveInterval allocInterval) {
        Set<Integer> remainRegs = new HashSet<>(allocatableRegs);
        for (LiveInterval liveInterval : liveIntervalSet) {
            if (allocInterval.intersect(liveInterval)) {
                remainRegs.remove(liveInterval.getMidVar().getReg());
            }
        }
        if (!remainRegs.isEmpty()) {
            return remainRegs.iterator().next();
        } else if (!freeRegs.isEmpty()) {
            Iterator<Integer> regIter = freeRegs.iterator();
            int latestReg = regIter.next();
            while (regIter.hasNext()) {
                latestReg = regIter.next();
            }
            return latestReg;
        } else {
            return null;
        }
    }

    private void pushInterval(LiveInterval newInterval) {
        Integer allocReg = allocRegForInterval(newInterval);
        if (allocReg == null) {
            // alloc failed
            return;
        }
        // free/live regs
        if (liveRegs.containsKey(allocReg)) {
            int regCount = liveRegs.get(allocReg);
            liveRegs.put(allocReg, regCount + 1);
        } else {
            liveRegs.put(allocReg, 1);
            freeRegs.remove(allocReg);
        }
        // interval and mid var
//        System.out.println(newInterval.getMidVar());
        newInterval.getMidVar().allocReg(allocReg);
        liveIntervalSet.add(newInterval);
    }

    private void removeInterval(LiveInterval removeInterval) {
        int freeReg = removeInterval.getMidVar().getReg();
        int regCount = liveRegs.get(freeReg);
        // live/free regs
        if (regCount > 1) {
            liveRegs.put(freeReg, regCount - 1);
        } else {
            liveRegs.remove(freeReg);
            freeRegs.add(freeReg);
        }
        // interval
        liveIntervalSet.remove(removeInterval);
    }

    // 4
    private void removeDeadIns() {
        List<BasicIns> insList = funcFrame.insList();
        List<BasicIns> newList = new ArrayList<>();
        for (BasicIns basicIns : insList) {
            if (!basicIns.isDead()) {
                newList.add(basicIns);
            }
        }
        insList.clear();
        insList.addAll(newList);
    }

    // 9
    public void outputInterval(PrintStream ps) {
        ps.println(funcFrame.getLabel());
        for (LiveInterval interval : intervalList) {
            ps.println(interval);
        }
        ps.println();
    }

    // util
    public int getInsPos(BasicIns basicIns) {
        return posMap.get(basicIns);
    }

    public Set<BasicIns> getAtomKillSet(BasicIns basicIns) {
        if (basicIns.leftSet().isEmpty()) {
            return new HashSet<>();
        } else {
            MidVar midVar = basicIns.leftSet().iterator().next();
            Set<BasicIns> atomKillSet = new HashSet<>(varToDef.get(midVar));
            atomKillSet.remove(basicIns);
            return atomKillSet;
        }
    }

    public void setDefVal(BasicIns basicIns, int val) {
        defToVal.put(basicIns, val);
    }

    public Integer getDefVal(BasicIns basicIns) {
        return defToVal.getOrDefault(basicIns, null);
    }
}
