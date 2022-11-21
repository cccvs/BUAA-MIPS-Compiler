package exception;

import front.TkType;
import front.lexical.Token;

public class ParserError extends Exception {
    private final Token recvToken;
    private final TkType expectType;

    public ParserError(Token recvToken, TkType expectType) {
        this.recvToken = recvToken;
        this.expectType = expectType;
    }

    public TkType getExpectType() {
        return expectType;
    }

    @Override
    public String toString() {
        return "code line " + recvToken.getLine() +
                ": expect " + expectType + ", get " + recvToken.getType();
    }
}
