package mid.operand;

import front.ast.decl.DefNode;
import front.ast.func.FuncFParamNode;
import mid.MidCode;

import java.util.Objects;

public class MidVar implements Operand {
    protected final Integer id;
    protected Integer stackOffset = null;
    protected final RefType refType;

    public MidVar(RefType refType) {
        assert refType.equals(RefType.VALUE) || refType.equals(RefType.POINTER);
        this.id = MidCode.genId();
        this.refType = refType;
    }

    protected MidVar(DefNode defNode) {
        this.id = MidCode.genId();
        this.refType = (defNode.getDimensions().size() > 0) ? RefType.ARRAY : RefType.VALUE;
        this.stackOffset = null;
    }

    protected MidVar(FuncFParamNode param) {
        this.id = MidCode.genId();
        this.refType = param.isPointer() ? RefType.POINTER : RefType.VALUE;
        this.stackOffset = null;
    }

    public void setStackOffset(int stackOffset) {
        assert this.stackOffset == null;
        this.stackOffset = stackOffset;
    }

    public Integer getOffset() {
        return stackOffset;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public Operand.RefType getRefType() {
        return refType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, refType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MidVar) {
            return Objects.equals(id, ((MidVar) obj).id);
        }
        return false;
    }

    @Override
    public String toString() {
        String typeStr = refType.name().substring(0, 1).toLowerCase();
        return "t" + id + "[" + typeStr + "]";
    }
}
