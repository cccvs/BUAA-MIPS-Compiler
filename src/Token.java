import java.util.HashMap;

public class Token {
    public static final HashMap<String, Type> KEYWORDS = new HashMap<String, Type>() {{
                put("main", Type.MAINTK);
                put("const", Type.CONSTTK);
                put("int", Type.INTTK);
                put("break", Type.BREAKTK);
                put("continue", Type.CONTINUETK);
                put("if", Type.IFTK);
                put("else", Type.ELSETK);
                put("while", Type.WHILETK);
                put("getint", Type.GETINTTK);
                put("printf", Type.PRINTFTK);
                put("return", Type.RETURNTK);
                put("void", Type.VOIDTK);
            }};

    public enum Type {
        // free choice of input
        IDENFR, INTCON, STRCON,
        // key word
        MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK,
        WHILETK, GETINTTK, PRINTFTK, RETURNTK,
        VOIDTK,
        // not identifier
        NOT, AND, OR,
        PLUS, MINU,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
        ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE;
    }

    private final Type type;
    private final String str;

    public Token(Type type, String str) {
        this.type = type;
        this.str = str;
    }

    @Override
    public String toString() {
        return type.name() + " " + str;
    }
}
