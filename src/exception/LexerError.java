package exception;

public class LexerError extends Exception {
    private final int line;
    private final char c;

    public LexerError(int line, char c) {
        this.line = line;
        this.c = c;
    }

    @Override
    public String toString() {
        return "code line " + line + ": unexpected char[" + c + "]";
    }
}
