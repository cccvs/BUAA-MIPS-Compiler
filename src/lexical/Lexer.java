package lexical;

import java.util.ArrayList;
import java.util.List;

import util.TkType;

public class Lexer {
    private final String inputStr;
    private final int length;

    private int pos;
    private String curToken;
    private final List<Token> tokens;

    public Lexer(String inputStr) {
        this.inputStr = inputStr;
        this.length = inputStr.length();

        this.pos = 0;
        this.curToken = "";
        this.tokens = new ArrayList<>();
    }

    public void lex() {
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
            ++pos;
            if (pos >= length) {
                return;
            }
        }
    }

    private void lexIdentifier() {
        int beginIndex = pos++;
        while (Character.isLetter(inputStr.charAt(pos)) ||
                Character.isDigit(inputStr.charAt(pos)) || inputStr.charAt(pos) == '_') {
            ++pos;
        }
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, TkType.IDENFR), curToken));
    }

    private void lexInt() {
        int beginIndex = pos++;
        while (Character.isDigit(inputStr.charAt(pos))) {
            ++pos;
        }
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, TkType.INTCON), curToken));
    }

    private void lexStr() {
        int beginIndex = pos++;
        while (inputStr.charAt(pos) != '\"') {
            ++pos;
        }
        ++pos;
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, TkType.STRCON), curToken));
    }

    private void lexSingle() {
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
        tokens.add(new Token(tkType, curToken));
        pos++;
    }

    private void lexDouble() {
        char c = inputStr.charAt(pos);
        String str = inputStr.substring(pos, pos + 2);
        if (c == '&') {
            if (str.equals("&&")) {
                tokens.add(new Token(TkType.AND, str));
            } else {
                error();
            }
        } else if (c == '|') {
            if (str.equals("||")) {
                tokens.add(new Token(TkType.OR, str));
            } else {
                error();
            }
        }
        pos += 2;
    }

    private void lexMix() {
        char c = inputStr.charAt(pos);
        String str = inputStr.substring(pos, pos + 2);
        switch (c) {
            case '<':
                if (str.equals("<=")) {
                    tokens.add(new Token(TkType.LEQ, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.LSS, String.valueOf(c)));
                    pos += 1;
                }
                break;
            case '>':
                if (str.equals(">=")) {
                    tokens.add(new Token(TkType.GEQ, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.GRE, String.valueOf(c)));
                    pos += 1;
                }
                break;
            case '=':
                if (str.equals("==")) {
                    tokens.add(new Token(TkType.EQL, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.ASSIGN, String.valueOf(c)));
                    pos += 1;
                }
                break;
            case '!':
                if (str.equals("!=")) {
                    tokens.add(new Token(TkType.NEQ, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(TkType.NOT, String.valueOf(c)));
                    pos += 1;
                }
                break;
            default:
                error();
        }
    }

    private void lexDiv() {
        if (pos >= length - 1 || "/*".indexOf(inputStr.charAt(pos + 1)) == -1) {
            tokens.add(new Token(TkType.DIV, "/"));
            ++pos;
        } else {
            ++pos;
            if (inputStr.charAt(pos) == '/') {
                while (inputStr.charAt(pos) != '\n' && inputStr.charAt(pos) != '\r') {
                    ++pos;
                }
                ++pos;
            } else if (inputStr.charAt(pos) == '*') {
                do {
                    ++pos;
                    if (pos >= length - 1) {
                        error();
                        return;
                    }
                } while (!inputStr.startsWith("*/", pos));
                pos += 2;
            }
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private void error() {
        ++pos;
        System.out.println("error in lexer!");
    }
}
