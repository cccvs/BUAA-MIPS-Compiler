package ir.operand;

import ast.decl.DefNode;
import ast.exp.ExpNode;
import ast.func.FuncFParamNode;
import ir.IrConverter;
import ir.MidCode;
import ir.frame.SymTab;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Symbol implements Operand {
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
    private int id;
    private String ident;
    private RefType refType;
    // for array
    private List<Integer> dimensions;   // for POINTER, 1 <= size <= 2, first element may be null
    private List<Integer> values;
    // ir information
    private boolean isConst;
    private boolean isGlobal;
    private Integer stackOffset;          // offset from sp or .data, default for null

    // global var/const
    public Symbol(DefNode defNode, boolean isGlobal) {
        // basic information
        this.id = MidCode.genId();
        this.isConst = defNode.isConst();
        this.ident = defNode.getIdent();
        this.dimensions = defNode.getDimensions().stream().map(ExpNode::getConst).collect(Collectors.toList());
        this.values = defNode.getInitValues().stream().map(ExpNode::getConst).collect(Collectors.toList());
        // ir information
        this.isGlobal = isGlobal;
        this.refType = (dimensions.size() > 0) ? RefType.ARRAY : RefType.VALUE;
        this.stackOffset = null;
    }

    // format param
    public Symbol(FuncFParamNode param) {
        // basic information
        this.id = MidCode.genId();
        this.isConst = false;
        this.ident = param.getIdent();
        if (param.isPointer()) {
            this.dimensions = param.getDimensions().stream().map(ExpNode::getConst).collect(Collectors.toList());
            this.dimensions.add(0, null);   // fix dimensions
        } else {
            this.dimensions = new ArrayList<>();
        }
        this.values = null;
        // ir information
        this.isGlobal = false;
        this.refType = param.isPointer() ? RefType.POINTER : RefType.VALUE;
        this.stackOffset = null;
    }

    // ir part
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
        assert !(refType.equals(RefType.POINTER) && dimensions.get(0) != null);
        if (refType.equals(RefType.POINTER)) {
            System.out.println(ident);
            System.out.println(dimensions.size());
            return dimensions.stream().reduce((x, y) -> x * y).orElse(1) << 2;
        } else {
            return 4;
        }
    }

    @Override
    public String toString() {
        String typeStr = refType.name().substring(0, 1).toLowerCase();
        return "v" + id + "_" + ident + "[" + typeStr +
                ", sp-0x" + Integer.toHexString(stackOffset) + "]";

    }

    // basic function
    public boolean isConst() {
        return isConst;
    }

    public String getIdent() {
        return ident;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    @Override
    public RefType getRefType() {
        return refType;
    }

    public void setStackOffset(Integer stackOffset) {
        this.stackOffset = stackOffset;
    }

    public Integer getDimension() {
        return dimensions.size();
    }
}
