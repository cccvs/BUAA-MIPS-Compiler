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
import util.TkType;

import java.util.Iterator;
import java.util.Stack;

public class IrConverter {
    private final MidCode midCode = new MidCode();

    // current info
    private static SymTab curTab = new SymTab();    // 初始化为全局符号表
    private static FuncFrame curFunc = null;
    private static BasicBlock curBlock = null;      // 对curBlock更新统一调用updateBlock()方法
    private boolean hasReturn = true;               // 标志当前函数是否含有return语句

    private final Stack<BasicBlock> loopBeginStack = new Stack<>();
    private final Stack<BasicBlock> loopEndStack = new Stack<>();

    public IrConverter(CompUnitNode compUnitNode){
        convCompUnit(compUnitNode);
        // testGlobal();
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
        Symbol symbol = new Symbol(defNode, isGlobal);  // create new symbol
        putSymbolAndUpdateStack(symbol);
        if (isGlobal) {
            midCode.putGlobalSym(symbol);
        } else {
            if (symbol.getRefType() == Operand.RefType.VALUE) {
                UnaryOp unaryOp = new UnaryOp(UnaryOp.Type.MOV, new Imm(symbol.getInitVal().get(0)), symbol);
                curBlock.append(unaryOp);
            } else {
                // TODO[12] array
                System.exit(12);
            }
        }
    }

    // func part
    private void convFunc(FuncDefNode funcDefNode, boolean isMain) {
        curFunc = isMain ? midCode.getMainFunc() : midCode.getFunc(funcDefNode.getIdent());
        hasReturn = false;
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
        if (curFunc.getRetType().equals(FuncFrame.RetType.VOID) && !hasReturn) {
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
        }  else { // TODO[7] branch part
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
        String formatStr = printfNode.getFormatStr();
        Iterator<ExpNode> params = printfNode.iterParam();
        while (formatStr.indexOf("%d", pos) != -1) {
            int beginPos = pos;
            pos = formatStr.indexOf("%d", pos);
            if (beginPos < pos) {
                String label = midCode.genStrLabel(formatStr.substring(beginPos, pos));
                curBlock.append(new PrintStr(label));
            }
            ExpNode param = params.next();
            Operand operand = convExp(param);
            curBlock.append(new PrintInt(operand));
            pos += 2;
        }
        if (pos < formatStr.length()) {
            String label = midCode.genStrLabel(formatStr.substring(pos));
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
        hasReturn = true;
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
            convAndExp(left, labelTrue, labelOr);       // 左递归, 左边不可能为OrExp
            // right
            updateBlock(labelOr);
            convOrExp(right, labelTrue, labelFalse);    // 左递归, OrExp向右扩展
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
            convEqExp(left, labelAnd, labelFalse);      // 左递归, 左边不可能为AndExp
            // right
            updateBlock(labelAnd);
            convAndExp(right, labelTrue, labelFalse);     // 左递归, AndExp向右扩展
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
            MidVar recv = new MidVar(MidVar.RefType.POINTER);
            // TODO[1]: cond of array/pointer
            return null;
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
        BinaryOp binaryOp = new BinaryOp(BinaryExpNode.typeMap(op), left, right, dst);
        curBlock.append(binaryOp);
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
