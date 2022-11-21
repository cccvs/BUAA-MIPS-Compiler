package exception;

public class SysYError extends Exception implements Comparable<SysYError> {
    public static final String ILLEGAL_STRING = "a";
    public static final String DUPLICATED_IDENT = "b";
    public static final String UNDEFINED_IDENT = "c";
    public static final String MISSING_SEMICOLON = "i";
    public static final String MISSING_RIGHT_PARENT = "j";
    public static final String MISSING_RIGHT_BRACKET = "k";

    private final String type;
    private final int line;

    public SysYError(String type, int line) {
        this.type = type;
        this.line = line;
    }

    @Override
    public int compareTo(SysYError o) {
        return this.line < o.line ? -1 : 1;
    }

    @Override
    public String toString() {
        return line + " " + type;
    }
}
