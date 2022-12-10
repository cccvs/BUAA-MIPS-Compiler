package mid;

import exception.ErrorTable;
import exception.SysYError;
import front.ast.CompUnitNode;
import front.ast.decl.DeclNode;
import front.ast.decl.DefNode;
import front.ast.exp.*;
import front.ast.func.FuncDefNode;
import front.ast.func.FuncFParamNode;
import front.ast.stmt.*;
import mid.code.*;
import mid.frame.MidLabel;
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

    private final Stack<MidLabel> loopBeginStack = new Stack<>();
    private final Stack<MidLabel> loopEndStack = new Stack<>();

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
            try {
                // 需要在加入func前检查，而putFunc过程必须执行，否则与全局变量重名时会触发null pointer
                checkDupIdent(funcDefNode);
            } catch (SysYError error) {
                ErrorTable.append(error);
            }
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
        // check duplicated def
        try {
            checkDupIdent(defNode);
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
                    curFunc.append(new UnaryOp(UnaryOp.Type.MOV, initOperand, symbol));
                } else {
                    // array part
                    assert symbol.getRefType() == Operand.RefType.ARRAY;
                    int size = symbol.getSize();    // has multiplied 4
                    for (int index = 0; index * 4 < size; ++index) {
                        // offset
                        MidVar pointer = new MidVar(Operand.RefType.POINTER);
                        curFunc.append(new Offset(symbol, new Imm(index * 4), pointer));
                        // get exp
                        ExpNode initExp = defNode.getInitValues().get(index);
                        Operand initOperand = convExp(initExp);
                        // store
                        curFunc.append(new MemOp(MemOp.Type.STORE, initOperand, pointer));
                    }
                }
            }
        } catch (SysYError error) {
            ErrorTable.append(error);
        }
    }

    // func part
    private void convFunc(FuncDefNode funcDefNode, boolean isMain) {
        // check duplicated func def
        try {
            checkFuncEnd(funcDefNode);
            curFunc = isMain ? midCode.getMainFunc() : midCode.getFunc(funcDefNode.getIdent());
            // params part
            Iterator<FuncFParamNode> paramIter = funcDefNode.paramIter();
            while (paramIter.hasNext()) {
                FuncFParamNode paramNode = paramIter.next();
                if (curFunc.hasParamName(paramNode.getIdent())) {
                    // dup f param error
                    ErrorTable.append(new SysYError(SysYError.DUPLICATED_IDENT, paramNode.getLine()));
                } else {
                    Symbol param = new Symbol(paramNode);
                    curFunc.addParam(param);
                }
            }
            // block and symTab part
            curFunc.append(new MidLabel(MidLabel.Type.func));
            BlockNode block = funcDefNode.getBlock();
            convBlock(block);
            // append "return;" for void func
            if (curFunc.getRetType().equals(FuncFrame.RetType.VOID)) {
                curFunc.append(new Return());
            }
            curFunc = null;
        } catch (SysYError error) {
            ErrorTable.append(error);
        }
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
            convBreak((BreakNode) stmtNode);
        } else if (stmtNode instanceof ContinueNode) {
            convContinue((ContinueNode) stmtNode);
        } else {
            System.out.println("illegal file for hw1!");
            System.exit(5);
        }
    }

    private void convAssign(AssignNode assignNode) {
        // error/declare part
        try {
            LValNode leftVal = assignNode.getLeftVal();
            ExpNode exp = assignNode.getExp();
            MidVar leftSym = findLValIdent(leftVal, true);
            // begin
            if (leftSym.getRefType().equals(Symbol.RefType.VALUE)) {
                if (assignNode.isGetInt()) {    // e.g. a = getint();
                    curFunc.append(new GetInt(leftSym));
                } else {
                    Operand src = convExp(exp);
                    curFunc.append(new UnaryOp(UnaryOp.Type.MOV, src, leftSym));
                }
            } else {
                MidVar leftPointer = convLVal(leftVal, true);
                if (assignNode.isGetInt()) {    // e.g. a[3] = getint()
                    MidVar recv = new MidVar(Operand.RefType.VALUE);
                    curFunc.append(new GetInt(recv));
                    curFunc.append(new MemOp(MemOp.Type.STORE, recv, leftPointer));
                } else {
                    Operand src = convExp(exp);
                    curFunc.append(new MemOp(MemOp.Type.STORE, src, leftPointer));
                }
            }
        } catch (SysYError error) {
            ErrorTable.append(error);
        }
    }

    private void convPrintf(PrintfNode printfNode) {
        try {
            printfNode.checkParamCount();
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
                curFunc.append(printIns);
            }
        } catch (SysYError error) {
            ErrorTable.append(error);
        }
    }

    private void convReturn(ReturnNode returnNode) {
        try {
            checkReturn(returnNode);
        } catch (SysYError error) {
            ErrorTable.append(error);
        }
        ExpNode retVal = returnNode.getRetVal();
        if (retVal == null) {
            curFunc.append(new Return());
        } else {
            Operand retOperand = convExp(retVal);
            curFunc.append(new Return(retOperand));
        }
    }

    private void convBranch(BranchNode branchNode) {
        ExpNode cond = branchNode.getCond();
        StmtNode thenBody = branchNode.getThenStmt();
        MidLabel branchThen = new MidLabel(MidLabel.Type.branch_then);
        MidLabel branchEnd = new MidLabel(MidLabel.Type.branch_end);
        if (branchNode.hasElse()) {
            StmtNode elseBody = branchNode.getElseStmt();
            MidLabel branchElse = new MidLabel(MidLabel.Type.branch_else);
            // exp part
            convOrExp(cond, branchThen, branchElse);
            // then part
            curFunc.append(branchThen);
            curTab = new SymTab(curTab);
            convStmt(thenBody);
            curFunc.append(new Jump(branchEnd));
            curTab = curTab.prev();
            // else part
            curFunc.append(branchElse);
            curTab = new SymTab(curTab);
            convStmt(elseBody);
        } else {
            // exp part
            convOrExp(cond, branchThen, branchEnd);
            // then part
            curFunc.append(branchThen);
            curTab = new SymTab(curTab);
            convStmt(thenBody);
            curFunc.append(new Jump(branchEnd));
        }
        // end part
        curTab = curTab.prev();     // 统一回到上一级符号表
        curFunc.append(branchEnd);
    }

    private void convLoop(LoopNode loopNode) {
        ExpNode cond = loopNode.getCond();
        StmtNode body = loopNode.getBody();
        MidLabel loopBegin = new MidLabel(MidLabel.Type.loop_begin);
        MidLabel loopBody = new MidLabel(MidLabel.Type.loop_body);
        MidLabel loopEnd = new MidLabel(MidLabel.Type.loop_end);
        // exp part
        curFunc.append(loopBegin);
        convOrExp(cond, loopBody, loopEnd);
        // body part
        curFunc.append(loopBody);
        curTab = new SymTab(curTab);    // 更新符号表
        loopBeginStack.push(loopBegin); // continue
        loopEndStack.push(loopEnd);     // break
        convStmt(body);                 // convert loop body
        loopBeginStack.pop();           // continue
        loopEndStack.pop();             // break
        curFunc.append(new Jump(loopBegin));
        curTab = curTab.prev();         // 回到上一级符号表
        // end part
        curFunc.append(loopEnd);
    }

    private void convBreak(BreakNode breakNode) {
        if (!loopEndStack.isEmpty()) {
            curFunc.append(new Jump(loopEndStack.peek()));
            curFunc.append(new MidLabel(MidLabel.Type.break_follow));
        } else {
            ErrorTable.append(new SysYError(SysYError.CTRL_OUTSIDE_LOOP, breakNode.getLine()));
        }
    }

    private void convContinue(ContinueNode continueNode) {
        if (!loopBeginStack.isEmpty()) {
            curFunc.append(new Jump(loopBeginStack.peek()));
            curFunc.append(new MidLabel(MidLabel.Type.continue_follow));
        } else {
            ErrorTable.append(new SysYError(SysYError.CTRL_OUTSIDE_LOOP, continueNode.getLine()));
        }
    }

    // short circuit evaluation part
    private void convOrExp(ExpNode exp, MidLabel labelTrue, MidLabel labelFalse) {
        if (exp.isOrBinary()) {
            MidLabel labelOr = new MidLabel(MidLabel.Type.or);
            BinaryExpNode orExp = (BinaryExpNode) exp;
            ExpNode left = orExp.getLeftExp();
            ExpNode right = orExp.getRightExp();
            // left
            convOrExp(left, labelTrue, labelOr);         // 左递归, 右边不可能为OrExp
            // right
            curFunc.append(labelOr);
            convAndExp(right, labelTrue, labelFalse);    // 左递归, OrExp向右上扩展
        } else {
            convAndExp(exp, labelTrue, labelFalse);
        }
    }

    private void convAndExp(ExpNode exp, MidLabel labelTrue, MidLabel labelFalse) {
        if (exp.isAndBinary()) {
            MidLabel labelAnd = new MidLabel(MidLabel.Type.and);
            BinaryExpNode andExp = (BinaryExpNode) exp;
            ExpNode left = andExp.getLeftExp();
            ExpNode right = andExp.getRightExp();
            // left
            convAndExp(left, labelAnd, labelFalse);      // 左递归, 右边不可能为AndExp
            // right
            curFunc.append(labelAnd);
            convEqExp(right, labelTrue, labelFalse);     // 左递归, AndExp向右上扩展
        } else {
            convEqExp(exp, labelTrue, labelFalse);
        }
    }

    private void convEqExp(ExpNode exp, MidLabel labelTrue, MidLabel labelFalse) {
        // TODO[14]: 后续窥孔优化[slt, bez]指令序列，以及[j label, label:]指令序列
        Operand value = convExp(exp);
        curFunc.append(new Branch(Branch.Type.BNEZ, value, labelTrue, labelFalse));
    }

    // exp part
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
        // error/declare part
        try {
            Symbol symbol = findLValIdent(lValNode, assign);
            // assign为真代表LVal当作左值赋值
            if (symbol.getRefType() == Symbol.RefType.VALUE) {
                return symbol;
            } else {
                int leftValDim = lValNode.getIndexNum();
                int symbolDim = symbol.getDimension();
                assert leftValDim <= symbolDim;
                // cal offset
                Operand offsetVal = new Imm(0);
                Iterator<ExpNode> indexExps = lValNode.iterIndexExp();
                for (int i = 0; i < leftValDim; i++) {
                    Operand indexOperand = convExp(indexExps.next());
                    MidVar prod = new MidVar(Operand.RefType.VALUE);
                    MidVar newOffsetVal = new MidVar(Operand.RefType.VALUE);
                    Imm base = new Imm(symbol.getBase(i) * 4);
                    curFunc.append(new BinaryOp(BinaryOp.Type.MUL, indexOperand, base, prod));
                    curFunc.append(new BinaryOp(BinaryOp.Type.ADD, offsetVal, prod, newOffsetVal));
                    offsetVal = newOffsetVal;
                }
                // deal with pointer
                MidVar pointer = new MidVar(Operand.RefType.POINTER);
                curFunc.append(new Offset(symbol, offsetVal, pointer));
                if (leftValDim < symbolDim || assign) {
                    // 作为实参地址, or用作左值赋值时
                    return pointer;
                } else {
                    MidVar loadValue = new MidVar(Operand.RefType.VALUE);
                    curFunc.append(new MemOp(MemOp.Type.LOAD, loadValue, pointer));
                    return loadValue;
                }
            }
        } catch (SysYError error) {
            ErrorTable.append(error);
            return new MidVar(Operand.RefType.VALUE);
        }
    }

    private Imm convNum(NumNode numNode) {
        return new Imm(numNode.getVal());
    }

    private MidVar convFuncCall(FuncCallNode funcCallNode) {
        try {
            FuncFrame func = findFuncIdent(funcCallNode);
            // call part
            MidVar recv = func.getRetType().equals(FuncFrame.RetType.INT) ? new MidVar(MidVar.RefType.VALUE) : null;
            Call call = new Call(func, recv);
            // params part
            Iterator<ExpNode> realParams = funcCallNode.iterRealParam();
            while (realParams.hasNext()) {
                ExpNode param = realParams.next();
                Operand op = convExp(param);
                call.addParam(op);
            }
            checkFuncCall(funcCallNode, func);
            // add and ret
            curFunc.append(call);
            return recv;    // may be null
        } catch (SysYError error) {
            ErrorTable.append(error);
            return new MidVar(Operand.RefType.VALUE);
        }
    }

    private MidVar convBinaryExp(BinaryExpNode binaryExpNode) {
        TkType op = binaryExpNode.getOp();
        ExpNode leftExp = binaryExpNode.getLeftExp();
        ExpNode rightExp = binaryExpNode.getRightExp();
        assert !op.equals(TkType.AND) && !op.equals(TkType.OR); // no && and ||
        Operand left = convExp(leftExp);
        Operand right = convExp(rightExp);
        MidVar dst = new MidVar(left.getRefType());
        curFunc.append(new BinaryOp(BinaryExpNode.typeMap(op), left, right, dst));
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
            curFunc.append(new UnaryOp(UnaryExpNode.typeMap(op), src, dst));
            return dst;
        }
    }

    // util
    private void fillSymbolTabAndUpdateStack(Symbol symbol) {
        curTab.putSym(symbol);                  // put current symbol tab.update/set stack size
        int symbolSize = symbol.getSize();
        int newStackSize = curTab.isGlobal() ? midCode.addStackSize(symbolSize) : curFunc.addStackSize(symbolSize);
        symbol.updateStackOffset(newStackSize);
    }

    public static SymTab getCurTab() {
        return curTab;
    }

    // check
    private void checkDupIdent(DefNode defNode) throws SysYError {
        String ident = defNode.getIdent();
        int line = defNode.getIdentLine();
        if (curTab.isGlobal() && midCode.getFunc(ident) != null || curTab.containSym(ident)) {
            throw new SysYError(SysYError.DUPLICATED_IDENT, line);
        }
    }

    private void checkDupIdent(FuncDefNode defNode) throws SysYError {
        String ident = defNode.getIdent();
        int line = defNode.getIdentLine();
        if (curTab.isGlobal() && midCode.getFunc(ident) != null || curTab.containSym(ident)) {
            throw new SysYError(SysYError.DUPLICATED_IDENT, line);
        }
    }

    private Symbol findLValIdent(LValNode leftVal, boolean assign) throws SysYError {
        Symbol symbol = curTab.findSym(leftVal.getIdent());
        if (symbol == null) {
            throw new SysYError(SysYError.UNDEFINED_IDENT, leftVal.getIdentLine());
        } else if (assign && symbol.isConst()) {
            throw new SysYError(SysYError.MODIFIED_CONSTANT, leftVal.getIdentLine());
        } else {
            return symbol;
        }
    }

    private FuncFrame findFuncIdent(FuncCallNode funcCallNode) throws SysYError {
        String ident = funcCallNode.getIdent();
        FuncFrame func = midCode.getFunc(ident);
        if (func == null) {
            throw new SysYError(SysYError.UNDEFINED_IDENT, funcCallNode.getIdentLine());
        } else {
            return func;
        }
    }

    private void checkFuncCall(FuncCallNode funcCallNode, FuncFrame funcFrame) throws SysYError {
        if (funcCallNode.realParamNum() != funcFrame.formatParamNum()) {
            throw new SysYError(SysYError.MISMATCHED_PARAM_NUM, funcCallNode.getIdentLine());
        }
        Iterator<ExpNode> realParams = funcCallNode.iterRealParam();
        Iterator<Symbol> formatParams = funcFrame.iterFormatParam();
        while (realParams.hasNext()) {
            ExpNode real = realParams.next();
            Symbol format = formatParams.next();
            // check void caller, e.g. f(a(), 1); [a is void func]
            if (real instanceof FuncCallNode) {
                String paramFuncIdent = ((FuncCallNode) real).getIdent();
                FuncFrame paramFunc = midCode.getFunc(paramFuncIdent);
                assert paramFunc != null;   // 因为在FuncFrame添加参数之后，因此不可能为null
                if (paramFunc.getRetType().equals(FuncFrame.RetType.VOID)) {
                    throw new SysYError(SysYError.MISMATCHED_PARAM_TYPE, funcCallNode.getIdentLine());
                }
            }
            // check pointer dim
            int formatDim = format.getDimension();
            if (real instanceof LValNode) {
                LValNode leftVal = (LValNode) real;
                Symbol leftSym = curTab.findSym(leftVal.getIdent());
                int indexDim = leftVal.getIndexNum();
                int symDim = leftSym.getDimension();
                if (symDim - indexDim != formatDim) {
                    throw new SysYError(SysYError.MISMATCHED_PARAM_TYPE, funcCallNode.getIdentLine());
                }
                for (int i = 1; i < formatDim; i++) {
                    if (format.getDimIndex(i) != leftSym.getDimIndex(i + indexDim)) {
                        throw new SysYError(SysYError.MISMATCHED_PARAM_TYPE, funcCallNode.getIdentLine());
                    }
                }
            } else if (formatDim != 0) {
                throw new SysYError(SysYError.MISMATCHED_PARAM_TYPE, funcCallNode.getIdentLine());
            }
        }
    }

    private void checkReturn(ReturnNode returnNode) throws SysYError {
        ExpNode retVal = returnNode.getRetVal();
        if (retVal != null && curFunc.getRetType().equals(FuncFrame.RetType.VOID)) {
            throw new SysYError(SysYError.MISMATCHED_RETURN, returnNode.getLine());
        }
    }

    private void checkFuncEnd(FuncDefNode funcDefNode) throws SysYError {
        BlockNode funcBlock = funcDefNode.getBlock();
        BlockItemNode lastItem = funcBlock.getLastItem();
        if (funcDefNode.getFuncType().equals(TkType.INTTK)) {
            if (!(lastItem instanceof ReturnNode) || ((ReturnNode) lastItem).getRetVal() == null) {
                throw new SysYError(SysYError.MISSING_RETURN, funcBlock.getEndLine());
            }
        }
    }
}
