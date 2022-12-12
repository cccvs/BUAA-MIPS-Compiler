package front.lexical;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import exception.ErrorTable;
import exception.LexerError;
import exception.SysYError;
import front.TkType;

public class Lexer {
    private final String inputStr;
    private final int length;

    private int pos;
    private int curLine;
    private String curToken;
    private final List<Token> tokens;

    public Lexer(String inputStr) {
        this.inputStr = inputStr;
        this.length = inputStr.length();
        this.pos = 0;
        this.curLine = 1;
        this.curToken = "";
        this.tokens = new ArrayList<>();
    }

    public void lex() throws LexerError {
        while (pos < length) {
            skipSpace();
            if (pos >= length) {
                break;
            }
            curToken = "";
            char c = inputStr.charAt(pos);
            if (Character.isLetter(c) || c == '_') {
                lexIdentifier();
            } else if (Character.isDigit(c)) {
                lexInt();
            } else if (c == '\"') {
                lexStr();
            } else if ("+-*%;,()[]{}".indexOf(c) != -1) {
                lexSingle();
            } else if ("&|".indexOf(c) != -1) {
                lexDouble();
            } else if ("<>=!".indexOf(c) != -1) {
                lexMix();
            } else if (c == '/') {
                lexDiv();
            } else {
                error();
            }
        }
    }

    private void skipSpace() {
        if (pos >= length) {
            return;
        }
        while ("\n\r \t".indexOf(inputStr.charAt(pos)) != -1) {
            next();
            if (pos >= length) {
                return;
            }
        }
    }

    private void checkIllegalString(String str) {
        for (int i = 0; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (!(curChar == 32 || curChar == 33 || curChar == '%' || (40 <= curChar && curChar <= 126))) {
                ErrorTable.append(new SysYError(SysYError.ILLEGAL_STRING, curLine));
                return;
            }
            if (curChar == '%' && (i >= str.length() - 1 || str.charAt(i + 1) != 'd')) {
                ErrorTable.append(new SysYError(SysYError.ILLEGAL_STRING, curLine));
                return;
            }
            if (curChar == '\\' && (i >= str.length() - 1 || str.charAt(i + 1) != 'n')) {
                ErrorTable.append(new SysYError(SysYError.ILLEGAL_STRING, curLine));
                return;
            }
        }
    }

    private void lexIdentifier() {
        int beginIndex = pos;
        next();
        while (Character.isLetter(inputStr.charAt(pos)) ||
                Character.isDigit(inputStr.charAt(pos)) || inputStr.charAt(pos) == '_') {
            next();
        }
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, TkType.IDENFR), curToken, curLine));
    }

    private void lexInt() {
        int beginIndex = pos;
        next();
        while (Character.isDigit(inputStr.charAt(pos))) {
            next();
        }
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, TkType.INTCON), curToken, curLine));
    }

    private void lexStr() {
        int beginIndex = pos;
        next();
        while (inputStr.charAt(pos) != '\"') {
            next();
        }
        next();
        curToken = inputStr.substring(beginIndex, pos);
        checkIllegalString(curToken.substring(1, curToken.length() - 1));
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, TkType.STRCON), curToken, curLine));
    }

    private void lexSingle() throws LexerError {
        char c = inputStr.charAt(pos);
        TkType tkType = null;
        switch (c) {
            case '+':
                tkType = TkType.PLUS;
                break;
            case '-':
                tkType = TkType.MINU;
                break;
            case '*':
                tkType = TkType.MULT;
                break;
            case '%':
                tkType = TkType.MOD;
                break;
            case ';':
                tkType = TkType.SEMICN;
                break;
            case ',':
                tkType = TkType.COMMA;
                break;
            case '(':
                tkType = TkType.LPARENT;
                break;
            case ')':
                tkType = TkType.RPARENT;
                break;
            case '[':
                tkType = TkType.LBRACK;
                break;
            case ']':
                tkType = TkType.RBRACK;
                break;
            case '{':
                tkType = TkType.LBRACE;
                break;
            case '}':
                tkType = TkType.RBRACE;
                break;
            default:
                error();
        }
        curToken = String.valueOf(c);
        tokens.add(new Token(tkType, curToken, curLine));
        pos++;
    }

    private void lexDouble() throws LexerError {
        char c = inputStr.charAt(pos);
        String str = inputStr.substring(pos, pos + 2);
        if (c == '&') {
            if (str.equals("&&")) {
                tokens.add(new Token(TkType.AND, str, curLine));
            } else {
                error();
            }
        } else if (c == '|') {
            if (str.equals("||")) {
                tokens.add(new Token(TkType.OR, str, curLine));
            } else {
                error();
            }
        }
        pos += 2;
    }

    private void lexMix() throws LexerError {
        char c = inputStr.charAt(pos);
        String str = inputStr.substring(pos, pos + 2);
        switch (c) {
            case '<':
                if (str.equals("<=")) {
                    tokens.add(new Token(TkType.LEQ, str, curLine));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.LSS, String.valueOf(c), curLine));
                    pos += 1;
                }
                break;
            case '>':
                if (str.equals(">=")) {
                    tokens.add(new Token(TkType.GEQ, str, curLine));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.GRE, String.valueOf(c), curLine));
                    pos += 1;
                }
                break;
            case '=':
                if (str.equals("==")) {
                    tokens.add(new Token(TkType.EQL, str, curLine));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.ASSIGN, String.valueOf(c), curLine));
                    pos += 1;
                }
                break;
            case '!':
                if (str.equals("!=")) {
                    tokens.add(new Token(TkType.NEQ, str, curLine));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.NOT, String.valueOf(c), curLine));
                    pos += 1;
                }
                break;
            default:
                error();
        }
    }

    private void lexDiv() throws LexerError {
        if (pos >= length - 1 || "/*".indexOf(inputStr.charAt(pos + 1)) == -1) {
            tokens.add(new Token(TkType.DIV, "/", curLine));
            next();
        } else {
            next();
            if (inputStr.charAt(pos) == '/') {
                while (inputStr.charAt(pos) != '\n' && inputStr.charAt(pos) != '\r') {
                    next();
                }
                next();
            } else if (inputStr.charAt(pos) == '*') {
                do {
                    next();
                    if (pos >= length - 1) {
                        error();
                    }
                } while (!inputStr.startsWith("*/", pos));
                next();
                next();
            }
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void outputTokens(PrintStream ps, boolean execute) {
        if (!execute) {
            return;
        }
        for (Token token : tokens) {
            ps.println(token);
        }
    }

    public void next() {
        if (inputStr.charAt(pos) == '\n' || inputStr.charAt(pos) == '\r') {
            ++curLine;
        }
        ++pos;
    }

    private void error() throws LexerError {
        throw new LexerError(curLine, inputStr.charAt(pos));
    }
}
