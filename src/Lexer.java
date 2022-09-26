import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Lexer {
    private final PrintStream out;

    private final String inputStr;
    private final int length;

    private int pos;
    private String curToken;
    private final ArrayList<Token> tokens;

    public Lexer(Scanner in, PrintStream out) {
        this.out = out;

        this.inputStr = readAll(in);
        this.length = inputStr.length();

        this.pos = 0;
        this.curToken = "";
        this.tokens = new ArrayList<>();
    }

    public String readAll(Scanner in) {
        StringBuilder sb = new StringBuilder();
        // nextLine() doesn't include last '\n'
        while (in.hasNextLine()) {
            sb.append(in.nextLine());
            sb.append('\n');
        }
        return sb.toString();
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
        outputAll();
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
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, Token.Type.IDENFR), curToken));
    }

    private void lexInt() {
        int beginIndex = pos++;
        while (Character.isDigit(inputStr.charAt(pos))) {
            ++pos;
        }
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, Token.Type.INTCON), curToken));
    }

    private void lexStr() {
        int beginIndex = pos++;
        while (inputStr.charAt(pos) != '\"') {
            ++pos;
        }
        ++pos;
        curToken = inputStr.substring(beginIndex, pos);
        tokens.add(new Token(Token.KEYWORDS.getOrDefault(curToken, Token.Type.STRCON), curToken));
    }

    private void lexSingle() {
        char c = inputStr.charAt(pos);
        Token.Type type = null;
        switch (c) {
            case '+':
                type = Token.Type.PLUS;
                break;
            case '-':
                type = Token.Type.MINU;
                break;
            case '*':
                type = Token.Type.MULT;
                break;
            case '%':
                type = Token.Type.MOD;
                break;
            case ';':
                type = Token.Type.SEMICN;
                break;
            case ',':
                type = Token.Type.COMMA;
                break;
            case '(':
                type = Token.Type.LPARENT;
                break;
            case ')':
                type = Token.Type.RPARENT;
                break;
            case '[':
                type = Token.Type.LBRACK;
                break;
            case ']':
                type = Token.Type.RBRACK;
                break;
            case '{':
                type = Token.Type.LBRACE;
                break;
            case '}':
                type = Token.Type.RBRACE;
                break;
            default:
                error();
        }
        curToken = String.valueOf(c);
        tokens.add(new Token(type, curToken));
        pos++;
    }

    private void lexDouble() {
        char c = inputStr.charAt(pos);
        String str = inputStr.substring(pos, pos + 2);
        if (c == '&') {
            if (str.equals("&&")) {
                tokens.add(new Token(Token.Type.AND, str));
            } else {
                error();
            }
        } else if (c == '|') {
            if (str.equals("||")) {
                tokens.add(new Token(Token.Type.OR, str));
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
                    tokens.add(new Token(Token.Type.LEQ, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(Token.Type.LSS, String.valueOf(c)));
                    pos += 1;
                }
                break;
            case '>':
                if (str.equals(">=")) {
                    tokens.add(new Token(Token.Type.GEQ, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(Token.Type.GRE, String.valueOf(c)));
                    pos += 1;
                }
                break;
            case '=':
                if (str.equals("==")) {
                    tokens.add(new Token(Token.Type.EQL, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(Token.Type.ASSIGN, String.valueOf(c)));
                    pos += 1;
                }
                break;
            case '!':
                if (str.equals("!=")) {
                    tokens.add(new Token(Token.Type.NEQ, str));
                    pos += 2;
                } else {
                    tokens.add(new Token(Token.Type.NOT, String.valueOf(c)));
                    pos += 1;
                }
                break;
            default:
                error();
        }
    }

    private void lexDiv() {
        if (pos >= length - 1 || "/*".indexOf(inputStr.charAt(pos + 1)) == -1) {
            tokens.add(new Token(Token.Type.DIV, "/"));
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

    private void outputAll() {
        for (Token token : tokens) {
            out.println(token);
        }
        out.flush();
    }

    private void error() {
        ++pos;
        System.out.println("error in lexer!");
    }
}
