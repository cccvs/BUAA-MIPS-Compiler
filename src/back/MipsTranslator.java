package back;

import back.ins.*;
import back.special.Comment;
import back.special.Label;
import back.special.MipsIns;
import back.special.Syscall;
import ir.MidCode;
import ir.code.*;
import ir.frame.BasicBlock;
import ir.frame.FuncFrame;
import ir.operand.Imm;
import ir.operand.Operand;
import ir.operand.Symbol;
import ir.operand.MidVar;

import java.io.PrintStream;
import java.util.*;

public class MipsTranslator {
    private final RegAllocator regAllocator = new RegAllocator();

    // info
    private final MidCode midCode;
    private final List<MipsIns> mipsInsList = new ArrayList<>();

    // current
    private boolean isMain = false; // 标记当前函数是否为main函数，特殊处理return
    private Integer stackSize = null;
    private Queue<MidVar> regBuffer = new LinkedList<>();

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
        mipsInsList.add(new Label("\n" + func.getLabel()));
        stackSize = func.addStackSize(0);   // 相当于getStackSize
        BasicBlock basicBlock = func.getBody();
        transBlock(basicBlock);
    }

    private void transBlock(BasicBlock basicBlock) {
        mipsInsList.add(new Label(basicBlock.getLabel()));
        Iterator<BasicIns> insIter = basicBlock.iterIns();
        while (insIter.hasNext()) {
            BasicIns basicIns = insIter.next();
            transIns(basicIns);
        }
    }

    private void transIns(BasicIns basicIns) {
        if (basicIns instanceof BasicBlock) {
            transBlock((BasicBlock) basicIns);
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
        } else {    // TODO[8] branch part
            System.out.println("illegal file for hw1!");
            System.exit(8);
        }
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
        mipsInsList.add(new Comment(call.toString()));
        // save current reg
        int movSize = stackSize + 4;
        mipsInsList.add(new Sw(Reg.RA, -movSize, Reg.SP));
        mipsInsList.add(new Addi(Reg.SP, Reg.SP, -movSize));  // $ra stackSize + 4
        // store param
        FuncFrame func = call.getFunc();
        Iterator<Operand> realParams = call.iterRealParam();
        Iterator<Symbol> formatParams = func.iterFormatParam();
        while (realParams.hasNext()) {
            Operand realParam = realParams.next();
            Symbol formatParam = formatParams.next();
            loadRegHelper(realParam, Reg.A1);
            mipsInsList.add(new Sw(Reg.A1, -formatParam.getOffset(), Reg.SP));
        }
        // jump and link
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
        Symbol var = getInt.getVar();
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

    // util
    private void loadRegHelper(Operand operand, int reg) {
        if (operand instanceof Imm) {
            mipsInsList.add(new Addi(reg, Reg.ZERO,((Imm) operand).getVal()));
        } else {
            MidVar midVar = (MidVar) operand;
            assert midVar.getOffset() != null;
            mipsInsList.add(new Lw(reg, -midVar.getOffset(), Reg.SP));
        }
    }

    private void storeRegHelper(MidVar midVar, int reg) {
        if (midVar.getOffset() == null) {
            allocStack(midVar);
        }
        mipsInsList.add(new Sw(reg, -midVar.getOffset(), Reg.SP));
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

    private void allocReg(MidVar operand, boolean isSrc) {
        if (regAllocator.getReg(operand) == null) {
            if (!regAllocator.hasFreeReg()) {
                MidVar replaced = replaceAlgo();
                allocStack(replaced);       // 如果已经分配栈，则不操作
                mipsInsList.add(new Sw(regAllocator.getReg(replaced), -replaced.getOffset(), Reg.SP));
                regAllocator.freeReg(replaced);
            }
            regAllocator.allocReg(operand);
            regBuffer.add(operand);
            if (isSrc) {
                mipsInsList.add(new Lw(regAllocator.getReg(operand), -operand.getOffset(), Reg.SP));
            }
        }
    }

    private MidVar replaceAlgo() {
        return regBuffer.remove();
    }
    // output
    public void outputMips(PrintStream ps) {
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
            String initStr = initList.stream().map(x -> Integer.toString(x)).reduce((x, y) -> x + " " + y).orElse("");
            ps.println("\t" + symbol.getIdent() + ": .word " + initStr);
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
