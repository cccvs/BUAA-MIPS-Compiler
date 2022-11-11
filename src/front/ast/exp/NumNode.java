package front.ast.exp;

public class NumNode extends ExpNode {
    private final int num;

    public NumNode(int num) {
        this.num = num;
    }

    public Integer getConst() {
        return num;
    }
}
