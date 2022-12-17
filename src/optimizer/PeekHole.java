package optimizer;

import back.MipsTranslator;
import back.Reg;
import back.ins.Add;
import back.ins.J;
import back.ins.Lw;
import back.ins.Sw;
import back.special.Comment;
import back.special.Label;
import back.special.MipsIns;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PeekHole {
    private final List<MipsIns> mipsIns;
    private final Set<Integer> tmpRegs = new HashSet<Integer>() {{
        add(MipsTranslator.TMP_R1);
        add(MipsTranslator.TMP_R2);
    }};

    public PeekHole(MipsTranslator mipsTranslator) {
        mipsIns = mipsTranslator.getMipsInsList();
    }

    public void run() {
        for (int cur = 0; cur < mipsIns.size() - 1; ) {
            MipsIns curIns = mipsIns.get(cur);
            int next = cur + 1;
            while (next < mipsIns.size() && mipsIns.get(next) instanceof Comment) {
                ++next;
            }
            MipsIns nextIns = mipsIns.get(next);
            // cond 1
            if (curIns instanceof J && nextIns instanceof Label) {
                if (Objects.equals(((J) curIns).getLabel(), ((Label) nextIns).getLabel())) {
                    mipsIns.remove(cur);
                    continue;
                }
            }
            // cond 2

            else if (curIns instanceof Lw && nextIns instanceof Add) {
                // lw $fp, 4($v1)
                // add $t0, $zero, $fp
                Lw lw = (Lw) curIns;
                Add add = (Add) nextIns;
                int lwVal = lw.getReg();
                int addDst = add.getDst();
                int addSrc1 = add.getSrc1();
                int addSrc2 = add.getSrc2();
                if (tmpRegs.contains(lwVal) && lwVal == addSrc2 && addSrc1 == Reg.ZERO) {
                    // 必须从大到小！
                    mipsIns.remove(next);
                    mipsIns.remove(cur);
                    mipsIns.add(cur, new Lw(addDst, lw.getOffset(), lw.getBase()));
                    continue;
                }
            }
            // cond
            else if (curIns instanceof Add && nextIns instanceof Sw) {
                // add $fp, $zero, $t1
                // sw $fp, 4($v1)
                Add add= (Add) curIns;
                Sw sw = (Sw) nextIns;
                int swVal = sw.getReg();
                int addDst = add.getDst();
                int addSrc1 = add.getSrc1();
                int addSrc2 = add.getSrc2();
                if (tmpRegs.contains(swVal) && swVal == addDst && addSrc1 == Reg.ZERO) {
                    mipsIns.remove(next);
                    mipsIns.remove(cur);
                    mipsIns.add(cur, new Sw(addSrc2, sw.getOffset(), sw.getBase()));
                    continue;
                }
            }
            // 否则++i
            ++cur;
        }
    }
}
