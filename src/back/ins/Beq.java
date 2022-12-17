package back.ins;

import back.Reg;
import back.special.MipsIns;

public class Beq extends MipsIns {
    private Integer src1;
    private Integer src2;
    private String label;

    public Beq(Integer src1, Integer src2, String label) {
        this.src1 = src1;
        this.src2 = src2;
        this.label = label;
    }

    public Integer getSrc1() {
        return src1;
    }

    public void setSrc1(Integer src1) {
        this.src1 = src1;
    }

    public Integer getSrc2() {
        return src2;
    }

    public void setSrc2(Integer src2) {
        this.src2 = src2;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("beq $%s, $%s, %s", Reg.name(src1), Reg.name(src2), label);
    }
}
