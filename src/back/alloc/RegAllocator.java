package back.alloc;

import mid.code.BasicIns;
import mid.code.Branch;
import mid.code.Jump;
import mid.code.Return;
import mid.frame.FuncFrame;
import mid.frame.MidLabel;
import mid.operand.MidVar;

import java.util.*;


public class RegAllocator {
    private final HashSet<Integer> allocatableRegs = new HashSet<>
            (Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25));

    // mid code
    private final List<BasicBlock> blockList = new LinkedList<>();
    private final List<LiveInterval> intervalList = new ArrayList<>();
    private final Map<String, BasicBlock> labelToBlock = new HashMap<>();

    // regs
    private final Set<LiveInterval> liveIntervalSet = new HashSet<>();
    private final Map<Integer, Integer> liveRegs = new HashMap<>();     // key: 寄存器编号, value: 现存次数
    private final Set<Integer> freeRegs = new LinkedHashSet<>(allocatableRegs);
    private final Set<MidVar> spilledVarSet = new HashSet<>();

    private BasicBlock curBlock;

    public RegAllocator(FuncFrame funcFrame) {
        readFuncInfo(funcFrame);
        cleanBlocks();
        buildFlowGraph();
        buildDefUse();
        livenessAnalysis();
        buildIntervals();
        walkIntervals();
    }

    // 1
    private void readFuncInfo(FuncFrame funcFrame) {
        int cnt = 0;
        Iterator<BasicIns> insIter = funcFrame.iterIns();
        while (insIter.hasNext()) {
            BasicIns basicIns = insIter.next();
            SerialIns serialIns = new SerialIns(cnt * 2, basicIns);
            // begin or label
            if (basicIns instanceof MidLabel || cnt == 0) {
                updateBlock();
                if (basicIns instanceof MidLabel) {
                    labelToBlock.put(((MidLabel) basicIns).getLabel(), curBlock);
                }
            }
            // append
            curBlock.append(serialIns);
            // jump follow or branch follow
            if (basicIns instanceof Jump || basicIns instanceof Branch) {
                updateBlock();
            }
            cnt += 1;
        }
    }

    private void updateBlock() {
        if (curBlock == null || !curBlock.isEmpty()) {
            curBlock = new BasicBlock();
            blockList.add(curBlock);
        }
    }

    // 2
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

    // 3
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

    // 4
    private void buildDefUse() {
        for (BasicBlock basicBlock : blockList) {
            basicBlock.buildDefUse();
        }
    }

    // 5
    private void livenessAnalysis() {
        boolean change = true;
        while (change) {
            change = false;
            for (int i = blockList.size() - 1; i >= 0; --i) {
                change = change || blockList.get(i).updateLiveness();
            }
        }
    }

    // 6
    private void buildIntervals() {
        HashMap<MidVar, LiveInterval> varToInterval = new HashMap<>();
        for (int i = blockList.size() - 1; i >= 0; --i) {
            blockList.get(i).buildIntervals(varToInterval);
        }
        intervalList.addAll(varToInterval.values());
        intervalList.sort(Comparator.naturalOrder());
    }

    // 7
    private void walkIntervals() {
        for (LiveInterval newInterval : intervalList) {
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
            spilledVarSet.add(newInterval.getMidVar());
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
}
