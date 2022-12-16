package mid;

import exception.ErrorTable;
import exception.SysYError;
import front.TkType;
import front.ast.exp.*;
import mid.frame.SymTab;
import mid.operand.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ConstUtil {
    private final SymTab symTab;

    public ConstUtil(SymTab symTab) {
        this.symTab = symTab;
    }

    public int calExp(ExpNode exp) {
        if (exp instanceof BinaryExpNode) {
            return calBinaryExp((BinaryExpNode) exp);
        } else if (exp instanceof UnaryExpNode) {
            return calUnaryExp((UnaryExpNode) exp);
        } else if (exp instanceof NumNode) {
            return calNum((NumNode) exp);
        } else if (exp instanceof LValNode) {
            return calLVal((LValNode) exp);
        }
        assert false;   // error if func ca
        return 0;
    }

    public int calBinaryExp(BinaryExpNode binaryExp) {
        int left = calExp(binaryExp.getLeftExp());
        int right = calExp(binaryExp.getRightExp());
        BinaryCal binaryCal = binaryOpMap.get(binaryExp.getOp());
        return binaryCal.cal(left, right);
    }

    public int calUnaryExp(UnaryExpNode unaryExp) {
        int val = calExp(unaryExp.getExp());
        UnaryCal unaryCal = unaryOpMap.get(unaryExp.getOp());
        return unaryCal.cal(val);
    }

    public int calNum(NumNode num) {
        return num.getVal();
    }

    public int calLVal(LValNode leftVal) {
        String ident = leftVal.getIdent();
        Symbol symbol = symTab.findSym(ident);
        if (symbol == null) {
            ErrorTable.append(new SysYError(SysYError.UNDEFINED_IDENT, leftVal.getIdentLine()));
            return 0;
        } else {
            assert symbol.isConst();
            List<Integer> indexList = new ArrayList<>();
            Iterator<ExpNode> indexExps = leftVal.iterIndexExp();
            while (indexExps.hasNext()) {
                ExpNode exp = indexExps.next();
                indexList.add(calExp(exp));
            }
            return symbol.getConstVal(indexList);
        }
    }

    public static int calBinaryInteger(TkType type, int left, int right) {
        BinaryCal binaryCal = binaryOpMap.get(type);
        return binaryCal.cal(left, right);
    }

    public static int calUnaryInteger(TkType type, int val) {
        UnaryCal unaryCal = unaryOpMap.get(type);
        return unaryCal.cal(val);
    }

    @FunctionalInterface
    private interface UnaryCal {
        int cal(int val);
    }

    @FunctionalInterface
    private interface BinaryCal {
        int cal(int left, int right);
    }

    private static final HashMap<TkType, ConstUtil.UnaryCal> unaryOpMap =
            new HashMap<TkType, ConstUtil.UnaryCal>() {{
                put(TkType.PLUS, x -> x);
                put(TkType.MINU, x -> -x);
                put(TkType.NOT, x -> (x != 0) ? 0 : 1);
            }};
    private static final HashMap<TkType, ConstUtil.BinaryCal> binaryOpMap
            = new HashMap<TkType, ConstUtil.BinaryCal>() {{
        // AddExp
        put(TkType.PLUS, Integer::sum);
        put(TkType.MINU, (a, b) -> a - b);
        // MulExp
        put(TkType.MULT, (a, b) -> a * b);
        put(TkType.DIV, (a, b) -> a / b);
        put(TkType.MOD, (a, b) -> a % b);
        // RelExp
        put(TkType.GEQ, (a, b) -> (a >= b) ? 1 : 0);
        put(TkType.GRE, (a, b) -> (a > b) ? 1 : 0);
        put(TkType.LEQ, (a, b) -> (a <= b) ? 1 : 0);
        put(TkType.LSS, (a, b) -> (a < b) ? 1 : 0);
        // EqExp
        put(TkType.EQL, (a, b) -> (a == b) ? 1 : 0);
        put(TkType.NEQ, (a, b) -> (a != b) ? 1 : 0);
        // AndExp, OrExp
        put(TkType.AND, (a, b) -> ((a != 0) && (b != 0)) ? 1 : 0);
        put(TkType.OR, (a, b) -> ((a != 0) || (b != 0)) ? 1 : 0);
    }};
}
