package ast.exp;

import ast.stmt.StmtNode;

public interface ExpNode extends StmtNode {
    public Integer getConstVal();
}
