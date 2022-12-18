package optimizer;

import back.MipsTranslator;
import back.Reg;
import back.ins.*;
import back.special.Comment;
import back.special.Label;
import back.special.MipsIns;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MipsPeekHole {
    private final List<MipsIns> mipsIns;
    private static final int turn = 3;
    private final Set<Integer> tmpRegs = new HashSet<Integer>() {{
        add(MipsTranslator.TMP_R1);
        add(MipsTranslator.TMP_R2);
    }};

    public MipsPeekHole(MipsTranslator mipsTranslator) {
        mipsIns = mipsTranslator.getMipsInsList();
    }

    public void run() {
        int t = turn;
        while (t >=0) {
            runOneTurn();
            t--;
        }
    }

    public void runOneTurn() {
        for (int cur = 0; cur < mipsIns.size() - 1; ) {
            MipsIns curIns = mipsIns.get(cur);
            if (curIns instanceof Comment) {
                ++cur;
                continue;
            }
            int next = cur + 1;
            while (next < mipsIns.size() && mipsIns.get(next) instanceof Comment) {
                ++next;
            }
            MipsIns nextIns = mipsIns.get(next);
            System.out.println(cur + " "  + next);
            System.out.println(curIns + " " + nextIns);
            // j-label
            if (curIns instanceof J && nextIns instanceof Label) {
                if (Objects.equals(((J) curIns).getLabel(), ((Label) nextIns).getLabel())) {
                    mipsIns.remove(cur);
                    continue;
                }
            }
            // lw-add
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
                    if (lw.getBase() != null && lw.getOffset() != null && lw.getReg() != null) {
                        // 必须从大到小！
                        mipsIns.remove(next);
                        mipsIns.remove(cur);
                        mipsIns.add(cur, new Lw(addDst, lw.getOffset(), lw.getBase()));
                        continue;
                    }
                }
            }
            // add-sw
            else if (curIns instanceof Add && nextIns instanceof Sw) {
                Add add = (Add) curIns;
                Sw sw = (Sw) nextIns;
                int addDst = add.getDst();
                int addSrc1 = add.getSrc1();
                int addSrc2 = add.getSrc2();
                // add $fp, $zero, $t1
                // sw $fp, 4($v1)
                if (tmpRegs.contains(sw.getReg()) && sw.getReg() == addDst && addSrc1 == Reg.ZERO) {
                    if (sw.getBase() != null && sw.getOffset() != null && sw.getReg() != null) {
                        mipsIns.remove(next);
                        mipsIns.remove(cur);
                        mipsIns.add(cur, new Sw(addSrc2, sw.getOffset(), sw.getBase()));
                        continue;
                    }
                }
                //  add $v1, $zero, $t4
                //	sw $t5, 0($v1)
                if (tmpRegs.contains(sw.getBase()) && sw.getBase() == addDst && addSrc1 == Reg.ZERO) {
                    if (sw.getBase() != null && sw.getOffset() != null && sw.getReg() != null) {
                        mipsIns.remove(next);
                        mipsIns.remove(cur);
                        mipsIns.add(cur, new Sw(sw.getReg(), sw.getOffset(), addSrc2));
                        continue;
                    }
                }
            }
            // add-lw
            else if (curIns instanceof Add && nextIns instanceof Lw) {
                Add add = (Add) curIns;
                Lw lw = (Lw) nextIns;
                //  add $v1, $zero, $t4
                //	lw $t5, 0($v1)
                if (tmpRegs.contains(lw.getBase()) && lw.getBase() == add.getDst() && add.getSrc1() == Reg.ZERO) {
                    if (lw.getBase() != null && lw.getOffset() != null && lw.getReg() != null) {
                        mipsIns.remove(next);
                        mipsIns.remove(cur);
                        mipsIns.add(cur, new Lw(lw.getReg(), lw.getOffset(), add.getSrc2()));
                        continue;
                    }
                }
            }
            // add-beq
            else if (curIns instanceof Add && nextIns instanceof Beq) {
                // add $fp, $zero, $t1
                // sw $fp, 4($v1)
                Add add = (Add) curIns;
                Beq beq = (Beq) nextIns;
                int beqSrc1 = beq.getSrc1();
                int beqSrc2 = beq.getSrc2();
                int addDst = add.getDst();
                int addSrc1 = add.getSrc1();
                int addSrc2 = add.getSrc2();
                if (tmpRegs.contains(beqSrc1) && beqSrc1 == addDst && addSrc1 == Reg.ZERO) {
                    beq.setSrc1(addSrc2);
                    mipsIns.remove(cur);
                    continue;
                }
            }
            // add-la
            else if (curIns instanceof Add && nextIns instanceof La) {
                // add $fp, $zero, $t4
                //	la $v1, g_record($fp)
                Add add = (Add) curIns;
                La la = (La) nextIns;
                int addDst = add.getDst();
                int addSrc1 = add.getSrc1();
                int addSrc2 = add.getSrc2();
                if (tmpRegs.contains(la.getBase()) && la.getBase() == addDst && addSrc1 == Reg.ZERO) {
                    mipsIns.remove(next);
                    mipsIns.remove(cur);
                    mipsIns.add(cur, new La(la.getDst(), la.getLabel(), addSrc2));
                }
            }
            // 否则++i
            ++cur;
        }
    }
}
