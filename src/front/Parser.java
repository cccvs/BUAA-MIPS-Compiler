package front;

import front.ast.CompUnitNode;
import front.ast.decl.DeclNode;
import front.ast.decl.DefNode;
import front.ast.exp.BinaryExpNode;
import front.ast.exp.ExpNode;
import front.ast.exp.FuncCallNode;
import front.ast.exp.LValNode;
import front.ast.exp.NumNode;
import front.ast.exp.UnaryExpNode;
import front.ast.func.FuncDefNode;
import front.ast.func.FuncFParamNode;
import front.ast.stmt.AssignNode;
import front.ast.stmt.BlockNode;
import front.ast.stmt.BreakNode;
import front.ast.stmt.ContinueNode;
import front.ast.stmt.BranchNode;
import front.ast.stmt.LoopNode;
import front.ast.stmt.PrintfNode;
import front.ast.stmt.ReturnNode;
import front.ast.stmt.StmtNode;
import front.lexical.Lexer;
import front.lexical.Token;
import util.TkType;

import java.io.PrintStream;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos;
    private final List<String> outStrings = new ArrayList<>();

    public Parser(Lexer lexer) {
        this.tokens = lexer.getTokens();
        this.pos = 0;
    }

    public void outputSyntax(PrintStream ps) {
        for (String outString : outStrings) {
            ps.println(outString);
        }
    }

    private void next(TkType type) {
        if (tokens.get(pos).eqType(type)) {
            outStrings.add(tokens.get(pos).toString());
            ++pos;
        } else {
            error(type);
            System.exit(2);
        }
    }

    public CompUnitNode parseCompUnit() {
        CompUnitNode compUnitNode = new CompUnitNode();
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        // Decl -> ConstDecl | VarDecl
        while (isConstDecl() || isVarDecl()) {
            if (isConstDecl()) {
                compUnitNode.addDecl(parseConstDecl());
            } else {
                compUnitNode.addDecl(parseVarDecl());
            }
            // no need to print <Decl>
        }
        while (isFuncDef()) {
            compUnitNode.addFuncDef(parseFuncDef());
        }
        compUnitNode.setMainFuncDef(parseMainFuncDef());
        outStrings.add("<CompUnit>");
        return compUnitNode;
    }

    // 1 decl part
    private DeclNode parseConstDecl() {
        DeclNode constDeclNode = new DeclNode(true);
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        next(TkType.CONSTTK);
        next(TkType.INTTK);
        constDeclNode.addDefs(parseConstDef());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            constDeclNode.addDefs(parseConstDef());
        }
        next(TkType.SEMICN);
        outStrings.add("<ConstDecl>");
        return constDeclNode;
    }

    private DefNode parseConstDef() {
        // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
        next(TkType.IDENFR);
        DefNode constDefNode = new DefNode(true, tokens.get(pos - 1).getName());
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            constDefNode.addDimension(parseConstExp());
            next(TkType.RBRACK);
        }
        next(TkType.ASSIGN);
        parseConstInitVal(constDefNode);
        outStrings.add("<ConstDef>");
        return constDefNode;
    }

    private void parseConstInitVal(DefNode constDefNode) {
        // ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (!tokens.get(pos).eqType(TkType.LBRACE)) {
            constDefNode.addInitValues(parseConstExp());
        } else {
            next(TkType.LBRACE);
            if (!tokens.get(pos).eqType(TkType.RBRACE)) {
                parseConstInitVal(constDefNode);
                while (tokens.get(pos).eqType(TkType.COMMA)) {
                    next(TkType.COMMA);
                    parseConstInitVal(constDefNode);
                }
            }
            next(TkType.RBRACE);
        }
        outStrings.add("<ConstInitVal>");
    }

    private ExpNode parseConstExp() {
        // Ident here need to be constant! need to be refactored next time!
        ExpNode constExp = parseAddExp();
        outStrings.add("<ConstExp>");
        return constExp;
    }

    private DeclNode parseVarDecl() {
        DeclNode varDeclNode = new DeclNode(false);
        // VarDecl -> BType VarDef { ',' VarDef } ';'
        next(TkType.INTTK);
        varDeclNode.addDefs(parseVarDef());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            varDeclNode.addDefs(parseVarDef());
        }
        next(TkType.SEMICN);
        outStrings.add("<VarDecl>");
        return varDeclNode;
    }

    private DefNode parseVarDef() {
        // VarDef -> Ident { '[' ConstExp ']' } ['=' InitVal]
        next(TkType.IDENFR);
        DefNode varDefNode = new DefNode(false, tokens.get(pos - 1).getName());
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            varDefNode.addDimension(parseConstExp());
            next(TkType.RBRACK);
        }
        if (tokens.get(pos).eqType(TkType.ASSIGN)) {
            next(TkType.ASSIGN);
            parseInitVal(varDefNode);
        }
        outStrings.add("<VarDef>");
        return varDefNode;
    }

    private void parseInitVal(DefNode varDefNode) {
        // InitVal ->  Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if (!tokens.get(pos).eqType(TkType.LBRACE)) {
            varDefNode.addInitValues(parseExp());
        } else {
            next(TkType.LBRACE);
            if (!tokens.get(pos).eqType(TkType.RBRACE)) {
                parseInitVal(varDefNode);
                while (tokens.get(pos).eqType(TkType.COMMA)) {
                    next(TkType.COMMA);
                    parseInitVal(varDefNode);
                }
            }
            next(TkType.RBRACE);
        }
        outStrings.add("<InitVal>");
    }

    // 2 func part
    private FuncDefNode parseFuncDef() {
        TkType funcType = parseFuncType();
        next(TkType.IDENFR);
        // define the function
        FuncDefNode funcDefNode = new FuncDefNode(funcType, tokens.get(pos - 1).getName());
        next(TkType.LPARENT);
        if (!tokens.get(pos).eqType(TkType.RPARENT)) {
            parseFuncFParams(funcDefNode);
        }
        next(TkType.RPARENT);
        funcDefNode.setBlock(parseBlock());
        outStrings.add("<FuncDef>");
        return funcDefNode;
    }

    private FuncDefNode parseMainFuncDef() {
        FuncDefNode mainFuncDefNode = new FuncDefNode(TkType.INTTK, "main");
        next(TkType.INTTK);
        next(TkType.MAINTK);
        next(TkType.LPARENT);
        next(TkType.RPARENT);
        mainFuncDefNode.setBlock(parseBlock());
        outStrings.add("<MainFuncDef>");
        return mainFuncDefNode;
    }

    private TkType parseFuncType() {
        TkType type = tokens.get(pos).getType();
        if (type.equals(TkType.VOIDTK)) {
            next(TkType.VOIDTK);
        } else {
            next(TkType.INTTK);
        }
        outStrings.add("<FuncType>");
        return type;
    }

    private void parseFuncFParams(FuncDefNode funcDefNode) {
        funcDefNode.addParam(parseFuncFParam());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            funcDefNode.addParam(parseFuncFParam());
        }
        outStrings.add("<FuncFParams>");
    }

    private FuncFParamNode parseFuncFParam() {
        next(TkType.INTTK);
        next(TkType.IDENFR);
        FuncFParamNode funcFParamNode = new FuncFParamNode(tokens.get(pos - 1).getName());
        if (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            next(TkType.RBRACK);
            funcFParamNode.setPointer(true);
            while (tokens.get(pos).eqType(TkType.LBRACK)) {
                next(TkType.LBRACK);
                funcFParamNode.addDimension(parseConstExp());
                next(TkType.RBRACK);
            }
        }
        outStrings.add("<FuncFParam>");
        return funcFParamNode;
    }

    // 3 blk and stmt
    private BlockNode parseBlock() {
        BlockNode blockNode = new BlockNode();
        next(TkType.LBRACE);
        while (!tokens.get(pos).eqType(TkType.RBRACE)) {
            if (isConstDecl()) {
                blockNode.addItem(parseConstDecl());
            } else if (isVarDecl()) {
                blockNode.addItem(parseVarDecl());
            } else {
                blockNode.addItem(parseStmt());
            }
        }
        next(TkType.RBRACE);
        outStrings.add("<Block>");
        return blockNode;
    }

    // may return null
    @SuppressWarnings("checkstyle:MethodLength")
    private StmtNode parseStmt() {
        // default value for case Stmt -> ';'
        StmtNode retStmt;
        // Block
        if (tokens.get(pos).eqType(TkType.LBRACE)) {
            retStmt = parseBlock();
        }
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        else if (tokens.get(pos).eqType(TkType.IFTK)) {
            next(TkType.IFTK);
            next(TkType.LPARENT);
            ExpNode cond = parseCond();
            next(TkType.RPARENT);
            StmtNode thenStmt = parseStmt();
            // new if
            BranchNode branchNode = new BranchNode(cond, thenStmt);
            while (tokens.get(pos).eqType(TkType.ELSETK)) {
                next(TkType.ELSETK);
                branchNode.setElseStmt(parseStmt());
            }
            retStmt = branchNode;
        }
        // 'while' '(' Cond ')' Stmt
        else if (tokens.get(pos).eqType(TkType.WHILETK)) {
            next(TkType.WHILETK);
            next(TkType.LPARENT);
            ExpNode cond = parseCond();
            next(TkType.RPARENT);
            StmtNode loopBody = parseStmt();
            retStmt = new LoopNode(cond, loopBody);
        }
        // 'break' ';'
        else if (tokens.get(pos).eqType(TkType.BREAKTK)) {
            next(TkType.BREAKTK);
            next(TkType.SEMICN);
            retStmt = new BreakNode();
        }
        // 'continue' ';'
        else if (tokens.get(pos).eqType(TkType.CONTINUETK)) {
            next(TkType.CONTINUETK);
            next(TkType.SEMICN);
            retStmt = new ContinueNode();
        }
        // 'return' [Exp] ';'
        else if (tokens.get(pos).eqType(TkType.RETURNTK)) {
            ReturnNode returnNode = new ReturnNode();
            next(TkType.RETURNTK);
            if (!tokens.get(pos).eqType(TkType.SEMICN)) {
                returnNode.setRetVal(parseExp());
            }
            next(TkType.SEMICN);
            retStmt = returnNode;
        }
        // 'printf' '(' FormatString {',' Exp} ')' ';'
        else if (tokens.get(pos).eqType(TkType.PRINTFTK)) {
            next(TkType.PRINTFTK);
            next(TkType.LPARENT);
            next(TkType.STRCON);
            PrintfNode printfNode = new PrintfNode(tokens.get(pos - 1).getName());
            while (tokens.get(pos).eqType(TkType.COMMA)) {
                next(TkType.COMMA);
                printfNode.addParam(parseExp());
            }
            next(TkType.RPARENT);
            next(TkType.SEMICN);
            retStmt = printfNode;
        }
        // LVal '=' Exp ';'
        // LVal '=' 'getint''('')'';'
        // Exp -> LVal
        // Ident '(' [FuncRParams] ')'
        else if (isAssign()) {
            AssignNode assignNode = new AssignNode(parseLVal());
            next(TkType.ASSIGN);
            if (tokens.get(pos).eqType(TkType.GETINTTK)) {
                next(TkType.GETINTTK);
                next(TkType.LPARENT);
                next(TkType.RPARENT);
                assignNode.setGetInt();
            } else {
                assignNode.setExp(parseExp());
            }
            next(TkType.SEMICN);
            retStmt = assignNode;
        }
        // [Exp] ';'
        else {
            if (!tokens.get(pos).eqType(TkType.SEMICN)) {
                retStmt = parseExp();
            } else {
                retStmt = new BlockNode();
            }
            next(TkType.SEMICN);
        }
        outStrings.add("<Stmt>");
        return retStmt;
    }

    // 4 exp part
    private ExpNode parseExp() {
        ExpNode exp = parseAddExp();
        outStrings.add("<Exp>");
        return exp;
    }

    private ExpNode parseCond() {
        ExpNode cond = parseLOrExp();
        outStrings.add("<Cond>");
        return cond;
    }

    private LValNode parseLVal() {
        LValNode leftVal = new LValNode();
        next(TkType.IDENFR);
        leftVal.setIdent(tokens.get(pos - 1).getName());
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            leftVal.addArrayIndex(parseExp());
            next(TkType.RBRACK);
        }
        outStrings.add("<LVal>");
        return leftVal;
    }

    private NumNode parseNumber() {
        next(TkType.INTCON);
        outStrings.add("<Number>");
        return new NumNode(Integer.parseInt(tokens.get(pos - 1).getName()));
    }

    private ExpNode parseUnaryExp() {
        // {UnaryOp} (PrimaryExp | Ident '(' [FuncRParams] ')')
        // {UnaryOp}
        ExpNode retExp;
        if (isUnaryOp()) {
            TkType unaryOp = parseUnaryOp();
            ExpNode unaryExp = parseUnaryExp();
            retExp = new UnaryExpNode(unaryOp, unaryExp);
        } else {
            // Ident '(' [FuncRParams] ')'
            if (pos + 1 < tokens.size() &&
                    tokens.get(pos).eqType(TkType.IDENFR) &&
                    tokens.get(pos + 1).eqType(TkType.LPARENT)) {
                FuncCallNode funcCall = new FuncCallNode(tokens.get(pos).getName());
                next(TkType.IDENFR);
                next(TkType.LPARENT);
                if (!tokens.get(pos).eqType(TkType.RPARENT)) {
                    parseFuncRParams(funcCall);
                }
                next(TkType.RPARENT);
                retExp = funcCall;
            }
            // PrimaryExp -> '(' Exp ')' | LVal | Number
            else {
                if (tokens.get(pos).eqType(TkType.INTCON)) {
                    retExp = parseNumber();
                } else if (tokens.get(pos).eqType(TkType.LPARENT)) {
                    next(TkType.LPARENT);
                    retExp = parseExp();
                    next(TkType.RPARENT);
                } else {
                    retExp = parseLVal();
                }
                outStrings.add("<PrimaryExp>");
            }
        }
        outStrings.add("<UnaryExp>");
        return retExp;
    }

    private TkType parseUnaryOp() {
        TkType type = tokens.get(pos).getType();
        if (type.equals(TkType.PLUS)) {
            next(TkType.PLUS);
        } else if (type.equals(TkType.MINU)) {
            next(TkType.MINU);
        } else {
            next(TkType.NOT);
        }
        outStrings.add("<UnaryOp>");
        return type;
    }

    private void parseFuncRParams(FuncCallNode funcCall) {
        funcCall.addParam(parseExp());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            funcCall.addParam(parseExp());
        }
        outStrings.add("<FuncRParams>");
    }

    private ExpNode parseMulExp() {
        ExpNode leftExp = parseUnaryExp();
        outStrings.add("<MulExp>");
        while (isMulLink()) {
            TkType mulLink = tokens.get(pos).getType();
            if (mulLink.equals(TkType.MULT)) {
                next(TkType.MULT);
            } else if (mulLink.equals(TkType.DIV)) {
                next(TkType.DIV);
            } else {
                next(TkType.MOD);
            }
            leftExp = new BinaryExpNode(mulLink, leftExp, parseUnaryExp());
            outStrings.add("<MulExp>");
        }
        return leftExp;
    }

    private ExpNode parseAddExp() {
        ExpNode leftExp = parseMulExp();
        outStrings.add("<AddExp>");
        while (isAddLink()) {
            TkType addLink = tokens.get(pos).getType();
            if (addLink.equals(TkType.PLUS)) {
                next(TkType.PLUS);
            } else {
                next(TkType.MINU);
            }
            leftExp = new BinaryExpNode(addLink, leftExp, parseMulExp());
            outStrings.add("<AddExp>");
        }
        return leftExp;
    }

    private ExpNode parseRelExp() {
        ExpNode leftExp = parseAddExp();
        outStrings.add("<RelExp>");
        while (isRelLink()) {
            TkType relLink = tokens.get(pos).getType();
            if (tokens.get(pos).eqType(TkType.LSS)) {
                next(TkType.LSS);
            } else if (tokens.get(pos).eqType(TkType.GRE)) {
                next(TkType.GRE);
            } else if (tokens.get(pos).eqType(TkType.LEQ)) {
                next(TkType.LEQ);
            } else {
                next(TkType.GEQ);
            }
            leftExp = new BinaryExpNode(relLink, leftExp, parseAddExp());
            outStrings.add("<RelExp>");
        }
        return leftExp;
    }

    private ExpNode parseEqExp() {
        ExpNode leftExp = parseRelExp();
        outStrings.add("<EqExp>");
        while (isEqLink()) {
            TkType eqLink = tokens.get(pos).getType();
            if (tokens.get(pos).eqType(TkType.EQL)) {
                next(TkType.EQL);
            } else {
                next(TkType.NEQ);
            }
            leftExp = new BinaryExpNode(eqLink, leftExp, parseRelExp());
        }
        return leftExp;
    }

    private ExpNode parseLAndExp() {
        ExpNode leftExp = parseEqExp();
        outStrings.add("<LAndExp>");
        while (tokens.get(pos).eqType(TkType.AND)) {
            TkType andLink = tokens.get(pos).getType();
            next(TkType.AND);
            leftExp = new BinaryExpNode(andLink, leftExp, parseEqExp());
            outStrings.add("<LAndExp>");
        }
        return leftExp;
    }

    private ExpNode parseLOrExp() {
        ExpNode leftExp = parseLAndExp();
        outStrings.add("<LOrExp>");
        while (tokens.get(pos).eqType(TkType.OR)) {
            TkType orLink = tokens.get(pos).getType();
            next(TkType.OR);
            leftExp = new BinaryExpNode(orLink, leftExp, parseLAndExp());
            outStrings.add("<LOrExp>");
        }
        return leftExp;
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
