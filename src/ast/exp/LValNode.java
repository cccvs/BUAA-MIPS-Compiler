package ast.exp;

import ir.IrConverter;
import ir.MidCode;
import ir.code.BasicIns;
import ir.code.MemOp;
import ir.frame.BasicBlock;
import ir.operand.Operand;
import ir.operand.Symbol;
import ir.operand.TmpVar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LValNode implements ExpNode {
    private String ident;
    private final List<ExpNode> arrayIndexes;

    public LValNode() {
        ident = null;
        arrayIndexes = new ArrayList<>();
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public void addArrayIndex(ExpNode exp) {
        arrayIndexes.add(exp);
    }

    // ir part
    public Integer getConst() {
        Symbol symbol = IrConverter.getGlobalSym(ident);
        if (symbol.isConst()) {
            return symbol.getConstVal(arrayIndexes.stream().map(ExpNode::getConst).collect(Collectors.toList()));
        } else {
            System.out.println("expect Const or Num, get Var!");
            System.exit(3);
            return null;
        }
    }

    @Override
    public Operand expToIr(BasicBlock basicBlock) {
        TmpVar recv = new TmpVar(basicBlock);
        Symbol symbol = basicBlock.getSymTab().findSym(ident);
        if (symbol.getRefType() == Symbol.RefType.VALUE) {
            MemOp memOp = new MemOp(MemOp.Type.LOAD, symbol, recv);
            basicBlock.addIns(memOp);
            return recv;
        } else {
            // TODO[1]: cond of array
            return null;
        }
    }

    // basic interface
    public String getIdent() {
        return ident;
    }
}
