package mid;

import front.ast.CompUnitNode;
import front.ast.decl.DeclNode;
import front.ast.decl.DefNode;
import front.ast.exp.*;
import front.ast.func.FuncDefNode;
import front.ast.func.FuncFParamNode;
import front.ast.stmt.*;
import mid.code.*;
import mid.frame.BasicBlock;
import mid.frame.FuncFrame;
import mid.frame.SymTab;
import mid.operand.Imm;
import mid.operand.Operand;
import mid.operand.Symbol;
import mid.operand.MidVar;
import front.TkType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class IrConverter {
    private final MidCode midCode = new MidCode();

    // current info
    private static SymTab curTab = new SymTab();    // 初始化为全局符号表
    private static FuncFrame curFunc = null;
    private static BasicBlock curBlock = null;      // 对curBlock更新统一调用updateBlock()方法

    private final Stack<BasicBlock> loopBeginStack = new Stack<>();
    private final Stack<BasicBlock> loopEndStack = new Stack<>();

    public IrConverter(CompUnitNode compUnitNode) {
        convCompUnit(compUnitNode);
    }

    public MidCode getMidCode() {
        return midCode;
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
            midCode.putFunc(new FuncFrame(funcDefNode.getIdent(), funcDefNode.getFuncType()));
        }
        FuncDefNode mainFunc = compUnitNode.getMainFuncDefNode();
        midCode.setMainFunc(new FuncFrame(mainFunc.getIdent(), mainFunc.getFuncType()));
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
        Symbol symbol = new Symbol(defNode, isGlobal, defNode.isConst());  // create new symbol
        fillSymbolTabAndUpdateStack(symbol);
        if (isGlobal) {
            midCode.putGlobalSym(symbol);
        } else if (!defNode.getInitValues().isEmpty()) {
            // 如果声明语句包含初始赋值
            if (symbol.getRefType() == Operand.RefType.VALUE) {
                // value part
                ExpNode initExp = defNode.getInitValues().get(0);
                Operand initOperand = convExp(initExp);     // 非全局常量初值可能不是常量表达式
                curBlock.append(new UnaryOp(UnaryOp.Type.MOV, initOperand, symbol));
            } else {
                // array part
                assert symbol.getRefType() == Operand.RefType.ARRAY;
                int size = symbol.getSize();    // has multiplied 4
                for (int index = 0; index * 4 < size; ++index) {
                    // offset
                    MidVar pointer = new MidVar(Operand.RefType.POINTER);
                    curBlock.append(new Offset(symbol, new Imm(index * 4), pointer));
                    // get exp
                    ExpNode initExp = defNode.getInitValues().get(index);
                    Operand initOperand = convExp(initExp);
                    // store
                    curBlock.append(new MemOp(MemOp.Type.STORE, initOperand, pointer));
                }
            }
        }
    }

    // func part
    private void convFunc(FuncDefNode funcDefNode, boolean isMain) {
        curFunc = isMain ? midCode.getMainFunc() : midCode.getFunc(funcDefNode.getIdent());
        // params part
        Iterator<FuncFParamNode> paramIter = funcDefNode.paramIter();
        while (paramIter.hasNext()) {
            FuncFParamNode paramNode = paramIter.next();
            Symbol param = new Symbol(paramNode);
            curFunc.addParam(param);
        }
        // block and symTab part
        updateBlock(new BasicBlock(BasicBlock.Type.func));
        BlockNode block = funcDefNode.getBlock();
        convBlock(block);
        // append "return;" for void func
        if (curFunc.getRetType().equals(FuncFrame.RetType.VOID)) {
            curBlock.append(new Return());
        }
        curFunc = null;
        curBlock = null;
    }

    // stmt part
    private void convBlock(BlockNode blockNode) {
        boolean isFuncBlock = curTab.isGlobal();
        boolean isOtherBlock = !curTab.isGlobal();
        assert isFuncBlock || isOtherBlock;
        curTab = new SymTab(curTab);
        // fill params into symbol table
        if (isFuncBlock) {
            Iterator<Symbol> paramIter = curFunc.iterFormatParam();
            while (paramIter.hasNext()) {
                Symbol param = paramIter.next();
                fillSymbolTabAndUpdateStack(param);
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
        curTab = curTab.prev();
    }

    private void convStmt(StmtNode stmtNode) {
        if (stmtNode instanceof BlockNode) {
            convBlock((BlockNode) stmtNode);
        } else if (stmtNode instanceof AssignNode) {
            convAssign((AssignNode) stmtNode);
        } else if (stmtNode instanceof PrintfNode) {
            convPrintf((PrintfNode) stmtNode);
        } else if (stmtNode instanceof ReturnNode) {
            convReturn((ReturnNode) stmtNode);
        } else if (stmtNode instanceof ExpNode) {
            convExp((ExpNode) stmtNode);
        } else if (stmtNode instanceof BranchNode) {
            convBranch((BranchNode) stmtNode);
        } else if (stmtNode instanceof LoopNode) {
            convLoop((LoopNode) stmtNode);
        } else if (stmtNode instanceof BreakNode) {
            convBreak();
        } else if (stmtNode instanceof ContinueNode) {
            convContinue();
        } else {
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
                curBlock.append(new GetInt(leftSym));
            } else {
                Operand src = convExp(exp);
                curBlock.append(new UnaryOp(UnaryOp.Type.MOV, src, leftSym));
            }
        } else {
            MidVar leftPointer = convLVal(leftVal, true);
            if (assignNode.isGetInt()) {    // e.g. a[3] = getint()
                MidVar recv = new MidVar(Operand.RefType.VALUE);
                curBlock.append(new GetInt(recv));
                curBlock.append(new MemOp(MemOp.Type.STORE, recv, leftPointer));
            } else {
                Operand src = convExp(exp);
                curBlock.append(new MemOp(MemOp.Type.STORE, src, leftPointer));
            }
        }
    }

    private void convPrintf(PrintfNode printfNode) {
        int pos = 0;
        String formatStr = printfNode.getFormatStr();
        Iterator<ExpNode> params = printfNode.iterParam();
        List<BasicIns> printBuffer = new ArrayList<>();
        while (formatStr.indexOf("%d", pos) != -1) {
            int beginPos = pos;
            pos = formatStr.indexOf("%d", pos);
            if (beginPos < pos) {
                String label = midCode.genStrLabel(formatStr.substring(beginPos, pos));
                printBuffer.add(new PrintStr(label));
            }
            ExpNode param = params.next();
            Operand operand = convExp(param);
            printBuffer.add(new PrintInt(operand));
            pos += 2;
        }
        if (pos < formatStr.length()) {
            String label = midCode.genStrLabel(formatStr.substring(pos));
            printBuffer.add(new PrintStr(label));
        }
        // print buffer
        for (BasicIns printIns : printBuffer) {
            curBlock.append(printIns);
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

    private void convBranch(BranchNode branchNode) {
        ExpNode cond = branchNode.getCond();
        StmtNode thenBody = branchNode.getThenStmt();
        BasicBlock branchThen = new BasicBlock(BasicBlock.Type.branch_then);
        BasicBlock branchEnd = new BasicBlock(BasicBlock.Type.branch_end);
        if (branchNode.hasElse()) {
            StmtNode elseBody = branchNode.getElseStmt();
            BasicBlock branchElse = new BasicBlock(BasicBlock.Type.branch_else);
            // exp part
            convOrExp(cond, branchThen, branchElse);
            // then part
            updateBlock(branchThen);
            curTab = new SymTab(curTab);
            convStmt(thenBody);
            curBlock.append(new Jump(branchEnd));
            curTab = curTab.prev();
            // else part
            updateBlock(branchElse);
            curTab = new SymTab(curTab);
            convStmt(elseBody);
        } else {
            // exp part
            convOrExp(cond, branchThen, branchEnd);
            // then part
            updateBlock(branchThen);
            curTab = new SymTab(curTab);
            convStmt(thenBody);
            curBlock.append(new Jump(branchEnd));
        }
        // end part
        curTab = curTab.prev();     // 统一回到上一级符号表
        updateBlock(branchEnd);
    }

    private void convLoop(LoopNode loopNode) {
        ExpNode cond = loopNode.getCond();
        StmtNode body = loopNode.getBody();
        BasicBlock loopBegin = new BasicBlock(BasicBlock.Type.loop_begin);
        BasicBlock loopBody = new BasicBlock(BasicBlock.Type.loop_body);
        BasicBlock loopEnd = new BasicBlock(BasicBlock.Type.loop_end);
        // exp part
        updateBlock(loopBegin);
        convOrExp(cond, loopBody, loopEnd);
        // body part
        updateBlock(loopBody);
        curTab = new SymTab(curTab);    // 更新符号表
        loopBeginStack.push(loopBegin); // continue
        loopEndStack.push(loopEnd);     // break
        convStmt(body);                 // convert loop body
        loopBeginStack.pop();           // continue
        loopEndStack.pop();             // break
        curBlock.append(new Jump(loopBegin));
        curTab = curTab.prev();         // 回到上一级符号表
        // end part
        updateBlock(loopEnd);
    }

    private void convBreak() {
        curBlock.append(new Jump(loopEndStack.peek()));
        updateBlock(new BasicBlock(BasicBlock.Type.break_follow));
    }

    private void convContinue() {
        curBlock.append(new Jump(loopBeginStack.peek()));
        updateBlock(new BasicBlock(BasicBlock.Type.continue_follow));
    }

    // short circuit evaluation part
    private void convOrExp(ExpNode exp, BasicBlock labelTrue, BasicBlock labelFalse) {
        if (exp.isOrBinary()) {
            BasicBlock labelOr = new BasicBlock(BasicBlock.Type.or);
            BinaryExpNode orExp = (BinaryExpNode) exp;
            ExpNode left = orExp.getLeftExp();
            ExpNode right = orExp.getRightExp();
            // left
            convOrExp(left, labelTrue, labelOr);         // 左递归, 右边不可能为OrExp
            // right
            updateBlock(labelOr);
            convAndExp(right, labelTrue, labelFalse);    // 左递归, OrExp向右上扩展
        } else {
            convAndExp(exp, labelTrue, labelFalse);
        }
    }

    private void convAndExp(ExpNode exp, BasicBlock labelTrue, BasicBlock labelFalse) {
        if (exp.isAndBinary()) {
            BasicBlock labelAnd = new BasicBlock(BasicBlock.Type.and);
            BinaryExpNode andExp = (BinaryExpNode) exp;
            ExpNode left = andExp.getLeftExp();
            ExpNode right = andExp.getRightExp();
            // left
            convAndExp(left, labelAnd, labelFalse);      // 左递归, 右边不可能为AndExp
            // right
            updateBlock(labelAnd);
            convEqExp(right, labelTrue, labelFalse);     // 左递归, AndExp向右上扩展
        } else {
            convEqExp(exp, labelTrue, labelFalse);
        }
    }

    private void convEqExp(ExpNode exp, BasicBlock labelTrue, BasicBlock labelFalse) {
        // TODO[14]: 后续窥孔优化[slt, bez]指令序列，以及[j label, label:]指令序列
        Operand value = convExp(exp);
        curBlock.append(new Branch(Branch.Type.BNEZ, value, labelTrue));
        curBlock.append(new Jump(labelFalse));
    }

    // exp
    private Operand convExp(ExpNode expNode) {
        if (expNode instanceof LValNode) {
            return convLVal((LValNode) expNode, false);
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

    private MidVar convLVal(LValNode lValNode, boolean assign) {
        // assign为真代表LVal当作左值赋值
        String ident = lValNode.getIdent();
        Symbol symbol = curTab.findSym(ident);
        if (symbol.getRefType() == Symbol.RefType.VALUE) {
            return symbol;
        } else {
            int leftValDim = lValNode.getArrayIndexes().size();
            int symbolDim = symbol.getDimension();
            assert leftValDim <= symbolDim;
            // cal offset
            Operand offsetVal = new Imm(0);
            for (int i = 0; i < leftValDim; i++) {
                Operand indexOperand = convExp(lValNode.getArrayIndexes().get(i));
                MidVar prod = new MidVar(Operand.RefType.VALUE);
                MidVar newOffsetVal = new MidVar(Operand.RefType.VALUE);
                Imm base = new Imm(symbol.getBase(i) * 4);
                curBlock.append(new BinaryOp(BinaryOp.Type.MUL, indexOperand, base, prod));
                curBlock.append(new BinaryOp(BinaryOp.Type.ADD, offsetVal, prod, newOffsetVal));
                offsetVal = newOffsetVal;
            }
            // deal with pointer
            MidVar pointer = new MidVar(Operand.RefType.POINTER);
            curBlock.append(new Offset(symbol, offsetVal, pointer));
            if (leftValDim < symbolDim || assign) {
                // 作为实参地址, or用作左值赋值
                return pointer;
            } else {
                MidVar loadValue = new MidVar(Operand.RefType.VALUE);
                curBlock.append(new MemOp(MemOp.Type.LOAD, loadValue, pointer));
                return loadValue;
            }
        }
    }

    private Imm convNum(NumNode numNode) {
        return new Imm(numNode.getConst());
    }

    private MidVar convFuncCall(FuncCallNode funcCallNode) {
        String ident = funcCallNode.getIdent();
        FuncFrame func = midCode.getFunc(ident);
        // call part
        MidVar recv = func.getRetType().equals(FuncFrame.RetType.INT) ? new MidVar(MidVar.RefType.VALUE) : null;
        Call call = new Call(func, recv);
        // params part
        Iterator<ExpNode> realParams = funcCallNode.iterParam();
        while (realParams.hasNext()) {
            ExpNode param = realParams.next();
            Operand op = convExp(param);
            call.addParam(op);
        }
        // add and ret
        curBlock.append(call);
        return recv;    // may be null
    }

    private MidVar convBinaryExp(BinaryExpNode binaryExpNode) {
        TkType op = binaryExpNode.getOp();
        ExpNode leftExp = binaryExpNode.getLeftExp();
        ExpNode rightExp = binaryExpNode.getRightExp();
        assert !op.equals(TkType.AND) && !op.equals(TkType.OR); // no && and ||
        Operand left = convExp(leftExp);
        Operand right = convExp(rightExp);
        MidVar dst = new MidVar(left.getRefType());
        curBlock.append(new BinaryOp(BinaryExpNode.typeMap(op), left, right, dst));
        return dst;
    }

    private Operand convUnaryExp(UnaryExpNode unaryExpNode) {
        ExpNode expNode = unaryExpNode.getExp();
        TkType op = unaryExpNode.getOp();
        Operand src = convExp(expNode);
        if (op.equals(TkType.PLUS)) {
            return src;
        } else {
            MidVar dst = new MidVar(MidVar.RefType.VALUE);
            curBlock.append( new UnaryOp(UnaryExpNode.typeMap(op), src, dst));
            return dst;
        }
    }

    // util
    public static Symbol getGlobalSym(String ident) {
        assert curTab.isGlobal();
        return curTab.findSym(ident);
    }

    private void fillSymbolTabAndUpdateStack(Symbol symbol) {
        curTab.putSym(symbol);                  // put current symbol tab.update/set stack size
        int symbolSize = symbol.getSize();
        int newStackSize = curTab.isGlobal() ? midCode.addStackSize(symbolSize) : curFunc.addStackSize(symbolSize);
        symbol.updateStackOffset(newStackSize);
    }

    private void updateBlock(BasicBlock basicBlock) {
        curBlock = basicBlock;
        curFunc.appendBlock(curBlock);
    }
}
