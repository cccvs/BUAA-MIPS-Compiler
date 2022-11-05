package back;

import back.ins.*;
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
        Iterator<FuncFrame> funcIter =  midCode.funcIter();
        while (funcIter.hasNext()) {
            FuncFrame func = funcIter.next();
            transFunc(func);
        }
        isMain = true;
        transFunc(midCode.getMainFunc());
    }

    private void transFunc(FuncFrame func) {
        stackSize = func.addStackSize(0);   // 相当于getStackSize
        BasicBlock basicBlock = func.getBody();
        transBlock(basicBlock);
    }

    private void transBlock(BasicBlock basicBlock) {
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

        } else if (basicIns instanceof Call) {

        } else if (basicIns instanceof GetInt) {

        } else if (basicIns instanceof PrintInt) {

        } else if (basicIns instanceof PrintStr) {

        } else if (basicIns instanceof Return) {

        } else {    // TODO[8] branch part
            System.out.println("illegal file for hw1!");
            System.exit(8);
        }
    }

    private void transBinaryOp(BinaryOp binaryOp) {
        BinaryOp.Type op = binaryOp.getOp();
        Operand src1 = binaryOp.getSrc1();
        Operand src2 = binaryOp.getSrc2();
        MidVar midVar = binaryOp.getDst();
        if (src1 instanceof Imm && src2 instanceof Imm) {

        } else if (!(src1 instanceof Imm) && !(src2 instanceof Imm)) {

        } else {

        }
    }

    private void transUnaryOp(UnaryOp unaryOp) {

    }

    private void transCall(Call call) {
        FuncFrame func = call.getFunc();
        mipsInsList.add(new Addi(29, 29, -stackSize));
        Iterator<Symbol> params = func.iterParam();
        while (params.hasNext()) {
            Symbol param = params.next();
            // mipsInsList.add(new Lw())
        }
        mipsInsList.add(new Jal(func.getLabel()));
        stackSize = func.addStackSize(0);
    }


    // util
    private void allocReg(MidVar operand, boolean isSrc) {
        assert operand instanceof MidVar || operand instanceof Symbol;
        if (regAllocator.getReg(operand) == null) {
            if (!regAllocator.hasFreeReg()) {
                MidVar replaced = replaceAlgo();
                allocStack(replaced);       // 如果已经分配栈，则不操作
                mipsInsList.add(new Sw(regAllocator.getReg(replaced), -replaced.getOffset(), 29));
                regAllocator.freeReg(replaced);
            }
            regAllocator.allocReg(operand);
            regBuffer.add(operand);
            mipsInsList.add(new Lw(regAllocator.getReg(operand), -operand.getOffset(), 29));
        }
    }

    private MidVar replaceAlgo() {
        return regBuffer.remove();
    }

    private void allocStack(MidVar midVar) {
        if (midVar.getOffset() == null) {
            stackSize += 4;
            midVar.setStackOffset(stackSize);
        }
    }

    // output
    public void outputMips(PrintStream ps) {
        outputGlobal(ps);
        outputIns(ps);
    }

    private void outputGlobal(PrintStream ps) {
        ps.println(".data");
        // global var
        Iterator<Symbol> globalSyms =  midCode.symIter();
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
    }
}
