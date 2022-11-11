package front.lexical;

import java.util.HashMap;

import util.TkType;

public class Token {
    public static final HashMap<String, TkType> KEYWORDS = new HashMap<String, TkType>() {{
            put("main", TkType.MAINTK);
            put("const", TkType.CONSTTK);
            put("int", TkType.INTTK);
            put("break", TkType.BREAKTK);
            put("continue", TkType.CONTINUETK);
            put("if", TkType.IFTK);
            put("else", TkType.ELSETK);
            put("while", TkType.WHILETK);
            put("getint", TkType.GETINTTK);
            put("printf", TkType.PRINTFTK);
            put("return", TkType.RETURNTK);
            put("void", TkType.VOIDTK);
        }};

    private final TkType tkType;
    private final String str;

    public Token(TkType tkType, String str) {
        this.tkType = tkType;
        this.str = str;
    }

    public TkType getType() {
        return tkType;
    }

    public boolean eqType(TkType tkType) {
        return tkType.equals(this.tkType);
    }

    public String getName() {
        return str;
    }

    @Override
    public String toString() {
        return tkType.name() + " " + str;
    }
}
