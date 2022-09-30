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
    private CompUnitNode root;
    private PrintStream out;

    public Parser(Lexer lexer) throws FileNotFoundException {
        this.tokens = lexer.getTokens();
        this.pos = 0;
        this.root = parseCompUnit();
        this.out = new PrintStream(OUTPUT_FILE);
    }

    private void next() {
        out.println(tokens.get(pos).toString());
        ++pos;
    }

    private CompUnitNode parseCompUnit() {
        CompUnitNode compUnitNode = new CompUnitNode();
        // {Decl}
        while (isConstDecl() || isVarDecl()) {
            if (isConstDecl()) {
                compUnitNode.addConstDecl(parseConstDecl());
            } else {
                compUnitNode.addVarDecl(parseVarDecl());
            }
            // no need to print <Decl>
        }
        // {FuncDef}
        while (isFuncDef()) {
            compUnitNode.addFuncDef(parseFuncDef());
        }
        // MainFuncDef
        compUnitNode.setMainFuncDef(parseMainFuncDef());
        out.println("<CompUnit>");
        return compUnitNode;
    }

    private ConstDeclNode parseConstDecl() {
        ConstDeclNode constDeclNode = new ConstDeclNode();
        // const
        if (tokens.get(pos).eqType(TkType.CONSTTK)) {
            next();
        } else {
            error();
        }
        // Btype
        if (tokens.get(pos).eqType(TkType.INTTK)) {
            next();
            // no need to print BType
        } else {
            error();
        }
        // ConstDef { ',' ConstDef } ';'

        // ;
        out.println("<ConstDecl>");
        return constDeclNode;
    }

    private VarDeclNode parseVarDecl() {
        VarDeclNode varDeclNode = new VarDeclNode();

        out.println("<VarDecl>");
        return varDeclNode;
    }

    private FuncDefNode parseFuncDef() {
        FuncDefNode funcDefNode = new FuncDefNode(false);

        out.println("<FuncDef>");
        return funcDefNode;
    }

    private FuncDefNode parseMainFuncDef() {
        FuncDefNode funcDefNode = new FuncDefNode(true);

        out.println("<MainFuncDef>");
        return funcDefNode;
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

    private boolean isFuncDef() {
        return pos + 2 < tokens.size() &&
                (tokens.get(pos).eqType(TkType.INTTK) || tokens.get(pos).eqType(TkType.VOIDTK)) &&
                tokens.get(pos + 1).eqType(TkType.IDENFR) &&
                tokens.get(pos + 2).eqType(TkType.LPARENT);
    }

    private void error() {
        System.out.println("error!");
    }
}
