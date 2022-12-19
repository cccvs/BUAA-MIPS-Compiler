package back.alloc;

import back.Reg;
import back.ins.Addi;
import back.ins.Seqi;
import mid.code.*;
import mid.frame.MidLabel;
import mid.operand.Imm;
import mid.operand.MidVar;
import mid.operand.Operand;
import mid.operand.Symbol;

import java.io.PrintStream;
import java.util.*;

public class BasicBlock {
    private static int idCount = 0;

    // basic information
    private final RegAllocator parent;
    private final List<BasicIns> insList = new ArrayList<>();
    private final int blockId;

    // optimize
    private final List<BasicBlock> preBlocks = new ArrayList<>();
    private final List<BasicBlock> subBlocks = new ArrayList<>();
    private final Set<MidVar> liveDef = new HashSet<>();
    private final Set<MidVar> liveUse = new HashSet<>();
    private final Set<MidVar> liveIn = new HashSet<>();
    private final Set<MidVar> liveOut = new HashSet<>();
    private final Set<BasicIns> arrGen = new HashSet<>();
    private final Set<BasicIns> arrKill = new HashSet<>();
    private final Set<BasicIns> arrIn = new HashSet<>();
    private final Set<BasicIns> arrOut = new HashSet<>();


    public BasicBlock(RegAllocator regAllocator) {
        parent = regAllocator;
        blockId = ++idCount;
    }

    public void append(BasicIns ins) {
        insList.add(ins);
    }

    public BasicIns getLastIns() {
        return insList.isEmpty() ? null : insList.get(insList.size() - 1);
    }

    public boolean isEmpty() {
        return insList.isEmpty();
    }

    // arr
    public void buildGenKill() {
        for (int i = insList.size() - 1; i >= 0; i--) {
            BasicIns basicIns = insList.get(i);
            if (!basicIns.leftSet().isEmpty()) {
                Set<BasicIns> atomGenSet = new HashSet<>();
                atomGenSet.add(basicIns);
                atomGenSet.removeAll(arrKill); //gen[di] - kill[dn] - ... - kill[di+1]
                arrGen.addAll(atomGenSet);
                arrKill.addAll(parent.getAtomKillSet(basicIns));
            }
        }
    }

    public boolean broadcast() {
        // 只保留定义1次的变量
        Map<MidVar, BasicIns> varToDef = new HashMap<>();
        Set<MidVar> varSet = new HashSet<>();
        boolean changed = false;
        for (BasicIns basicIns : arrIn) {
            assert !basicIns.leftSet().isEmpty();
            MidVar midVar = basicIns.leftSet().iterator().next();
            if (varSet.contains(midVar)) {
                varToDef.remove(midVar);
            } else {
                varToDef.put(midVar, basicIns);
                varSet.add(midVar);
            }
        }
        // for
        for (BasicIns basicIns : insList) {
            if (basicIns instanceof BinaryOp) {
                // val
                BinaryOp binaryOp = ((BinaryOp) basicIns);
                if (binaryOp.getSrc1() instanceof MidVar) {
                    MidVar midVar1 = (MidVar) binaryOp.getSrc1();
                    if (varToDef.containsKey(midVar1)) {
                        Integer defVal = parent.getDefVal(varToDef.get(midVar1));
                        if (defVal != null) {
                            binaryOp.setSrc1(new Imm(defVal));
                            changed = true;
                        }
                    }
                }
                if (binaryOp.getSrc2() instanceof MidVar) {
                    MidVar midVar2 = (MidVar) binaryOp.getSrc2();
                    if (varToDef.containsKey(midVar2)) {
                        Integer defVal = parent.getDefVal(varToDef.get(midVar2));
                        if (defVal != null) {
                            binaryOp.setSrc2(new Imm(defVal));
                            changed = true;
                        }
                    }
                }
                // update
                if (binaryOp.getSrc1() instanceof Imm && binaryOp.getSrc2() instanceof Imm) {
                    if (binaryOp.getDst().getRefType().equals(Operand.RefType.ARRAY)) {
                        continue;
                    }
                    BinaryOp.Type op = binaryOp.getOp();
                    int imm1 = ((Imm) ((BinaryOp) basicIns).getSrc1()).getVal();
                    int imm2 = ((Imm) ((BinaryOp) basicIns).getSrc2()).getVal();
                    if (op.equals(BinaryOp.Type.ADD)) {
                        parent.setDefVal(basicIns, imm1 + imm2);
                    } else if (op.equals(BinaryOp.Type.SUB)) {
                        parent.setDefVal(basicIns, imm1 - imm2);
                    } else if (op.equals(BinaryOp.Type.MUL)) {
                        parent.setDefVal(basicIns, imm1 * imm2);
                    } else if (op.equals(BinaryOp.Type.DIV)) {
                        parent.setDefVal(basicIns, imm1 / imm2);
                    } else if (op.equals(BinaryOp.Type.MOD)) {
                        parent.setDefVal(basicIns, imm1 % imm2);
                    } else if (op.equals(BinaryOp.Type.SGT)) {
                        parent.setDefVal(basicIns, (imm1 > imm2) ? 1 : 0);
                    } else if (op.equals(BinaryOp.Type.SGE)) {
                        parent.setDefVal(basicIns, (imm1 >= imm2) ? 1 : 0);
                    } else if (op.equals(BinaryOp.Type.SLT)) {
                        parent.setDefVal(basicIns, (imm1 < imm2) ? 1 : 0);
                    } else if (op.equals(BinaryOp.Type.SLE)) {
                        parent.setDefVal(basicIns, (imm1 <= imm2) ? 1 : 0);
                    } else if (op.equals(BinaryOp.Type.SEQ)) {
                        parent.setDefVal(basicIns, (imm1 == imm2) ? 1 : 0);
                    } else if (op.equals(BinaryOp.Type.SNE)) {
                        parent.setDefVal(basicIns, (imm1 != imm2) ? 1 : 0);
                    } else {
                        throw new AssertionError("wrong binaryOp Type");
                    }
                }
            }
            else if (basicIns instanceof UnaryOp) {
                // val
                UnaryOp unaryOp = (UnaryOp) basicIns;
                if (unaryOp.getSrc() instanceof MidVar) {
                    MidVar midVar1 = (MidVar) unaryOp.getSrc();
                    if (varToDef.containsKey(midVar1)) {
                        Integer defVal = parent.getDefVal(varToDef.get(midVar1));
                        if (defVal != null) {
                            unaryOp.setSrc(new Imm(defVal));
                            changed = true;
                        }
                    }
                }
                // map
                if (unaryOp.getSrc() instanceof Imm) {
                    if (unaryOp.getDst().getRefType().equals(Operand.RefType.ARRAY)) {
                        continue;
                    }
                    int imm = ((Imm) ((UnaryOp) basicIns).getSrc()).getVal();
                    UnaryOp.Type op = unaryOp.getOp();
                    if (op.equals(UnaryOp.Type.MOV)) {
                        parent.setDefVal(basicIns, imm);
                    } else if (op.equals(UnaryOp.Type.NEG)) {
                        parent.setDefVal(basicIns, -imm);
                    } else if (op.equals(UnaryOp.Type.NOT)) {
                        parent.setDefVal(basicIns, (imm == 0) ? 1 : 0);
                    } else {
                        throw new AssertionError("wrong binaryOp Type");
                    }
                }
            }
            else if (basicIns instanceof Call) {
                // val
                Call call = (Call) basicIns;
                for (int j = 0; j < call.getRealParams().size(); j++) {
                    if (call.getRealParams().get(j) instanceof MidVar) {
                        MidVar midVar =  (MidVar) call.getRealParams().get(j);
                        if (varToDef.containsKey(midVar)) {
                            Integer defVal = parent.getDefVal(varToDef.get(midVar));
                            if (defVal != null) {
                                call.getRealParams().set(j, new Imm(defVal));
                                changed = true;
                            }
                        }
                    }
                }
            }
            else if (basicIns instanceof PrintInt) {
                PrintInt printInt = ((PrintInt) basicIns);
                if (printInt.getSrc() instanceof MidVar) {
                    MidVar midVar = (MidVar) printInt.getSrc();
                    if (varToDef.containsKey(midVar)) {
                        Integer defVal = parent.getDefVal(varToDef.get(midVar));
                        if (defVal != null) {
                            printInt.setSrc(new Imm(defVal));
                        }
                    }
                }
            }
            else if (basicIns instanceof Return) {
                Return ret = ((Return) basicIns);
                if (ret.getRetVal() instanceof MidVar) {
                    MidVar midVar = (MidVar) ret.getRetVal();
                    if (varToDef.containsKey(midVar)) {
                        Integer defVal = parent.getDefVal(varToDef.get(midVar));
                        if (defVal != null) {
                            ret.setRetVal(new Imm(defVal));
                            changed = true;
                        }
                    }
                }
            }else if (basicIns instanceof Offset) {
                // val
                Offset offset = (Offset) basicIns;
                if (offset.getOffsetVal() instanceof MidVar) {
                    MidVar midVar1 = (MidVar) offset.getOffsetVal();
                    if (varToDef.containsKey(midVar1)) {
                        Integer defVal = parent.getDefVal(varToDef.get(midVar1));
                        if (defVal != null) {
                            offset.setOffsetVal(new Imm(defVal));
                            changed = true;
                        }
                    }
                }
            } else if (basicIns instanceof MemOp) {
                // val
                MemOp memOp = (MemOp) basicIns;
                MidVar midVar = memOp.getPointer();
                if (varToDef.containsKey(midVar)) {
                    BasicIns defIns = varToDef.get(midVar);
                    if (defIns instanceof Offset) {
                        Offset offset = (Offset) defIns;
                        if (offset.getOffsetVal() instanceof Imm && offset.getBase() instanceof Symbol
                                && (offset.getBase()).getRefType().equals(Operand.RefType.ARRAY)) {
                            memOp.setOffset(((Imm) offset.getOffsetVal()).getVal());
                            memOp.setPointer(offset.getBase());
                        }
                    }
                }
            }

            if (!basicIns.leftSet().isEmpty()) {
                MidVar midVar = basicIns.leftSet().iterator().next();
                varToDef.put(midVar, basicIns);
            }
            // MemOp Offset 不管了
        }
        return changed;
    }

    // live
    public void clearReturnFollows() {
        for (int i = 0; i < insList.size(); i++) {
            if (insList.get(i) instanceof Return) {
                if (insList.size() > i + 1) {
                    insList.subList(i + 1, insList.size()).clear();
                    return;
                }
            }
        }
    }

    public void linkNext(BasicBlock next) {
        subBlocks.add(next);
        next.preBlocks.add(this);
    }

    public void buildDefUse() {
        liveDef.clear();
        liveUse.clear();
        for (BasicIns basicIns : insList) {
            Set<MidVar> leftSet = basicIns.leftSet();
            Set<MidVar> rightSet = basicIns.rightSet();
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
        for (BasicBlock subBlock : subBlocks) {
            liveOut.addAll(subBlock.liveIn);
        }
        liveIn.clear();
        liveIn.addAll(liveOut);
        liveIn.removeAll(liveDef);
        liveIn.addAll(liveUse);
        return liveOut.size() > preOutSize || liveIn.size() > preInSize;
    }

    public boolean updateArrival() {
        int preInSize = arrIn.size();
        int preOutSize = arrOut.size();
        for (BasicBlock preBlock : preBlocks) {
            arrIn.addAll(preBlock.arrOut);
        }
        arrOut.clear();
        arrOut.addAll(arrIn);
        arrOut.removeAll(arrKill);
        arrOut.addAll(arrGen);
        return arrOut.size() > preOutSize || arrIn.size() > preInSize;
    }

    public void buildIntervals(Map<MidVar, LiveInterval> varToInterval) {
        Set<MidVar> liveSet = new HashSet<>(liveOut);
        for (int i = insList.size() - 1; i >= 0; --i) {
            // update user var
            BasicIns basicIns = insList.get(i);
            int pos = parent.getInsPos(basicIns);
            // 记录函数调用时当前的活跃变量，函数调用时用于保存寄存器
            if (basicIns instanceof Call) {
                ((Call) basicIns).addLiveVars(liveSet);
            }
            // 函数实参是调用前使用，因此如果call f(t)是t最后一次使用，t不应活跃
            liveSet.addAll(basicIns.rightSet());
            // append intervals
            for (MidVar midVar : liveSet) {
                if (!varToInterval.containsKey(midVar)) {
                    varToInterval.put(midVar, new LiveInterval(midVar));
                }
                LiveInterval interval = varToInterval.get(midVar);
                interval.addPair(pos, pos + 1);
            }
            // 标记死代码, 由于是基本指令, 只需查看左值是否活跃 TODO: 可能出bug
            // 这里不可以是全局变量, 右侧也不可以有call
            if (basicIns instanceof BinaryOp || basicIns instanceof UnaryOp
                    || basicIns instanceof GetInt || basicIns instanceof Offset) {
                MidVar midVar = basicIns.leftSet().iterator().next();
                // 不可以是全局变量!
                if (!liveSet.contains(midVar) && !(midVar instanceof Symbol && ((Symbol) midVar).isGlobal())) {
                    basicIns.setDead();
                }
            }
            // remove def var
            liveSet.removeAll(basicIns.leftSet());
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
        sb.append("sub: ");
        for (BasicBlock subBlock : subBlocks) {
            sb.append(subBlock).append(" ");
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
