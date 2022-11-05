package back;

import ir.MidCode;

public class MipsTranslator {
    MidCode midCode;
    RegAllocator regAllocator = new RegAllocator();

    public MipsTranslator(MidCode midCode) {
        this.midCode = midCode;
    }
}
