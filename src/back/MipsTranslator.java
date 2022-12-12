package back;

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
    // info
    private final MidCode midCode;
    private final List<MipsIns> mipsInsList = new ArrayList<>();

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
        new RegAllocator(func); // 分配寄存器
        mipsInsList.add(new Label("\n" + func.getLabel()));
        stackSize = func.addStackSize(0);   // 相当于getStackSize
        Iterator<BasicIns> insIter = func.iterIns();
        while (insIter.hasNext()) {
            BasicIns basicIns = insIter.next();
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
        loadRegHelper(src1, Reg.A2);
        loadRegHelper(src2, Reg.A3);
        BinaryInsHelper(op, Reg.A1, Reg.A2, Reg.A3);
        storeRegHelper(dst, Reg.A1);
    }

    private void transUnaryOp(UnaryOp unaryOp) {
        mipsInsList.add(new Comment(unaryOp.toString()));
        UnaryOp.Type op = unaryOp.getOp();
        Operand src = unaryOp.getSrc();
        MidVar dst = unaryOp.getDst();
        loadRegHelper(src, Reg.A2);
        UnaryInsHelper(op, Reg.A1, Reg.A2);
        storeRegHelper(dst, Reg.A1);
    }

    private void transCall(Call call) {
        int movSize = stackSize + 4;
        mipsInsList.add(new Comment(call.toString()));
        // store param
        FuncFrame func = call.getFunc();
        Iterator<Operand> realParams = call.iterRealParam();
        Iterator<Symbol> formatParams = func.iterFormatParam();
        while (realParams.hasNext()) {
            Operand realParam = realParams.next();
            Symbol formatParam = formatParams.next();
            loadRegHelper(realParam, Reg.A1);
            mipsInsList.add(new Sw(Reg.A1, -movSize-formatParam.getOffset(), Reg.SP));
        }
        // save current reg
        mipsInsList.add(new Sw(Reg.RA, -movSize, Reg.SP));
        mipsInsList.add(new Addi(Reg.SP, Reg.SP, -movSize));  // $ra stackSize + 4// jump and link
        mipsInsList.add(new Jal(func.getLabel()));
        // recover
        mipsInsList.add(new Addi(Reg.SP, Reg.SP, movSize));  // $ra stackSize + 4
        mipsInsList.add(new Lw(Reg.RA, -movSize, Reg.SP));
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
        storeRegHelper(var, Reg.V0);
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
        loadRegHelper(cond, Reg.A0);
        mipsInsList.add(new Bnez(Reg.A0, labelTrue));
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
        loadRegHelper(pointer, Reg.A1);
        if (type.equals(MemOp.Type.LOAD)) {
            assert value instanceof MidVar;
            allocStack((MidVar) value); // load 可能初次赋值
            mipsInsList.add(new Lw(Reg.A2, 0, Reg.A1));
            storeRegHelper((MidVar) value, Reg.A2);
        } else {
            loadRegHelper(value, Reg.A2);
            mipsInsList.add(new Sw(Reg.A2, 0, Reg.A1));
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
            loadRegHelper(offsetVal, Reg.A2);
            if (symBase.isGlobal()) {
                mipsInsList.add(new La(Reg.A1, symBase.getLabel(), Reg.A2));
            } else {
                mipsInsList.add(new Addi(Reg.A1, Reg.SP, -symBase.getOffset()));
                mipsInsList.add(new Add(Reg.A1, Reg.A1, Reg.A2));
            }
        } else {
            loadRegHelper(base, Reg.A2);
            loadRegHelper(offsetVal, Reg.A3);
            mipsInsList.add(new Add(Reg.A1, Reg.A2, Reg.A3));
        }
        storeRegHelper(dst, Reg.A1);
    }

    // util
    private void loadRegHelper(Operand operand, int reg) {
        if (operand instanceof Imm) {
            mipsInsList.add(new Addi(reg, Reg.ZERO,((Imm) operand).getVal()));
        } else {
            MidVar midVar = (MidVar) operand;
            assert midVar.getOffset() != null;
            if (midVar instanceof Symbol && ((Symbol) midVar).isGlobal()) {
                Symbol symbol = (Symbol) midVar;
                mipsInsList.add(new Lw(reg, symbol.getLabel()));
            } else {
                mipsInsList.add(new Lw(reg, -midVar.getOffset(), Reg.SP));
            }
        }
    }

    private void storeRegHelper(MidVar midVar, int reg) {
        if (midVar.getOffset() == null) {
            allocStack(midVar);
        }
        if (midVar instanceof Symbol && ((Symbol) midVar).isGlobal()) {
            Symbol symbol = (Symbol) midVar;
            mipsInsList.add(new Sw(reg, symbol.getLabel()));
        } else {
            mipsInsList.add(new Sw(reg, -midVar.getOffset(), Reg.SP));
        }
    }

    private void BinaryInsHelper(BinaryOp.Type type, int dst, int src1, int src2) {
        if (type.equals(BinaryOp.Type.ADD)) {
            mipsInsList.add(new Add(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.SUB)) {
            mipsInsList.add(new Sub(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.MUL)) {
            mipsInsList.add(new Mult(src1, src2));
            mipsInsList.add(new Mflo(dst));
        } else if (type.equals(BinaryOp.Type.DIV)) {
            mipsInsList.add(new Div(src1, src2));
            mipsInsList.add(new Mflo(dst));
        } else if (type.equals(BinaryOp.Type.MOD)) {
            mipsInsList.add(new Div(src1, src2));
            mipsInsList.add(new Mfhi(dst));
        } else if (type.equals(BinaryOp.Type.SGT)) {
            mipsInsList.add(new Sgt(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.SGE)) {
            mipsInsList.add(new Sge(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.SLT)) {
            mipsInsList.add(new Slt(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.SLE)) {
            mipsInsList.add(new Sle(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.SEQ)) {
            mipsInsList.add(new Seq(dst, src1, src2));
        } else if (type.equals(BinaryOp.Type.SNE)) {
            mipsInsList.add(new Sne(dst, src1, src2));
        } else {
            // TODO[11], other op
            System.exit(11);
        }
    }

    private void UnaryInsHelper(UnaryOp.Type type, int dst, int src) {
        if (type.equals(UnaryOp.Type.MOV)) {
            mipsInsList.add(new Add(dst, src, Reg.ZERO));
        } else if (type.equals(UnaryOp.Type.NEG)) {
            mipsInsList.add(new Sub(dst, Reg.ZERO, src));
        } else if (type.equals(UnaryOp.Type.NOT)) {
            mipsInsList.add(new Seq(dst, src, Reg.ZERO));
        } else {
            // TODO[12], other op
            System.exit(12);
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
}
