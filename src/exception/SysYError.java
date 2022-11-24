package exception;

public class SysYError extends Exception implements Comparable<SysYError> {
    public static final String ILLEGAL_STRING = "a";
    public static final String DUPLICATED_IDENT = "b";
    public static final String UNDEFINED_IDENT = "c";
    public static final String MISMATCHED_PARAM_NUM = "d";
    public static final String MISMATCHED_PARAM_TYPE = "e";
    public static final String MISMATCHED_RETURN = "f";
    public static final String MISSING_RETURN = "g";
    public static final String MODIFIED_CONSTANT = "h";
    public static final String MISSING_SEMICOLON = "i";
    public static final String MISSING_RIGHT_PARENT = "j";
    public static final String MISSING_RIGHT_BRACKET = "k";
    public static final String MISMATCHED_PRINTF = "l";
    public static final String CTRL_OUTSIDE_LOOP = "m";

    private final String type;
    private final int line;

    public SysYError(String type, int line) {
        this.type = type;
        this.line = line;
    }

    @Override
    public int compareTo(SysYError o) {
        if (this.line != o.line) {
            return this.line < o.line ? -1 : 1;
        } else {
            return this.type.compareTo(o.type);
        }
    }

    @Override
    public String toString() {
        return line + " " + type;
    }
}
