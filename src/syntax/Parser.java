package syntax;

import lexical.Lexer;
import lexical.Token;
import util.TkType;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import static util.Constant.OUTPUT_FILE;

public class Parser {
    private ArrayList<Token> tokens;
    private int pos;
    // private CompUnitNode root;
    private PrintStream out;

    public Parser(Lexer lexer) throws FileNotFoundException {
        this.tokens = lexer.getTokens();
        this.pos = 0;
        this.out = new PrintStream(OUTPUT_FILE);
        // this.root = parseCompUnit();
    }

    private void next(TkType type) {
        if (tokens.get(pos).eqType(type)) {
            out.println(tokens.get(pos).toString());
            ++pos;
        } else {
            error(type);
        }

    }

    public void parseCompUnit() {
        // CompUnitNode compUnitNode = new CompUnitNode();
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        // Decl -> ConstDecl | VarDecl
        while (isConstDecl() || isVarDecl()) {
            if (isConstDecl()) {
                parseConstDecl();
            } else {
                parseVarDecl();
            }
            // no need to print <Decl>
        }
        while (isFuncDef()) {
            parseFuncDef();
        }
        parseMainFuncDef();
        out.println("<CompUnit>");
        // return compUnitNode;
    }

    // 1 decl part
    private void parseConstDecl() {
        // ConstDeclNode constDeclNode = new ConstDeclNode();
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        next(TkType.CONSTTK);
        next(TkType.INTTK);
        parseConstDef();
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            parseConstDef();
        }
        next(TkType.SEMICN);
        out.println("<ConstDecl>");
        // return constDeclNode;
    }

    private void parseConstDef() {
        // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
        next(TkType.IDENFR);
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            parseConstExp();
            next(TkType.RBRACK);
        }
        next(TkType.ASSIGN);
        parseConstInitVal();
        out.println("<ConstDef>");
    }

    private void parseConstInitVal() {
        // ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (!tokens.get(pos).eqType(TkType.LBRACE)) {
            parseConstExp();
        } else {
            next(TkType.LBRACE);
            if (!tokens.get(pos).eqType(TkType.RBRACE)) {
                parseConstInitVal();
                while (tokens.get(pos).eqType(TkType.COMMA)) {
                    next(TkType.COMMA);
                    parseConstInitVal();
                }
            }
            next(TkType.RBRACE);
        }
        out.println("<ConstInitVal>");
    }

    private void parseConstExp() {
        // Ident here need to be constant! need to be refactored next time!
        parseAddExp();
        out.println("<ConstExp>");
    }

    private void parseVarDecl() {
        // VarDeclNode varDeclNode = new VarDeclNode();
        // VarDecl -> BType VarDef { ',' VarDef } ';'
        next(TkType.INTTK);
        parseVarDef();
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            parseVarDef();
        }
        next(TkType.SEMICN);
        out.println("<VarDecl>");
        // return varDeclNode;
    }

    private void parseVarDef() {
        // VarDef -> Ident { '[' ConstExp ']' } ['=' InitVal]
        next(TkType.IDENFR);
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            parseConstExp();
            next(TkType.RBRACK);
        }
        if (tokens.get(pos).eqType(TkType.ASSIGN)) {
            next(TkType.ASSIGN);
            parseInitVal();
        }
        out.println("<VarDef>");
    }

    private void parseInitVal() {
        // InitVal ->  Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if (!tokens.get(pos).eqType(TkType.LBRACE)) {
            parseExp();
        } else {
            next(TkType.LBRACE);
            if (!tokens.get(pos).eqType(TkType.RBRACE)) {
                parseInitVal();
                while (tokens.get(pos).eqType(TkType.COMMA)) {
                    next(TkType.COMMA);
                    parseInitVal();
                }
            }
            next(TkType.RBRACE);
        }
        out.println("<InitVal>");
    }

    // 2 func part
    private void parseFuncDef() {
        // FuncDefNode funcDefNode = new FuncDefNode(false);
        parseFuncType();
        next(TkType.IDENFR);
        next(TkType.LPARENT);
        if (!tokens.get(pos).eqType(TkType.RPARENT)) {
            parseFuncFParams();
        }
        next(TkType.RPARENT);
        parseBlock();
        out.println("<FuncDef>");
        // return funcDefNode;
    }

    private void parseMainFuncDef() {
        // FuncDefNode funcDefNode = new FuncDefNode(true);
        next(TkType.INTTK);
        next(TkType.MAINTK);
        next(TkType.LPARENT);
        next(TkType.RPARENT);
        parseBlock();
        out.println("<MainFuncDef>");
        // return funcDefNode;
    }

    private void parseFuncType() {
        if (tokens.get(pos).eqType(TkType.VOIDTK)) {
            next(TkType.VOIDTK);
        } else {
            next(TkType.INTTK);
        }
        out.println("<FuncType>");
    }

    private void parseFuncFParams() {
        parseFuncFParam();
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            parseFuncFParam();
        }
        out.println("<FuncFParams>");
    }

    private void parseFuncFParam() {
        next(TkType.INTTK);
        next(TkType.IDENFR);
        if (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            next(TkType.RBRACK);
            while (tokens.get(pos).eqType(TkType.LBRACK)) {
                next(TkType.LBRACK);
                parseConstExp();
                next(TkType.RBRACK);
            }
        }
        out.println("<FuncFParam>");
    }

    // 3 blk and stmt
    private void parseBlock() {
        next(TkType.LBRACE);
        while (!tokens.get(pos).eqType(TkType.RBRACE)) {
            if (isConstDecl()) {
                parseConstDecl();
            } else if (isVarDecl()) {
                parseVarDecl();
            } else {
                parseStmt();
            }
        }
        next(TkType.RBRACE);
        out.println("<Block>");
    }

    private void parseStmt() {
        // Block
        if (tokens.get(pos).eqType(TkType.LBRACE)) {
            parseBlock();
        }
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        else if (tokens.get(pos).eqType(TkType.IFTK)) {
            next(TkType.IFTK);
            next(TkType.LPARENT);
            parseCond();
            next(TkType.RPARENT);
            parseStmt();
            while (tokens.get(pos).eqType(TkType.ELSETK)) {
                next(TkType.ELSETK);
                parseStmt();
            }
        }
        // 'while' '(' Cond ')' Stmt
        else if (tokens.get(pos).eqType(TkType.WHILETK)) {
            next(TkType.WHILETK);
            next(TkType.LPARENT);
            parseCond();
            next(TkType.RPARENT);
            parseStmt();
        }
        // 'break' ';'
        else if (tokens.get(pos).eqType(TkType.BREAKTK)) {
            next(TkType.BREAKTK);
            next(TkType.SEMICN);
        }
        // 'continue' ';'
        else if (tokens.get(pos).eqType(TkType.CONTINUETK)) {
            next(TkType.CONTINUETK);
            next(TkType.SEMICN);
        }
        // 'return' [Exp] ';'
        else if (tokens.get(pos).eqType(TkType.RETURNTK)) {
            next(TkType.RETURNTK);
            if (!tokens.get(pos).eqType(TkType.SEMICN)) {
                parseExp();
            }
            next(TkType.SEMICN);
        }
        // 'printf' '(' FormatString {',' Exp} ')' ';'
        else if (tokens.get(pos).eqType(TkType.PRINTFTK)) {
            next(TkType.PRINTFTK);
            next(TkType.LPARENT);
            next(TkType.STRCON);
            while (tokens.get(pos).eqType(TkType.COMMA)) {
                next(TkType.COMMA);
                parseExp();
            }
            next(TkType.RPARENT);
            next(TkType.SEMICN);
        }
        // LVal '=' Exp ';'
        // LVal '=' 'getint''('')'';'
        // Exp -> LVal
        // Ident '(' [FuncRParams] ')'
        else if (isAssign()) {
            parseLVal();
            next(TkType.ASSIGN);
            if (tokens.get(pos).eqType(TkType.GETINTTK)) {
                next(TkType.GETINTTK);
                next(TkType.LPARENT);
                next(TkType.RPARENT);
            } else {
                parseExp();
            }
            next(TkType.SEMICN);
        }
        // [Exp] ';'
        else {
            if (!tokens.get(pos).eqType(TkType.SEMICN)) {
                parseExp();
            }
            next(TkType.SEMICN);
        }
        out.println("<Stmt>");
    }

    // 4 exp part
    private void parseExp() {
        parseAddExp();
        out.println("<Exp>");
    }

    private void parseCond() {
        parseLOrExp();
        out.println("<Cond>");
    }

    private void parseLVal() {
        next(TkType.IDENFR);
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            parseExp();
            next(TkType.RBRACK);
        }
        out.println("<LVal>");
    }

    private void parseNumber() {
        next(TkType.INTCON);
        out.println("<Number>");
    }

    private void parseUnaryExp() {
        // {UnaryOp} (PrimaryExp | Ident '(' [FuncRParams] ')')
        // {UnaryOp}
        int unaryOpCnt = 0;
        while (isUnaryOp()) {
            ++unaryOpCnt;
            parseUnaryOp();
        }
        // Ident '(' [FuncRParams] ')'
        if (pos + 1 < tokens.size() &&
                tokens.get(pos).eqType(TkType.IDENFR) &&
                tokens.get(pos + 1).eqType(TkType.LPARENT)) {
            next(TkType.IDENFR);
            next(TkType.LPARENT);
            if (!tokens.get(pos).eqType(TkType.RPARENT)) {
                parseFuncRParams();
            }
            next(TkType.RPARENT);
        }
        // PrimaryExp -> '(' Exp ')' | LVal | Number
        else {
            if (tokens.get(pos).eqType(TkType.INTCON)) {
                parseNumber();
            } else if (tokens.get(pos).eqType(TkType.LPARENT)) {
                next(TkType.LPARENT);
                parseExp();
                next(TkType.RPARENT);
            } else {
                parseLVal();
            }
            out.println("<PrimaryExp>");
        }
        while (unaryOpCnt >= 0) {
            out.println("<UnaryExp>");
            --unaryOpCnt;
        }
    }

    private void parseUnaryOp() {
        if (tokens.get(pos).eqType(TkType.PLUS)) {
            next(TkType.PLUS);
        } else if (tokens.get(pos).eqType(TkType.MINU)) {
            next(TkType.MINU);
        } else {
            next(TkType.NOT);
        }
        out.println("<UnaryOp>");
    }

    private void parseFuncRParams() {
        parseExp();
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            parseExp();
        }
        out.println("<FuncRParams>");
    }

    private void parseMulExp() {
        parseUnaryExp();
        while (isMulLink()) {
            out.println("<MulExp>");
            if (tokens.get(pos).eqType(TkType.MULT)) {
                next(TkType.MULT);
            } else if (tokens.get(pos).eqType(TkType.DIV)) {
                next(TkType.DIV);
            } else {
                next(TkType.MOD);
            }
            parseUnaryExp();
        }
        out.println("<MulExp>");
    }

    private void parseAddExp() {
        parseMulExp();
        while (isAddLink()) {
            out.println("<AddExp>");
            if (tokens.get(pos).eqType(TkType.PLUS)) {
                next(TkType.PLUS);
            } else {
                next(TkType.MINU);
            }
            parseMulExp();
        }
        out.println("<AddExp>");
    }

    private void parseRelExp() {
        parseAddExp();
        while (isRelLink()) {
            out.println("<RelExp>");
            if (tokens.get(pos).eqType(TkType.LSS)) {
                next(TkType.LSS);
            } else if (tokens.get(pos).eqType(TkType.GRE)) {
                next(TkType.GRE);
            } else if (tokens.get(pos).eqType(TkType.LEQ)) {
                next(TkType.LEQ);
            } else {
                next(TkType.GEQ);
            }
            parseAddExp();
        }
        out.println("<RelExp>");
    }

    private void parseEqExp() {
        parseRelExp();
        while (isEqLink()) {
            out.println("<EqExp>");
            if (tokens.get(pos).eqType(TkType.EQL)) {
                next(TkType.EQL);
            } else {
                next(TkType.NEQ);
            }
            parseRelExp();
        }
        out.println("<EqExp>");
    }

    private void parseLAndExp() {
        parseEqExp();
        while (tokens.get(pos).eqType(TkType.AND)) {
            out.println("<LAndExp>");
            next(TkType.AND);
            parseEqExp();
        }
        out.println("<LAndExp>");
    }

    private void parseLOrExp() {
        parseLAndExp();
        while (tokens.get(pos).eqType(TkType.OR)) {
            out.println("<LOrExp>");
            next(TkType.OR);
            parseLAndExp();
        }
        out.println("<LOrExp>");
    }

    private boolean isConstDecl() {
        return tokens.get(pos).eqType(TkType.CONSTTK);
    }

    private boolean isVarDecl() {
        return pos + 2 < tokens.size() &&
                tokens.get(pos).eqType(TkType.INTTK) &&
                tokens.get(pos + 1).eqType(TkType.IDENFR) &&
                !tokens.get(pos + 2).eqType(TkType.LPARENT);
    }

    private boolean isUnaryOp() {
        return tokens.get(pos).eqType(TkType.PLUS) ||
                tokens.get(pos).eqType(TkType.MINU) ||
                tokens.get(pos).eqType(TkType.NOT);
    }

    private boolean isMulLink() {
        return tokens.get(pos).eqType(TkType.MULT) ||
                tokens.get(pos).eqType(TkType.DIV) ||
                tokens.get(pos).eqType(TkType.MOD);
    }

    private boolean isAddLink() {
        return tokens.get(pos).eqType(TkType.PLUS) ||
                tokens.get(pos).eqType(TkType.MINU);
    }

    private boolean isRelLink() {
        return tokens.get(pos).eqType(TkType.LSS) ||
                tokens.get(pos).eqType(TkType.GRE) ||
                tokens.get(pos).eqType(TkType.LEQ) ||
                tokens.get(pos).eqType(TkType.GEQ);
    }

    private boolean isEqLink() {
        return tokens.get(pos).eqType(TkType.EQL) ||
                tokens.get(pos).eqType(TkType.NEQ);
    }

    private boolean isFuncDef() {
        return pos + 2 < tokens.size() &&
                (tokens.get(pos).eqType(TkType.INTTK) || tokens.get(pos).eqType(TkType.VOIDTK)) &&
                tokens.get(pos + 1).eqType(TkType.IDENFR) &&
                tokens.get(pos + 2).eqType(TkType.LPARENT);
    }

    private boolean isAssign() {
        int movPos = pos + 1;
        if (!tokens.get(pos).eqType(TkType.IDENFR)) {
            return false;
        }
        while (!tokens.get(movPos).eqType(TkType.SEMICN) &&
                !tokens.get(movPos).eqType(TkType.ASSIGN) &&
                movPos < tokens.size()) {
            ++movPos;
        }
        if (movPos >= tokens.size()) {
            error(TkType.MAINTK);
            return false;
        } else {
            return tokens.get(movPos).eqType(TkType.ASSIGN);
        }
    }

    private void error(TkType type) {
        System.out.println("error!");
        System.out.println(pos);
        System.out.println("expect " + type + " get " + tokens.get(pos).getType());
    }
}
