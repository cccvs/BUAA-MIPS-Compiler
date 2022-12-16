package back;

import back.alloc.BasicBlock;
import back.alloc.RegAllocator;
import back.ins.*;
import back.special.Comment;
import back.special.Label;
import back.special.MipsIns;
import back.special.Syscall;
import mid.MidCode;
import mid.code.*;
import mid.frame.FuncFrame;
import mid.frame.MidLabel;
import mid.operand.Imm;
import mid.operand.Operand;
import mid.operand.Symbol;
import mid.operand.MidVar;

import java.io.PrintStream;
import java.util.*;

public class MipsTranslator {
    public static final int TMP_R1 = Reg.V1;
    public static final int TMP_R2 = Reg.FP;

    // info
    private final MidCode midCode;
    private final List<MipsIns> mipsInsList = new ArrayList<>();
    private final List<RegAllocator> allocatorList = new ArrayList<>();

    // current
    private boolean isMain = false;     // 标记当前函数是否为main函数，特殊处理return
    private Integer stackSize = null;

    public MipsTranslator(MidCode midCode) {
        this.midCode = midCode;
        transIr();
    }

    private void transIr() {
        mipsInsList.add(new J(midCode.getMainFunc().getLabel()));
        Iterator<FuncFrame> funcIter = midCode.funcIter();
        while (funcIter.hasNext()) {
            FuncFrame func = funcIter.next();
            transFunc(func);
        }
        isMain = true;
        transFunc(midCode.getMainFunc());
    }

    private void transFunc(FuncFrame func) {
        allocatorList.add(new RegAllocator(func));
        mipsInsList.add(new Label("\n" + func.getLabel()));
        stackSize = func.addStackSize(0);   // 相当于getStackSize
        // 加载形参到寄存器
        List<Symbol> formatParams = func.getFormatParams();
        for (int i = 0; i < formatParams.size(); i++) {
            Symbol param = formatParams.get(i);
            if (i > 3) {
                if (param.getReg() != null) {
                    mipsInsList.add(new Lw(param.getReg(), -param.getOffset(), Reg.SP));
                }
            } else {
                if (param.getReg() != null) {
                    mipsInsList.add(new Add(param.getReg(), Reg.ZERO, Reg.A0 + i));
                } else {
                    mipsInsList.add(new Sw(Reg.A0 + i, -param.getOffset(), Reg.SP));
                }
            }
        }
        List<BasicIns> insList = func.insList();
        for (BasicIns basicIns : insList) {
            transIns(basicIns);
        }
    }

    private void transIns(BasicIns basicIns) {
        if (basicIns instanceof MidLabel) {
            transMidLabel((MidLabel) basicIns);
        } else if (basicIns instanceof BinaryOp) {
            transBinaryOp((BinaryOp) basicIns);
        } else if (basicIns instanceof UnaryOp) {
            transUnaryOp((UnaryOp) basicIns);
        } else if (basicIns instanceof Call) {
            transCall((Call) basicIns);
        } else if (basicIns instanceof GetInt) {
            transGetInt((GetInt) basicIns);
        } else if (basicIns instanceof PrintInt) {
            transPrintInt((PrintInt) basicIns);
        } else if (basicIns instanceof PrintStr) {
            transPrintStr((PrintStr) basicIns);
        } else if (basicIns instanceof Return) {
            transReturn((Return) basicIns);
        } else if (basicIns instanceof Branch) {
            transBranch((Branch) basicIns);
        } else if (basicIns instanceof Jump) {
            transJump((Jump) basicIns);
        } else if (basicIns instanceof MemOp) {
            transMemOp((MemOp) basicIns);
        } else if (basicIns instanceof Offset) {
            transOffset((Offset) basicIns);
        } else {
            System.out.println("illegal basic ins!");
            System.exit(8);
        }
    }

    private void transMidLabel(MidLabel midLabel) {
        mipsInsList.add(new Label(midLabel.getLabel()));
    }

    // stack ver, a1-a1(4-6)used to calculate
    private void transBinaryOp(BinaryOp binaryOp) {
        mipsInsList.add(new Comment(binaryOp.toString()));
        BinaryOp.Type op = binaryOp.getOp();
        Operand src1 = binaryOp.getSrc1();
        Operand src2 = binaryOp.getSrc2();
        MidVar dst = binaryOp.getDst();
        if (src1 instanceof Imm && src2 instanceof Imm) {
            binaryImmImmHelper(op, dst, ((Imm) src1).getVal(), ((Imm) src2).getVal());
        } else if (src1 instanceof MidVar && src2 instanceof Imm) {
            binaryVarImmHelper(op, dst, (MidVar) src1, ((Imm) src2).getVal());
        } else if (src1 instanceof Imm && src2 instanceof MidVar) {
            binaryImmVarHelper(op, dst, ((Imm) src1).getVal(), (MidVar) src2);
        } else if (src1 instanceof MidVar && src2 instanceof MidVar) {
            binaryVarVarHelper(op, dst, (MidVar) src1, (MidVar) src2);
        } else {
            throw new AssertionError("operand type error");
        }
    }

    private void transUnaryOp(UnaryOp unaryOp) {
        mipsInsList.add(new Comment(unaryOp.toString()));
        UnaryOp.Type op = unaryOp.getOp();
        Operand src = unaryOp.getSrc();
        MidVar dst = unaryOp.getDst();
        if (src instanceof Imm) {
            unaryImmHelper(op, dst, ((Imm) src).getVal());
        } else if (src instanceof MidVar) {
            unaryVarHelper(op, dst, (MidVar) src);
        } else {
            throw new AssertionError("operand type error");
        }
    }

    private void transCall(Call call) {
        Set<Integer> reserveSet = call.getLiveRegs();
        int movSize = stackSize + 4 + 4 * reserveSet.size();
        mipsInsList.add(new Comment(call.toString()));
        // store param
        FuncFrame func = call.getFunc();
        List<Operand> realParams = call.getRealParams();
        List<Symbol> formatParams = func.getFormatParams();
        for (int i = 0; i < realParams.size(); i++) {
            Operand realParam = realParams.get(i);
            Symbol formatParam = formatParams.get(i);
            if (i > 3) {
                loadRegHelper(realParam, TMP_R1);
                mipsInsList.add(new Sw(TMP_R1, -movSize - formatParam.getOffset(), Reg.SP));
            } else {
                loadRegHelper(realParam, Reg.A0 + i);
            }
        }
        // save current reg
        int bias = 4;
        for (Integer liveReg : call.getLiveRegs()) {
            mipsInsList.add(new Sw(liveReg, -movSize + bias, Reg.SP));
            bias += 4;
        }
        mipsInsList.add(new Sw(Reg.RA, -movSize, Reg.SP));
        mipsInsList.add(new Addi(Reg.SP, Reg.SP, -movSize));  // $ra stackSize + 4// jump and link
        mipsInsList.add(new Jal(func.getLabel()));
        // recover
        mipsInsList.add(new Addi(Reg.SP, Reg.SP, movSize));  // $ra stackSize + 4
        mipsInsList.add(new Lw(Reg.RA, -movSize, Reg.SP));
        bias = 4;
        for (Integer liveReg : call.getLiveRegs()) {
            mipsInsList.add(new Lw(liveReg, -movSize + bias, Reg.SP));
            bias += 4;
        }
        // recv ret val, 必须在恢复现场之后, 否则store时sp不对
        MidVar retVal = call.getRet();
        if (retVal != null) {
            storeRegHelper(retVal, Reg.V0);
        }
    }

    private void transGetInt(GetInt getInt) {
        mipsInsList.add(new Comment(getInt.toString()));
        MidVar var = getInt.getVar();
        mipsInsList.add(new Addi(Reg.V0, Reg.ZERO, 5));
        mipsInsList.add(new Syscall());
        if (var.getReg() != null) {
            mipsInsList.add(new Add(var.getReg(), Reg.ZERO, Reg.V0));
        } else {
            storeRegHelper(var, Reg.V0);
        }
    }

    private void transPrintInt(PrintInt printInt) {
        mipsInsList.add(new Comment(printInt.toString()));
        Operand src = printInt.getSrc();
        loadRegHelper(src, Reg.A0);
        mipsInsList.add(new Addi(Reg.V0, Reg.ZERO, 1));
        mipsInsList.add(new Syscall());
    }

    private void transPrintStr(PrintStr printStr) {
        mipsInsList.add(new Comment(printStr.toString()));
        String label = printStr.getLabel();
        mipsInsList.add(new La(Reg.A0, label));
        mipsInsList.add(new Addi(Reg.V0, Reg.ZERO, 4));
        mipsInsList.add(new Syscall());
    }

    private void transReturn(Return ret) {
        mipsInsList.add(new Comment(ret.toString()));
        Operand retVal = ret.getRetVal();
        if (isMain) {
            assert retVal instanceof Imm && ((Imm) retVal).getVal() == 0;
            mipsInsList.add(new Addi(Reg.V0, Reg.ZERO, 10));
            mipsInsList.add(new Syscall());
        } else {
            if (retVal != null) {
                loadRegHelper(retVal, Reg.V0);
            }
            mipsInsList.add(new Jr(Reg.RA));
        }
    }

    private void transBranch(Branch branch) {
        mipsInsList.add(new Comment(branch.toString()));
        Operand cond = branch.getCond();
        String labelTrue = branch.getLabelTrue().getLabel();
        String labelFalse = branch.getLabelFalse().getLabel();
        //if (branch.getType().equals(Branch.Type.BNEZ))
        loadRegHelper(cond, TMP_R1);
        mipsInsList.add(new Bnez(TMP_R1, labelTrue));
        mipsInsList.add(new J(labelFalse));
    }

    private void transJump(Jump jump) {
        mipsInsList.add(new J(jump.getTargetLabel().getLabel()));
    }

    private void transMemOp(MemOp memOp) {
        mipsInsList.add(new Comment(memOp.toString()));
        MidVar pointer = memOp.getPointer();
        Operand value = memOp.getValue();
        MemOp.Type type = memOp.getOp();
        loadRegHelper(pointer, TMP_R1);
        if (type.equals(MemOp.Type.LOAD)) {
            assert value instanceof MidVar;
            allocStack((MidVar) value); // load 可能初次赋值
            mipsInsList.add(new Lw(TMP_R2, 0, TMP_R1));
            storeRegHelper((MidVar) value, TMP_R2);
        } else {
            loadRegHelper(value, TMP_R2);
            mipsInsList.add(new Sw(TMP_R2, 0, TMP_R1));
        }
    }

    private void transOffset(Offset offset) {
        mipsInsList.add(new Comment(offset.toString()));
        MidVar dst = offset.getDst();
        MidVar base = offset.getBase();
        Operand offsetVal = offset.getOffsetVal();
        if (base.getRefType().equals(Operand.RefType.ARRAY)) {
            assert base instanceof Symbol;
            Symbol symBase = (Symbol) base;
            loadRegHelper(offsetVal, TMP_R2);
            if (symBase.isGlobal()) {
                mipsInsList.add(new La(TMP_R1, symBase.getLabel(), TMP_R2));
            } else {
                mipsInsList.add(new Addi(TMP_R1, Reg.SP, -symBase.getOffset()));
                mipsInsList.add(new Add(TMP_R1, TMP_R1, TMP_R2));
            }
        } else {
            loadRegHelper(base, TMP_R2);
            loadRegHelper(offsetVal, TMP_R1);
            mipsInsList.add(new Add(TMP_R1, TMP_R2, TMP_R1));
        }
        storeRegHelper(dst, TMP_R1);
    }

    // util
    private void loadRegHelper(Operand operand, int reg) {
        // reg <- operand
        if (operand instanceof Symbol && ((Symbol) operand).isGlobal()) {
            Symbol symbol = (Symbol) operand;
            mipsInsList.add(new Lw(reg, symbol.getLabel()));
            return;
        }
        if (operand instanceof Imm) {
            mipsInsList.add(new Addi(reg, Reg.ZERO, ((Imm) operand).getVal()));
        } else if (operand instanceof MidVar) {
            MidVar midVar = (MidVar) operand;
            if (midVar.getReg() != null) {
                mipsInsList.add(new Add(reg, Reg.ZERO, midVar.getReg()));
            } else {
                assert midVar.getOffset() != null;
                mipsInsList.add(new Lw(reg, -midVar.getOffset(), Reg.SP));
            }
        } else {
            throw new AssertionError("operand type error");
        }

    }

    private void storeRegHelper(MidVar midVar, int reg) {
        // midvar <- reg
        if (midVar instanceof Symbol && ((Symbol) midVar).isGlobal()) {
            Symbol symbol = (Symbol) midVar;
            mipsInsList.add(new Sw(reg, symbol.getLabel()));
        } else {
            if (midVar.getReg() != null) {
                mipsInsList.add(new Add(midVar.getReg(), Reg.ZERO, reg));
            } else {
                if (midVar.getOffset() == null) {
                    allocStack(midVar);
                }
                mipsInsList.add(new Sw(reg, -midVar.getOffset(), Reg.SP));
            }
        }
    }

    private void unaryImmHelper(UnaryOp.Type op, MidVar dst, int imm) {
        int regDst = dst.getReg() == null ? TMP_R1 : dst.getReg();
        if (op.equals(UnaryOp.Type.MOV)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, imm));
        } else if (op.equals(UnaryOp.Type.NEG)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, -imm));
        } else if (op.equals(UnaryOp.Type.NOT)) {
            mipsInsList.add(new Seqi(regDst, Reg.ZERO, imm));
        } else {
            throw new AssertionError("wrong binaryOp Type");
        }
        // store stack
        if (dst.getReg() == null) {
            storeRegHelper(dst, regDst);
        }
    }

    private void unaryVarHelper(UnaryOp.Type op, MidVar dst, MidVar src) {
        int regDst = dst.getReg() == null ? TMP_R1 : dst.getReg();
        int regSrc = src.getReg() == null ? TMP_R1 : src.getReg();
        // load stack
        if (src.getReg() == null) {
            loadRegHelper(src, regSrc);
        }
        if (op.equals(UnaryOp.Type.MOV)) {
            mipsInsList.add(new Add(regDst, Reg.ZERO, regSrc));
        } else if (op.equals(UnaryOp.Type.NEG)) {
            mipsInsList.add(new Sub(regDst, Reg.ZERO, regSrc));
        } else if (op.equals(UnaryOp.Type.NOT)) {
            mipsInsList.add(new Seq(regDst, Reg.ZERO, regSrc));
        } else {
            throw new AssertionError("wrong binaryOp Type");
        }
        // store stack
        if (dst.getReg() == null) {
            storeRegHelper(dst, regDst);
        }
    }

    private void binaryImmImmHelper(BinaryOp.Type op, MidVar dst, int imm1, int imm2) {
        int regDst = dst.getReg() == null ? TMP_R1 : dst.getReg();
        if (op.equals(BinaryOp.Type.ADD)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, imm1 + imm2));
        } else if (op.equals(BinaryOp.Type.SUB)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, imm1 - imm2));
        } else if (op.equals(BinaryOp.Type.MUL)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, imm1 * imm2));
        } else if (op.equals(BinaryOp.Type.DIV)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, imm1 / imm2));
        } else if (op.equals(BinaryOp.Type.MOD)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, imm1 % imm2));
        } else if (op.equals(BinaryOp.Type.SGT)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, (imm1 > imm2) ? 1 : 0));
        } else if (op.equals(BinaryOp.Type.SGE)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, (imm1 >= imm2) ? 1 : 0));
        } else if (op.equals(BinaryOp.Type.SLT)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, (imm1 < imm2) ? 1 : 0));
        } else if (op.equals(BinaryOp.Type.SLE)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, (imm1 <= imm2) ? 1 : 0));
        } else if (op.equals(BinaryOp.Type.SEQ)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, (imm1 == imm2) ? 1 : 0));
        } else if (op.equals(BinaryOp.Type.SNE)) {
            mipsInsList.add(new Addi(regDst, Reg.ZERO, (imm1 != imm2) ? 1 : 0));
        } else {
            throw new AssertionError("wrong binaryOp Type");
        }
        // store stack
        if (dst.getReg() == null) {
            storeRegHelper(dst, regDst);
        }
    }

    private void binaryVarImmHelper(BinaryOp.Type op, MidVar dst, MidVar src1, int imm2) {
        int regDst = dst.getReg() == null ? TMP_R1 : dst.getReg();
        int regSrc1 = src1.getReg() == null ? TMP_R1 : src1.getReg();
        // load stack
        if (src1.getReg() == null) {
            loadRegHelper(src1, regSrc1);
        }
        if (op.equals(BinaryOp.Type.ADD)) {
            mipsInsList.add(new Addi(regDst, regSrc1, imm2));
        } else if (op.equals(BinaryOp.Type.SUB)) {
            mipsInsList.add(new Addi(regDst, regSrc1, -imm2));
        } else if (op.equals(BinaryOp.Type.MUL)) {
            weakenMult(regDst, regSrc1, imm2);
        } else if (op.equals(BinaryOp.Type.DIV)) {
            weakenDiv(regDst, regSrc1, imm2);
        } else if (op.equals(BinaryOp.Type.MOD)) {
            int regSrc2 = TMP_R2;
            mipsInsList.add(new Addi(regSrc2, Reg.ZERO, imm2));
            mipsInsList.add(new Div(regSrc1, regSrc2));
            mipsInsList.add(new Mfhi(regDst));
        } else if (op.equals(BinaryOp.Type.SGT)) {
            mipsInsList.add(new Sgti(regDst, regSrc1, imm2));
        } else if (op.equals(BinaryOp.Type.SGE)) {
            mipsInsList.add(new Sgei(regDst, regSrc1, imm2));
        } else if (op.equals(BinaryOp.Type.SLT)) {
            if (imm2 > 32767) {
                mipsInsList.add(new Addi(TMP_R1, Reg.ZERO, imm2));
                mipsInsList.add(new Slt(regDst, regSrc1, TMP_R1));
            } else {
                mipsInsList.add(new Slti(regDst, regSrc1, imm2));
            }
        } else if (op.equals(BinaryOp.Type.SLE)) {
            mipsInsList.add(new Slei(regDst, regSrc1, imm2));
        } else if (op.equals(BinaryOp.Type.SEQ)) {
            mipsInsList.add(new Seqi(regDst, regSrc1, imm2));
        } else if (op.equals(BinaryOp.Type.SNE)) {
            mipsInsList.add(new Snei(regDst, regSrc1, imm2));
        } else {
            throw new AssertionError("wrong binaryOp Type");
        }
        if (dst.getReg() == null) {
            storeRegHelper(dst, TMP_R1);
        }
    }

    private void binaryImmVarHelper(BinaryOp.Type op, MidVar dst, int imm1, MidVar src2) {
        int regDst = dst.getReg() == null ? TMP_R1 : dst.getReg();
        int regSrc2 = src2.getReg() == null ? TMP_R1 : src2.getReg();
        // load stack
        if (src2.getReg() == null) {
            loadRegHelper(src2, regSrc2);
        }
        if (op.equals(BinaryOp.Type.ADD)) {
            mipsInsList.add(new Addi(regDst, regSrc2, imm1));
        } else if (op.equals(BinaryOp.Type.SUB)) {
            mipsInsList.add(new Sub(regDst, Reg.ZERO, regSrc2));
            mipsInsList.add(new Addi(regDst, regDst, imm1));
        } else if (op.equals(BinaryOp.Type.MUL)) {
            int regSrc1 = TMP_R2;
            mipsInsList.add(new Addi(regSrc1, Reg.ZERO, imm1));
            mipsInsList.add(new Mul(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.DIV)) {
            int regSrc1 = TMP_R2;
            mipsInsList.add(new Addi(regSrc1, Reg.ZERO, imm1));
            mipsInsList.add(new Div(regSrc1, regSrc2));
            mipsInsList.add(new Mflo(regDst));
        } else if (op.equals(BinaryOp.Type.MOD)) {
            int regSrc1 = TMP_R2;
            mipsInsList.add(new Addi(regSrc1, Reg.ZERO, imm1));
            mipsInsList.add(new Div(regSrc1, regSrc2));
            mipsInsList.add(new Mfhi(regDst));
        } else if (op.equals(BinaryOp.Type.SGT)) {
            if (imm1 > 32767) {
                mipsInsList.add(new Addi(TMP_R1, Reg.ZERO, imm1));
                mipsInsList.add(new Slt(regDst, regSrc2, TMP_R1));
            } else {
                mipsInsList.add(new Slti(regDst, regSrc2, imm1));
            }
            mipsInsList.add(new Slti(regDst, regSrc2, imm1));
        } else if (op.equals(BinaryOp.Type.SGE)) {
            mipsInsList.add(new Slei(regDst, regSrc2, imm1));
        } else if (op.equals(BinaryOp.Type.SLT)) {
            mipsInsList.add(new Sgti(regDst, regSrc2, imm1));
        } else if (op.equals(BinaryOp.Type.SLE)) {
            mipsInsList.add(new Sgei(regDst, regSrc2, imm1));
        } else if (op.equals(BinaryOp.Type.SEQ)) {
            mipsInsList.add(new Seqi(regDst, regSrc2, imm1));
        } else if (op.equals(BinaryOp.Type.SNE)) {
            mipsInsList.add(new Snei(regDst, regSrc2, imm1));
        } else {
            throw new AssertionError("wrong binaryOp Type");
        }
        if (dst.getReg() == null) {
            storeRegHelper(dst, TMP_R1);
        }
    }

    private void binaryVarVarHelper(BinaryOp.Type op, MidVar dst, MidVar src1, MidVar src2) {
        int regDst = dst.getReg() == null ? TMP_R1 : dst.getReg();
        int regSrc1 = src1.getReg() == null ? TMP_R1 : src1.getReg();
        int regSrc2 = src2.getReg() == null ? TMP_R2 : src2.getReg();
        // load stack
        if (src1.getReg() == null) {
            loadRegHelper(src1, regSrc1);
        }
        if (src2.getReg() == null) {
            loadRegHelper(src2, regSrc2);
        }
        if (op.equals(BinaryOp.Type.ADD)) {
            mipsInsList.add(new Add(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.SUB)) {
            mipsInsList.add(new Sub(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.MUL)) {
            mipsInsList.add(new Mul(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.DIV)) {
            mipsInsList.add(new Div(regSrc1, regSrc2));
            mipsInsList.add(new Mflo(regDst));
        } else if (op.equals(BinaryOp.Type.MOD)) {
            mipsInsList.add(new Div(regSrc1, regSrc2));
            mipsInsList.add(new Mfhi(regDst));
        } else if (op.equals(BinaryOp.Type.SGT)) {
            mipsInsList.add(new Sgt(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.SGE)) {
            mipsInsList.add(new Sge(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.SLT)) {
            mipsInsList.add(new Slt(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.SLE)) {
            mipsInsList.add(new Sle(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.SEQ)) {
            mipsInsList.add(new Seq(regDst, regSrc1, regSrc2));
        } else if (op.equals(BinaryOp.Type.SNE)) {
            mipsInsList.add(new Sne(regDst, regSrc1, regSrc2));
        }
        if (dst.getReg() == null) {
            storeRegHelper(dst, TMP_R1);
        }
    }


    private void allocStack(MidVar midVar) {
        if (midVar.getOffset() == null) {
            stackSize += 4;
            midVar.setStackOffset(stackSize);
        }
    }

    // output
    public void outputMips(PrintStream ps, boolean execute) {
        if (!execute) {
            return;
        }
        outputGlobal(ps);
        outputIns(ps);
    }

    private void outputGlobal(PrintStream ps) {
        ps.println(".data");
        // global var
        Iterator<Symbol> globalSyms = midCode.symIter();
        while (globalSyms.hasNext()) {
            Symbol symbol = globalSyms.next();
            List<Integer> initList = symbol.getInitVal();
            if (!initList.isEmpty()) {
                String initStr = initList.stream().map(x -> Integer.toString(x)).reduce((x, y) -> x + " " + y).orElse("");
                ps.println("\t" + symbol.getLabel() + ": .word " + initStr);
            } else {
                ps.println("\t" + symbol.getLabel() + ": .space " + symbol.getSize());
            }

        }
        // constant str
        Iterator<String> strLabels = midCode.strLabelIter();
        while (strLabels.hasNext()) {
            String label = strLabels.next();
            ps.println("\t" + label + ": .asciiz \"" + midCode.getStr(label) + "\"");
        }
    }

    private void outputIns(PrintStream ps) {
        ps.println(".text");
        for (MipsIns mipsIns : mipsInsList) {
            ps.println("\t" + mipsIns.toString());
        }
    }

    public void outputRegInfo(PrintStream ps) {
        for (RegAllocator allocator : allocatorList) {
            allocator.output(ps);
        }
    }

    // optimize
    private void weakenMult(int regDst, int regSrc, int imm) {
        Map<Integer, Integer> expMap = new HashMap<>();
        for (int i = 0; i < 31; i++) {
            expMap.put(1 << i, i);
            expMap.put(-(1 << i), i);
        }
        if (imm == 0) {
            mipsInsList.add(new Add(regDst, 0, 0));
        } else if (imm == 1) {
            mipsInsList.add(new Add(regDst, regSrc, 0));
        } else if (imm == -1) {
            mipsInsList.add(new Sub(regDst, regSrc, 0));
        } else if (expMap.containsKey(imm)) {
            if (imm > 0) {
                mipsInsList.add(new Sll(regDst, regSrc, expMap.get(imm)));
            } else {
                mipsInsList.add(new Sll(regDst, regSrc, expMap.get(imm)));
                mipsInsList.add(new Sub(regDst, Reg.ZERO, regSrc));
            }
        } else {
            mipsInsList.add(new Addi(TMP_R2, Reg.ZERO, imm));
            mipsInsList.add(new Mul(regDst, regSrc, TMP_R2));
        }
    }

    private void weakenDiv(int regDst, int regSrc, int imm) {
        Map<Integer, Integer> expMap = new HashMap<>();
        for (int i = 0; i < 31; i++) {
            expMap.put(1 << i, i);
            expMap.put(-(1 << i), i);
        }
        if (imm == 0) {
            throw new AssertionError("div by zero!");
        } else if (imm == 1) {
            mipsInsList.add(new Add(regDst, regSrc, 0));
        } else if (imm == -1) {
            mipsInsList.add(new Sub(regDst, regSrc, 0));
        }  else if (expMap.containsKey(imm)) {
            if (imm > 0) {
                mipsInsList.add(new Srl(regDst, regSrc, expMap.get(imm)));
            } else {
                mipsInsList.add(new Srl(regDst, regSrc, expMap.get(imm)));
                mipsInsList.add(new Sub(regDst, Reg.ZERO, regSrc));
            }
        } else {
            mipsInsList.add(new Addi(TMP_R2, Reg.ZERO, imm));
            mipsInsList.add(new Div(regSrc, TMP_R2));
            mipsInsList.add(new Mflo(regDst));
        }
    }

    private void weakenMod(int regDst, int regSrc, int imm) {
        Map<Integer, Integer> expMap = new HashMap<>();
        for (int i = 0; i < 31; i++) {
            expMap.put(1 << i, i);
            expMap.put(-(1 << i), i);
        }
        if (imm == 0) {
            throw new AssertionError("mod by zero!");
        } else if (imm == 1 || imm == -1) {
            mipsInsList.add(new Add(regDst, 0, 0));
        }  else if (expMap.containsKey(imm)) {
            if (imm < 0) {
                imm = -imm;
            }
            mipsInsList.add(new Srl(regDst, regSrc, expMap.get(imm)));
            mipsInsList.add(new Sll(regDst, regDst, expMap.get(imm)));
            mipsInsList.add(new Sub(regDst, regSrc, regDst));
        } else {
            mipsInsList.add(new Addi(TMP_R2, Reg.ZERO, imm));
            mipsInsList.add(new Div(regSrc, TMP_R2));
            mipsInsList.add(new Mfhi(regDst));
        }
    }
}
