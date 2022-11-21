package front;

import exception.ErrorTable;
import exception.ParserError;
import exception.SysYError;
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

import java.io.PrintStream;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos;
    private final List<String> output = new ArrayList<>();
    // for retrieve
    private Stack<Integer> tokenPosRecord = new Stack<>();
    private Stack<Integer> outputPosRecord = new Stack<>();

    public Parser(Lexer lexer) {
        this.tokens = lexer.getTokens();
        this.pos = 0;
    }

    public void outputSyntax(PrintStream ps) {
        for (String outString : output) {
            ps.println(outString);
        }
    }

    // error handling, enable retrieving
    private void next(TkType type) throws ParserError {
        Token curToken = tokens.get(pos);
        if (curToken.eqType(type)) {
            output.add(tokens.get(pos).toString());
            ++pos;
        } else {
            throw new ParserError(tokens.get(pos), type);
        }
    }

    private void nextWithHandler(TkType type) {
        try {
            next(type);
        } catch (ParserError e) {
            TkType expectType = e.getExpectType();
            assert pos > 0;
            int errorLine = tokens.get(pos - 1).getLine();
            if (expectType.equals(TkType.SEMICN)) {
                ErrorTable.append(new SysYError(SysYError.MISSING_SEMICOLON, errorLine));
            } else if (expectType.equals(TkType.RPARENT)) {
                ErrorTable.append(new SysYError(SysYError.MISSING_RIGHT_PARENT, errorLine));
            } else if (expectType.equals(TkType.RBRACK)) {
                ErrorTable.append(new SysYError(SysYError.MISSING_RIGHT_BRACKET, errorLine));
            } else {
                System.out.println("unexpected handling token");
                System.exit(7);
            }
        }
    }

    private void checkpoint() {
        tokenPosRecord.push(pos);
        outputPosRecord.push(output.size());
    }

    private void retrieve() {
        pos = tokenPosRecord.pop();
        int outputPos = outputPosRecord.pop();
        if (output.size() > outputPos) {
            output.subList(outputPos, output.size()).clear();
        }
    }

    public CompUnitNode parseCompUnit() throws ParserError {
        CompUnitNode compUnitNode = new CompUnitNode();
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        // Decl -> ConstDecl | VarDecl
        while (isConstDecl() || isVarDecl()) {
            if (isConstDecl()) {
                compUnitNode.addDecl(
                        parseConstDecl());
            } else {
                compUnitNode.addDecl(parseVarDecl());
            }
            // no need to print <Decl>
        }
        while (isFuncDef()) {
            compUnitNode.addFuncDef(parseFuncDef());
        }
        compUnitNode.setMainFuncDef(parseMainFuncDef());
        output.add("<CompUnit>");
        return compUnitNode;
    }

    // 1 decl part
    private DeclNode parseConstDecl() throws ParserError {
        DeclNode constDeclNode = new DeclNode(true);
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        next(TkType.CONSTTK);
        next(TkType.INTTK);
        constDeclNode.addDefs(parseConstDef());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            constDeclNode.addDefs(parseConstDef());
        }
        nextWithHandler(TkType.SEMICN);
        output.add("<ConstDecl>");
        return constDeclNode;
    }

    private DefNode parseConstDef() throws ParserError {
        // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
        next(TkType.IDENFR);
        DefNode constDefNode = new DefNode(true, tokens.get(pos - 1).getName());
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            constDefNode.addDimension(parseConstExp());
            nextWithHandler(TkType.RBRACK);
        }
        next(TkType.ASSIGN);
        parseConstInitVal(constDefNode);
        output.add("<ConstDef>");
        return constDefNode;
    }

    private void parseConstInitVal(DefNode constDefNode) throws ParserError {
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
        output.add("<ConstInitVal>");
    }

    private ExpNode parseConstExp() throws ParserError {
        // Ident here need to be constant! need to be refactored next time!
        ExpNode constExp = parseAddExp();
        output.add("<ConstExp>");
        return constExp;
    }

    private DeclNode parseVarDecl() throws ParserError {
        DeclNode varDeclNode = new DeclNode(false);
        // VarDecl -> BType VarDef { ',' VarDef } ';'
        next(TkType.INTTK);
        varDeclNode.addDefs(parseVarDef());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            varDeclNode.addDefs(parseVarDef());
        }
        nextWithHandler(TkType.SEMICN);
        output.add("<VarDecl>");
        return varDeclNode;
    }

    private DefNode parseVarDef() throws ParserError {
        // VarDef -> Ident { '[' ConstExp ']' } ['=' InitVal]
        next(TkType.IDENFR);
        DefNode varDefNode = new DefNode(false, tokens.get(pos - 1).getName());
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            varDefNode.addDimension(parseConstExp());
            nextWithHandler(TkType.RBRACK);
        }
        if (tokens.get(pos).eqType(TkType.ASSIGN)) {
            next(TkType.ASSIGN);
            parseInitVal(varDefNode);
        }
        output.add("<VarDef>");
        return varDefNode;
    }

    private void parseInitVal(DefNode varDefNode) throws ParserError {
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
        output.add("<InitVal>");
    }

    // 2 func part
    private FuncDefNode parseFuncDef() throws ParserError {
        TkType funcType = parseFuncType();
        next(TkType.IDENFR);
        // define the function
        FuncDefNode funcDefNode = new FuncDefNode(funcType, tokens.get(pos - 1).getName());
        next(TkType.LPARENT);
        // pre read an exp
        if (tokens.get(pos).eqType(TkType.INTTK)) {
            parseFuncFParams(funcDefNode);
        }
        nextWithHandler(TkType.RPARENT);
        funcDefNode.setBlock(parseBlock());
        output.add("<FuncDef>");
        return funcDefNode;
    }

    private FuncDefNode parseMainFuncDef() throws ParserError {
        FuncDefNode mainFuncDefNode = new FuncDefNode(TkType.INTTK, "main");
        next(TkType.INTTK);
        next(TkType.MAINTK);
        next(TkType.LPARENT);
        nextWithHandler(TkType.RPARENT);
        mainFuncDefNode.setBlock(parseBlock());
        output.add("<MainFuncDef>");
        return mainFuncDefNode;
    }

    private TkType parseFuncType() throws ParserError {
        TkType type = tokens.get(pos).getType();
        if (type.equals(TkType.VOIDTK)) {
            next(TkType.VOIDTK);
        } else {
            next(TkType.INTTK);
        }
        output.add("<FuncType>");
        return type;
    }

    private void parseFuncFParams(FuncDefNode funcDefNode) throws ParserError {
        funcDefNode.addParam(parseFuncFParam());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            funcDefNode.addParam(parseFuncFParam());
        }
        output.add("<FuncFParams>");
    }

    private FuncFParamNode parseFuncFParam() throws ParserError {
        next(TkType.INTTK);
        next(TkType.IDENFR);
        FuncFParamNode funcFParamNode = new FuncFParamNode(tokens.get(pos - 1).getName());
        if (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            nextWithHandler(TkType.RBRACK);
            funcFParamNode.setPointer(true);
            while (tokens.get(pos).eqType(TkType.LBRACK)) {
                next(TkType.LBRACK);
                funcFParamNode.addDimension(parseConstExp());
                nextWithHandler(TkType.RBRACK);
            }
        }
        output.add("<FuncFParam>");
        return funcFParamNode;
    }

    // 3 blk and stmt
    private BlockNode parseBlock() throws ParserError {
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
        output.add("<Block>");
        return blockNode;
    }

    // may return null
    @SuppressWarnings("checkstyle:MethodLength")
    private StmtNode parseStmt() throws ParserError {
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
            nextWithHandler(TkType.RPARENT);
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
            nextWithHandler(TkType.RPARENT);
            StmtNode loopBody = parseStmt();
            retStmt = new LoopNode(cond, loopBody);
        }
        // 'break' ';'
        else if (tokens.get(pos).eqType(TkType.BREAKTK)) {
            next(TkType.BREAKTK);
            nextWithHandler(TkType.SEMICN);
            retStmt = new BreakNode();
        }
        // 'continue' ';'
        else if (tokens.get(pos).eqType(TkType.CONTINUETK)) {
            next(TkType.CONTINUETK);
            nextWithHandler(TkType.SEMICN);
            retStmt = new ContinueNode();
        }
        // 'return' [Exp] ';'
        else if (tokens.get(pos).eqType(TkType.RETURNTK)) {
            ReturnNode returnNode = new ReturnNode();
            next(TkType.RETURNTK);
            // pre read an exp
            if (isExp()) {
                returnNode.setRetVal(parseExp());
            }
            nextWithHandler(TkType.SEMICN);
            retStmt = returnNode;
        }
        // 'printf' '(' FormatString {',' Exp} ')' ';'
        else if (tokens.get(pos).eqType(TkType.PRINTFTK)) {
            int printfTokenLine = tokens.get(pos).getLine(); // 'printf' line
            next(TkType.PRINTFTK);
            next(TkType.LPARENT);
            next(TkType.STRCON);
            PrintfNode printfNode = new PrintfNode(tokens.get(pos - 1).getName());
            while (tokens.get(pos).eqType(TkType.COMMA)) {
                next(TkType.COMMA);
                printfNode.addParam(parseExp());
            }
            printfNode.checkParamCount(printfTokenLine);
            nextWithHandler(TkType.RPARENT);
            nextWithHandler(TkType.SEMICN);
            retStmt = printfNode;
        }
        // LVal '=' Exp ';'
        // LVal '=' 'getint''('')'';'
        else if (isAssignStmt()) {
            AssignNode assignNode = new AssignNode(parseLVal());
            next(TkType.ASSIGN);
            if (tokens.get(pos).eqType(TkType.GETINTTK)) {
                next(TkType.GETINTTK);
                next(TkType.LPARENT);
                nextWithHandler(TkType.RPARENT);
                assignNode.setGetInt();
            } else {
                assignNode.setExp(parseExp());
            }
            nextWithHandler(TkType.SEMICN);
            retStmt = assignNode;
        }
        // [Exp] ';'
        else {
            if (isExp()) {
                retStmt = parseExp();
            } else {
                // 如果没有exp，Stmt返回空Block
                retStmt = new BlockNode();
            }
            nextWithHandler(TkType.SEMICN);
        }
        output.add("<Stmt>");
        return retStmt;
    }

    // 4 exp part
    private ExpNode parseExp() throws ParserError {
        ExpNode exp = parseAddExp();
        output.add("<Exp>");
        return exp;
    }

    private ExpNode parseCond() throws ParserError {
        ExpNode cond = parseLOrExp();
        output.add("<Cond>");
        return cond;
    }

    private LValNode parseLVal() throws ParserError {
        LValNode leftVal = new LValNode();
        next(TkType.IDENFR);
        leftVal.setIdent(tokens.get(pos - 1).getName());
        while (tokens.get(pos).eqType(TkType.LBRACK)) {
            next(TkType.LBRACK);
            leftVal.addArrayIndex(parseExp());
            nextWithHandler(TkType.RBRACK);
        }
        output.add("<LVal>");
        return leftVal;
    }

    private NumNode parseNumber() throws ParserError {
        next(TkType.INTCON);
        output.add("<Number>");
        return new NumNode(Integer.parseInt(tokens.get(pos - 1).getName()));
    }

    private ExpNode parseUnaryExp() throws ParserError {
        // {UnaryOp} (PrimaryExp | Ident '(' [FuncRParams] ')')
        ExpNode retExp;
        if (isUnaryOp()) {
            // {UnaryOp}
            TkType unaryOp = parseUnaryOp();
            ExpNode unaryExp = parseUnaryExp();
            retExp = new UnaryExpNode(unaryOp, unaryExp);
        } else {
            if (pos + 1 < tokens.size() &&
                    tokens.get(pos).eqType(TkType.IDENFR) &&
                    tokens.get(pos + 1).eqType(TkType.LPARENT)) {
                // Ident '(' [FuncRParams] ')'
                FuncCallNode funcCall = new FuncCallNode(tokens.get(pos).getName());
                next(TkType.IDENFR);
                next(TkType.LPARENT);
                if (isExp()) {
                    parseFuncRParams(funcCall);
                }
                nextWithHandler(TkType.RPARENT);
                retExp = funcCall;
            } else {
                // PrimaryExp -> '(' Exp ')' | LVal | Number
                if (tokens.get(pos).eqType(TkType.INTCON)) {
                    retExp = parseNumber();
                } else if (tokens.get(pos).eqType(TkType.LPARENT)) {
                    next(TkType.LPARENT);
                    retExp = parseExp();
                    nextWithHandler(TkType.RPARENT);
                } else {
                    retExp = parseLVal();
                }
                output.add("<PrimaryExp>");
            }
        }
        output.add("<UnaryExp>");
        return retExp;
    }

    private TkType parseUnaryOp() throws ParserError {
        TkType type = tokens.get(pos).getType();
        if (type.equals(TkType.PLUS)) {
            next(TkType.PLUS);
        } else if (type.equals(TkType.MINU)) {
            next(TkType.MINU);
        } else {
            next(TkType.NOT);
        }
        output.add("<UnaryOp>");
        return type;
    }

    private void parseFuncRParams(FuncCallNode funcCall) throws ParserError {
        funcCall.addParam(parseExp());
        while (tokens.get(pos).eqType(TkType.COMMA)) {
            next(TkType.COMMA);
            funcCall.addParam(parseExp());
        }
        output.add("<FuncRParams>");
    }

    private ExpNode parseMulExp() throws ParserError {
        ExpNode leftExp = parseUnaryExp();
        output.add("<MulExp>");
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
            output.add("<MulExp>");
        }
        return leftExp;
    }

    private ExpNode parseAddExp() throws ParserError {
        ExpNode leftExp = parseMulExp();
        output.add("<AddExp>");
        while (isAddLink()) {
            TkType addLink = tokens.get(pos).getType();
            if (addLink.equals(TkType.PLUS)) {
                next(TkType.PLUS);
            } else {
                next(TkType.MINU);
            }
            leftExp = new BinaryExpNode(addLink, leftExp, parseMulExp());
            output.add("<AddExp>");
        }
        return leftExp;
    }

    private ExpNode parseRelExp() throws ParserError {
        ExpNode leftExp = parseAddExp();
        output.add("<RelExp>");
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
            output.add("<RelExp>");
        }
        return leftExp;
    }

    private ExpNode parseEqExp() throws ParserError {
        ExpNode leftExp = parseRelExp();
        output.add("<EqExp>");
        while (isEqLink()) {
            TkType eqLink = tokens.get(pos).getType();
            if (tokens.get(pos).eqType(TkType.EQL)) {
                next(TkType.EQL);
            } else {
                next(TkType.NEQ);
            }
            leftExp = new BinaryExpNode(eqLink, leftExp, parseRelExp());
            output.add("<EqExp>");
        }
        return leftExp;
    }

    private ExpNode parseLAndExp() throws ParserError {
        ExpNode leftExp = parseEqExp();
        output.add("<LAndExp>");
        while (tokens.get(pos).eqType(TkType.AND)) {
            TkType andLink = tokens.get(pos).getType();
            next(TkType.AND);
            leftExp = new BinaryExpNode(andLink, leftExp, parseEqExp());
            output.add("<LAndExp>");
        }
        return leftExp;
    }

    private ExpNode parseLOrExp() throws ParserError {
        ExpNode leftExp = parseLAndExp();
        output.add("<LOrExp>");
        while (tokens.get(pos).eqType(TkType.OR)) {
            TkType orLink = tokens.get(pos).getType();
            next(TkType.OR);
            leftExp = new BinaryExpNode(orLink, leftExp, parseLAndExp());
            output.add("<LOrExp>");
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

    private boolean isAssignStmt() {
        boolean isAssign = true;
        checkpoint();
        try {
            parseLVal();
            next(TkType.ASSIGN);
        } catch (ParserError e) {
            isAssign = false;
        }
        retrieve();
        return isAssign;
    }

    private boolean isExp() {
        boolean isExp = true;
        checkpoint();
        try {
            parseExp();
        } catch (ParserError e) {
            isExp = false;
        }
        retrieve();
        return isExp;
    }
}
