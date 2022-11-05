package back;

import ir.operand.Operand;
import ir.operand.Symbol;
import ir.operand.MidVar;

import java.util.*;

public class RegAllocator {
    private final HashSet<Integer> allocatableRegs = new HashSet<>
            (Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25));
    private static final String[] REGS = new String[] {
            "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "t8", "t9",
            "k0", "k1", "gp", "sp", "fp", "ra"
    };

    private final Map<Integer, Integer> regMap = new LinkedHashMap<>();
    private final HashSet<Integer> freeRegs = new HashSet<>();

    public RegAllocator() {
        reset();
    }

    public void freeReg(MidVar var) {
        int reg = regMap.remove(var.getId());
        freeRegs.add(reg);
    }

    public Integer allocReg(MidVar var) {
        if (freeRegs.isEmpty()) {
            return null;
        }
        int reg = freeRegs.iterator().next();
        freeRegs.remove(reg);
        regMap.put(var.getId(), reg);
        return reg;
    }

    public boolean hasFreeReg() {
        return !freeRegs.isEmpty();
    }

    public Integer getReg(MidVar var) {
        int id = var.getId();
        return regMap.getOrDefault(id, null);
    }

    // util
    private void reset() {
        freeRegs.clear();
        regMap.clear();
        freeRegs.addAll(allocatableRegs);
    }

    public static String name(int regNum) {
        assert 0 <= regNum && regNum <= 31;
        return REGS[regNum];
    }
}
