package back;

import back.ins.MipsIns;
import ir.MidCode;

import java.util.ArrayList;
import java.util.List;

public class MipsTranslator {
    MidCode midCode;
    RegAllocator regAllocator = new RegAllocator();
    private static List<MipsIns> mipsInsList = new ArrayList<>();


    public MipsTranslator(MidCode midCode) {
        this.midCode = midCode;
    }
}
