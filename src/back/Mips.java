package back;

import back.ins.MipsIns;

import java.util.ArrayList;
import java.util.List;

public class Mips {
    private static List<MipsIns> mipsInsList = new ArrayList<>();

    public Mips() {

    }

    public static void append(MipsIns ins) {
        mipsInsList.add(ins);
    }
}
