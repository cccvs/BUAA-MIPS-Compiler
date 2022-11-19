package mid.operand;

import front.ast.decl.DefNode;
import front.ast.exp.ExpNode;
import front.ast.func.FuncFParamNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Symbol extends MidVar {
    /*
     *  Originate from DefNode, FuncFParamNode.
     *  For (VALUE/POINTER) RefType, it represents (constant/variable/format parameter)
     * of (int/array) type.
     *  For POINTER type representing format parameter, the first element of "dimensions"
     * is set as "null".Here are examples:
     *  For format param "int ident[]", its "dimensions" is [null].
     *  For format param "int ident[][7]", its "dimensions" is [null, 7].
     *  For array decl "int ident[5][6]", its "dimensions" is [5, 6].
     *  For array decl "int ident[5]", its "dimensions" is [5].
     */

    // basic information
    private final String ident;
    // for array
    private final List<Integer> dimensions;   // for POINTER, 1 <= size <= 2, first element may be null
    private final List<Integer> values;
    // ir information
    private final boolean isConst;
    private final boolean isGlobal;
    private Integer stackOffset;          // offset from sp or .data, default for null

    public Symbol(DefNode defNode, boolean isGlobal, boolean isConst) {
        super(defNode);
        this.isConst = defNode.isConst();
        this.ident = defNode.getIdent();
        this.dimensions = defNode.getDimensions().stream().map(ExpNode::getConst).collect(Collectors.toList());
        this.isGlobal = isGlobal;
        if (isGlobal || isConst) {
            // global var/const, local const
            this.values = defNode.getInitValues().stream().map(ExpNode::getConst).collect(Collectors.toList());
        } else {
            // local var
            this.values = new ArrayList<>();    // need to be assigned?
        }
    }

    // format param
    public Symbol(FuncFParamNode param) {
        super(param);
        // basic information
        this.isConst = false;
        this.ident = param.getIdent();
        if (param.isPointer()) {
            this.dimensions = param.getDimensions().stream().map(ExpNode::getConst).collect(Collectors.toList());
            this.dimensions.add(0, null);   // fix dimensions
        } else {
            this.dimensions = new ArrayList<>();
        }
        this.values = null;
        this.isGlobal = false;
    }

    public int getConstVal(List<Integer> indexes) {
        assert dimensions.size() == indexes.size();
        int arrayBias = 0;
        for (int j = 0; j < dimensions.size(); j++) {
            arrayBias *= dimensions.get(j);
            arrayBias += indexes.get(j);
        }
        return values.get(arrayBias);
    }

    public int getSize() {
        // if it's a pointer representing a fParam, terminate with error!
        assert !(refType.equals(RefType.ARRAY) && dimensions.get(0) != null);
        if (refType.equals(RefType.ARRAY)) {
            return dimensions.stream().reduce((x, y) -> x * y).orElse(1) << 2;
        } else {
            return 4;
        }
    }

    // with mult 4
    public int getBase(int dimIndex) {
        assert 0 <= dimIndex && dimIndex < dimensions.size();
        assert refType.equals(RefType.ARRAY);
        int prod = 1;
        for (int j = dimensions.size() - 1; j > dimIndex; --j) {
            prod *= dimensions.get(j);
        }
        return prod;
    }

    @Override
    public String toString() {
        String typeStr = refType.name().substring(0, 1).toLowerCase();
        if (isGlobal) {
            return "v" + id + "_" + ident + "[" + typeStr +
                    ", data+0x" + Integer.toHexString(stackOffset) + "]";
        } else {
            return "v" + id + "_" + ident + "[" + typeStr +
                    ", sp-0x" + Integer.toHexString(stackOffset) + "]";
        }
    }

    // mips part
    public List<Integer> getInitVal() {
        return values;
    }

    // basic function
    public boolean isConst() {
        return isConst;
    }

    public String getIdent() {
        return ident;
    }

    public String getLabel() {
        assert isGlobal;
        return "g_" + ident;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Integer getOffset() {
        return stackOffset;
    }

    @Override
    public Operand.RefType getRefType() {
        return refType;
    }

    // 全局变量向上增长，偏移需要-size大小
    public void updateStackOffset(Integer stackOffset) {
        if (isGlobal) {
            this.stackOffset = stackOffset - getSize();
        } else {
            this.stackOffset = stackOffset;
        }
    }

    public Integer getDimension() {
        return dimensions.size();
    }
}
