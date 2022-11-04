package ast.exp;

public class NumNode implements ExpNode {
    private final int num;

    public NumNode(int num) {
        this.num = num;
    }

    public Integer getConst() {
        return num;
    }
}
