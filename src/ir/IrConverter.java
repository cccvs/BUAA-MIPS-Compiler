package ir;

import ast.CompUnitNode;
import ast.decl.DeclNode;
import ast.decl.DefNode;
import ast.exp.*;
import ast.func.FuncDefNode;
import ast.func.FuncFParamNode;
import ast.stmt.*;
import ir.code.*;
import ir.frame.BasicBlock;
import ir.frame.FuncFrame;
import ir.frame.SymTab;
import ir.operand.Imm;
import ir.operand.Operand;
import ir.operand.Symbol;
import ir.operand.TmpVar;
import util.Constant;
import util.TkType;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IrConverter {
    private static List<Symbol> globalSyms = new ArrayList<>();
    private static List<String> globalStrLabels = new ArrayList<>();
    private static MidCode midCode = new MidCode();

    // current info
    private static SymTab curTab = new SymTab();   // 初始化为全局符号表
    private static FuncFrame curFunc = null;
    private static BasicBlock curBlock = null;

    // PrintStream
    private PrintStream ps;

    public IrConverter(CompUnitNode compUnitNode) throws FileNotFoundException {
        convCompUnit(compUnitNode);
        ps = new PrintStream(Constant.MID_CODE);
        outputMidCode();
        // testGlobal();
    }

    public void convCompUnit(CompUnitNode compUnitNode) {
        // decls
        Iterator<DeclNode> globalDecls = compUnitNode.getDeclIter();
        while (globalDecls.hasNext()) {
            DeclNode decl = globalDecls.next();
            convDecl(decl);
        }
        // 将函数调用添加到funcTab中，可以应对a call b, b call a的情况
        Iterator<FuncDefNode> funcDefs = compUnitNode.getFuncIter();
        while (funcDefs.hasNext()) {
            FuncDefNode funcDefNode = funcDefs.next();
            MidCode.putFunc(new FuncFrame(funcDefNode.getIdent(), funcDefNode.getFuncType()));
        }
        FuncDefNode mainFunc = compUnitNode.getMainFuncDefNode();
        MidCode.setMainFunc(new FuncFrame(mainFunc.getIdent(), mainFunc.getFuncType()));
        // fill in params/body info
        funcDefs = compUnitNode.getFuncIter();
        while (funcDefs.hasNext()) {
            FuncDefNode funcDef = funcDefs.next();
            convFunc(funcDef, false);
        }
        mainFunc = compUnitNode.getMainFuncDefNode();
        convFunc(mainFunc, true);
    }

    // decl part
    private void convDecl(DeclNode declNode) {
        Iterator<DefNode> defs = declNode.defIter();
        while (defs.hasNext()) {
            DefNode def = defs.next();
            convDef(def);
        }
    }

    private void convDef(DefNode defNode) {
        // construct symbol
        boolean isGlobal = curTab.isGlobal();
        Symbol symbol = new Symbol(defNode, isGlobal);  // create new symbol
        putSymbolAndUpdateStack(symbol);
        if (isGlobal) {
            globalSyms.add(symbol);
        }
    }

    // func part
    private void convFunc(FuncDefNode funcDefNode, boolean isMain) {
        curFunc = isMain ? MidCode.getMainFunc() : MidCode.getFunc(funcDefNode.getIdent());
        // params part
        Iterator<FuncFParamNode> paramIter = funcDefNode.paramIter();
        while (paramIter.hasNext()) {
            FuncFParamNode paramNode = paramIter.next();
            Symbol param = new Symbol(paramNode);
            curFunc.addParam(param);
        }
        // block and symTab part
        BlockNode block = funcDefNode.getBlock();
        BasicBlock funcBlock = convBlock(block);
        curFunc.setBody(funcBlock);
        curFunc = null;
    }

    // stmt part
    private BasicBlock convBlock(BlockNode blockNode) {
        boolean isFuncBlock = curTab.isGlobal() && curBlock == null;
        boolean isOtherBlock = !curTab.isGlobal() && curBlock != null;
        assert isFuncBlock || isOtherBlock;
        curTab = new SymTab(curTab);
        curBlock = new BasicBlock();
        // fill params into symbol table
        if (isFuncBlock) {
            Iterator<Symbol> paramIter = curFunc.iterParam();
            while (paramIter.hasNext()) {
                Symbol param = paramIter.next();
                putSymbolAndUpdateStack(param);
            }
        }
        // analyze block item
        Iterator<BlockItemNode> blockItems = blockNode.iterBlockItem();
        while (blockItems.hasNext()) {
            BlockItemNode blockItem = blockItems.next();
            if (blockItem instanceof StmtNode) {
                convStmt((StmtNode) blockItem);
            } else if (blockItem instanceof DeclNode) {
                convDecl((DeclNode) blockItem);
            }
        }
        // recover
        BasicBlock basicBlock = curBlock;
        curTab = curTab.prev();
        curBlock = curBlock.prev();
        return basicBlock;
    }

    private void convStmt(StmtNode stmtNode) {
        if (stmtNode instanceof BlockNode) {
            BasicBlock basicBlock = convBlock((BlockNode) stmtNode);
            curBlock.append(basicBlock);
        } else if (stmtNode instanceof AssignNode) {
            convAssign((AssignNode) stmtNode);
        } else if (stmtNode instanceof PrintfNode) {
            convPrintf((PrintfNode) stmtNode);
        } else if (stmtNode instanceof ReturnNode) {
            convReturn((ReturnNode) stmtNode);
        } else if (stmtNode instanceof ExpNode) {
            convExp((ExpNode) stmtNode);
        } else { // TODO[7] branch part
            System.out.println("illegal file for hw1!");
            System.exit(5);
        }
    }

    private void convAssign(AssignNode assignNode) {
        LValNode leftVal = assignNode.getLeftVal();
        ExpNode exp = assignNode.getExp();
        Symbol leftSym = curTab.findSym(leftVal.getIdent());
        assert !leftSym.getRefType().equals(Symbol.RefType.POINTER);
        assert leftSym.getDimension() == leftVal.getArrayIndexes().size();
        if (leftSym.getRefType().equals(Symbol.RefType.VALUE)) {
            if (assignNode.isGetInt()) {    // e.g. a = getint();
                GetInt getInt = new GetInt(leftSym);
                curBlock.append(getInt);
            } else {
                Operand src = convExp(exp);
                UnaryOp mov = new UnaryOp(UnaryOp.Type.MOV, src, leftSym);
                curBlock.append(mov);
            }
        } else {                            // e.g. a[3] = getint()
            // TODO[8] array part
            System.exit(7);
        }
    }

    private void convPrintf(PrintfNode printfNode) {
        int pos = 0;
        int beginPos = 0;
        String formatStr = printfNode.getFormatStr();
        Iterator<ExpNode> params = printfNode.iterParam();
        while (formatStr.indexOf("%d", pos) != -1) {
            beginPos = pos;
            pos = formatStr.indexOf("%d", pos);
            if (beginPos < pos) {
                String label = MidCode.genStrLabel(formatStr.substring(beginPos, pos));
                globalStrLabels.add(label);
                curBlock.append(new PrintStr(label));
            }
            ExpNode param = params.next();
            Operand operand = convExp(param);
            curBlock.append(new PrintInt(operand));
            pos += 2;
        }
        if (pos < formatStr.length()) {
            String label = MidCode.genStrLabel(formatStr.substring(pos));
            globalStrLabels.add(label);
            curBlock.append(new PrintStr(label));
        }
    }

    private void convReturn(ReturnNode returnNode) {
        ExpNode retVal = returnNode.getRetVal();
        boolean voidRet = (retVal == null) && curFunc.getRetType().equals(FuncFrame.RetType.VOID);
        boolean intRet = (retVal != null) && !curFunc.getRetType().equals(FuncFrame.RetType.VOID);
        assert voidRet || intRet;
        if (voidRet) {
            curBlock.append(new Return());
        } else {
            Operand retOperand = convExp(retVal);
            curBlock.append(new Return(retOperand));
        }
    }

    // exp
    private Operand convExp(ExpNode expNode) {
        if (expNode instanceof LValNode) {
            return convLVal((LValNode) expNode);
        } else if (expNode instanceof NumNode) {
            return convNum((NumNode) expNode);
        } else if (expNode instanceof FuncCallNode) {
            return convFuncCall((FuncCallNode) expNode);
        } else if (expNode instanceof BinaryExpNode) {
            return convBinaryExp((BinaryExpNode) expNode);
        } else if (expNode instanceof UnaryExpNode) {
            return convUnaryExp((UnaryExpNode) expNode);
        } else {
            System.exit(6);
            return null;
        }
    }

    private Operand convLVal(LValNode lValNode) {
        String ident = lValNode.getIdent();
        Symbol symbol = curTab.findSym(ident);
        if (symbol.getRefType() == Symbol.RefType.VALUE) {
            return symbol;
        } else {
            TmpVar recv = new TmpVar();
            // TODO[1]: cond of array/pointer
            return null;
        }
    }

    private Imm convNum(NumNode numNode) {
        return new Imm(numNode.getConst());
    }

    private TmpVar convFuncCall(FuncCallNode funcCallNode) {
        String ident = funcCallNode.getIdent();
        FuncFrame func = MidCode.getFunc(ident);
        // call part
        TmpVar recv = func.getRetType().equals(FuncFrame.RetType.INT) ? new TmpVar() : null;
        Call call = new Call(func, recv);
        // params part
        Iterator<ExpNode> realParams = funcCallNode.iterParam();
        while (realParams.hasNext()) {
            ExpNode param = realParams.next();
            Operand op = convExp(param);
        }
        // add and ret
        curBlock.append(call);
        return recv;    // may be null
    }

    private TmpVar convBinaryExp(BinaryExpNode binaryExpNode) {
        TkType op = binaryExpNode.getOp();
        ExpNode leftExp = binaryExpNode.getLeftExp();
        ExpNode rightExp = binaryExpNode.getRightExp();
        if (!op.equals(TkType.AND) && !op.equals(TkType.OR)) {
            Operand left = convExp(leftExp);
            Operand right = convExp(rightExp);
            TmpVar dst = new TmpVar();
            BinaryOp binaryOp = new BinaryOp(BinaryExpNode.typeMap(op), left, right, dst);
            curBlock.append(binaryOp);
            return dst;
        } else {
            // TODO[2]: cond of && and ||
            return null;
        }
    }

    private Operand convUnaryExp(UnaryExpNode unaryExpNode) {
        ExpNode expNode = unaryExpNode.getExp();
        TkType op = unaryExpNode.getOp();
        Operand src = convExp(expNode);
        if (op.equals(TkType.PLUS)) {
            return src;
        } else {
            TmpVar dst = new TmpVar();
            UnaryOp unaryOp = new UnaryOp(UnaryExpNode.typeMap(op), src, dst);
            curBlock.append(unaryOp);
            return dst;
        }
    }

    // util
    public static Symbol getGlobalSym(String ident) {
        assert curTab.isGlobal();
        return curTab.findSym(ident);
    }

    private void putSymbolAndUpdateStack(Symbol symbol) {
        curTab.putSym(symbol);                          // put current symbol tab.update/set stack size
        int newStackSize = curFunc.addStackSize(symbol.getSize());
        symbol.setStackOffset(newStackSize);
    }

    // test


    private void outputMidCode() {
        ps.println("# Global Value:");
        for (Symbol symbol : globalSyms) {
            ps.println(symbol.getIdent() + "[0x" + Integer.toHexString(symbol.getStackOffset()) + "]");
        }
        ps.println();
        ps.println("# Global String:");
        for (String label : globalStrLabels) {
            ps.println(label + ": \"" + MidCode.getStr(label) + "\"");
        }
        ps.println();
        Iterator<FuncFrame> funcIter = MidCode.funcIter();
        while (funcIter.hasNext()) {
            ps.println(funcIter.next().toString());
            ps.println();
        }
        ps.println(MidCode.getMainFunc().toString());
    }
}
